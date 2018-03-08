package test;

import datastruct.TemporalEventPair;
import datastruct.TemporalStructure;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.TLINK;
import edu.illinois.cs.cogcomp.nlp.util.CrossValidationSplit;
import edu.illinois.cs.cogcomp.nlp.util.ExecutionTimeUtil;
import edu.illinois.cs.cogcomp.nlp.util.PrecisionRecallManager;
import lbjava.*;
import util.ParamLBJ;
import util.TempDocLoader;
import util.TempLangMdl;

import java.io.File;
import java.util.*;

import static java.lang.System.exit;

public class local_ee_test {
    public static ResourceManager rm;
    private String modelPath = "local_ee_tmp.lc";
    private String lexiconPath = "local_ee_tmp.lex";
    protected int cls_mode= 1;//0:general feats. 1:corpus stat feats only. 2: general+corpus. 3: general+corpus variation. 4: general+corpus(all labels). 5: mode 1 but with all label prior
    public void initCls(int cls_mode){
        switch(cls_mode){
            case 0:
                cls = new local_ee(modelPath,lexiconPath);
                break;
            case 2:
                cls = new local_ee_corpus(modelPath,lexiconPath);
                break;
            case 4:
                cls = new local_ee_corpus_allLabels(modelPath,lexiconPath);
                break;
            case 14:
                cls = new local_ee_pp_poswin_conn_corr(modelPath,lexiconPath);
                break;
            case 15:
                cls = new local_ee_pp_poswin_conn_corr1(modelPath,lexiconPath);
                break;
            case 16:
                cls = new local_ee_pp_poswin_conn_corr2(modelPath,lexiconPath);
                break;
            case 17:
                cls = new local_ee_corpus_allLabels_gold(modelPath,lexiconPath);
                break;
            default:
                cls = new local_ee(modelPath,lexiconPath);
        }
    }
    private int evalMetric = 2;//0:prec. 1: recall. 2: f1
    private int totalFold;
    private int seed;
    private double negVagSampling;//only work for training sample; as for test sample, it's always 1
    private CrossValidationSplit cv;
    public int dist_filter = 0;
    public List<TemporalStructure> allTrainingStructs;
    public List<TemporalStructure> allTestStructs;
    public List<TemporalEventPair> trainsplit;
    public List<TemporalEventPair> devsplit;
    public List<TemporalEventPair> testsplit;
    public Learner cls;
    public best_param_struct best_params;
    public boolean verbose = true;
    protected class best_param_struct{
        public double learning_rate;
        public double thickness;
        public double negVagSampling;
        public int learning_round;

        public best_param_struct(double learning_rate, double thickness, double negVagSampling, int learning_round) {
            this.learning_rate = learning_rate;
            this.thickness = thickness;
            this.negVagSampling = negVagSampling;
            this.learning_round = learning_round;
        }
    }

    public local_ee_test(String dir, String name, int cls_mode, int dist_filter, int evalMetric, double lr, double th, double negVagSampling) throws Exception{
        this(dir,name,cls_mode,dist_filter,evalMetric,lr,th,negVagSampling,2,0);
    }
    public local_ee_test(String dir, String name, int cls_mode, int dist_filter, int evalMetric, int totalFold, int seed) throws Exception{
        this(dir,name,cls_mode,dist_filter,evalMetric,ParamLBJ.eeLearningRate,ParamLBJ.eeThickness,1,totalFold,seed);
    }
    public local_ee_test(String dir, String name, int cls_mode, int dist_filter, int evalMetric, double lr, double th, double negVagSampling, int totalFold, int seed) throws Exception{
        allTrainingStructs = new ArrayList<>();
        allTestStructs = new ArrayList<>();
        trainsplit = new ArrayList<>();
        devsplit = new ArrayList<>();
        testsplit = new ArrayList<>();
        setModelPath(dir,name);
        this.cls_mode = cls_mode;
        this.dist_filter = dist_filter;
        this.evalMetric = evalMetric;
        setParamsAndInitCls(lr,th);
        setNegVagSampling(negVagSampling);
        this.totalFold = totalFold;
        this.seed = seed;
    }

    public void setNegVagSampling(double negVagSampling) {
        this.negVagSampling = negVagSampling;
    }

    public double getNegVagSampling() {
        return negVagSampling;
    }

    public void setModelPath(String dir, String name) {
        modelPath = dir+File.separator+name+".lc";
        lexiconPath = dir+File.separator+name+".lex";
    }

    public void setParamsAndInitCls(double lr, double th){
        ParamLBJ.eeLearningRate = lr;
        ParamLBJ.eeThickness = th;
        initCls(cls_mode);
    }

    private void preprocess(List<TemporalStructure> allStructs){
        for(TemporalStructure st:allStructs){
            st.keepEventPairsOnlyInSent(dist_filter);
            st.addIsBeforeCorpusFeat();
            st.addOtherLabelCorpusFeat();
            if(cls_mode>=10|cls_mode<0)
                st.extractPrepPhrase();
            if(cls_mode>=20) {
                st.addEmbeddings();
            }
        }
    }
    public void load() throws Exception{
        loadTBDense(false);
        //loadQuang(true);
        //loadTBDenseAndQuang();
    }
    public void loadTBDense(boolean useNewRelAnn) throws Exception{
        TempDocLoader.reload = true;
        TempDocLoader.TBDense dataset = TempDocLoader.getTBDenseInstance(useNewRelAnn);
        List<TemporalStructure> allStructs = dataset.allStructs;
        preprocess(allStructs);
        allTrainingStructs = dataset.getAllStructs(1);
        allTrainingStructs.addAll(dataset.getAllStructs(2));
        allTestStructs = dataset.getAllStructs(3);
        for(TemporalStructure st:allTestStructs)
            testsplit.addAll(st.eventPairs);
        cv = new CrossValidationSplit(totalFold,allTrainingStructs.size(),seed);
    }
    public void split(int fold){
        List<Integer> trainidx = cv.TrainTestSplit(fold).getFirst();
        List<Integer> devidx = cv.TrainTestSplit(fold).getSecond();
        trainsplit = new ArrayList<>();
        devsplit = new ArrayList<>();
        for(int i:trainidx)
            trainsplit.addAll(allTrainingStructs.get(i).extractEventPairs_negVagSampling(negVagSampling,cls_mode>=0));
        for(int j:devidx)
            devsplit.addAll(allTrainingStructs.get(j).eventPairs);
    }
    public void shuffleTrain(){
        Collections.shuffle(trainsplit);
    }
    public void shuffleTrain(int seed){
        Collections.shuffle(trainsplit,new Random(seed));
    }
    public double evaluate(List<TemporalEventPair> split){
        ExecutionTimeUtil timer = new ExecutionTimeUtil();
        PrecisionRecallManager evaluator = new PrecisionRecallManager();
        PrecisionRecallManager evaluator_sent0 = new PrecisionRecallManager();
        PrecisionRecallManager evaluator_sent1 = new PrecisionRecallManager();
        PrecisionRecallManager evaluator_sentMore = new PrecisionRecallManager();
        timer.start();
        for(TemporalEventPair ep:split){
            String p = cls.discreteValue(ep);
            String l = cls_mode>=0?ep.relation.toStringfull():ep.causal_relation.toStringfull();
            evaluator.addPredGoldLabels(p,l);
            switch(ep.event2.getSentId()-ep.event1.getSentId()){
                case 0:
                    evaluator_sent0.addPredGoldLabels(p,l);
                    break;
                case 1:
                    evaluator_sent1.addPredGoldLabels(p,l);
                    break;
                default:
                    evaluator_sentMore.addPredGoldLabels(p,l);
                    break;
            }
        }
        timer.end();
        if(verbose) {
            System.out.printf("Total time/examples: %d ms/%d pairs\n", timer.getTimeMillis(), split.size());
            if(dist_filter<0) {
                //dist_filter<0-->no sentence distance filtering applied to event pairs. In this case, we decompose the performance.
                System.out.println("Sent 0:");
                evaluator_sent0.printPrecisionRecall(new String[]{TLINK.TlinkType.UNDEF.toStringfull()});
                System.out.println("Sent 1:");
                evaluator_sent1.printPrecisionRecall(new String[]{TLINK.TlinkType.UNDEF.toStringfull()});
                System.out.println("Sent >=2:");
                evaluator_sentMore.printPrecisionRecall(new String[]{TLINK.TlinkType.UNDEF.toStringfull()});
                System.out.println("Sent All:");
            }
            evaluator.printPrecisionRecall(new String[]{TLINK.TlinkType.UNDEF.toStringfull()});
            evaluator.printConfusionMatrix();
        }
        double res;
        switch(evalMetric){
            case 0:
                res = evaluator.getResultStruct(new String[]{TLINK.TlinkType.UNDEF.toStringfull()}).prec;
                break;
            case 1:
                res = evaluator.getResultStruct(new String[]{TLINK.TlinkType.UNDEF.toStringfull()}).rec;
                break;
            case 2:
                res = evaluator.getResultStruct(new String[]{TLINK.TlinkType.UNDEF.toStringfull()}).f;
                break;
            default:
                res = evaluator.getResultStruct(new String[]{TLINK.TlinkType.UNDEF.toStringfull()}).f;
        }
        return res;
    }
    public void train_singleRound(List<TemporalEventPair> split, int round){
        cls.forget();
        cls.beginTraining();
        for(int iter = 0; iter < round; iter++){
            shuffleTrain(iter*iter);
            for(TemporalEventPair ep:split) {
                try {
                    cls.learn(ep);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        cls.doneLearning();
    }
    public double[] train_multiRounds(List<TemporalEventPair> split4train, List<TemporalEventPair> split4dev, int[] round){
        //The multirounds setting is used in cross validation
        Arrays.sort(round);
        int n = round.length;
        double[] scores = new double[n];
        cls.forget();
        cls.beginTraining();
        for(int iter = 0; iter < round[0]; iter++){
            for(TemporalEventPair ep:split4train) {
                try {
                    cls.learn(ep);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            shuffleTrain();
        }
        cls.doneLearning();
        scores[0] = evaluate(split4dev);
        cls.beginTraining();
        for(int i=1;i<n;i++){
            for(int iter = round[i-1]; iter<round[i]; iter++) {
                for (TemporalEventPair ep : split4train)
                    cls.learn(ep);
                shuffleTrain();
            }
            cls.doneLearning();
            scores[i] = evaluate(split4dev);
            if(i<n-1)
                cls.beginTraining();
        }
        return scores;
    }
    public void addDev2TrainSplit(){
        // After cross validation, we are going to retrain using all the data (train+dev) using the param from CV
        // Note: given negVagSampling, dev cannot be directly added; a negative sampling needs to be done before we add dev into train split.
        trainsplit = new ArrayList<>();
        for(TemporalStructure ts:allTrainingStructs){
            trainsplit.addAll(ts.extractEventPairs_negVagSampling(negVagSampling,cls_mode>=0));
        }
    }
    public void crossValidation(){
        double[][][][] scores = new double[ParamLBJ.learningRates.length][ParamLBJ.thicknesses.length][ParamLBJ.negVagSamplingRates.length][ParamLBJ.learningRounds.length];
        for(int i=0;i<ParamLBJ.learningRates.length;i++){
            double lr = ParamLBJ.learningRates[i];
            for(int j=0;j<ParamLBJ.thicknesses.length;j++){
                double th = ParamLBJ.thicknesses[j];
                setParamsAndInitCls(lr,th);
                for(int m=0;m<ParamLBJ.negVagSamplingRates.length;m++) {
                    double negVagSampling = ParamLBJ.negVagSamplingRates[m];
                    setNegVagSampling(negVagSampling);
                    double[][] tmpscores = new double[totalFold][ParamLBJ.learningRounds.length];
                    for (int f = 1; f <= totalFold; f++) {
                        split(f);
                        tmpscores[f - 1] = train_multiRounds(trainsplit,devsplit,ParamLBJ.learningRounds);
                    }
                    for (int k = 0; k < ParamLBJ.learningRounds.length; k++) {
                        double avg_score = 0;
                        for (int f = 0; f < totalFold; f++) {
                            avg_score += tmpscores[f][k];
                        }
                        avg_score /= totalFold;
                        scores[i][j][m][k] = avg_score;
                    }
                }
            }
        }
        double best_lr = -1, best_th = -1, best_negVagSampling = -1;
        double best_score = -1;
        int best_round = -1;
        System.out.printf("All scores: metric=%d (0->prec,1->rec,2->f)\n",evalMetric);
        for(int i=0;i<ParamLBJ.learningRates.length;i++) {
            for (int j = 0; j < ParamLBJ.thicknesses.length; j++) {
                for(int m=0;m<ParamLBJ.negVagSamplingRates.length;m++) {
                    for (int k = 0; k < ParamLBJ.learningRounds.length; k++) {
                        if(scores[i][j][m][k]>best_score){
                            best_score = scores[i][j][m][k];
                            best_lr = ParamLBJ.learningRates[i];
                            best_th = ParamLBJ.thicknesses[j];
                            best_negVagSampling = ParamLBJ.negVagSamplingRates[m];
                            best_round = ParamLBJ.learningRounds[k];
                        }
                        System.out.printf("%.2f\t", scores[i][j][m][k]*100);
                    }
                    System.out.printf("\n");
                }
                System.out.printf("\n");
            }
            System.out.printf("\n");
        }
        System.out.printf("Best params: %.4f, %.4f, %.4f, %d=>%.2f\n", best_lr, best_th, best_negVagSampling, best_round, best_score*100);
        best_params = new best_param_struct(best_lr,best_th,best_negVagSampling,best_round);
    }
    private void setCrossValidationBestParams(){
        if(best_params!=null){
            setParamsAndInitCls(best_params.learning_rate,best_params.thickness);
            setNegVagSampling(best_params.negVagSampling);
        }
        else{
            System.out.println("Error: best_params not initialized!");
        }
    }
    public void reTrainUsingBestParam(){
        setCrossValidationBestParams();
        addDev2TrainSplit();
        train_singleRound(trainsplit,best_params.learning_round);
        System.out.println("\nTrainsplit evaluation");
        evaluate(trainsplit);
        System.out.println("\nTestsplit evaluation");
        evaluate(testsplit);
    }
    public void saveClassifier(){
        cls.write(modelPath,lexiconPath);
    }
    public void saveClassifier(String modelPath, String modelName){
        String modelFile = modelPath+ File.separator+modelName+".lc";
        String lexFile = modelPath+File.separator+modelName+".lex";
        cls.write(modelFile,lexFile);
    }
    public static void CrossValidationExp(int dist_filter, int cls_mode, int evalMetric, int totalFold, boolean saveCls, String name)throws Exception{
        local_ee_test tester = new local_ee_test(rm.getString("LBJMdlDir"),name+"_dist"+dist_filter+"_mod"+cls_mode+"_met"+evalMetric,
                cls_mode,dist_filter,evalMetric,
                totalFold,0);
        tester.load();
        tester.verbose = false;
        tester.crossValidation();//best param is setup inside CV
        tester.verbose = true;
        tester.reTrainUsingBestParam();
        if(saveCls)
            tester.saveClassifier();
    }
    public static void reTrainUserDefBestParamExp(int dist_filter, int cls_mode, int evalMetric, double lr, double th, double negVagSampling, int round, boolean saveCls, String name) throws Exception{
        local_ee_test tester = new local_ee_test(rm.getString("LBJMdlDir"),name+"_dist"+dist_filter+"_mod"+cls_mode+"_met"+evalMetric,
                cls_mode,dist_filter,evalMetric,
                lr,th,negVagSampling);
        tester.load();
        tester.addDev2TrainSplit();
        tester.train_singleRound(tester.trainsplit,round);
        System.out.println("Trainsplit evaluation");
        tester.evaluate(tester.trainsplit);
        System.out.println("Testsplit evaluation");
        tester.evaluate(tester.testsplit);
        if(saveCls)
            tester.saveClassifier();
    }
    public static void main(String[] args) throws Exception{
        String cluster_name = args[0];
        String mdl_subdir = args[1];
        String lm_name = args[2];
        rm = new ResourceManager("config/reproducibility.properties");
        String lm_path = rm.getString("TemProb");
        if(cluster_name.equals("noClustering"))
            TemporalStructure.myTempLangMdl = TempLangMdl.getInstance(lm_path);
        else {
            exit(-1);
        }

        int fold = 3;
        int evalMetric = 2;
        boolean saveCls = true;
        int[] DIST = new int[]{0,1};
        int[] MODE = new int[]{100,101};
        for(int dist : DIST){
            for(int mode : MODE){
                if(mode<0){
                    ParamLBJ.negVagSamplingRates = new double[]{0};
                    //ParamLBJ.negVagSamplingRates = new double[]{0.1,0.3,0.5};
                }
                System.out.printf("***********dist_filter=%d, cls_mode=%d***********\n",dist,mode);
                CrossValidationExp(dist,mode,evalMetric,fold,saveCls,mdl_subdir+"/perceptron_CV");
            }
        }
        //reTrainUserDefBestParamExp(1,2,2,0.01,3,0.3,400,true,"perceptron_User");
        //reTrainUserDefBestParamExp(0,2,2,0.005,3,0.7,200,true,"perceptron_User");
    }
}
