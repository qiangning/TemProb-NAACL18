package datastruct;

import edu.illinois.cs.cogcomp.core.datastructures.IQueryable;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.transformers.Predicate;
import edu.illinois.cs.cogcomp.nlp.utilities.ParseTreeProperties;
import edu.uw.cs.lil.uwtime.chunking.chunks.TemporalJointChunk;

import java.util.Iterator;

public class Timex {
    private int tid;
    private String text;
    private int sentId;
    private Pair<Integer,Integer> tokenSpan;
    private String type;
    private String mod;
    private boolean isDCT;
    private String pp_head;
    private TextAnnotation ta;


    public Timex(int tid, String text, int sentId, Pair<Integer, Integer> tokenSpan, String type, String mod, boolean isDCT, TextAnnotation ta) {
        this.tid = tid;
        this.text = text;
        this.sentId = sentId;
        this.tokenSpan = tokenSpan;
        this.type = type;
        this.mod = mod;
        this.isDCT = isDCT;
        this.ta = ta;
    }

    public static Timex tjc2timex(TemporalJointChunk tjc, TextAnnotation ta, boolean isDCT){
        int tid = tjc.getTID();
        String text = tjc.getPhrase().toString();
        Pair<Integer, Integer> tokenSpan;
        int sentId;
        if(!isDCT) {
            tokenSpan = new Pair<>(ta.getTokenIdFromCharacterOffset(tjc.getCharStart()),
                    ta.getTokenIdFromCharacterOffset(tjc.getCharEnd() - 1) + 1);
            sentId = ta.getSentenceId(tokenSpan.getFirst());
        }
        else{
            tokenSpan = new Pair<>(-1,-1);
            sentId = -1;
        }
        String type = tjc.getResult().getType();
        String mod = tjc.getResult().getMod();
        return new Timex(tid,text,sentId,tokenSpan,type,mod,isDCT,ta);

    }

    public String getText() {
        return text;
    }

    public int getSentId() {
        return sentId;
    }

    public Pair<Integer, Integer> getTokenSpan() {
        return tokenSpan;
    }

    public int getTokenHeadId(){
        return tokenSpan.getFirst();
    }

    public String getType() {
        return type==null?"":type;
    }

    public String getMod() {
        return mod==null?"":mod;
    }

    public boolean isDCT() {
        return isDCT;
    }

    public String getPp_head() {
        return pp_head;
    }

    public int getTid() {
        return tid;
    }

    public String[] getTMD(){
        String[] tmd = new String[3];
        tmd[0] = type;
        tmd[1] = mod;
        tmd[2] = isDCT?"True":"False";
        return tmd;
    }
    public void extractPPHead(){
        if(!ta.hasView(ViewNames.PARSE_CHARNIAK)){
            pp_head = "N/A";
            return;
        }
        int tokenId = tokenSpan.getFirst();
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
}
