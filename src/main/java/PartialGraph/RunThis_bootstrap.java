package PartialGraph;

import datastruct.TemporalEventPair;
import datastruct.TemporalStructure;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.TLINK;
import edu.illinois.cs.cogcomp.nlp.util.ExecutionTimeUtil;
import lbjava.local_ee_pp_poswin_conn_corr_allE0;
import util.TempDocLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

public class RunThis_bootstrap {
    static String modelDir, setupName;
    static List<TemporalStructure> trainingStructs_fromFullData,trainingStructs_fromPartialData,devStructs,testStruct;
    static List<TemporalStructure> trainingStructs = new ArrayList<>();
    static double[] negSamRate0, negSamRate1;
    static double target_vague_ratio = 0.25;
    static Learner base_cls0, base_cls1;
    public static void loadBaseCls_TBDense() throws Exception{
        ResourceManager rm = new ResourceManager("config/reproducibility.properties");
        String modeldir = rm.getString("ModelPartialPaper");
        String modelname = "PurelyOnTBDense";
        base_cls0 = new local_ee_pp_poswin_conn_corr_allE0(String.format("%s/%s_dist0.lc",modeldir,modelname),
                String.format("%s/%s_dist0.lex",modeldir,modelname));
        base_cls1  = new local_ee_pp_poswin_conn_corr_allE0(String.format("%s/%s_dist1.lc",modeldir,modelname),
                String.format("%s/%s_dist1.lex",modeldir,modelname));
    }
    public static void loadTBDenseAsFullData(){
        TempDocLoader.TBDense dataset = TempDocLoader.getTBDenseInstance();
        trainingStructs_fromFullData = dataset.getAllStructs(1);
        for(TemporalStructure st:trainingStructs_fromFullData){
            st.extractPrepPhrase();
            st.extractRelGraphFull();
        }
        trainingStructs.addAll(trainingStructs_fromFullData);
    }
    public static void loadDevTest(){
        TempDocLoader.TBDense dataset = TempDocLoader.getTBDenseInstance();
        devStructs = dataset.getAllStructs(2);
        testStruct = dataset.getAllStructs(3);
        for(TemporalStructure st:devStructs){
            st.extractPrepPhrase();
            st.extractRelGraphFull();
        }
        for(TemporalStructure st:testStruct){
            st.extractPrepPhrase();
            st.extractRelGraphFull();
        }
    }
    public static void TBAQ(){
        TempDocLoader.AQUAINT aquaint = TempDocLoader.getAQUAINTInstance();
        trainingStructs_fromPartialData = aquaint.allStructs;
        TempDocLoader.TBDense tbdense = TempDocLoader.getTBDenseInstance();
        trainingStructs_fromPartialData.addAll(tbdense.getAllStructs(0));

        for(TemporalStructure st:trainingStructs_fromPartialData){
            st.extractPrepPhrase();
            st.extractRelGraphFull();
        }

        trainingStructs.addAll(trainingStructs_fromPartialData);
    }
    public static void autoSelect_negSamRate(){
        double[] autoSelect = RunThis.autoSelect_negSamRate(trainingStructs,target_vague_ratio);
        System.out.printf("Autoselected negSamRate (dist=0) = %8.4f\n", autoSelect[0]);
        System.out.printf("Autoselected negSamRate (dist=1) = %8.4f\n", autoSelect[1]);
        negSamRate0 = new double[]{autoSelect[0]*0.9,autoSelect[0],autoSelect[0]*1.1};
        negSamRate1 = new double[]{autoSelect[1]*0.9,autoSelect[1],autoSelect[1]*1.1};
    }
    public static void inference(boolean local_or_global, boolean useLabelsInPartial){
        if(local_or_global){
            int cnt_total = 0, cnt_changed = 0;
            for(TemporalStructure st:trainingStructs_fromPartialData){
                for(TemporalEventPair ep:st.allEventPairs){
                    cnt_total++;
                    if(useLabelsInPartial&&!ep.isRelNull())
                        continue;
                    int dist = ep.event2.getSentId() - ep.event1.getSentId();
                    String pred = TLINK.TlinkType.UNDEF.toStringfull();
                    switch (dist){
                        case 0:
                            pred = base_cls0.discreteValue(ep);
                            break;
                        case 1:
                            pred = base_cls1.discreteValue(ep);
                            break;
                        default:
                            exit(-1);
                    }
                    ep.relation = TLINK.TlinkType.str2TlinkType(pred);
                    cnt_changed++;
                }
            }
            System.out.printf("Edges: %d/%d relabeled by base classifier in partially annotated data.\n",cnt_changed,cnt_total);
        }
        else{
            ExecutionTimeUtil timer = new ExecutionTimeUtil();
            timer.start();
            for(TemporalStructure st:trainingStructs_fromPartialData){
                myGlobalSolver ilpsolver = new myGlobalSolver(st, base_cls0, base_cls1);
                if(useLabelsInPartial)
                    ilpsolver.applyGoldNonvagueAsConstraint();
                ilpsolver.evaluate();

                st.relGraphFull = ilpsolver.result_tlink;
                st.relGraphFull2pairs();
            }
            timer.end();
            System.out.println("\n\n***************");
            System.out.printf("Bootstrapping on partial dataset (global inference): %5d seconds/%4d documents\n",timer.getTimeSeconds(),trainingStructs_fromPartialData.size());
            System.out.println("***************\n\n");
        }
    }
    public static void main(String[] args) throws Exception{
        ResourceManager rm = new ResourceManager("config/reproducibility.properties");
        modelDir = rm.getString("ModelPartialPaperBootStrap");
        String partial_data_name = args.length>0? args[0] : "";
        String inference_type = args.length>1?args[1]:"local";
        target_vague_ratio = args.length>2?Double.valueOf(args[2]):0.25d;
        int maxIter = args.length>3?Integer.valueOf(args[3]):1;
        boolean useLabelsInPartial = args.length>4?Boolean.valueOf(args[4]):true;
        boolean force_update = args.length>5? Boolean.valueOf(args[5]):false;
        setupName = partial_data_name+"_"+inference_type;
        if(useLabelsInPartial)
            setupName+="_asP";
        else
            setupName+="_asU";
        setupName+=String.format("_target%.2f",target_vague_ratio);
        System.out.println("\n\n***************");
        System.out.printf("Bootstrap setup=%s, maxIter=%d, inference type=%s, useLabelsInPartial=%s, force_update=%s\n",setupName,maxIter,inference_type,String.valueOf(useLabelsInPartial),
                String.valueOf(force_update));
        System.out.printf("Target vague ratio=%8.4f\n",target_vague_ratio);
        System.out.println("***************\n\n");
        switch (partial_data_name){
            case "TBAQ":
                TBAQ();
                break;
            default:
                System.out.println("[WARNING] setupname mismatch. Using default (TBAQ)");
                TBAQ();
        }
        loadDevTest();
        loadTBDenseAsFullData();

        int iter = 0;
        System.out.println("\n\n***************");
        System.out.printf("Bootstrapping Iteration %d\n", iter);
        System.out.println("***************\n\n");
        loadBaseCls_TBDense();
        inference(inference_type.equals("local"),useLabelsInPartial);
        autoSelect_negSamRate();
        myLearn_DistSeparate exp = new myLearn_DistSeparate(force_update,"output/PartialPaper/bootstrap/","PartialPaper/bootstrap/awareness");
        exp.setModelDirAndName(modelDir,setupName);
        exp.myLearnWrapper_DistSeparate(trainingStructs,devStructs,negSamRate0,negSamRate1);
        exp.setTestStruct(testStruct);
        System.out.println("\n\n***************");
        System.out.printf("Bootstrapping Iteration %d Evaluation Result\n", iter);
        System.out.println("***************\n\n");
        exp.evaluateTest();
        iter++;

        while(iter<maxIter) {
            System.out.println("\n\n***************");
            System.out.printf("Bootstrapping Iteration %d\n", iter);
            System.out.println("***************\n\n");
            //String modeldir = "models_TBDense/partial_graph_paper/bootstrap";
            String modeldir = modelDir;
            String modelname = setupName;
            if(iter==1) {
                base_cls0 = new local_ee_pp_poswin_conn_corr_allE0(String.format("%s/%s_dist0.lc", modeldir, modelname),
                        String.format("%s/%s_dist0.lex", modeldir, modelname));
                base_cls1 = new local_ee_pp_poswin_conn_corr_allE0(String.format("%s/%s_dist1.lc", modeldir, modelname),
                        String.format("%s/%s_dist1.lex", modeldir, modelname));
            }
            else{
                base_cls0 = new local_ee_pp_poswin_conn_corr_allE0(String.format("%s/%s_iter%d_dist0.lc", modeldir, modelname,iter-1),
                        String.format("%s/%s_iter%d_dist0.lex", modeldir, modelname,iter-1));
                base_cls1 = new local_ee_pp_poswin_conn_corr_allE0(String.format("%s/%s_iter%d_dist1.lc", modeldir, modelname,iter-1),
                        String.format("%s/%s_iter%d_dist1.lex", modeldir, modelname,iter-1));
            }
            TempDocLoader.reload = true;
            trainingStructs = new ArrayList<>();
            switch (partial_data_name){
                case "TBAQ":
                    TBAQ();
                    break;
                default:
                    System.out.println("[WARNING] setupname mismatch. Using default (TBAQ)");
                    TBAQ();
            }
            loadDevTest();
            trainingStructs.addAll(trainingStructs_fromFullData);

            inference(inference_type.equals("local"), useLabelsInPartial);
            autoSelect_negSamRate();
            exp = new myLearn_DistSeparate(force_update, "output/PartialPaper/bootstrap/","PartialPaper/bootstrap/awareness");
            exp.setModelDirAndName(modelDir, String.format("%s_iter%d",setupName,iter));
            exp.myLearnWrapper_DistSeparate(trainingStructs, devStructs, negSamRate0, negSamRate1);

            exp.setTestStruct(testStruct);
            System.out.println("\n\n***************");
            System.out.printf("Bootstrapping Iteration %d Evaluation Result\n", iter);
            System.out.println("***************\n\n");
            exp.evaluateTest();
            iter++;
        }
    }
}
