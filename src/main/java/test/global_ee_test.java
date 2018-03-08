package test;

import datastruct.Event;
import datastruct.TemporalEventPair;
import datastruct.TemporalStructure;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.infer.ilp.GurobiHook;
import edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet;
import edu.illinois.cs.cogcomp.lbjava.learn.Softmax;
import edu.illinois.cs.cogcomp.lbjava.learn.SparseNetworkLearner;
import edu.illinois.cs.cogcomp.nlp.CompareCAVEO.TBDense_split;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.TLINK;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.TLINK.*;
import edu.illinois.cs.cogcomp.nlp.util.PrecisionRecallManager;
import edu.illinois.cs.cogcomp.nlp.util.TempDocEval;
import edu.illinois.cs.cogcomp.nlp.util.TransitivityTriplets;
import edu.uw.cs.lil.uwtime.data.TemporalDocument;
import lbjava.*;
import util.TempDocLoader;
import util.TempLangMdl;

import java.io.File;
import java.util.*;

import static java.lang.System.exit;

public class global_ee_test {
    public int n;
    public List<Event> allEvents;
    public double[][][] local_scores;
    public double[][][] prior_scores;
    public double lambda = 1;
    public HashMap<Integer, List<Integer>> ignoreVarsMap;
    public HashMap<Integer, HashMap<Integer, List<TlinkType>>> knownVarsMap;
    public TlinkType[][] result;

    public global_ee_test(List<Event> allEvents, double[][][] local_scores, double[][][] prior_scores, double lambda) {
        this.allEvents = allEvents;
        this.local_scores = local_scores;
        this.prior_scores = prior_scores;
        this.lambda = lambda;
        n = allEvents.size();
        result = new TlinkType[n][n];

        int none_edge = Integer.MAX_VALUE;
        int ignore_edge = 2;
        int include_edge = 1;
        HashMap<TlinkType,Pair<Integer,Integer>> reliableRange = new HashMap<>();
        reliableRange.put(TlinkType.BEFORE,new Pair<Integer,Integer>(0,none_edge));
        reliableRange.put(TlinkType.AFTER,new Pair<Integer,Integer>(0,none_edge));
        reliableRange.put(TlinkType.INCLUDES,new Pair<Integer,Integer>(0,include_edge));
        reliableRange.put(TlinkType.IS_INCLUDED,new Pair<Integer,Integer>(0,include_edge));
        reliableRange.put(TlinkType.EQUAL,new Pair<Integer,Integer>(0,none_edge));
        reliableRange.put(TlinkType.UNDEF,new Pair<Integer,Integer>(0,Integer.MAX_VALUE));
        setReliableRange(reliableRange);
        genIgnoreMapByDist(ignore_edge,none_edge);
    }

    /*Ignore event pairs with distance between min_dist and max_dist (inclusive).*/
    public void genIgnoreMapByDist(int min_dist, int max_dist) {
        if(ignoreVarsMap==null)
            ignoreVarsMap = new HashMap<>();
        for(int i=0;i<n;i++){
            for(int j=i+1;j<n;j++){
                if(allEvents.get(j).getSentId()-allEvents.get(i).getSentId()>=min_dist
                        &&allEvents.get(j).getSentId()-allEvents.get(i).getSentId()<=max_dist) {
                    if(!ignoreVarsMap.containsKey(i))
                        ignoreVarsMap.put(i,new ArrayList<>());
                    ignoreVarsMap.get(i).add(j);
                }
            }
        }
    }
    public void setReliableRange(HashMap<TlinkType,Pair<Integer,Integer>> reliableRange){
        if(knownVarsMap==null)
            knownVarsMap = new HashMap<>();
        for(int i=0;i<n;i++){
            knownVarsMap.put(i,new HashMap<>());
            for(int j=i+1;j<n;j++){
                int diff = allEvents.get(j).getSentId()-allEvents.get(i).getSentId();
                Set<TlinkType> types = reliableRange.keySet();
                if(types.size()==0) {
                    System.out.println("reliableRange.keySet.size()==0!");
                    exit(-1);
                }
                List<TlinkType> tmp = new ArrayList<>();
                for(TlinkType type:types){
                    int min_dist = reliableRange.get(type).getFirst();
                    int max_dist = reliableRange.get(type).getSecond();
                    if(diff>=min_dist&&diff<=max_dist){
                        tmp.add(type);
                    }
                }
                if(tmp.size()>0)
                    knownVarsMap.get(i).put(j,tmp);
            }
        }
    }
    public void solve() throws Exception{
        GurobiHook solver = new GurobiHook();
        HashMap<Integer, HashMap<Integer, HashMap<TlinkType, Integer>>> eeVar = new HashMap<>();
        /*Add variable*/
        for(int i=0;i<n;i++) {
            eeVar.put(i, new HashMap<>());
            for (int j = i + 1; j < n; j++) {
                if (ignoreVarsMap != null
                        && ignoreVarsMap.containsKey(i)
                        && ignoreVarsMap.get(i).contains(j)) {
                    continue;
                }
                eeVar.get(i).put(j, new HashMap<>());
                for(int k=0;k<TlinkType.tvalues().length;k++){
                    int var = solver.addBooleanVariable(local_scores[i][j][k]+lambda*prior_scores[i][j][k]);
                    eeVar.get(i).get(j).put(TlinkType.tvalues()[k],var);
                }
            }
        }
        /*Use knownVarsMap information*/
        if (knownVarsMap != null) {
            for (int id1 : knownVarsMap.keySet()) {
                for (int id2 : knownVarsMap.get(id1).keySet()) {
                    if (id1 == id2)
                        continue;
                    if (ignoreVarsMap != null
                            && ignoreVarsMap.containsKey(id1)
                            && ignoreVarsMap.get(id1).contains(id2)) {
                        continue;
                    }
                    int k = knownVarsMap.get(id1).get(id2).size();
                    int[] vars = new int[k];
                    double[] coefs = new double[k];
                    int i = 0;
                    for (TlinkType str : knownVarsMap.get(id1).get(id2)) {
                        vars[i] = eeVar.get(id1).get(id2).get(str);
                        coefs[i] = 1;
                        i++;
                    }
                    solver.addEqualityConstraint(vars, coefs, 1);
                }
            }
        }
        /*Add uniqueness constraints*/
        for(int i=0;i<n;i++) {
            for (int j = i + 1; j < n; j++) {
                if (ignoreVarsMap != null
                        && ignoreVarsMap.containsKey(i)
                        && ignoreVarsMap.get(i).contains(j)) {
                    continue;
                }
                Set<TlinkType> rels = eeVar.get(i).get(j).keySet();
                int k = rels.size();
                int[] vars = new int[k];
                double[] coefs = new double[k];
                int m = 0;
                for (TlinkType rel : rels) {
                    vars[m] = eeVar.get(i).get(j).get(rel);
                    coefs[m] = 1;
                    m++;
                }
                solver.addEqualityConstraint(vars, coefs, 1);
            }
        }
        /*Add transitivity constraints*/
        for(int i=0;i<n;i++) {
            for (int j = i + 1; j < n; j++) {
                if (ignoreVarsMap != null
                        && ignoreVarsMap.containsKey(i)
                        && ignoreVarsMap.get(i).contains(j)) {
                    continue;
                }
                for(int k=i+1;k<n;k++){
                    if (k == j)
                        continue;
                    if (ignoreVarsMap != null
                            && ignoreVarsMap.containsKey(i)
                            && ignoreVarsMap.get(i).contains(k)) {
                        continue;
                    }
                    if(j<k) {
                        if (ignoreVarsMap != null
                                && ignoreVarsMap.containsKey(j)
                                && ignoreVarsMap.get(j).contains(k)) {
                            continue;
                        }
                    }
                    else {//k<j
                        if (ignoreVarsMap != null
                                && ignoreVarsMap.containsKey(k)
                                && ignoreVarsMap.get(k).contains(j)) {
                            continue;
                        }
                    }
                    List<TransitivityTriplets> transTriplets = TransitivityTriplets.transTriplets();
                    for (TransitivityTriplets triplet : transTriplets) {
                        int m = triplet.getThird().length;
                        double[] coefs = new double[m + 2];
                        int[] vars = new int[m + 2];
                        coefs[0] = 1;
                        coefs[1] = 1;
                        vars[0] = eeVar.get(i).get(j).get(triplet.getFirst());
                        vars[1] = j<k?
                                eeVar.get(j).get(k).get(triplet.getSecond())
                                :eeVar.get(k).get(j).get(triplet.getSecond().reverse())
                        ;
                        for (int p = 0; p < m; p++) {
                            coefs[p + 2] = -1;
                            vars[p + 2] = eeVar.get(i).get(k).get(triplet.getThird()[p]);
                        }
                        solver.addLessThanConstraint(vars, coefs, 1);
                    }
                }
            }
        }
        solver.setMaximize(true);
        solver.solve();
        for(int i=0;i<n;i++){
            for(int j=i+1;j<n;j++){
                if (ignoreVarsMap != null
                        && ignoreVarsMap.containsKey(i)
                        && ignoreVarsMap.get(i).contains(j)) {
                    continue;
                }
                Set<TlinkType> rels = eeVar.get(i).get(j).keySet();
                int var;
                TlinkType tt = TlinkType.UNDEF;
                int cnt = 0;
                for (TlinkType rel : rels) {
                    var = eeVar.get(i).get(j).get(rel);
                    if (solver.getBooleanValue(var)) {
                        tt = rel;
                        cnt++;
                    }
                }
                if (cnt > 1)
                    exit(-1);
                result[i][j] = tt;
            }
        }
    }
    public static void Table5_reproduce(SparseNetworkLearner cls0, SparseNetworkLearner cls1) throws Exception{
        List<TemporalDocument> allStructs_tmp = TempDocLoader.getTimeBankInstance().allDocs;
        TempDocLoader.readBethard(allStructs_tmp);
        TempDocLoader.readChambers(allStructs_tmp);
        List<TemporalDocument> allTestStructs_tmp = new ArrayList<>();
        for(TemporalDocument doc:allStructs_tmp){
            if(TBDense_split.findDoc(doc.getDocID())==3)
                allTestStructs_tmp.add(doc);
        }
        List<TemporalStructure> allTestStructs = new ArrayList<>();
        for(TemporalDocument doc:allTestStructs_tmp){
            TemporalStructure newStruct = new TemporalStructure(doc,true);
            newStruct.addIsBeforeCorpusFeat();
            newStruct.addOtherLabelCorpusFeat();
            newStruct.extractPrepPhrase();
            newStruct.annTemporalDocumentUsingSRLEventPairs(doc,newStruct.relGraph);
            allTestStructs.add(newStruct);
        }
        PrecisionRecallManager evaluator_dist0 = new PrecisionRecallManager();
        PrecisionRecallManager evaluator_dist1 = new PrecisionRecallManager();
        PrecisionRecallManager evaluator = new PrecisionRecallManager();
        for (TemporalStructure tempSt : allTestStructs) {
            for (TemporalEventPair ep : tempSt.eventPairs) {
                String gold = ep.relation.toStringfull();
                String pred;
                int diff = ep.event2.getSentId() - ep.event1.getSentId();
                switch (diff) {
                    case 0:
                        pred = cls0.discreteValue(ep);
                        evaluator_dist0.addPredGoldLabels(pred, gold);
                        evaluator.addPredGoldLabels(pred, gold);
                        break;
                    case 1:
                        pred = cls1.discreteValue(ep);
                        evaluator_dist1.addPredGoldLabels(pred, gold);
                        evaluator.addPredGoldLabels(pred, gold);
                        break;
                    default:
                }
            }
        }
        System.out.println("\n\n**Sent dist=0**\n\n");
        evaluator_dist0.printPrecisionRecall(new String[]{TLINK.TlinkType.UNDEF.toStringfull()});

        System.out.println("\n\n**Sent dist=1**\n\n");
        evaluator_dist1.printPrecisionRecall(new String[]{TLINK.TlinkType.UNDEF.toStringfull()});

        System.out.println("\n\n**Overall**\n\n");
        evaluator.printPrecisionRecall(new String[]{TLINK.TlinkType.UNDEF.toStringfull()});
    }
    public static void main(String[] args) throws Exception{
        ResourceManager rm = new ResourceManager("config/reproducibility.properties");
        String lm_path = rm.getString("TemProb");
        TemporalStructure.myTempLangMdl = TempLangMdl.getInstance(lm_path);
        boolean update_gold = false;
        boolean use_prior_in_local = false;
        int local_mode = 2;
        String awareness_name = args[0].toLowerCase();
        double lambda = 0d;
        String cls0_path,cls1_path;
        SparseNetworkLearner cls0, cls1;
        switch(awareness_name){
            //models/noClustering/perceptron_CV_dist0_mod14_met0
            case "table5_line1":
                cls0_path = "models/noClustering/perceptron_CV_dist0_mod14_met0";
                cls1_path = "models/local_ee_perceptron_bestCV_1";
                cls0 = new local_ee_pp_poswin_conn_corr(cls0_path+".lc",cls0_path+".lex");
                cls1 = new local_ee(cls1_path+".lc",cls1_path+".lex");
                Table5_reproduce(cls0,cls1);
                return;
            case "table5_line2":
                cls0_path = "models/noClustering/perceptron_CV_dist0_mod15_met0";
                cls1_path = "models/noClustering/perceptron_CV_dist1_mod2_met0";
                cls0 = new local_ee_pp_poswin_conn_corr1(cls0_path+".lc",cls0_path+".lex");
                cls1 = new local_ee_corpus(cls1_path+".lc",cls1_path+".lex");
                Table5_reproduce(cls0,cls1);
                return;
            case "table5_line3":
                cls0_path = rm.getString("ModelCls0_noGoldProp");
                cls1_path = rm.getString("ModelCls1_noGoldProp");
                cls0 = new local_ee_pp_poswin_conn_corr2(cls0_path+".lc",cls0_path+".lex");
                cls1 = new local_ee_corpus_allLabels(cls1_path+".lc",cls1_path+".lex");
                Table5_reproduce(cls0,cls1);
                return;
            case "table6_line1":
                use_prior_in_local = false;
                lambda = 0d;
                break;
            case "table6_line2":
                use_prior_in_local = true;
                local_mode = 100;
                lambda = 0d;
                break;
            case "table6_line3":
                use_prior_in_local = true;
                local_mode = 100;
                lambda = 0.2d;
                break;
            case "table8_line3":
                use_prior_in_local = true;
                local_mode = 101;
                lambda = 0.5d;
                break;
            default:
                exit(-1);
        }

        String awareness_log_dir = rm.getString("AwarenessLog");
        String awareness_output_dir = rm.getString("AwarenessOutput")+File.separator;
        String awareness_ser_dir = rm.getString("AwarenessSer")+File.separator;
        String gold_dir = awareness_output_dir+"gold";
        String global_dir = awareness_output_dir+"global"+File.separator+awareness_name;
        String global_ser_dir = awareness_ser_dir+"global"+File.separator+awareness_name;
        IOUtils.mkdir(awareness_log_dir);
        IOUtils.mkdir(gold_dir);
        IOUtils.mkdir(global_dir);
        IOUtils.mkdir(global_ser_dir);

        List<TemporalDocument> allStructs_tmp = TempDocLoader.getTimeBankInstance().allDocs;
        TempDocLoader.readBethard(allStructs_tmp);
        TempDocLoader.readChambers(allStructs_tmp);
        List<TemporalDocument> allTestStructs_tmp = new ArrayList<>();
        for(TemporalDocument doc:allStructs_tmp){
            if(TBDense_split.findDoc(doc.getDocID())==3)
                allTestStructs_tmp.add(doc);
        }
        List<TemporalStructure> allTestStructs = new ArrayList<>();
        for(TemporalDocument doc:allTestStructs_tmp){
            TemporalStructure newStruct = new TemporalStructure(doc,true);
            newStruct.addIsBeforeCorpusFeat();
            newStruct.addOtherLabelCorpusFeat();
            newStruct.extractPrepPhrase();
            newStruct.annTemporalDocumentUsingSRLEventPairs(doc,newStruct.relGraph);
            if(update_gold)
                doc.temporalDocumentToText(gold_dir+File.separator+doc.getDocID()+".tml");
            allTestStructs.add(newStruct);
        }
        if(!use_prior_in_local) {
            cls0 = new local_ee_pp_poswin_conn_corr("models/noClustering/perceptron_CV_dist0_mod14_met0.lc",
                    "models/noClustering/perceptron_CV_dist0_mod14_met0.lex");
            cls1 = new local_ee("models/local_ee_perceptron_bestCV_1.lc",
                    "models/local_ee_perceptron_bestCV_1.lex");
        }
        else {
            switch(local_mode) {
                case 100:
                    cls0_path = rm.getString("ModelCls0_noGoldProp");
                    cls1_path = rm.getString("ModelCls1_noGoldProp");
                    cls0 = new local_ee_pp_poswin_conn_corr2(cls0_path+".lc",cls0_path+".lex");
                    cls1 = new local_ee_corpus_allLabels(cls1_path+".lc",cls1_path+".lex");
                    break;
                case 101:
                    cls0_path = rm.getString("ModelCls0");
                    cls1_path = rm.getString("ModelCls1");
                    cls0 = new local_ee_pp_poswin_conn_corr2(cls0_path+".lc",cls0_path+".lex");
                    cls1 = new local_ee_corpus_allLabels_gold(cls1_path+".lc",cls1_path+".lex");
                    break;
                default:
                    System.out.println("Please specify local classifier mode. Default mode 2 is used.");
                    cls0 = new local_ee_corpus("models/noClustering" + String.format("/perceptron_CV_dist0_mod%d_met0.lc", local_mode),
                            "models/noClustering" + String.format("/perceptron_CV_dist0_mod%d_met0.lex", local_mode));
                    cls1 = new local_ee_corpus("models/noClustering" + String.format("/perceptron_CV_dist1_mod%d_met0.lc", local_mode),
                            "models/noClustering" + String.format("/perceptron_CV_dist1_mod%d_met0.lex", local_mode));
            }
        }
        PrecisionRecallManager evaluatorLocal = new PrecisionRecallManager();
        PrecisionRecallManager evaluatorGlobal = new PrecisionRecallManager();
        for (TemporalStructure tempSt : allTestStructs) {
            PrecisionRecallManager evaluatorDocLocal = new PrecisionRecallManager();
            PrecisionRecallManager evaluatorDocGlobal = new PrecisionRecallManager();
            List<Event> allEvents = tempSt.allSRLEvents;
            int eventSize = allEvents.size();
            double[][][] local_scores = new double[eventSize][eventSize][TlinkType.tvalues().length];
            double[][][] prior_scores = new double[eventSize][eventSize][TlinkType.tvalues().length];
            TLINK.TlinkType[][] localGraph = new TlinkType[eventSize][eventSize];
            for (TemporalEventPair ep : tempSt.eventPairs) {
                int diff = ep.event2.getSentId() - ep.event1.getSentId();
                int id1 = allEvents.indexOf(ep.event1);
                int id2 = allEvents.indexOf(ep.event2);
                SparseNetworkLearner cls = cls0;
                switch (diff) {
                    case 0:
                        cls = cls0;
                        break;
                    case 1:
                        cls = cls1;
                        break;
                    default:
                        cls = cls0;
                }
                ScoreSet scores = cls.scores(ep);
                String pred = cls.discreteValue(ep);
                String gold = ep.relation.toStringfull();
                evaluatorDocLocal.addPredGoldLabels(pred, gold);
                evaluatorLocal.addPredGoldLabels(pred, gold);
                Softmax sm = new Softmax();
                ScoreSet normScores = sm.normalize(scores);
                localGraph[id1][id2] = TlinkType.str2TlinkType(pred);
                for (int i = 0; i < TlinkType.tvalues().length; i++) {
                    local_scores[id1][id2][i]
                            = normScores.get(TlinkType.tvalues()[i].toStringfull());
                }
                double total = ep.c1 + ep.c2 + ep.c_i + ep.c_ii + ep.c_e + ep.c_v;
                prior_scores[id1][id2] = new double[]{ep.c1 / total, ep.c2 / total, ep.c_e / total, ep.c_i / total, ep.c_ii / total, ep.c_v / total};
            }
            global_ee_test exp = new global_ee_test(allEvents, local_scores, prior_scores, lambda);
            exp.solve();
            TemporalDocument docGlobal, docLocal;
            docGlobal = allTestStructs_tmp.get(allTestStructs.indexOf(tempSt));
            docLocal = new TemporalDocument(docGlobal);
            tempSt.annTemporalDocumentUsingSRLEventPairs(docGlobal,exp.result);
            tempSt.annTemporalDocumentUsingSRLEventPairs(docLocal,localGraph);
            docGlobal.temporalDocumentToText(global_dir +File.separator+ docGlobal.getDocID() + ".tml");
            docGlobal.serialize(global_ser_dir, docGlobal.getDocID(), true);
            for (int i = 0; i < eventSize; i++) {
                for (int j = i + 1; j < eventSize; j++) {
                    if (allEvents.get(j).getSentId() - allEvents.get(i).getSentId() > 1)
                        continue;
                    TlinkType pred = exp.result[i][j];
                    TlinkType gold = tempSt.relGraph[i][j];
                    evaluatorGlobal.addPredGoldLabels(pred.toStringfull(), gold.toStringfull());
                    evaluatorDocGlobal.addPredGoldLabels(pred.toStringfull(), gold.toStringfull());
                }
            }
            System.out.printf("-----------Doc:%d-----------\n", allTestStructs.indexOf(tempSt));
            System.out.println("**Local**");
            evaluatorDocLocal.printPrecisionRecall(new String[]{TLINK.TlinkType.UNDEF.toStringfull()});
            System.out.println("**Global**");
            evaluatorDocGlobal.printPrecisionRecall(new String[]{TLINK.TlinkType.UNDEF.toStringfull()});
        }
        System.out.println("-----------Overall-----------");
        System.out.println("**Local**");
        evaluatorLocal.printPrecisionRecall(new String[]{TLINK.TlinkType.UNDEF.toStringfull()});
        System.out.println("**Global**");
        evaluatorGlobal.printPrecisionRecall(new String[]{TLINK.TlinkType.UNDEF.toStringfull()});


        /*Evaluation*/
        System.out.println("-----------Awareness Scores-----------");
        System.out.printf("\nThe awareness scores should be in %s. If it seems unfinished, manually run this\n\n",
                awareness_log_dir+File.separator+awareness_name);
        TempDocEval.RunOfficialTemporalEval(gold_dir,global_dir,awareness_log_dir.replaceAll("logs/",""),awareness_name);
    }
}
