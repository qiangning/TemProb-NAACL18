package PartialGraph;

import datastruct.TemporalEventPair;
import datastruct.TemporalStructure;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.lbjava.learn.Softmax;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.TLINK;
import edu.illinois.cs.cogcomp.nlp.util.PrecisionRecallManager;
import edu.illinois.cs.cogcomp.nlp.util.TransitivityTriplets;
import edu.illinois.cs.cogcomp.nlp.util.Triplet;
import util.TemporalInferenceWrapper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class myGlobalSolver {
    public static String[] ignore = new String[]{TLINK.TlinkType.UNDEF.toStringfull()};
    public static List<Triplet<Integer, Integer, List<Integer>>> transitivityMap = new ArrayList<>();


    public static PrecisionRecallManager eval_all_global = new PrecisionRecallManager();
    public static PrecisionRecallManager eval_all_local = new PrecisionRecallManager();


    public int n_event;
    public double[][][] local_score;
    public boolean[][] ignoreMap;
    public List<int[][][]> constraintMap;
    public TemporalStructure struct;

    public int[][] result;
    public int[][] gold;
    public TLINK.TlinkType[][] result_tlink;
    public MultiClassifiers<TemporalEventPair> ee_dist0, ee_dist1;
    private String awareness_dir;


    public void printGold(){
        System.out.println("Gold relations in "+struct.structID);
        for(int i=0;i<n_event;i++){
            for(int j=0;j<=i;j++){
                System.out.printf("%10s","x");
            }
            for(int j=i+1;j<n_event;j++){
                System.out.printf("%10s",ignoreMap[i][j]?"-":TLINK.TlinkType.tvalues()[gold[i][j]].toStringfull());
            }
            System.out.println();
        }
    }
    public void printPred(){
        System.out.println("ILP solved relations in "+struct.structID);
        for(int i=0;i<n_event;i++){
            for(int j=0;j<=i;j++){
                System.out.printf("%10s","x");
            }
            for(int j=i+1;j<n_event;j++){
                System.out.printf("%10s",ignoreMap[i][j]?"-":TLINK.TlinkType.tvalues()[result[i][j]].toStringfull());
            }
            System.out.println();
        }
    }
    public static void getTransitivityMap(){
        if(transitivityMap==null|| transitivityMap.size()==0) {
            List<TransitivityTriplets> transTriplets = TransitivityTriplets.transTriplets();
            for (TransitivityTriplets triplet : transTriplets) {
                TLINK.TlinkType t1 = triplet.getFirst();
                TLINK.TlinkType t2 = triplet.getSecond();
                TLINK.TlinkType[] t3 = triplet.getThird();
                List<Integer> tmp = new ArrayList<>();
                for (TLINK.TlinkType tt : t3) {
                    tmp.add(tt.getTValueIdx());
                }
                Triplet<Integer, Integer, List<Integer>> transMap = new Triplet<>(t1.getTValueIdx(), t2.getTValueIdx(), tmp);
                transitivityMap.add(transMap);
            }
        }
    }
    public void initAllArrays(){
        gold = new int[n_event][n_event];
        result_tlink = new TLINK.TlinkType[n_event][n_event];
        local_score = new double[n_event][n_event][TLINK.TlinkType.tvalues().length];
        ignoreMap = new boolean[n_event][n_event];
        constraintMap = new ArrayList<>();
        int[][][] uniqueness = new int[n_event][n_event][TLINK.TlinkType.tvalues().length+1];
        for(int i=0;i<n_event;i++){
            Arrays.fill(ignoreMap[i],true);
            Arrays.fill(result_tlink[i], TLINK.TlinkType.UNDEF);
            for(int j=0;j<n_event;j++) {
                Arrays.fill(local_score[i][j], 0);
                Arrays.fill(uniqueness[i][j],1);
                gold[i][j] = struct.relGraphFull[i][j].getTValueIdx();
            }
        }
        constraintMap.add(uniqueness);
    }
    public void getLocalScoreAndIgnoreMap(){
        Softmax sm = new Softmax();
        for(TemporalEventPair ee:struct.allEventPairs){
            int id1 = struct.allEvents.indexOf(ee.event1);
            int id2 = struct.allEvents.indexOf(ee.event2);
            String gold = ee.relation.toStringfull();
            String pred = "placeholder";
            ScoreSet scores = null;
            int dist = Math.abs(ee.event1.getSentId()-ee.event2.getSentId());
            switch (dist){
                case 0:
                    scores = ee_dist0.scores(ee);
                    pred = ee_dist0.discreteValue(ee);
                    break;
                case 1:
                    scores = ee_dist1.scores(ee);
                    pred = ee_dist1.discreteValue(ee);
                    break;
                default:
                    System.out.printf("EventPair dist larger than 1. %s\n",ee.toString());
            }
            if(scores!=null) {
                if(id1>id2)
                    System.out.println("WARNING: unexpected eventpair with reversed appearance order.");
                ignoreMap[id1][id2] = false;
                ScoreSet normScores = sm.normalize(scores);
                for (Object val : normScores.values()){
                    for(int k=0;k<TLINK.TlinkType.tvalues().length;k++){
                        if(val.equals(TLINK.TlinkType.tvalues()[k].toStringfull())){
                            local_score[id1][id2][TLINK.TlinkType.tvalues()[k].getTValueIdx()] = normScores.get((String)val);
                            break;
                        }
                    }
                }
                /*for(int k=0;k< TLINK.TlinkType.tvalues().length;k++){
                    double tmp = normScores.get(TLINK.TlinkType.tvalues()[k].toStringfull());
                    local_score[id1][id2][TLINK.TlinkType.tvalues()[k].getTValueIdx()]
                            = tmp;
                }*/
            }
            eval_all_local.addPredGoldLabels(pred,gold);
        }
    }
    public void addConstraintMap(int[][][] newConstraintMap){
        constraintMap.add(newConstraintMap);
    }
    public void applyGoldNonvagueAsConstraint(){
        int[][][] newConstraintMap = new int[n_event][n_event][TLINK.TlinkType.tvalues().length+1];
        for(int i=0;i<n_event;i++){
            for(int j=i+1;j<n_event;j++){
                Arrays.fill(newConstraintMap[i][j],1);
                if(struct.relGraphFull[i][j] != TLINK.TlinkType.UNDEF){
                    for(int k=0;k<TLINK.TlinkType.tvalues().length;k++){
                        if(gold[i][j]==k)
                            continue;
                        newConstraintMap[i][j][k] = 0;
                    }
                }
            }
        }
        addConstraintMap(newConstraintMap);
    }
    public void evaluate(){
        TemporalInferenceWrapper solver = new TemporalInferenceWrapper(n_event, TLINK.TlinkType.tvalues().length,
                local_score,ignoreMap,constraintMap,transitivityMap);
        solver.solve();
        result = solver.getResult();
        for(int i=0;i<n_event;i++){
            for(int j=i+1;j<n_event;j++){
                if(ignoreMap[i][j])
                    continue;
                String goldlabel = TLINK.TlinkType.tvalues()[gold[i][j]].toStringfull();
                result_tlink[i][j] = TLINK.TlinkType.tvalues()[result[i][j]];
                String predlabel = result_tlink[i][j].toStringfull();
                eval_all_global.addPredGoldLabels(predlabel, goldlabel);
            }
        }
    }
    public void saveForAwareness(){
        IOUtils.mkdir(awareness_dir+ File.separator+"gold");
        IOUtils.mkdir(awareness_dir+ File.separator+"global");
        struct.annTemporalDocumentUsingAllEventTimex(struct.getDoc(),struct.relGraphFull);
        struct.getDoc().temporalDocumentToText(awareness_dir+ File.separator+"gold"+File.separator+struct.structID+".tml");
        struct.annTemporalDocumentUsingAllEventTimex(struct.getDoc(),result_tlink);
        struct.getDoc().temporalDocumentToText(awareness_dir+File.separator+"global"+File.separator+struct.structID+".tml");
    }
    public static void restartEvaluator(){
        eval_all_local = new PrecisionRecallManager();
        eval_all_global = new PrecisionRecallManager();
    }
    public static void printLocalPerformance(){
        System.out.println("**********Local**********");
        System.out.println("-----Ignore Null-----");
        eval_all_local.printPrecisionRecall(ignore);
        System.out.println("-----No ignore-----");
        eval_all_local.printPrecisionRecall(new String[]{});
    }
    public static void printGlobalPerformance(){
        System.out.println("**********Global**********");
        System.out.println("-----Ignore Null-----");
        eval_all_global.printPrecisionRecall(ignore);
        System.out.println("-----No ignore-----");
        eval_all_global.printPrecisionRecall(new String[]{});
    }

    public String getAwareness_dir() {
        return awareness_dir;
    }

    public void setAwareness_dir(String awareness_dir) {
        this.awareness_dir = awareness_dir;
    }

    public myGlobalSolver(TemporalStructure struct, Learner cls0, Learner cls1) {
        this.struct = struct;
        this.ee_dist0 = new MultiClassifiers<TemporalEventPair>(cls0,1,true);
        this.ee_dist1 = new MultiClassifiers<TemporalEventPair>(cls1,1,true);
        n_event = struct.allEvents.size();
        getTransitivityMap();
        initAllArrays();
        getLocalScoreAndIgnoreMap();
    }
    public myGlobalSolver(TemporalStructure struct, MultiClassifiers<TemporalEventPair> cls0, MultiClassifiers<TemporalEventPair> cls1) {
        this.struct = struct;
        this.ee_dist0 = cls0;
        this.ee_dist1 = cls1;
        n_event = struct.allEvents.size();
        getTransitivityMap();
        initAllArrays();
        getLocalScoreAndIgnoreMap();
    }
}
