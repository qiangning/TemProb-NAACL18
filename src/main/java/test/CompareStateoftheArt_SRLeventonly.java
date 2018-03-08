package test;

import datastruct.TemporalStructure;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.TLINK;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.TempEval3Reader;
import edu.illinois.cs.cogcomp.nlp.util.PrecisionRecallManager;
import edu.illinois.cs.cogcomp.nlp.util.TempDocEval;
import edu.uw.cs.lil.uwtime.data.TemporalDocument;
import util.TempDocLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class CompareStateoftheArt_SRLeventonly {
    static ResourceManager rm;
    public static class DocAndStruct{
        public TemporalDocument doc;
        public TemporalStructure struct;
        public DocAndStruct(TemporalDocument doc, TemporalStructure struct) {
            this.doc = doc;
            this.struct = struct;
        }
    }
    public static List<DocAndStruct> naacl = new ArrayList<>();
    public static List<DocAndStruct> emnlp = new ArrayList<>();
    public static List<DocAndStruct> caevo = new ArrayList<>();
    public static List<DocAndStruct> gold = new ArrayList<>();
    public static void loadGold(){
        List<TemporalStructure> gold_structs =  TempDocLoader.getTBDenseInstance().getAllStructs(3);
        List<TemporalDocument> gold_docs = TempDocLoader.getTBDenseInstance().getAllDocs(3);
        for(int i=0;i<gold_structs.size();i++){
            gold.add(new DocAndStruct(gold_docs.get(i),gold_structs.get(i)));
        }
        gold.sort(new Comparator<DocAndStruct>() {
            @Override
            public int compare(DocAndStruct o1, DocAndStruct o2) {
                return o1.struct.structID.compareTo(o2.struct.structID);
            }
        });
    }
    public static void loadNaacl(){
        try {
            //List<TemporalDocument> kbcom_docs = TempEval3Reader.deserialize("/home/qning2/Research/KBconstruction/serialized_data/TBDense-SRLEvent/best/noClustering_allLabels_vagueCorr2_regGloVe42BK1000_newfeat");
            List<TemporalDocument> kbcom_docs = TempEval3Reader.deserialize(rm.getString("AwarenessSer")+File.separator+
                    "global/table8_line3");
            for(TemporalDocument doc:kbcom_docs){
                TemporalStructure struct = new TemporalStructure(doc,false,false);
                naacl.add(new DocAndStruct(doc,struct));
            }
            naacl.sort(new Comparator<DocAndStruct>() {
                @Override
                public int compare(DocAndStruct o1, DocAndStruct o2) {
                    return o1.struct.structID.compareTo(o2.struct.structID);
                }
            });
        }
        catch (Exception e){e.printStackTrace();}
    }
    public static void loadEmnlp(){
        try {
            if(naacl.size()==0)
                loadNaacl();
            List<TemporalDocument> emnlp_docs = TempEval3Reader.deserialize(rm.getString("EMNLPSer"));
            for(DocAndStruct docAndStruct: naacl){
                TemporalDocument emnlp_doc = new TemporalDocument(docAndStruct.doc);
                TemporalDocument tmp = null;
                for(int i=0;i<emnlp_docs.size();i++){
                    tmp = emnlp_docs.get(i);
                    if(tmp.getDocID().equals(emnlp_doc.getDocID()))
                        break;
                }
                if(tmp!=null)
                    emnlp_doc.setBodyTlinks(tmp.deepCopyTlink());
                else
                    System.out.println("Warning");
                TemporalStructure struct = new TemporalStructure(emnlp_doc,false,false);
                emnlp.add(new DocAndStruct(emnlp_doc,struct));
            }
            emnlp.sort(new Comparator<DocAndStruct>() {
                @Override
                public int compare(DocAndStruct o1, DocAndStruct o2) {
                    return o1.struct.structID.compareTo(o2.struct.structID);
                }
            });
        }
        catch (Exception e){e.printStackTrace();}
    }
    public static HashMap<String,List<TLINK>> get_CAEVO_tlinks(){
        HashMap<String,List<TLINK>> tlinks = new HashMap<>();
        try {
            FileInputStream fileIn = new FileInputStream(rm.getString("CAEVOSer"));
            ObjectInputStream in = new ObjectInputStream(fileIn);
            tlinks = (HashMap<String,List<TLINK>>) in.readObject();
            in.close();
            fileIn.close();
        }catch(Exception i) { i.printStackTrace();}
        return tlinks;
    }
    public static void loadCaevo(){
        try {
            if(naacl.size()==0)
                loadNaacl();
            HashMap<String, List<TLINK>> caevo_tlinks = get_CAEVO_tlinks();
            for(DocAndStruct docAndStruct: naacl){
                TemporalDocument caevo_doc = new TemporalDocument(docAndStruct.doc);
                caevo_doc.setBodyTlinks(caevo_tlinks.get(caevo_doc.getDocID()+".tml"));
                TemporalStructure struct = new TemporalStructure(caevo_doc,false,false);
                caevo.add(new DocAndStruct(caevo_doc,struct));
            }
            caevo.sort(new Comparator<DocAndStruct>() {
                @Override
                public int compare(DocAndStruct o1, DocAndStruct o2) {
                    return o1.struct.structID.compareTo(o2.struct.structID);
                }
            });
        }
        catch (Exception e){e.printStackTrace();}
    }
    public static void eval(List<DocAndStruct> testset, String label){
        String awareness_data_dir = rm.getString("AwarenessOutput")+File.separator+"CompareStateoftheArt_PartialTBDense"+ File.separator+label;
        IOUtils.mkdir(awareness_data_dir);
        String awareness_log_dir = rm.getString("AwarenessLog")+File.separator+"CompareStateoftheArt_PartialTBDense";
        IOUtils.mkdir(awareness_log_dir);
        PrecisionRecallManager eval = new PrecisionRecallManager();
        int n = gold.size();
        for(int k=0;k<n;k++){
            TemporalStructure st = testset.get(k).struct;
            st.annTemporalDocumentUsingSRLEventPairs(testset.get(k).doc,st.relGraph);
            testset.get(k).doc.temporalDocumentToText(awareness_data_dir+File.separator+st.structID+".tml");
            int eventSize = st.allSRLEvents.size();
            for (int i = 0; i < eventSize; i++) {
                for (int j = i + 1; j < eventSize; j++) {
                    if (st.allSRLEvents.get(j).getSentId() - st.allSRLEvents.get(i).getSentId() > 1)
                        continue;
                    String predlabel = st.getRelTypefromIndex(i,j).toStringfull();
                    String goldlabel = gold.get(k).struct.getRelTypefromIndex(i,j).toStringfull();
                    eval.addPredGoldLabels(predlabel,goldlabel);
                }
            }
        }
        eval.printPrecisionRecall(new String[]{TLINK.TlinkType.UNDEF.toStringfull()});
        String gold_dir = rm.getString("AwarenessOutput")+File.separator+"gold";
        System.out.println("\nTo evaluate. Run this\n");
        try {
            TempDocEval.RunOfficialTemporalEval(gold_dir, awareness_data_dir, awareness_log_dir.replaceAll("logs/",""), label);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void eval_naacl(){
        loadGold();
        loadNaacl();
        eval(naacl,"naacl");
    }
    public static void eval_emnlp(){
        loadGold();
        loadEmnlp();
        eval(emnlp,"emnlp");
    }
    public static void eval_caevo(){
        loadGold();
        loadCaevo();
        eval(caevo,"caevo");
    }
    public static void main(String[] args) throws Exception{
        rm = new ResourceManager("config/reproducibility.properties");
        switch(args[0].toLowerCase()){
            case "caevo":
                eval_caevo();
                break;
            case "emnlp":
                eval_emnlp();
                break;
            case "naacl":
                eval_naacl();
                break;
            default:
                eval_emnlp();
        }
    }
}
