package datastruct;

import edu.illinois.cs.cogcomp.nlp.corpusreaders.TLINK;

import java.util.List;

public class TemporalEventTimexPair {
    public Event event;
    public Timex timex;
    public TLINK.TlinkType relation;//from event to timex
    public TemporalStructure parentDoc;
    public List<String> connectives_between;
    public List<String> modelverbs_between;
    public List<String> connectives_before;
    public List<String> modelverbs_before;
    public List<String> connectives_after;
    public List<String> modelverbs_after;
    public boolean isClosest;

    public TemporalEventTimexPair(Event event, Timex timex, TLINK.TlinkType relation, TemporalStructure parentDoc) {
        this.event = event;
        this.timex = timex;
        this.relation = relation;
        this.parentDoc = parentDoc;
    }
    public boolean isRelNull(){
        return relation==null || relation == TLINK.TlinkType.UNDEF;
    }
    public boolean isEDCT(){
        return timex.isDCT();
    }

}
