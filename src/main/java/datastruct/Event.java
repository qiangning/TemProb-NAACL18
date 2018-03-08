package datastruct;

import edu.illinois.cs.cogcomp.core.datastructures.IQueryable;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.transformers.Predicate;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.util.wordnet.WNSim;
import edu.illinois.cs.cogcomp.nlp.utilities.ParseTreeProperties;
import edu.uw.cs.lil.uwtime.chunking.chunks.EventChunk;
import java.util.*;

public class Event {
    private String pos;
    private String text;
    private String lemma;
    private String sense;
    private String cluster;// to-do: frame cluster, or custom cluster
    private int eid;
    private int eiid;
    private int index_in_doc;
    private int tokenId;
    private int sentId;
    private List<String> synsets;
    private List<String> derivations;// to-do
    private String pp_head;//prepositional phrase head
    private TextAnnotation ta;
    public double[] embedding;

    private String[] prev_pos;
    private String[] next_pos;
    private Event prevEvent_SameSynset,nextEvent_SameSynset;

    // used when gold properties are available
    public String tense, aspect, eventclass, polarity;
    public static ResourceManager rm;

    @Override
    public String toString() {
        return "Event{" +
                "text='" + text + '\'' +
                ", lemma='" + lemma + '\'' +
                ", sense='" + sense + '\'' +
                ", cluster='" + cluster + '\'' +
                ", eid=" + eid +
                ", eiid=" + eiid +
                ", index_in_doc=" + index_in_doc +
                ", tokenId=" + tokenId +
                ", sentId=" + sentId +
                '}';
    }
    private static void loadResourceManagerIfNull(){
        if(rm==null){
            try {
                rm = new ResourceManager("config/reproducibility.properties");
            }
            catch (Exception e){e.printStackTrace();}
        }
    }
    public Event(TextAnnotation ta, int tokenId, int sentId, int eid){
        loadResourceManagerIfNull();
        //text = ta.getSentence(sentId).getToken(tokenId);
        text = ta.getToken(tokenId);
        this.eid = eid;
        eiid = eid;
        index_in_doc = eid;
        this.tokenId = tokenId;
        this.sentId = sentId;
        this.ta = ta;
        extractPosLemmaFeat();
        extractSynset();
    }
    public Event(EventChunk ec, TextAnnotation ta, int index_in_doc) {
        loadResourceManagerIfNull();
        eid = ec.getEid();
        eiid = ec.getEiid();
        text = ec.getText();
        this.index_in_doc = index_in_doc;
        tokenId = ta.getTokenIdFromCharacterOffset(ec.getCharStart());
        sentId = ta.getSentenceId(tokenId);
        this.ta = ta;
        extractPosLemmaFeat();
        extractSynset();
    }

    public EventChunk event2chunk(){
        IntPair charOffset = ta.getTokenCharacterOffset(tokenId);
        int charStart=charOffset.getFirst(), charEnd=charOffset.getSecond();
        EventChunk ec = new EventChunk("","","",charStart,charEnd,eid);
        ec.setPolarity("");
        ec.setPos(pos);
        ec.setText(text);
        ec.setEiid(eiid);
        ec.setCardinality("");
        return ec;
    }

    private void extractSynset(){
        loadResourceManagerIfNull();
        WNSim wnsim = WNSim.getInstance(rm.getString("WordNet"));
        synsets = wnsim.getSynset(lemma,pos);
    }

    public static void extractClosestSynset(List<Event> elist){
        List<Event> elist2 = new ArrayList<>();
        elist2.addAll(elist);
        elist2.sort(new Comparator<Event>(){// will this change the original list who called this function?
                public int compare(Event e1, Event e2) {
                    return e1.getTokenId()-e2.getTokenId();
                }
        });
        for(int i=0;i<elist2.size();i++){
            Event e = elist2.get(i);
            // left
            for(int j=i-1;j>0;j--){
                Event e_prev = elist2.get(j);
                List<String> prev_synsets = new ArrayList<>();
                prev_synsets.addAll(e_prev.synsets);
                prev_synsets.retainAll(e.synsets);// will this change e_prev.synsets?
                if(prev_synsets.size()>0){
                    e.prevEvent_SameSynset = e_prev;
                    break;
                }
            }
            // right
            for(int j=i+1;j<elist2.size();j++){
                Event e_next = elist2.get(j);
                List<String> next_synsets = new ArrayList<>();
                next_synsets.addAll(e_next.synsets);
                next_synsets.retainAll(e.synsets);
                if(next_synsets.size()>0){
                    e.nextEvent_SameSynset = e_next;
                    break;
                }
            }
        }
    }

    public void extractPPHead(){
        if(!ta.hasView(ViewNames.PARSE_CHARNIAK)){
            pp_head = "N/A";
            return;
        }
        TreeView charniakView = (TreeView) ta.getView(ViewNames.PARSE_CHARNIAK);
        Predicate<Constituent> ppQuery = new Predicate<Constituent>() {
            private static final long serialVersionUID = -8421140892037175370L;

            @Override
            public Boolean transform(Constituent arg0) {
                return ParseTreeProperties.isNonTerminalPP(arg0.getLabel());
            }
        };
        Constituent constituent = new Constituent("", "", ta,
                tokenId, tokenId+1);
        Predicate<Constituent> query = ppQuery.and(Queries
                .containsConstituent(constituent));
        IQueryable<Constituent> output = charniakView.where(query).orderBy(
                TextAnnotationUtilities.constituentLengthComparator);
        Iterator<Constituent> it = output.iterator();
        if (it.hasNext()) {
            Constituent pp = it.next();
            int spp = pp.getStartSpan();
            pp_head = ta.getToken(spp);
        } else {
            pp_head = "N/A";
        }
    }
    private String[] extractPosLemmaFeat(int tokid){
        TokenLabelView posView = (TokenLabelView) ta.getView(ViewNames.POS);
        TokenLabelView lemmaView = (TokenLabelView) ta.getView(ViewNames.LEMMA);
        String posFeat = "N/A", lemmaFeat = "N/A";
        List<Constituent> cons = posView.getConstituentsCoveringToken(tokid);
        if(cons==null||cons.size()==0) {
            return new String[]{posFeat,lemmaFeat};
        }
        int eventHeadTokenId = cons.get(0).getStartSpan();
        if (cons.size() == 1) {
            String label = cons.get(0).getLabel();
            posFeat = (label.startsWith("N") ? "N" : label);
            try {
                lemmaFeat = lemmaView.getConstituentAtToken(
                        cons.get(0).getStartSpan()).getLabel();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        } else if (cons.size() > 1) {
            cons.sort(TextAnnotationUtilities.constituentStartComparator);
            Constituent conAtStart = cons.get(0);
            Constituent conAtEnd = cons.get(cons.size() - 1);
            String labelStart = cons.get(0).getLabel();
            String labelEnd = cons.get(cons.size() - 1).getLabel();
            if (labelStart.startsWith("V") || labelEnd.startsWith("V")) {
                eventHeadTokenId = (labelStart.startsWith("V") ? conAtStart
                        .getStartSpan() : conAtEnd.getStartSpan());
                posFeat = posView.getConstituentAtToken(eventHeadTokenId)
                        .getLabel();
                lemmaFeat = lemmaView.getConstituentAtToken(eventHeadTokenId)
                        .getLabel();
            } else if (labelStart.startsWith("N") || labelEnd.startsWith("N")) {
                posFeat = "N";
                lemmaFeat = lemmaView.getConstituentAtToken(eventHeadTokenId)
                        .getLabel();
            } else {
                posFeat = posView.getConstituentAtToken(eventHeadTokenId)
                        .getLabel();
                lemmaFeat = lemmaView.getConstituentAtToken(eventHeadTokenId)
                        .getLabel();
            }
        } else {
            posFeat = "N/A";
            lemmaFeat = "N/A";
        }
        return new String[]{posFeat,lemmaFeat};
    }
    private void extractPosLemmaFeat(){
        String[] pos_lemma0 = extractPosLemmaFeat(tokenId);
        pos = pos_lemma0[0];
        lemma = pos_lemma0[1];
        int win = 3;
        prev_pos = new String[win];
        next_pos = new String[win];
        for(int i=1;i<=win;i++){
            String[] pos_lemma_prev_i, pos_lemma_next_i;
            if(tokenId-i>=0) {
                pos_lemma_prev_i = extractPosLemmaFeat(tokenId - i);
            }
            else{
                pos_lemma_prev_i = new String[]{"N/A","N/A"};
            }
            if(tokenId+i<ta.getTokens().length){
                pos_lemma_next_i = extractPosLemmaFeat(tokenId+i);
            }
            else{
                pos_lemma_next_i = new String[]{"N/A","N/A"};
            }
            prev_pos[i-1] = pos_lemma_prev_i[0];
            next_pos[i-1] = pos_lemma_next_i[0];
        }

    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getPos() {

        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public int getEid() {
        return eid;
    }

    public void setEid(int eid) {
        this.eid = eid;
    }

    public int getEiid() {
        return eiid;
    }

    public void setEiid(int eiid) {
        this.eiid = eiid;
    }

    public int getIndex_in_doc() {
        return index_in_doc;
    }

    public void setIndex_in_doc(int index_in_doc) {
        this.index_in_doc = index_in_doc;
    }

    public int getTokenId() {
        return tokenId;
    }

    public void setTokenId(int tokenId) {
        this.tokenId = tokenId;
    }

    public int getSentId() {
        return sentId;
    }

    public void setSentId(int sentId) {
        this.sentId = sentId;
    }

    public List<String> getSynsets() {
        return synsets;
    }

    public void setSynsets(List<String> synsets) {
        this.synsets = synsets;
    }

    public List<String> getDerivations() {
        return derivations;
    }

    public void setDerivations(List<String> derivations) {
        this.derivations = derivations;
    }

    public String getPp_head() {
        return pp_head;
    }

    public void setPp_head(String pp_head) {
        this.pp_head = pp_head;
    }

    public String getSense() {
        return sense;
    }

    public void setSense(String sense) {
        this.sense = sense;
    }

    public String getPrevPos(int i){
        if(i<prev_pos.length)
            return prev_pos[i];
        else
            return "N/A";
    }

    public String getNextPos(int i){
        if(i<next_pos.length)
            return next_pos[i];
        else
            return "N/A";
    }

    public int getEmbeddingLen(){
        if(embedding==null)
            return 0;
        return embedding.length;
    }

    public void setEmbedding(HashMap<String,double[]> embeddingMap){
        if(embeddingMap==null||embeddingMap.size()==0){
            System.out.println("Embeddingmap empty.");
            return;
        }
        if(embeddingMap.containsKey(cluster)){
            embedding = embeddingMap.get(cluster);
        }
        else{
            Map.Entry<String, double[]> entry = embeddingMap.entrySet().iterator().next();
            int n = entry.getValue().length;
            embedding = new double[n];
        }
    }

    public void setTACP(String tense, String aspect, String eventclass, String polarity){
        this.tense = tense;
        this.aspect = aspect;
        this.eventclass = eventclass;
        this.polarity = polarity;
    }
    public String[] getTACP(){
        String[] tacp = new String[4];
        tacp[0] = tense;
        tacp[1] = aspect;
        tacp[2] = eventclass;
        tacp[3] = polarity;
        return tacp;
    }
    public Event getPrevEvent_SameSynset() {
        return prevEvent_SameSynset;
    }

    public Event getNextEvent_SameSynset() {
        return nextEvent_SameSynset;
    }
}
