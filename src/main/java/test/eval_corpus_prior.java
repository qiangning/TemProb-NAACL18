package test;

import datastruct.TemporalEventPair;
import datastruct.TemporalStructure;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.CompareCAVEO.TBDense_split;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.TLINK;
import edu.illinois.cs.cogcomp.nlp.util.PrecisionRecallManager;
import edu.uw.cs.lil.uwtime.data.TemporalDocument;
import util.TempDocLoader;
import util.TempLangMdl;

import java.util.ArrayList;
import java.util.List;

import static util.TempDocLoader.readBethard;
import static util.TempDocLoader.readChambers;

public class eval_corpus_prior {
    public static void main(String[] args) throws Exception{
        ResourceManager rm = new ResourceManager("config/reproducibility.properties");
        double th = Double.valueOf(args[0]);
        List<TemporalDocument> allDocs_tmp = TempDocLoader.getTimeBankInstance().allDocs;
        readBethard(allDocs_tmp);
        readChambers(allDocs_tmp);
        List<TemporalDocument> allDocs = new ArrayList<>();
        for(TemporalDocument doc:allDocs_tmp){
            if(TBDense_split.findDoc(doc.getDocID())>0)
                allDocs.add(doc);
        }
        TempLangMdl mdl = TempLangMdl.getInstance(rm.getString("TemProb"));
        TemporalStructure.myTempLangMdl = mdl;
        PrecisionRecallManager evaluator = new PrecisionRecallManager();
        PrecisionRecallManager evaluator_sent0 = new PrecisionRecallManager();
        PrecisionRecallManager evaluator_sent1 = new PrecisionRecallManager();
        PrecisionRecallManager evaluator_sentMore = new PrecisionRecallManager();

        List<TemporalStructure> allStructs = new ArrayList<>();
        for(TemporalDocument doc:allDocs){
            TemporalStructure newStruct = new TemporalStructure(doc,true);
            newStruct.addIsBeforeCorpusFeat();
            allStructs.add(newStruct);
        }
        for(TemporalStructure tempStr:allStructs){
            for(TemporalEventPair ep:tempStr.eventPairs){
                if(ep.isRelNull()||ep.isRelInclude()||ep.relation== TLINK.TlinkType.EQUAL)
                    continue;
                double r = ep.c1/(ep.c1+ep.c2);
                String pred;
                if(r>th)
                    pred = TLINK.TlinkType.BEFORE.toStringfull();
                else if(r<1-th)
                    pred = TLINK.TlinkType.AFTER.toStringfull();
                else
                    pred = TLINK.TlinkType.UNDEF.toStringfull();
                String gold = ep.relation.toStringfull();
                evaluator.addPredGoldLabels(pred,gold);
                switch(ep.event2.getSentId()-ep.event1.getSentId()){
                    case 0:
                        evaluator_sent0.addPredGoldLabels(pred,gold);
                        break;
                    case 1:
                        evaluator_sent1.addPredGoldLabels(pred,gold);
                        break;
                    default:
                        evaluator_sentMore.addPredGoldLabels(pred,gold);
                        break;
                }
            }
        }
        System.out.println("Sent 0:");
        evaluator_sent0.printPrecisionRecall(new String[]{TLINK.TlinkType.UNDEF.toStringfull()});
        System.out.println("Sent 1:");
        evaluator_sent1.printPrecisionRecall(new String[]{TLINK.TlinkType.UNDEF.toStringfull()});
        System.out.println("Sent >=2:");
        evaluator_sentMore.printPrecisionRecall(new String[]{TLINK.TlinkType.UNDEF.toStringfull()});
        System.out.println("Sent All:");
        evaluator.printPrecisionRecall(new String[]{TLINK.TlinkType.UNDEF.toStringfull()});
    }
}

