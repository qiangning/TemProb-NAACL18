package util;

import com.opencsv.CSVReader;
import datastruct.ClinkType;
import datastruct.Event;
import datastruct.TemporalStructure;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.CompareCAVEO.TBDense_split;
import edu.illinois.cs.cogcomp.nlp.JointTempCausal.CEA_output;
import edu.illinois.cs.cogcomp.nlp.JointTempCausal.myPair;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.BethardVerbClauseReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ChambersDenseReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.TLINK;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.TempEval3Reader;
import edu.illinois.cs.cogcomp.nlp.util.TmlToCSV;
import edu.uw.cs.lil.uwtime.chunking.chunks.EventChunk;
import edu.uw.cs.lil.uwtime.data.TemporalDocument;

import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import static util.TempDocLoader.QuangAugment.QuangMissingClinkAnn.addMissingClink2Docs;

public class TempDocLoader {
    public static TimeBank TimeBankInstance = null;
    public static AQUAINT AQUAINTInstance = null;
    public static TBDense TBDenseInstance = null;
    public static boolean reload = false;
    public static ResourceManager rm;

    public static void loadResourceManagerIfNull() {
        try {
            if(rm==null)
                rm = new ResourceManager("config/reproducibility.properties");
        }
        catch (Exception e){e.printStackTrace();}
    }

    public static abstract class myDataset{
        public List<TemporalDocument> allDocs = new ArrayList<>();
        public List<TemporalStructure> allStructs = new ArrayList<>();
        public TemporalDocument st2doc(TemporalStructure st){
            for(TemporalDocument doc:allDocs){
                if(doc.getDocID().equals(st.structID))
                    return doc;
            }
            return null;
        }
        public TemporalStructure doc2st(TemporalDocument doc){
            for(TemporalStructure st:allStructs){
                if(st.structID.equals(doc.getDocID()))
                    return st;
            }
            return null;
        }
        public static class IDX2KEEP{
            public static HashMap<String,List<Integer>> idx2keep = null;
            public static int total = 0;
            public static void setIdx2keep(HashMap<String,List<Integer>> idx2keep){
                IDX2KEEP.idx2keep = idx2keep;
                for(String docid:idx2keep.keySet()){
                    total+=idx2keep.get(docid).size();
                }
            }
        }
        public static class NEWRELANN{
            public static HashMap<String,HashMap<String,TLINK.TlinkType>> newRelAnn = null;
            public static HashMap<String,HashMap<String,Integer>> stats = null;
            public static String totalStr = "total";
            public static String totalStr_ee = "total_ee";
            public static String totalStr_et = "total_et";
            public static void printTotal(){
                initStats();
                System.out.printf("Total ee %d: before=%d,after=%d,equal=%d,vague=%d\n",
                        stats.get(totalStr_ee).getOrDefault(totalStr_ee,0),
                        stats.get(totalStr_ee).getOrDefault(TLINK.TlinkType.BEFORE.toStringfull(),0),
                        stats.get(totalStr_ee).getOrDefault(TLINK.TlinkType.AFTER.toStringfull(),0),
                        stats.get(totalStr_ee).getOrDefault(TLINK.TlinkType.EQUAL.toStringfull(),0),
                        stats.get(totalStr_ee).getOrDefault(TLINK.TlinkType.UNDEF.toStringfull(),0));
                System.out.printf("Total et %d: before=%d,after=%d,equal=%d,vague=%d\n",
                        stats.get(totalStr_et).getOrDefault(totalStr_et,0),
                        stats.get(totalStr_et).getOrDefault(TLINK.TlinkType.BEFORE.toStringfull(),0),
                        stats.get(totalStr_et).getOrDefault(TLINK.TlinkType.AFTER.toStringfull(),0),
                        stats.get(totalStr_et).getOrDefault(TLINK.TlinkType.EQUAL.toStringfull(),0),
                        stats.get(totalStr_et).getOrDefault(TLINK.TlinkType.UNDEF.toStringfull(),0));
                System.out.printf("Total %d: before=%d,after=%d,equal=%d,vague=%d\n",
                        stats.get(totalStr).getOrDefault(totalStr,0),
                        stats.get(totalStr).getOrDefault(TLINK.TlinkType.BEFORE.toStringfull(),0),
                        stats.get(totalStr).getOrDefault(TLINK.TlinkType.AFTER.toStringfull(),0),
                        stats.get(totalStr).getOrDefault(TLINK.TlinkType.EQUAL.toStringfull(),0),
                        stats.get(totalStr).getOrDefault(TLINK.TlinkType.UNDEF.toStringfull(),0));
            }
            public static boolean isEmpty(){
                if(stats==null)
                    return true;
                int total = stats.getOrDefault(totalStr,new HashMap<>()).getOrDefault(totalStr,0);
                return total==0;
            }
            private static void initStats(){
                if(NEWRELANN.isEmpty()){
                    stats = new HashMap<>();
                    stats.put(totalStr,new HashMap<>());
                    stats.get(totalStr).put(totalStr,0);
                    stats.put(totalStr_ee,new HashMap<>());
                    stats.get(totalStr_ee).put(totalStr_ee,0);
                    stats.put(totalStr_et,new HashMap<>());
                    stats.get(totalStr_et).put(totalStr_et,0);
                }
            }
            private static void initRelAnn(){
                if(newRelAnn==null)
                    newRelAnn = new HashMap<>();
            }
            public static void setNewRelAnn(HashMap<String,HashMap<String,TLINK.TlinkType>> newRelAnn){
                initStats();
                initRelAnn();
                if(newRelAnn==null)
                    return;
                NEWRELANN.newRelAnn = newRelAnn;
                for(String docid:newRelAnn.keySet()){
                    for(String key:newRelAnn.get(docid).keySet()){
                        add2Stats(docid,key,newRelAnn.get(docid).get(key));
                    }
                }
            }
            private static void add2Stats(String docid,String key,TLINK.TlinkType rel){
                initStats();
                if(!stats.containsKey(docid)) {
                    stats.put(docid, new HashMap<>());
                }
                if(!stats.get(docid).containsKey(rel.toStringfull()))
                    stats.get(docid).put(rel.toStringfull(),0);
                stats.get(docid).put(rel.toStringfull(),stats.get(docid).get(rel.toStringfull())+1);
                stats.get(totalStr).put(rel.toStringfull(),stats.get(totalStr).getOrDefault(rel.toStringfull(),0)+1);
                stats.get(totalStr).put(totalStr,stats.get(totalStr).getOrDefault(totalStr,0)+1);

                String[] parts = key.split(" ");
                if(parts[0].startsWith("e")&&parts[1].startsWith("e")){
                    stats.get(totalStr_ee).put(rel.toStringfull(),stats.get(totalStr_ee).getOrDefault(rel.toStringfull(),0)+1);
                    stats.get(totalStr_ee).put(totalStr_ee,stats.get(totalStr_ee).getOrDefault(totalStr_ee,0)+1);
                }
                else if(parts[0].startsWith("t")&&parts[1].startsWith("t")){

                }
                else{
                    stats.get(totalStr_et).put(rel.toStringfull(),stats.get(totalStr_et).getOrDefault(rel.toStringfull(),0)+1);
                    stats.get(totalStr_et).put(totalStr_et,stats.get(totalStr_et).getOrDefault(totalStr_et,0)+1);
                }
            }
            private static void add2RelAnnMap(String docid, String key, TLINK.TlinkType tt){
                initRelAnn();
                if(!newRelAnn.containsKey(docid))
                    newRelAnn.put(docid,new HashMap<>());
                if(!newRelAnn.get(docid).containsKey(key))
                    newRelAnn.get(docid).put(key,tt);
            }
            private static void update(String docid, String key, TLINK.TlinkType tt){
                add2RelAnnMap(docid,key,tt);
                add2Stats(docid,key,tt);
            }
            public static void loadNewRelAnn(String dir){
                loadNewRelAnn(dir,1);
            }
            public static void loadNewRelAnn(String dir, int version){
                List<String[]> newRelAnn_raw = new ArrayList<>();
                try {
                    CSVReader csvReader = new CSVReader(new FileReader(dir));
                    newRelAnn_raw = csvReader.readAll();
                    newRelAnn_raw.remove(0);
                }
                catch(Exception e){
                    System.out.println("Data loading error.");
                    e.printStackTrace();
                }
                for (String[] strs : newRelAnn_raw) {
                    try {
                        if (version == 1) {
                            String docid = strs[11];
                            boolean q1_yesorno = strs[7].toLowerCase().contains("yes");
                            boolean q2_yesorno = strs[5].toLowerCase().contains("yes");
                            double q1_conf = Double.valueOf(strs[8]);
                            double q2_conf = Double.valueOf(strs[6]);
                            int id1 = Integer.valueOf(strs[16]);
                            int id2 = Integer.valueOf(strs[17]);
                            String type1 = strs[18], type2 = strs[19];
                            String e1 = (type1.equals("event") ? "e" : "t") + String.valueOf(id1);
                            String e2 = (type2.equals("event") ? "e" : "t") + String.valueOf(id2);
                            if (q1_yesorno) {
                                if (q2_yesorno) {
                                    update(docid, e1 + " " + e2, TLINK.TlinkType.UNDEF);
                                } else {
                                    update(docid, e1 + " " + e2, TLINK.TlinkType.BEFORE);
                                }
                            } else {
                                if (q2_yesorno) {
                                    update(docid, e1 + " " + e2, TLINK.TlinkType.AFTER);
                                } else {
                                    update(docid, e1 + " " + e2, TLINK.TlinkType.EQUAL);
                                }
                            }
                        }
                        else if (version == 2) {
                            if (strs[1].equals("False") && strs[2].equals("golden"))
                                continue;
                            if(strs[5].isEmpty()||strs[27].isEmpty())
                                continue;
                            String docid = strs[9];
                            boolean q1_yesorno = strs[5].toLowerCase().contains("yes");
                            boolean q2_yesorno = strs[27].toLowerCase().contains("yes");
                            double q1_conf = strs[6].isEmpty()?0d:Double.valueOf(strs[6]);
                            double q2_conf = strs[28].isEmpty()?0d:Double.valueOf(strs[28]);
                            int id1 = Integer.valueOf(strs[14]);
                            int id2 = Integer.valueOf(strs[15]);
                            String type1 = strs[16], type2 = strs[17];
                            String e1 = (type1.equals("event") ? "e" : "t") + String.valueOf(id1);
                            String e2 = (type2.equals("event") ? "e" : "t") + String.valueOf(id2);
                            if (q1_yesorno) {
                                if (q2_yesorno) {
                                    update(docid, e1 + " " + e2, TLINK.TlinkType.UNDEF);
                                } else {
                                    update(docid, e1 + " " + e2, TLINK.TlinkType.BEFORE);
                                }
                            } else {
                                if (q2_yesorno) {
                                    update(docid, e1 + " " + e2, TLINK.TlinkType.AFTER);
                                } else {
                                    update(docid, e1 + " " + e2, TLINK.TlinkType.EQUAL);
                                }
                            }
                        }
                    }
                    catch (Error e){
                        System.out.println("Row parsing error.");
                        System.out.println(strs[0]);
                    }
                }
            }
        }

        public void loadDocs(String dir){
            try {
                allDocs = TempEval3Reader.deserialize(dir);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        public void loadStructs(boolean useNewRelAnn,boolean SRLevents){
            if(useNewRelAnn){
                if(TmlToCSV.idx2keep.size()==0) {
                    try {
                        TmlToCSV.readEventFilter("/home/qning2/Servers/home/test/temporal-reasoning/data/CrowdFlower_EventFiltering/TBDense_all.csv", false);
                        TmlToCSV.readEventFilter("/home/qning2/Servers/home/test/temporal-reasoning/data/CrowdFlower_EventFiltering/quang_all.csv", false);
                    }
                    catch(Exception e){e.printStackTrace();}
                }
                if(IDX2KEEP.total==0) {
                    IDX2KEEP.setIdx2keep(TmlToCSV.idx2keep);
                }
                if(NEWRELANN.isEmpty()){
                    //NEWRELANN.loadNewRelAnn("/home/qning2/Servers/home/test/temporal-reasoning/data/CrowdFlower_RelAnn/tbdense_rel_all.csv");
                    NEWRELANN.loadNewRelAnn("/home/qning2/Servers/home/test/temporal-reasoning/data/CrowdFlower_RelAnn/tbdense_rel2.0_all.csv",2);
                    NEWRELANN.loadNewRelAnn("/home/qning2/Servers/home/test/temporal-reasoning/data/CrowdFlower_RelAnn/quang_rel_all.csv");
                }
                TemporalStructure.setIdx2keep(IDX2KEEP.idx2keep);
                TemporalStructure.setNewRelAnn(NEWRELANN.newRelAnn);
            }
            for(TemporalDocument doc:allDocs){
                if(SRLevents){
                    TemporalStructure newStruct = new TemporalStructure(doc,false,false);
                    List<EventChunk> newEventMentions = new ArrayList<>();
                    for(Event e:newStruct.allSRLEvents){
                        newEventMentions.add(doc.getEventMentionFromEIID(e.getEiid()));
                        //newEventMentions.add(e.event2chunk());
                    }
                    doc.setBodyEventMentions(newEventMentions);
                }
                allStructs.add(new TemporalStructure(doc,true,useNewRelAnn));
            }
        }
    }

    public static class TimeBank extends myDataset{
        public TimeBank(boolean useNewRelAnn){
            loadResourceManagerIfNull();
            loadDocs(rm.getString("TimeBankSer"));
            loadStructs(useNewRelAnn,false);
        }
    }

    public static class AQUAINT extends myDataset{
        public AQUAINT(boolean useNewRelAnn){
            loadResourceManagerIfNull();
            loadDocs(rm.getString("AQUAINTSer"));
            loadStructs(useNewRelAnn,false);
        }
    }
    public static class TBDense extends myDataset{
        public List<TemporalDocument> getAllDocs(int mode){//1: train. 2: dev. 3: test. 4: rest
            List<TemporalDocument> res = new ArrayList<>();
            for(TemporalDocument doc:allDocs){
                if(TBDense_split.findDoc(doc.getDocID())==mode)
                    res.add(doc);
            }
            return res;
        }
        public List<TemporalStructure> getAllStructs(int mode){//1: train. 2: dev. 3: test. 0: rest
            List<TemporalStructure> res = new ArrayList<>();
            for(TemporalStructure doc:allStructs){
                if(TBDense_split.findDoc(doc.structID)==mode)
                    res.add(doc);
            }
            return res;
        }
        public TBDense(boolean useNewRelAnn){
            loadResourceManagerIfNull();
            loadDocs(rm.getString("TBDenseSer"));
            try {
                readBethard(allDocs);
                readChambers(allDocs);
            }
            catch(Exception e){e.printStackTrace();}
            loadStructs(useNewRelAnn,false);
        }

    }

    public static TimeBank getTimeBankInstance(){return getTimeBankInstance(false);}
    public static TimeBank getTimeBankInstance(boolean useNewRelAnn) {
        if (TimeBankInstance == null || reload) {
            TimeBankInstance = new TimeBank(useNewRelAnn);
        }
        return TimeBankInstance;
    }
    public static AQUAINT getAQUAINTInstance() {return getAQUAINTInstance(false);}
    public static AQUAINT getAQUAINTInstance(boolean useNewRelAnn) {
        if (AQUAINTInstance == null || reload) {
            AQUAINTInstance = new AQUAINT(useNewRelAnn);
        }
        return AQUAINTInstance;
    }
    public static TBDense getTBDenseInstance(){return getTBDenseInstance(false);}
    public static TBDense getTBDenseInstance(boolean useNewRelAnn){
        if(TBDenseInstance == null || reload){
            TBDenseInstance = new TBDense(useNewRelAnn);
        }
        return TBDenseInstance;
    }

    public static void readBethard(List<TemporalDocument> allDocs) throws Exception{
        BethardVerbClauseReader reader = new BethardVerbClauseReader();
        reader.readData();
        HashMap<String, List<TLINK>> extraRels = reader.getExtraRels();
        for(TemporalDocument doc:allDocs){
            int lid = doc.getMaxLID();
            lid++;
            for (Map.Entry<String, List<TLINK>> entry : extraRels.entrySet()) {
                String key = entry.getKey();
                List<TLINK> tlinks = entry.getValue();
                if (doc.getDocID().equals(key)) {
                    for (TLINK tlink : tlinks) {
                        if(!doc.checkTlinkExistence(tlink)) {
                            if(!doc.validateTlink(tlink))//check if this tlink is valid
                                continue;
                            tlink.setLid(lid);
                            doc.insertMention(tlink);
                            lid++;
                        }
                    }
                }
            }
        }
    }
    public static void readChambers(List<TemporalDocument> allDocs) throws Exception{
        ChambersDenseReader reader = new ChambersDenseReader();
        reader.readData();
        HashMap<String, List<TLINK>> extraRels = reader.getExtraRels();
        for(TemporalDocument doc:allDocs){
            int lid = doc.getMaxLID();
            lid++;
            for (Map.Entry<String, List<TLINK>> entry : extraRels.entrySet()) {
                String key = entry.getKey();
                List<TLINK> tlinks = entry.getValue();
                if (doc.getDocID().equals(key)) {
                    for (TLINK tlink : tlinks) {
                        if(tlink.getSourceType().equals(TempEval3Reader.Type_Event)){
                            EventChunk ec = doc.getEventMentionFromEID(tlink.getSourceId(),false);
                            if(ec==null)
                                continue;
                            tlink.setSourceId(ec.getEiid());
                        }
                        if(tlink.getTargetType().equals(TempEval3Reader.Type_Event)){
                            EventChunk ec = doc.getEventMentionFromEID(tlink.getTargetId(),false);
                            if(ec==null)
                                continue;
                            tlink.setTargetId(ec.getEiid());
                        }
                        if(!doc.checkTlinkExistence(tlink)) {
                            if(!doc.validateTlink(tlink))//check if this tlink is valid
                                continue;
                            tlink.setLid(lid);
                            doc.insertMention(tlink);
                            lid++;
                        }
                    }
                }
            }
        }
    }
    public static void main(String[] args) throws Exception{
        /*myDataset.NEWRELANN.loadNewRelAnn("/home/qning2/Servers/home/test/temporal-reasoning/data/CrowdFlower_RelAnn/quang_rel_all.csv");
        myDataset.NEWRELANN.printTotal();*/
        /*QuangAugment tmp = getQuangAugmentInstance(true);
        int total = 0;
        for(TemporalStructure st:tmp.allStructs){
            total+=st.countClink();
        }
        System.out.println(total);
        QuangAugment.QuangTokenOffset.printTotal();*/
        TBDense tmp = getTBDenseInstance();
        PrintStream ps = new PrintStream("data/TempEval3/tbdense_eid2eiid.txt");
        edu.illinois.cs.cogcomp.core.io.IOUtils.mkdir("data/TempEval3/TBDense_Rawtext");
        ps.println("docid\teid\teiid\tid");
        for(int i=1;i<=3;i++){
            List<TemporalDocument> docs = tmp.getAllDocs(i);
            for(TemporalDocument doc:docs){
                PrintStream ps2 = new PrintStream("data/TempEval3/TBDense_Rawtext/"+doc.getDocID());
                try{
                    ps2.println(doc.getOriginalText());
                }
                catch (Exception e){}
                ps2.close();
                for(EventChunk ec:doc.getBodyEventMentions()){
                    try {
                        ps.println(doc.getDocID() + "\t" + ec.getEid() + "\t" + ec.getEiid() + "\t" + doc.getBodyEventMentions().indexOf(ec));
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
        ps.close();
        System.out.println();
    }
}
