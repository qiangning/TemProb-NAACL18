package PartialGraph;

import datastruct.TemporalEventPair;
import datastruct.TemporalStructure;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import util.TempDocLoader;

import java.util.ArrayList;
import java.util.List;

public class RunThis {
    static String modelDir, setupName;
    static List<TemporalStructure> trainingStructs,devStructs,testStruct;
    static double[] negSamRate0, negSamRate1;
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
    public static double[] autoSelect_negSamRate(List<TemporalStructure> list){
        return  autoSelect_negSamRate(list,0.4);
    }
    public static double[] autoSelect_negSamRate(List<TemporalStructure> list,double ratio_vague){
        double[] negSamRates = new double[]{1,1};
        int[] cnt_vague = new int[2];
        int[] cnt_total = new int[2];
        for(TemporalStructure st:list){
            for(TemporalEventPair ep:st.allEventPairs){
                int dist = ep.event2.getSentId() - ep.event1.getSentId();
                if(dist<0||dist>1)
                    System.out.println("[WARNING] eventpair has unexpected sentence distance.");
                cnt_total[dist]++;
                if(ep.isRelNull())
                    cnt_vague[dist]++;
            }
        }
        negSamRates[0] = ratio_vague/cnt_vague[0]*cnt_total[0];
        negSamRates[1] = ratio_vague/cnt_vague[1]*cnt_total[1];
        return negSamRates;
    }
    public static void TBDense(){
        TempDocLoader.TBDense dataset = TempDocLoader.getTBDenseInstance();
        trainingStructs = dataset.getAllStructs(1);
        for(TemporalStructure st:trainingStructs){
            st.extractPrepPhrase();
            st.extractRelGraphFull();
        }
        double[] autoSelect = autoSelect_negSamRate(trainingStructs,0.3);
        System.out.printf("Autoselected negSamRate (dist=0) = %8.4f\n", autoSelect[0]);
        System.out.printf("Autoselected negSamRate (dist=1) = %8.4f\n", autoSelect[1]);
        negSamRate0 = new double[]{autoSelect[0]*0.95,autoSelect[0],autoSelect[0]*1.05};
        negSamRate1 = new double[]{0.5,0.7};
    }
    public static void TBDense_TBAQNonvague(){
        TempDocLoader.TBDense dataset = TempDocLoader.getTBDenseInstance();
        trainingStructs = dataset.getAllStructs(1);
        for(TemporalStructure st:trainingStructs){
            st.extractPrepPhrase();
            st.extractRelGraphFull();
        }

        TempDocLoader.AQUAINT aquaint = TempDocLoader.getAQUAINTInstance();
        List<TemporalStructure> tmp = aquaint.allStructs;
        tmp.addAll(dataset.getAllStructs(0));
        for(TemporalStructure st:tmp){
            st.extractPrepPhrase();
            st.extractRelGraphFull();
            List<TemporalEventPair> filtered = new ArrayList<>();
            for(TemporalEventPair ep:st.allEventPairs){
                if(ep.isRelNull())
                    continue;
                filtered.add(ep);
            }
            st.allEventPairs = filtered;
        }
        trainingStructs.addAll(tmp);
        double[] autoSelect = autoSelect_negSamRate(trainingStructs,0.25);
        System.out.printf("Autoselected negSamRate (dist=0) = %8.4f\n", autoSelect[0]);
        System.out.printf("Autoselected negSamRate (dist=1) = %8.4f\n", autoSelect[1]);
        negSamRate0 = new double[]{autoSelect[0]*0.9,autoSelect[0],autoSelect[0]*1.1};
        negSamRate1 = new double[]{autoSelect[1]*0.9,autoSelect[1],autoSelect[1]*1.1};
    }

    public static void main(String[] args) throws Exception{
        ResourceManager rm = new ResourceManager("config/reproducibility.properties");
        modelDir = rm.getString("ModelPartialPaper");
        setupName = args.length>0? args[0] : "";
        boolean force_update = args.length>1? Boolean.valueOf(args[1]):false;
        System.out.println("\n\n***************");
        System.out.printf("Setup=%s, force_update=%s\n",setupName,String.valueOf(force_update));
        System.out.println("***************\n\n");
        switch (setupName){
            case "PurelyOnTBDense":
                TBDense();
                break;
            case "TBDense+TBAQ_Nonvague":
                TBDense_TBAQNonvague();
                break;
            default:
                System.out.println("[WARNING] setupname mismatch. Using default (PurelyOnTBDense)");
                TBDense();
        }
        loadDevTest();
        myLearn_DistSeparate exp = new myLearn_DistSeparate(force_update);
        exp.setModelDirAndName(modelDir,setupName);
        exp.myLearnWrapper_DistSeparate(trainingStructs,devStructs,negSamRate0,negSamRate1);

        exp.setTestStruct(testStruct);
        exp.evaluateTest();
    }
}
