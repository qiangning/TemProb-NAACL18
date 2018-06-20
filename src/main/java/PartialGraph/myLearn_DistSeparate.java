package PartialGraph;

import datastruct.TemporalEventPair;
import datastruct.TemporalStructure;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.TLINK;
import edu.illinois.cs.cogcomp.nlp.util.ExecutionTimeUtil;
import edu.illinois.cs.cogcomp.nlp.util.PrecisionRecallManager;
import edu.illinois.cs.cogcomp.nlp.util.TempDocEval;
import util.TempDocLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class myLearn_DistSeparate {
    public myLearn learner_dist0;
    public myLearn learner_dist1;

    /*public List<TemporalStructure> trainingStructs = new ArrayList<>();
    public List<TemporalStructure> devStructs = new ArrayList<>();*/
    public List<TemporalStructure> testStruct = new ArrayList<>();
    private String modelDir, setupName, awareness_output_dir = "output/PartialPaper/", awareness_log_dir = "PartialPaper/Awareness";

    public myLearn_DistSeparate(boolean force_update_models){
        learner_dist0 = new myLearn(0,force_update_models);
        learner_dist1 = new myLearn(0,force_update_models);
    }
    public myLearn_DistSeparate(boolean force_update_models,String awareness_output_dir,String awareness_log_dir){
        learner_dist0 = new myLearn(0,force_update_models);
        learner_dist1 = new myLearn(0,force_update_models);
        this.awareness_output_dir = awareness_output_dir;
        this.awareness_log_dir = awareness_log_dir;
    }
    public String getModelDir() {
        return modelDir;
    }

    public void setModelDir(String modelDir) {
        this.modelDir = modelDir;
    }

    public String getSetupName() {
        return setupName;
    }

    public void setSetupName(String setupName) {
        this.setupName = setupName;
    }

    public void setModelDirAndName(String modelDir, String setupName){
        IOUtils.mkdir(modelDir);
        setModelDir(modelDir);
        setSetupName(setupName);
    }

    /*public void setTrainingStructs(List<TemporalStructure> trainingStructs) {
        this.trainingStructs = trainingStructs;
    }*/

    /*public void setDevStructs(List<TemporalStructure> devStructs) {
        this.devStructs = devStructs;
    }*/

    public void setTestStruct(List<TemporalStructure> testStruct) {
        this.testStruct = testStruct;
    }

    public void myLearnWrapper_DistSeparate(List<TemporalStructure> trainingStructs, List<TemporalStructure> devStructs, double[] negSamRate0, double[] negSamRate1){
        System.out.println("#################");
        System.out.println("Training on dist=0");
        System.out.println("#################");
        learner_dist0.setModelPath(modelDir,setupName+"_dist0");
        learner_dist0.setNEGVAGSAMRATE(negSamRate0);
        learner_dist0.myLearnWrapper(trainingStructs,devStructs,0);
        if(learner_dist0.force_update_models)
            learner_dist0.saveClassifier();

        System.out.println("\n\n#################");
        System.out.println("Training on dist=1");
        System.out.println("#################");
        learner_dist1.setModelPath(modelDir,setupName+"_dist1");
        learner_dist1.setNEGVAGSAMRATE(negSamRate1);
        learner_dist1.myLearnWrapper(trainingStructs,devStructs,1);
        if(learner_dist1.force_update_models)
            learner_dist1.saveClassifier();
    }

    public void evaluateTest(){
        System.out.println("-------------------");
        System.out.println("Evaluating TestSet...");

        // local evaluation
        System.out.println("\n\n#################");
        System.out.println("Local evaluation:");
        System.out.println("#################");
        evaluate_local(3);

        // global evaluation + awareness
        myGlobalSolver.restartEvaluator();
        System.out.println("\n\n#################");
        System.out.println("Global evaluation:");
        System.out.println("#################");
        evaluate_global();
    }
    public void evaluateTest(MultiClassifiers<TemporalEventPair> multiClassifiers_dist0,MultiClassifiers<TemporalEventPair> multiClassifiers_dist1){
        System.out.println("-------------------");
        System.out.println("Evaluating TestSet...");

        // local evaluation
        System.out.println("\n\n#################");
        System.out.println("Local evaluation:");
        System.out.println("#################");
        evaluate_local(multiClassifiers_dist0,multiClassifiers_dist1,3);

        // global evaluation + awareness
        myGlobalSolver.restartEvaluator();
        System.out.println("\n\n#################");
        System.out.println("Global evaluation:");
        System.out.println("#################");
        evaluate_global(multiClassifiers_dist0,multiClassifiers_dist1);
    }
    public void evaluate_local(MultiClassifiers<TemporalEventPair> multiClassifiers_dist0,MultiClassifiers<TemporalEventPair> multiClassifiers_dist1, int verbose){
        List<TemporalEventPair> elist = new ArrayList<>();
        for(TemporalStructure st:testStruct) {
            elist.addAll(st.allEventPairs);
        }
        ExecutionTimeUtil timer = new ExecutionTimeUtil();
        PrecisionRecallManager evaluator = new PrecisionRecallManager();
        PrecisionRecallManager evaluator_sent0 = new PrecisionRecallManager();
        PrecisionRecallManager evaluator_sent1 = new PrecisionRecallManager();
        PrecisionRecallManager evaluator_sentMore = new PrecisionRecallManager();
        timer.start();
        for(TemporalEventPair ep:elist){
            String p = "WRONG";
            String l = ep.relation.toStringfull();
            switch(ep.event2.getSentId()-ep.event1.getSentId()){
                case 0:
                    p = multiClassifiers_dist0.discreteValue(ep);
                    evaluator_sent0.addPredGoldLabels(p,l);
                    evaluator.addPredGoldLabels(p,l);
                    break;
                case 1:
                    p = multiClassifiers_dist1.discreteValue(ep);
                    evaluator_sent1.addPredGoldLabels(p,l);
                    evaluator.addPredGoldLabels(p,l);
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
    }
    public void evaluate_local(int verbose) {
        evaluate_local(new MultiClassifiers<TemporalEventPair>(learner_dist0.classifier,1,true),
                new MultiClassifiers<TemporalEventPair>(learner_dist1.classifier,1,true),verbose);
    }
    public void evaluate_global(MultiClassifiers<TemporalEventPair> multiClassifiers_dist0,MultiClassifiers<TemporalEventPair> multiClassifiers_dist1){
        ExecutionTimeUtil timer = new ExecutionTimeUtil();
        timer.start();
        for(TemporalStructure st:testStruct) {
            myGlobalSolver ilpsolver = new myGlobalSolver(st, multiClassifiers_dist0, multiClassifiers_dist1);
            ilpsolver.evaluate();
            ilpsolver.setAwareness_dir(awareness_output_dir +File.separator+getSetupName());
            ilpsolver.saveForAwareness();
        }
        myGlobalSolver.printLocalPerformance();
        myGlobalSolver.printGlobalPerformance();
        timer.end();
        timer.print();
        System.out.println("\n\n#################");
        System.out.println("Awareness: Run this");
        System.out.println("#################");
        try {
            TempDocEval.RunOfficialTemporalEval(awareness_output_dir + File.separator+getSetupName() +File.separator+ "gold",
                    awareness_output_dir + File.separator+getSetupName() +File.separator+ "global",
                    awareness_log_dir, getSetupName()+"_aware");
        }
        catch (Exception e){e.printStackTrace();}
    }
    public void evaluate_global(){
        evaluate_global(new MultiClassifiers<TemporalEventPair>(learner_dist0.classifier,1,true),
                new MultiClassifiers<TemporalEventPair>(learner_dist1.classifier,1,true));
    }
    public static void main(String[] args) throws Exception{
        ResourceManager rm = new ResourceManager("config/reproducibility.properties");
        String modelDir = rm.getString("ModelPartialPaper");
        String setupName = "PurelyOnTB";

        TempDocLoader.TBDense dataset = TempDocLoader.getTBDenseInstance();
        List<TemporalStructure> trainingStructs = new ArrayList<>();
        List<TemporalStructure> devStructs = new ArrayList<>();
        List<TemporalStructure> testStruct = new ArrayList<>();
        trainingStructs = dataset.getAllStructs(0);
        devStructs = dataset.getAllStructs(2);
        testStruct = dataset.getAllStructs(3);
        for(TemporalStructure st:trainingStructs){
            st.extractPrepPhrase();
            st.extractRelGraphFull();
        }
        for(TemporalStructure st:devStructs){
            st.extractPrepPhrase();
            st.extractRelGraphFull();
        }
        for(TemporalStructure st:testStruct){
            st.extractPrepPhrase();
            st.extractRelGraphFull();
        }
        double[] negSamRate0 = new double[]{0.8,1};
        double[] negSamRate1 = new double[]{0.5,0.7};

        myLearn_DistSeparate exp = new myLearn_DistSeparate(false);
        exp.setModelDirAndName(modelDir,setupName);
        exp.myLearnWrapper_DistSeparate(trainingStructs,devStructs,negSamRate0,negSamRate1);
        exp.setTestStruct(testStruct);
        exp.evaluateTest();
    }
}
