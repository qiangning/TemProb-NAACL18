package PartialGraph;

import datastruct.TemporalEventPair;
import datastruct.TemporalStructure;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.TLINK;
import edu.illinois.cs.cogcomp.nlp.util.ExecutionTimeUtil;
import edu.illinois.cs.cogcomp.nlp.util.PrecisionRecallManager;
import lbjava.*;
import util.CrossValidationWrapper;
import util.ParamLBJ;
import util.TempDocLoader;

import java.io.File;
import java.util.*;

import static datastruct.TemporalStructure.extractEventPairs_negVagSampling;

public class myLearn extends CrossValidationWrapper<TemporalStructure>{
    public List<TemporalStructure> trainingStructs = new ArrayList<>();
    public List<TemporalStructure> devStructs = new ArrayList<>();
    public List<TemporalStructure> testStruct = new ArrayList<>();
    public double[] LEARNRATE = new double[]{0.002,0.005};
    public double[] THICKNESS = new double[]{1};
    public double[] NEGVAGSAMRATE= new double[]{0.5,0.7};
    public double[] ROUND = new double[]{50,100};
    public Learner classifier;
    public int cls_mode = 0;
    public int evalMetric = 2;//0:prec. 1: recall. 2: f1
    public String modelPath, lexiconPath;
    public boolean force_update_models = true;

    @Override
    public void load() {
        super.trainingStructs = trainingStructs;
        super.devStructs = devStructs;
    }

    @Override
    public void learn(List<TemporalStructure> slist, double[] param, int seed) {
        double lr = param[0];
        double th = param[1];
        double nvsr = param[2];
        int round = (int) Math.round(param[3]);
        List<TemporalEventPair> elist = new ArrayList<>();
        for(TemporalStructure st:slist){
            elist.addAll(extractEventPairs_negVagSampling(st.allEventPairs,nvsr,true, new Random(seed++)));
        }
        ParamLBJ.eeLearningRate = lr;
        ParamLBJ.eeThickness = th;
        switch (cls_mode){
            case 0:
                classifier = new local_ee_pp_poswin_conn_corr_allE0(modelPath, lexiconPath);
                break;
            default:
                classifier = new local_ee_pp_poswin_conn_corr_allE0(modelPath, lexiconPath);
                System.out.println("cls_mode undefined. Using default local_ee_pp_poswin_conn_corr_allE");
        }

        classifier.forget();
        classifier.beginTraining();
        for(int iter=0;iter<round;iter++){
            Collections.shuffle(elist, new Random(seed++));
            for(TemporalEventPair ep:elist){
                try{
                    classifier.learn(ep);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        classifier.doneLearning();
    }

    @Override
    public double evaluate(List<TemporalStructure> slist, int verbose) {
        List<TemporalEventPair> elist = new ArrayList<>();
        for(TemporalStructure st:slist) {
            elist.addAll(st.allEventPairs);
        }
        ExecutionTimeUtil timer = new ExecutionTimeUtil();
        PrecisionRecallManager evaluator = new PrecisionRecallManager();
        PrecisionRecallManager evaluator_sent0 = new PrecisionRecallManager();
        PrecisionRecallManager evaluator_sent1 = new PrecisionRecallManager();
        PrecisionRecallManager evaluator_sentMore = new PrecisionRecallManager();
        timer.start();
        for(TemporalEventPair ep:elist){
            String p = classifier.discreteValue(ep);
            String l = ep.relation.toStringfull();
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
            }
        }
        timer.end();
        if(verbose>0) {
            System.out.println("Sent All:");
            evaluator.printPrecisionRecall(new String[]{TLINK.TlinkType.UNDEF.toStringfull()});
            if(verbose>1) {
                //dist_filter<0-->no sentence distance filtering applied to event pairs. In this case, we decompose the performance.
                System.out.println("Sent 0:");
                evaluator_sent0.printPrecisionRecall(new String[]{TLINK.TlinkType.UNDEF.toStringfull()});
                System.out.println("Sent 1:");
                evaluator_sent1.printPrecisionRecall(new String[]{TLINK.TlinkType.UNDEF.toStringfull()});
                System.out.println("Sent >=2:");
                evaluator_sentMore.printPrecisionRecall(new String[]{TLINK.TlinkType.UNDEF.toStringfull()});
                System.out.printf("Total time/examples: %d ms/%d pairs\n", timer.getTimeMillis(), elist.size());
            }
            if(verbose>2){
                System.out.println("Sent All (With no ignored label):");
                evaluator.printPrecisionRecall();
                evaluator.printConfusionMatrix();
            }

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

    @Override
    public void setParams2tune() {
        params2tune = new double[LEARNRATE.length*THICKNESS.length*NEGVAGSAMRATE.length*ROUND.length][4];
        int cnt = 0;
        for(double lr:LEARNRATE){
            for(double th:THICKNESS){
                for(double nvsr:NEGVAGSAMRATE){
                    for(double r:ROUND){
                        params2tune[cnt] = new double[]{lr,th,nvsr,r};
                        cnt++;
                    }
                }
            }
        }
    }

    public myLearn(int seed) {
        super(seed);
        force_update_models = true;
    }

    public myLearn(int seed,boolean force_update_models){
        super(seed);
        this.force_update_models = force_update_models;
    }

    public void setTrainingStructs(List<TemporalStructure> trainingStructs) {
        this.trainingStructs = trainingStructs;
    }

    public void setDevStructs(List<TemporalStructure> devStructs) {
        this.devStructs = devStructs;
    }

    public void setTestStruct(List<TemporalStructure> testStruct) {
        this.testStruct = testStruct;
    }

    public void setLEARNRATE(double[] LEARNRATE) {
        this.LEARNRATE = LEARNRATE;
    }

    public void setTHICKNESS(double[] THICKNESS) {
        this.THICKNESS = THICKNESS;
    }

    public void setNEGVAGSAMRATE(double[] NEGVAGSAMRATE) {
        this.NEGVAGSAMRATE = NEGVAGSAMRATE;
    }

    public void setROUND(double[] ROUND) {
        this.ROUND = ROUND;
    }

    public double evaluateTest(){
        System.out.println("-------------------");
        System.out.println("Evaluating TestSet...");
        return evaluate(testStruct,3);
    }

    public void saveClassifier(){
        classifier.write(modelPath,lexiconPath);
    }

    public void setModelPath(String dir, String name) {
        modelPath = dir+ File.separator+name+".lc";
        lexiconPath = dir+File.separator+name+".lex";
    }

    public void myLearnWrapper(List<TemporalStructure> trainingStructs, List<TemporalStructure> devStructs, int dist_filter){
        System.out.printf("[CHECK] Calling of myLearnWrapper: #trainingStructs=%d, #devStructs=%d\n",trainingStructs.size(),devStructs.size());
        System.out.println("[CHECK] last struct of trainingStructs:");
        trainingStructs.get(trainingStructs.size()-1).printStat();
        System.out.println("[CHECK] last struct of devStructs:");
        devStructs.get(devStructs.size()-1).printStat();
        File modelfile = new File(modelPath);
        if(modelfile.exists()&&!force_update_models) {
            System.out.printf("Model [%s] exists. Don't force update.\n",modelPath);
            switch (cls_mode){
                case 0:
                    classifier = new local_ee_pp_poswin_conn_corr_allE0(modelPath, lexiconPath);
                    break;
                default:
                    classifier = new local_ee_pp_poswin_conn_corr_allE0(modelPath, lexiconPath);
                    System.out.println("cls_mode undefined. Using default local_ee_pp_poswin_conn_corr_allE");
            }
            return;
        }
        setTrainingStructs(trainingStructs);
        setDevStructs(devStructs);

        load();
        // store copy of eventpairs before filtering
        HashMap<TemporalStructure,List<TemporalEventPair>> allEventPairs_train = new HashMap<>();
        HashMap<TemporalStructure,List<TemporalEventPair>> allEventPairs_dev = new HashMap<>();
        // distance filter
        for(TemporalStructure st:trainingStructs){
            allEventPairs_train.put(st,st.allEventPairs);
            st.keepFullEventPairsOnlyInSent(dist_filter);
        }
        for(TemporalStructure st:devStructs){
            allEventPairs_dev.put(st,st.allEventPairs);
            st.keepFullEventPairsOnlyInSent(dist_filter);
        }
        myParamTuner();
        retrainUsingBest();
        // reset eventpairs to be the same as before filtering
        for(TemporalStructure st:trainingStructs){
            st.allEventPairs = allEventPairs_train.get(st);
        }
        for(TemporalStructure st:devStructs){
            st.allEventPairs = allEventPairs_dev.get(st);
        }
    }
    public static void main(String[] args) throws Exception{
        ResourceManager rm = new ResourceManager("config/reproducibility.properties");
        TempDocLoader.TBDense dataset = TempDocLoader.getTBDenseInstance();
        List<TemporalStructure> trainingStructs = new ArrayList<>();
        List<TemporalStructure> devStructs = new ArrayList<>();
        List<TemporalStructure> testStruct = new ArrayList<>();
        trainingStructs = dataset.getAllStructs(1);
        devStructs = dataset.getAllStructs(2);
        testStruct = dataset.getAllStructs(3);
        for(TemporalStructure st:trainingStructs){
            st.extractPrepPhrase();
        }
        for(TemporalStructure st:devStructs){
            st.extractPrepPhrase();
        }
        for(TemporalStructure st:testStruct){
            st.extractPrepPhrase();
        }

        myLearn exp = new myLearn(0);
        String dir = rm.getString("ModelPartialPaper");
        String name = "test";
        exp.setModelPath(dir,name);


        exp.myLearnWrapper(trainingStructs,devStructs,1);
        exp.setTestStruct(testStruct);
        exp.evaluateTest();
        exp.saveClassifier();
    }
}
