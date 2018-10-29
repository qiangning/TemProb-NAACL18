package test;

import datastruct.Event;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.JointTempCausal.CEA_output;
import edu.illinois.cs.cogcomp.nlp.JointTempCausal.myPair;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.TLINK;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.TempEval3Reader;
import edu.illinois.cs.cogcomp.nlp.util.PrecisionRecallManager;
import edu.uw.cs.lil.uwtime.chunking.chunks.EventChunk;
import edu.uw.cs.lil.uwtime.data.TemporalDocument;
import edu.uw.cs.lil.uwtime.data.readers.TimeMLReader;
import util.TempLangMdl;

import java.util.HashMap;
import java.util.List;

import static edu.illinois.cs.cogcomp.nlp.util.TempDocEval.Dir2ReaderFormat;

public class CausalDirection {
    //@@ this is a quick test. Later should use all 20 docs and get rid of the CEA_output.RemovePairsNotAnnotated part
    public static void useNYTprior() throws Exception{
        ResourceManager rm = new ResourceManager("config/reproducibility.properties");
        String lm_path = rm.getString("TemProb");
        String causality_dir = rm.getString("CausalityData");
        TemporalDocument.cacheDir_ta = rm.getString("TextAnnotationCacheDir");

        TempLangMdl myTempLangMdl = TempLangMdl.getInstance(lm_path);
        PrecisionRecallManager eval_baseline = new PrecisionRecallManager();
        PrecisionRecallManager eval = new PrecisionRecallManager();

        TimeMLReader.force_update = false;
        TempEval3Reader reader = new TempEval3Reader("TIMEML", Dir2ReaderFormat(causality_dir).getSecond(), Dir2ReaderFormat(causality_dir).getFirst());
        reader.ReadData();
        reader.createTextAnnotation();
        List<TemporalDocument> testdocs = reader.getDataset().getDocuments();
        //List<TemporalDocument> testdocs = TempEval3Reader.deserialize("serialized_data/joint_causal/quang_gold_temporal");
        CEA_output.goldfile = "data/Quang/keys/eval.keys";
        HashMap<String,HashMap<myPair,Double>> allGold = CEA_output.ReadGoldGivenDocs(testdocs);
        for(TemporalDocument doc:testdocs){
            HashMap<myPair,Double> gold = allGold.get(doc.getDocID());
            for(myPair p:gold.keySet()){
                String goldlabel = p.e1.compareTo(p.e2)<0?"causes":"caused_by", predlabel, predlabel_baseline;
                EventChunk ec1 = doc.getEventMentionFromEID(Integer.valueOf(CEA_output.idConversion(doc,p.e1).substring(1)));
                EventChunk ec2 = doc.getEventMentionFromEID(Integer.valueOf(CEA_output.idConversion(doc,p.e2).substring(1)));
                Event e1 = new Event(ec1,doc.getTextAnnotation(),ec1.getEid());
                Event e2 = new Event(ec2,doc.getTextAnnotation(),ec2.getEid());
                String c1,c2;
                if(myTempLangMdl.cluster!=null) {
                    c1 = myTempLangMdl.cluster.getOrDefault(e1.getLemma(), "-1");
                    c2 = myTempLangMdl.cluster.getOrDefault(e2.getLemma(), "-1");
                }
                else{
                    c1 = e1.getLemma()+".01";
                    c2 = e2.getLemma()+".01";
                }
                int cnt1=1,cnt2=1;
                if(myTempLangMdl.tempLangMdl.containsKey(c1)&&myTempLangMdl.tempLangMdl.get(c1).containsKey(c2)){
                    cnt1 = myTempLangMdl.tempLangMdl.get(c1).get(c2).getOrDefault(TLINK.TlinkType.BEFORE,0)+1;
                    cnt2 = myTempLangMdl.tempLangMdl.get(c1).get(c2).getOrDefault(TLINK.TlinkType.AFTER,0)+1;
                }
                if(cnt1>=cnt2){
                    if(p.e1.compareTo(p.e2)<0)
                        predlabel = "causes";
                    else
                        predlabel = "caused_by";
                }
                else{
                    if(p.e1.compareTo(p.e2)<0)
                        predlabel = "caused_by";
                    else
                        predlabel = "causes";
                }
                predlabel_baseline = "causes";

                eval.addPredGoldLabels(predlabel,goldlabel);
                eval_baseline.addPredGoldLabels(predlabel_baseline,goldlabel);
            }
        }
        eval.printPrecisionRecall(new String[]{"undef"});
        eval_baseline.printPrecisionRecall(new String[]{"undef"});
    }
    public static void main(String[] args) throws Exception{
        useNYTprior();
    }
}
