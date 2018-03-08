package util;

import edu.illinois.cs.cogcomp.nlp.corpusreaders.TLINK;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Scanner;

public class TempLangMdl {
    public static TempLangMdl instance = null;
    public HashMap<String,HashMap<String,HashMap<TLINK.TlinkType,Integer>>> tempLangMdl = null;
    public HashMap<String,String> cluster = null;
    public HashMap<String,double[]> embeddings = null;
    public TempLangMdl(String path){
        try {
            if(path.endsWith(".ser"))
                tempLangMdl = (HashMap<String, HashMap<String, HashMap<TLINK.TlinkType, Integer>>>) mySerialization.deserialize(path);
            else
                readLM(path);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public TempLangMdl(String cluster_path, String lm_path){
        try {
            readCluster(cluster_path);//set up "cluster"
            if(lm_path.endsWith(".ser"))
                tempLangMdl = (HashMap<String, HashMap<String, HashMap<TLINK.TlinkType, Integer>>>) mySerialization.deserialize(lm_path);
            else
                readLM(lm_path);
            clusteringLM();//update "tempLangMdl"
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public static TempLangMdl getInstance(String path) {
        if (instance == null) {
            instance = new TempLangMdl(path);
        }
        return instance;
    }
    public static TempLangMdl getInstance(String cluster_path, String lm_path) {
        if (instance == null) {
            instance = new TempLangMdl(cluster_path,lm_path);
        }
        return instance;
    }
    public void readCluster(String fullpath) throws Exception{
        int clusterID = 0;
        Scanner in = new Scanner(new FileReader(fullpath));
        cluster = new HashMap<>();
        while(in.hasNextLine()){
            String line = in.nextLine().trim();
            String[] words = line.split(" ");
            for(String w:words){
                cluster.put(w,String.valueOf(clusterID));
            }
            clusterID++;
        }
        in.close();
    }
    public void readEmbeddingsIfEmpty(String fullpath) throws Exception{
        if(embeddings==null) {
            Scanner in = new Scanner(new FileReader(fullpath));
            embeddings = new HashMap<>();
            while (in.hasNextLine()) {
                String line = in.nextLine().trim();
                String[] words = line.split(",");
                String key = words[0];
                double[] emb = new double[words.length - 1];
                for (int i = 1; i < words.length; i++) {
                    emb[i - 1] = Double.valueOf(words[i]);
                }
                embeddings.put(key, emb);
            }
            in.close();
        }
    }
    public void readLM(String fullpath) throws Exception{
        Scanner in = new Scanner(new FileReader(fullpath));
        int nline = 0;
        tempLangMdl = new HashMap<>();
        while(in.hasNextLine()){
            nline++;
            String line = in.nextLine().trim();
            String[] words = line.split("\t");
            TLINK.TlinkType tt = TLINK.TlinkType.str2TlinkType(words[2]);
            int cnt = Integer.valueOf(words[3]);
            String c1 = words[0];
            String c2 = words[1];
            if(!tempLangMdl.keySet().contains(c1))
                tempLangMdl.put(c1,new HashMap<>());
            if(!tempLangMdl.get(c1).keySet().contains(c2))
                tempLangMdl.get(c1).put(c2,new HashMap<>());
            if(!tempLangMdl.get(c1).get(c2).keySet().contains(tt)) {
                tempLangMdl.get(c1).get(c2).put(tt, cnt);
            }
            else {
                int prev = tempLangMdl.get(c1).get(c2).get(tt);
                tempLangMdl.get(c1).get(c2).put(tt, prev + 1);
            }
        }
        in.close();
    }
    public void clusteringLM(){
        if(cluster == null)
            return;
        HashMap<String,HashMap<String,HashMap<TLINK.TlinkType,Integer>>> tempLangMdl_new = new HashMap<>();
        for(String v1:tempLangMdl.keySet()){
            String c1 = cluster.getOrDefault(v1.replaceAll("\\.0[1-9]",""),"-1");
            if(!tempLangMdl_new.containsKey(c1))
                tempLangMdl_new.put(c1,new HashMap<>());
            for(String v2:tempLangMdl.get(v1).keySet()){
                String c2 = cluster.getOrDefault(v2.replaceAll("\\.0[1-9]",""),"-1");
                if(!tempLangMdl_new.get(c1).containsKey(c2))
                    tempLangMdl_new.get(c1).put(c2,new HashMap<>());
                for(TLINK.TlinkType tt : tempLangMdl.get(v1).get(v2).keySet()){
                    int curr = tempLangMdl_new.get(c1).get(c2).getOrDefault(tt,0);
                    tempLangMdl_new.get(c1).get(c2).put(tt,curr+tempLangMdl.get(v1).get(v2).get(tt));
                }
            }
        }
        tempLangMdl = tempLangMdl_new;
    }
}
