package test;

import datastruct.TemporalEventPair;
import datastruct.TemporalStructure;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.TLINK;
import edu.illinois.cs.cogcomp.nlp.util.PrecisionRecallManager;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.TempEval3Reader;
import edu.illinois.cs.cogcomp.nlp.util.TempDocEval;
import edu.uw.cs.lil.uwtime.data.TemporalDocument;
import util.TempDocLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static edu.illinois.cs.cogcomp.nlp.util.TempDocEval.Dir2ReaderFormat;

public class CompareStateoftheArt_AllEventTimex {
    static ResourceManager rm;
    public static class DocAndStruct extends CompareStateoftheArt_SRLeventonly.DocAndStruct{
        public DocAndStruct(TemporalDocument doc, TemporalStructure struct) {
            super(doc,struct);
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
        for(DocAndStruct gold_docandstruct:gold){
            TemporalStructure st = gold_docandstruct.struct;
            st.extractAllTimexes();
            st.extractAllEventTimexPairs(false);
            st.extractPrepPhrase();
            st.extractRelGraphFull();
        }
    }
    public static void loadCaevo(){
        try {
            if(gold.size()==0)
                loadGold();

            TempEval3Reader myReader;
            String caevo_output_dir = rm.getString("CAEVOOutput");
            myReader = new TempEval3Reader("TIMEML", Dir2ReaderFormat(caevo_output_dir).getSecond(),
                    Dir2ReaderFormat(caevo_output_dir).getFirst());
            myReader.ReadData();
            List<TemporalDocument> caevo_docs = myReader.getDataset().getDocuments();
            for(TemporalDocument caevo_doc:caevo_docs){
                boolean flag = false;
                for(DocAndStruct docAndStruct:gold){
                    if(docAndStruct.struct.structID.equals(caevo_doc.getDocID())){
                        caevo_doc.setTextAnnotation(docAndStruct.struct.ta);
                        flag = true;
                        break;
                    }
                }
                if(!flag)
                    System.out.println("Warning");
                TemporalStructure struct = new TemporalStructure(caevo_doc,false,false);
                caevo.add(new DocAndStruct(caevo_doc,struct));
            }
            caevo.sort(new Comparator<DocAndStruct>() {
                @Override
                public int compare(DocAndStruct o1, DocAndStruct o2) {
                    return o1.struct.structID.compareTo(o2.struct.structID);
                }
            });
            for(DocAndStruct caevo_docandstruct:caevo){
                TemporalStructure st = caevo_docandstruct.struct;
                st.extractAllTimexes();
                st.extractAllEventTimexPairs(false);
                st.extractPrepPhrase();
                st.extractRelGraphFull();//this changes the TT links in doc
            }
        }
        catch (Exception e){e.printStackTrace();}
    }
    public static void loadEmnlp(){
        try {
            if(gold.size()==0)
                loadGold();
            if(caevo.size()==0)
                loadCaevo();
            TempEval3Reader myReader;
            String emnlp_output_dir = rm.getString("EMNLPOutput");
            myReader = new TempEval3Reader("TIMEML", Dir2ReaderFormat(emnlp_output_dir).getSecond(),
                    Dir2ReaderFormat(emnlp_output_dir).getFirst());
            myReader.ReadData();
            List<TemporalDocument> emnlp_docs = myReader.getDataset().getDocuments();
            for(TemporalDocument emnlp_doc:emnlp_docs){
                boolean flag = false;
                for(DocAndStruct docAndStruct:gold){
                    if(docAndStruct.struct.structID.equals(emnlp_doc.getDocID())){
                        emnlp_doc.setTextAnnotation(docAndStruct.struct.ta);
                        flag = true;
                        break;
                    }
                }
                if(!flag)
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
            for(DocAndStruct emnlp_docandstruct:emnlp){
                TemporalStructure st = emnlp_docandstruct.struct;
                st.extractAllTimexes();
                st.extractAllEventTimexPairs(false);
                st.extractPrepPhrase();
                st.extractRelGraphFull();//this changes the TT links in doc
            }
        }
        catch (Exception e){e.printStackTrace();}
    }
    public static void loadEmnlpAugmentedByNaacl(){
        CompareStateoftheArt_SRLeventonly.rm = rm;
        CompareStateoftheArt_SRLeventonly.loadNaacl();
        List<CompareStateoftheArt_SRLeventonly.DocAndStruct> naacl_srlonly = CompareStateoftheArt_SRLeventonly.naacl;
        try{
            if(emnlp.size()==0)
                loadEmnlp();
            naacl = emnlp;
            for(DocAndStruct docAndStruct: naacl){
                TemporalStructure augmented_st = docAndStruct.struct;
                TemporalStructure naacl_srlonly_st = null;
                for(CompareStateoftheArt_SRLeventonly.DocAndStruct tmp:naacl_srlonly){
                    if(tmp.struct.structID.equals(augmented_st.structID)){
                        naacl_srlonly_st = tmp.struct;
                        break;
                    }
                }
                if(naacl_srlonly_st==null)
                    System.out.println("warning");
                else{
                    for(TemporalEventPair ep:naacl_srlonly_st.eventPairs){
                        augmented_st.setRelGraphFullFromObjStr("e"+ep.event1.getEiid(),"e"+ep.event2.getEiid(),ep.relation);
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void eval(List<DocAndStruct> testset, String label){
        String awareness_data_dir = rm.getString("AwarenessOutput")+File.separator+"CompareStateoftheArt_AllEventTimex"+ File.separator+label;
        IOUtils.mkdir(awareness_data_dir);
        PrecisionRecallManager eval = new PrecisionRecallManager();
        int n = gold.size();
        for(int k=0;k<n;k++){
            TemporalStructure st = testset.get(k).struct;
            st.annTemporalDocumentUsingAllEventTimex(testset.get(k).doc,st.relGraphFull);//this changes the doc tlinks
            int eventFullSize = st.allEvents.size();
            for (int i = 0; i < eventFullSize; i++) {
                for (int j = i + 1; j < eventFullSize; j++) {
                    if (Math.abs(st.allEvents.get(j).getSentId() - st.allEvents.get(i).getSentId()) > 1)
                        continue;
                    String predlabel = st.relGraphFull[i][j].toStringfull();
                    String goldlabel = gold.get(k).struct.relGraphFull[i][j].toStringfull();
                    eval.addPredGoldLabels(predlabel,goldlabel);
                }
            }
            int timexSize = st.allTimexes.size();
            for (int i = 0; i < eventFullSize; i++) {
                for (int j = 0; j < timexSize; j++) {
                    if (!st.allTimexes.get(j).isDCT()&&
                            Math.abs(st.allEvents.get(i).getSentId() - st.allTimexes.get(j).getSentId()) > 1)
                        continue;
                    String predlabel = st.relGraphFull[i][eventFullSize+j].toStringfull();
                    String goldlabel = gold.get(k).struct.relGraphFull[i][eventFullSize+j].toStringfull();
                    eval.addPredGoldLabels(predlabel,goldlabel);
                }
            }
        }
        eval.printPrecisionRecall(new String[]{TLINK.TlinkType.UNDEF.toStringfull()});
        /*String gold_dir = rm.getString("TBDense_gold");
        System.out.println("\nTo evaluate. Run this\n");
        try {
            TempDocEval.RunOfficialTemporalEval(gold_dir, awareness_data_dir, awareness_log_dir.replaceAll("logs/", ""), label);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

    }
    public static void eval_emnlp(){
        loadGold();
        loadEmnlp();
        String label = "emnlp";
        eval(emnlp,label);
        String gold_dir = rm.getString("TBDense_gold");
        String awareness_log_dir = rm.getString("AwarenessLog")+File.separator+"CompareStateoftheArt_AllEventTimex";
        System.out.println("\nTo evaluate. Run this\n");
        try {
            TempDocEval.RunOfficialTemporalEval(gold_dir, rm.getString("EMNLPOutput"), awareness_log_dir.replaceAll("logs/", ""), label);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void eval_caevo(){
        loadGold();
        loadCaevo();
        String label = "caevo";
        eval(caevo,label);
        String gold_dir = rm.getString("TBDense_gold");
        String awareness_log_dir = rm.getString("AwarenessLog")+File.separator+"CompareStateoftheArt_AllEventTimex";
        System.out.println("\nTo evaluate. Run this\n");
        try {
            TempDocEval.RunOfficialTemporalEval(gold_dir, rm.getString("CAEVOOutput"), awareness_log_dir.replaceAll("logs/", ""), label);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void eval_naacl(){
        loadGold();
        loadEmnlpAugmentedByNaacl();
        String label = "naacl";
        eval(naacl,label);
        String gold_dir = rm.getString("TBDense_gold");
        String awareness_log_dir = rm.getString("AwarenessLog")+File.separator+"CompareStateoftheArt_AllEventTimex";
        System.out.println("\nTo evaluate. Run this\n");
        try {
            TempDocEval.RunOfficialTemporalEval(gold_dir, rm.getString("EMNLPAugOutput"), awareness_log_dir.replaceAll("logs/", ""), label);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
