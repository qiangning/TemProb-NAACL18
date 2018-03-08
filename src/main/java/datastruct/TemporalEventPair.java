package datastruct;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.TLINK;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.TLINK.TlinkType;
import edu.illinois.cs.cogcomp.nlp.util.wordnet.WNSim;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TemporalEventPair {
    // event1 has to appear before event2
    public Event event1;
    public Event event2;
    public TlinkType relation;
    public ClinkType causal_relation;
    public TemporalStructure parentDoc;
    public List<String> connectives_between;
    public List<String> modelverbs_between;
    public List<String> connectives_before;
    public List<String> modelverbs_before;
    public List<String> connectives_after;
    public List<String> modelverbs_after;
    public double c1 = 1, c2 = 1;
    public double c_i = 1, c_ii = 1, c_e = 1, c_v = 1;
    private Double embeddingIP;
    public static ResourceManager rm;

    public TemporalEventPair(Event event1, Event event2, TlinkType relation, TemporalStructure parentDoc) {
        this.event1 = event1;
        this.event2 = event2;
        this.relation = relation;
        this.parentDoc = parentDoc;
        if(rm==null){
            try {
                rm = new ResourceManager("config/reproducibility.properties");
            }
            catch (Exception e){e.printStackTrace();}
        }
    }
    @Override
    public String toString(){
        return String.format("[e%d (%s), e%d (%s)]=%s, PP_HEAD=[%s,%s]", event1.getIndex_in_doc(),event1.getCluster(),event2.getIndex_in_doc(),event2.getCluster(),relation.toStringfull(),
                event1.getPp_head(),event2.getPp_head());
    }
    public boolean isRelNull(){
        return relation==null || relation == TLINK.TlinkType.UNDEF;
    }

    public boolean isRelInclude(){
        return relation!=null && (relation==TlinkType.INCLUDES||relation==TlinkType.IS_INCLUDED);
    }

    public boolean sameSynset(){
        List<String> e1Synsets = event1.getSynsets();
        List<String> e2Synsets = event2.getSynsets();
        Set<String> e1SetSynsets = new HashSet<String>(e1Synsets);
        Set<String> e2SetSynsets = new HashSet<String>(e2Synsets);
        e1SetSynsets.retainAll(e2SetSynsets);
        return e1SetSynsets.size() > 0;
    }
    public boolean sameDerivation(){
        WNSim wnsim = WNSim.getInstance(rm.getString("WordNet"));
        List<String> e1Derivations = wnsim.getDerivations(event1.getLemma(),
                event1.getPos());
        e1Derivations.add(event1.getLemma());
        Set<String> e1SetDerivations = new HashSet<String>(e1Derivations);
        List<String> e2Derivations = wnsim.getDerivations(event2.getLemma(),
                event2.getPos());
        e2Derivations.add(event2.getLemma());
        Set<String> e2SetDerivations = new HashSet<String>(e2Derivations);
        e1SetDerivations.retainAll(e2SetDerivations);
        return e1SetDerivations.size() > 0;
    }

    public void isBeforeCorpus(HashMap<String,HashMap<String,HashMap<TlinkType,Integer>>> temporalLM){
        if(temporalLM.containsKey(event1.getCluster())&&temporalLM.get(event1.getCluster()).containsKey(event2.getCluster())){
            c1 = temporalLM.get(event1.getCluster()).get(event2.getCluster()).getOrDefault(TlinkType.BEFORE,0)+1;
            c2 = temporalLM.get(event1.getCluster()).get(event2.getCluster()).getOrDefault(TlinkType.AFTER,0)+1;
        }
    }
    public void readCorpusStats(HashMap<String,HashMap<String,HashMap<TlinkType,Integer>>> temporalLM){
        if(temporalLM.containsKey(event1.getCluster())&&temporalLM.get(event1.getCluster()).containsKey(event2.getCluster())){
            c_i = temporalLM.get(event1.getCluster()).get(event2.getCluster()).getOrDefault(TlinkType.INCLUDES,0)+1;
            c_ii = temporalLM.get(event1.getCluster()).get(event2.getCluster()).getOrDefault(TlinkType.IS_INCLUDED,0)+1;
            c_e = temporalLM.get(event1.getCluster()).get(event2.getCluster()).getOrDefault(TlinkType.EQUAL,0)+1;
            c_v = temporalLM.get(event1.getCluster()).get(event2.getCluster()).getOrDefault(TlinkType.UNDEF,0)+1;
        }
    }
    public void addEmbedding(HashMap<String,double[]> embeddingMap){
        event1.setEmbedding(embeddingMap);
        event2.setEmbedding(embeddingMap);
    }
    public double getEmbeddingIP(){
        if(embeddingIP==null) {
            double[] emb1 = event1.embedding;
            double[] emb2 = event2.embedding;
            int n = emb1.length;
            if (emb2.length != n) {
                System.out.printf("Embedding length mismatch.");
                return 0;
            }
            double res = 0;
            for (int i = 0; i < n; i++) {
                res += emb1[i] * emb2[i];
            }
            embeddingIP = res;
        }
        return embeddingIP;
    }
}
