package datastruct;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.TLINK;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.TempEval3Reader;
import edu.illinois.cs.cogcomp.nlp.graph.GraphJavaScript;
import edu.illinois.cs.cogcomp.nlp.graph.vertex;
import edu.illinois.cs.cogcomp.nlp.util.TransitivityTriplets;
import edu.illinois.cs.cogcomp.slm.util.MyFunc;
import edu.uw.cs.lil.uwtime.chunking.chunks.EventChunk;
import edu.uw.cs.lil.uwtime.chunking.chunks.TemporalJointChunk;
import edu.uw.cs.lil.uwtime.data.TemporalDocument;
import util.TempLangMdl;
import util.constants;

import java.util.*;

import static datastruct.Event.extractClosestSynset;
import static datastruct.Timex.tjc2timex;

public class TemporalStructure{
    public List<Event> allSRLEvents = new ArrayList<>();
    public List<Event> allEvents = new ArrayList<>();
    public List<Timex> allTimexes = new ArrayList<>();
    private List<Event> additionalEvents = new ArrayList<>();
    public List<TemporalEventPair> eventPairs = new ArrayList<>();// event1 always appears before event2 in text
    public List<TemporalEventPair> allEventPairs = new ArrayList<>();
    public List<TemporalEventTimexPair> allEventTimexPairs = new ArrayList<>();
    private List<TemporalEventPair> additionalEventPairs = new ArrayList<>();//for those eventpairs that are not associated with relGraph
    public TLINK.TlinkType[][] relGraph;// upper triangle
    public TLINK.TlinkType[][] relGraphFull;// upper triangle
    public ClinkType[][] causalGraph;//upper triangle
    public String structID = "";
    public int reliableRange = 1;
    public TextAnnotation ta;
    private TemporalDocument doc;
    public boolean useNewRelAnn = true;

    public static boolean verbose = false;
    public static boolean newLabel = false;//newLabel means includes/included are removed
    public static TempLangMdl myTempLangMdl;
    private static HashMap<String,List<Integer>> idx2keep;
    private static HashMap<String,HashMap<String,TLINK.TlinkType>> newRelAnn;

    public TemporalStructure(TemporalDocument other, boolean sat) {
        this(other,sat,false);
    }
    public TemporalStructure(TemporalDocument other, boolean sat, boolean useNewRelAnn) {
        if(useNewRelAnn){
            if(idx2keep==null||newRelAnn==null){
                useNewRelAnn = false;
                System.out.println("idx2keep or newRelAnn not set up in TemporalStructure. Force useNewRelAnn=false.");
            }
        }
        this.useNewRelAnn = useNewRelAnn;
        structID = other.getDocID();
        ta = other.getTextAnnotation();
        doc = other;
        HashMap<Event,EventChunk> srl2event = extractSRLEventsFromTempDoc();
        extractEventPairsFromTempDoc(srl2event);
        initRelGraph();
        extractRelGraph();
        if(sat) {
            saturateTemporalStruct();
        }
    }
    public TemporalStructure(TextAnnotation srl_ta, boolean sat){
        ta = srl_ta;
        structID = srl_ta.getCorpusId()+":"+srl_ta.getId();
        extractSRLEventsFromTextAnn(srl_ta);
        extractEventPairsFromTextAnn(srl_ta);
        initRelGraph();
        extractRelGraph();
        if(sat){
            saturateTemporalStruct();
        }
    }
    public void initRelGraph(){
        int n = allSRLEvents.size();
        relGraph = new TLINK.TlinkType[n][n];
        causalGraph = new ClinkType[n][n];
        for(int i=0;i<n;i++) {
            for (int j = 0; j < n; j++) {
                relGraph[i][j] = TLINK.TlinkType.UNDEF;
                causalGraph[i][j] = ClinkType.UNDEF;
            }
        }
    }

    public void saturateTemporalStruct(){//keeps eventPairs consistent with relGraph
        if(verbose)
            System.out.printf("Saturating %s...", structID);
        saturateRelGraph();
        if(verbose)
            System.out.printf("Finished...");
        relGraph2eventPairs();
        if(verbose)
            System.out.printf("Labels in eventPairs corrected.\n");
    }
    public void saturateRelGraph(){
        saturateRelGraph(Integer.MAX_VALUE);
    }
    public void saturateRelGraph(int maxIter){
        List<TransitivityTriplets> transTriplets_all = TransitivityTriplets.transTriplets();
        List<TransitivityTriplets> singleTransTriplets = new ArrayList<>();
        for(TransitivityTriplets triplet:transTriplets_all){
            int n = triplet.getThird().length;
            if (n > 1)
                continue;
            singleTransTriplets.add(triplet);
        }

        int n = allSRLEvents.size();
        boolean update = true;
        int iter = 0;
        while(update&&iter<=maxIter) {
            iter++;
            update = false;
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (!isRelNull(relGraph[i][j]))
                        continue;
                    boolean found_bridge = false;
                    for (int k = i + 1; k < n; k++) {
                        if (k == j)
                            continue;
                        // k!=i or j, k is a third node that potentially connects i and j

                        if (isRelNull(relGraph[i][k]) ||
                                k < j && isRelNull(relGraph[k][j]) ||
                                k > j && isRelNull(relGraph[j][k]))
                            continue;
                        // now [i,k] and [k,j] are both connected
                        for (TransitivityTriplets triplet : singleTransTriplets) {
                            if (k < j) {
                                if (relGraph[i][k] == triplet.getFirst()
                                        && relGraph[k][j] == triplet.getSecond()) {
                                    relGraph[i][j] = triplet.getThird()[0];
                                    update = true;
                                    found_bridge = true;
                                    break;
                                }
                            } else {// k>j
                                if (relGraph[i][k] == triplet.getFirst()
                                        && relGraph[j][k].reverse() == triplet.getSecond()) {
                                    relGraph[i][j] = triplet.getThird()[0];
                                    update = true;
                                    found_bridge = true;
                                    break;
                                }
                            }
                        }
                        if(found_bridge)
                            break;
                    }
                }
            }
        }
    }
    public void extractRelGraph(){
        int n = allSRLEvents.size();
        for(int i=0;i<n;i++)
            for(int j=0;j<n;j++)
                relGraph[i][j] = TLINK.TlinkType.UNDEF;
        for(TemporalEventPair ep:eventPairs){
            if(!ep.isRelNull())
                relGraph[allSRLEvents.indexOf(ep.event1)][allSRLEvents.indexOf(ep.event2)] = ep.relation;
        }
    }
    public void relGraph2eventPairs(){
        for(TemporalEventPair ep:eventPairs){
            ep.relation = relGraph[allSRLEvents.indexOf(ep.event1)][allSRLEvents.indexOf(ep.event2)];
        }
    }
    public void relGraphFull2pairs(){
        for(TemporalEventPair ep:allEventPairs){
            ep.relation = relGraphFull[getEntityIdx(ep.event1)][getEntityIdx(ep.event2)];
        }
        for(TemporalEventTimexPair etp:allEventTimexPairs){
            etp.relation = relGraphFull[getEntityIdx(etp.event)][getEntityIdx(etp.timex)];
        }
    }
    public void causalRelGraph2eventPairs(){
        for(TemporalEventPair ep:eventPairs){
            ep.causal_relation = causalGraph[allSRLEvents.indexOf(ep.event1)][allSRLEvents.indexOf(ep.event2)];
        }
    }

    private boolean isRelNull(TLINK.TlinkType tt){
        return tt==null || tt == TLINK.TlinkType.UNDEF;
    }

    private String regularize(String str) {
        if (str.startsWith("\'")) {
            str = str.substring(1, str.length());
        }
        return str;
    }
    public List<Constituent> filterIgnores(List<Constituent> predicates){
        List<Constituent> filtered = new ArrayList<>();
        for(Constituent c:predicates){
            if(MyFunc.isIgnoreVerb(c.getAttribute("predicate")))
                continue;
            filtered.add(c);
        }
        return filtered;
    }
    private HashMap<Event,EventChunk> extractSRLEventsFromTempDoc(){
        // to-do: cluster id is not generated now
        HashMap<Event,EventChunk> srl2event = new HashMap<>();
        List<EventChunk> bodyEventMentions = doc.getBodyEventMentions();
        if(useNewRelAnn){
            if (idx2keep.containsKey(doc.getDocID())) {
                List<EventChunk> events_filtered = new ArrayList<>();
                List<Integer> idx2keep_doc = idx2keep.get(doc.getDocID());
                for (EventChunk e : bodyEventMentions) {
                    if (!idx2keep_doc.contains(bodyEventMentions.indexOf(e))) {
                        continue;
                    }
                    events_filtered.add(e);
                }
                bodyEventMentions = events_filtered;
            }
            doc.setBodyEventMentions(bodyEventMentions);
        }
        List<Constituent> allPredicates = ((PredicateArgumentView)doc.getTextAnnotation().getView(ViewNames.SRL_VERB)).getPredicates();
        allPredicates = filterIgnores(allPredicates);
        for(EventChunk ec:bodyEventMentions){
            Event event = new Event(ec,doc.getTextAnnotation(),bodyEventMentions.indexOf(ec));
            event.setTACP(ec.getTense(),ec.getAspect(),ec.getEventclass(),ec.getPolarity());
            allEvents.add(event);
            for(Constituent c:allPredicates){
                if(ec.getCharStart()==c.getStartCharOffset()
                        &&ec.getCharEnd()==c.getEndCharOffset()){// matched
                    String clusterID = "";
                    event.setSense(c.getAttribute("SenseNumber"));
                    if(myTempLangMdl!=null && myTempLangMdl.cluster!=null){
                        clusterID = myTempLangMdl.cluster.getOrDefault(c.getAttribute("predicate").replaceAll("\\.0[1-9]",""),"-1");
                    }
                    else {
                        String pred = regularize(c.getAttribute("predicate")) + "." + c.getAttribute("SenseNumber");
                        //String clusterID = frame_mapping.getFrame(pred);
                        clusterID = pred;
                    }
                    event.setCluster(clusterID);
                    allSRLEvents.add(event);
                    srl2event.put(event,ec);
                    break;
                }
            }
        }
        extractClosestSynset(allEvents);
        return srl2event;
    }
    public void annTemporalDocumentUsingSRLEventPairs(TemporalDocument doc, TLINK.TlinkType[][] relGraph){
        doc.removeTTlinks();doc.removeEElinks();doc.removeETlinks();
        List<TLINK> predTlinks = new ArrayList<>();
        int lid = 0;
        for(TemporalEventPair ep:eventPairs){
            /*if(ep.isRelNull())
                continue;*/
            Event e1 = ep.event1;
            Event e2 = ep.event2;
            TLINK tt = new TLINK(lid,"", TempEval3Reader.Type_Event,TempEval3Reader.Type_Event,e1.getEiid(),e2.getEiid(),
                    relGraph[allSRLEvents.indexOf(e1)][allSRLEvents.indexOf(e2)]);
            if(tt.getReducedRelType() == TLINK.TlinkType.UNDEF)
                continue;
            lid++;
            predTlinks.add(tt);
        }
        doc.setBodyTlinks(predTlinks);
    }
    public void annTemporalDocumentUsingAllEventTimex(TemporalDocument doc, TLINK.TlinkType[][] relGraphFull){
        doc.removeTTlinks();doc.removeEElinks();doc.removeETlinks();
        List<TLINK> predTlinks = new ArrayList<>();
        int lid = 0;
        for(TemporalEventPair ep:allEventPairs){
            /*if(ep.isRelNull())//@@@@fix this!!!
                continue;*/
            Event e1 = ep.event1;
            Event e2 = ep.event2;
            TLINK tt = new TLINK(lid,"", TempEval3Reader.Type_Event,TempEval3Reader.Type_Event,e1.getEiid(),e2.getEiid(),
                    relGraphFull[getEntityIdx(e1)][getEntityIdx(e2)]);
            if(tt.getReducedRelType() == TLINK.TlinkType.UNDEF)
                continue;
            lid++;
            predTlinks.add(tt);
        }
        for(TemporalEventTimexPair etp:allEventTimexPairs){
            /*if(etp.isRelNull())
                continue;*/
            Event e = etp.event;
            Timex t = etp.timex;
            TLINK tt = new TLINK(lid,"",TempEval3Reader.Type_Event,TempEval3Reader.Type_Timex,e.getEiid(),t.getTid(),
                    relGraphFull[getEntityIdx(e)][getEntityIdx(t)]);
            if(tt.getReducedRelType() == TLINK.TlinkType.UNDEF)
                continue;
            lid++;
            predTlinks.add(tt);
        }
        doc.setBodyTlinks(predTlinks);
    }
    private void extractSRLEventsFromTextAnn(TextAnnotation ta){
        PredicateArgumentView srl = ((PredicateArgumentView) ta.getView(ViewNames.SRL_VERB));
        if(srl==null)
            return;
        List<Constituent> allPredicates = srl.getPredicates();
        allPredicates = filterIgnores(allPredicates);
        for(Constituent c:allPredicates) {
            int tokid = c.getStartSpan();
            //int sentid = 0;//@@this is temp solution
            int sentid = ta.getSentenceId(tokid);
            int eid = allPredicates.indexOf(c);
            Event event = new Event(ta,tokid,sentid,eid);
            String clusterID = "";
            event.setSense(c.getAttribute("SenseNumber"));
            if(myTempLangMdl!=null && myTempLangMdl.cluster!=null){
                clusterID = myTempLangMdl.cluster.getOrDefault(c.getAttribute("predicate").replaceAll("\\.0[1-9]",""),"-1");//seems not necessary
            }
            else {
                String pred = regularize(c.getAttribute("predicate")) + "." + c.getAttribute("SenseNumber");
                //String clusterID = frame_mapping.getFrame(pred);
                clusterID = pred;
            }
            event.setCluster(clusterID);
            allSRLEvents.add(event);
        }
    }
    private void extractEventPairsFromTempDoc(HashMap<Event,EventChunk> srl2event){
        if(useNewRelAnn&&!newRelAnn.containsKey(doc.getDocID()))
            return;
        for(int i=0;i<allEvents.size();i++){
            Event e1 = allEvents.get(i);
            EventChunk ec1 = doc.getBodyEventMentions().get(i);
            for(int j=i+1;j<allEvents.size();j++){
                Event e2 = allEvents.get(j);
                if(e2.getSentId()-e1.getSentId()>reliableRange)
                    continue;
                TemporalEventPair ep = null;
                EventChunk ec2 = doc.getBodyEventMentions().get(j);
                if(useNewRelAnn){
                    TLINK.TlinkType tt = newRelAnn.get(doc.getDocID()).getOrDefault(doc.getObjectTypeIdStr(ec1)+" "+doc.getObjectTypeIdStr(ec2),
                            TLINK.TlinkType.UNDEF);
                    TLINK.TlinkType tt_reverse = newRelAnn.get(doc.getDocID()).getOrDefault(doc.getObjectTypeIdStr(ec1)+" "+doc.getObjectTypeIdStr(ec2),
                            TLINK.TlinkType.UNDEF).reverse();
                    if(tt!= TLINK.TlinkType.UNDEF)
                        ep = new TemporalEventPair(e1, e2, tt, this);
                    else if(tt_reverse!= TLINK.TlinkType.UNDEF){
                        ep = new TemporalEventPair(e1, e2, tt_reverse, this);
                    }
                    else{
                        ep = new TemporalEventPair(e1, e2, TLINK.TlinkType.UNDEF, this);
                    }
                }
                else {
                    TLINK tt = doc.getTlink(ec1, ec2);
                    TLINK tt_reverse = doc.getTlink(ec2, ec1);
                    if (tt != null && !tt.getReducedRelType().equals(TLINK.TlinkType.UNDEF)) {
                        ep = new TemporalEventPair(e1, e2, tt.getReducedRelType(), this);
                    } else if (tt_reverse != null && !tt_reverse.getReducedRelType().equals(TLINK.TlinkType.UNDEF)) {
                        ep = new TemporalEventPair(e1, e2, tt_reverse.getReducedRelType().reverse(), this);
                    } else {
                        ep = new TemporalEventPair(e1, e2, TLINK.TlinkType.UNDEF, this);
                    }
                }
                setKeywords4EventPairs(ep,constants.connectivesSet,constants.modalVerbSet);
                allEventPairs.add(ep);
            }
        }
        for(int i=0;i<allSRLEvents.size();i++){
            Event e1 = allSRLEvents.get(i);
            EventChunk ec1 = srl2event.get(e1);
            for(int j=i+1;j<allSRLEvents.size();j++){
                Event e2 = allSRLEvents.get(j);
                if(e2.getSentId()-e1.getSentId()>reliableRange)
                    continue;
                TemporalEventPair ep = null;
                EventChunk ec2 = srl2event.get(e2);
                if(useNewRelAnn){
                    TLINK.TlinkType tt = newRelAnn.get(doc.getDocID()).getOrDefault(doc.getObjectTypeIdStr(ec1)+" "+doc.getObjectTypeIdStr(ec2),
                            TLINK.TlinkType.UNDEF);
                    TLINK.TlinkType tt_reverse = newRelAnn.get(doc.getDocID()).getOrDefault(doc.getObjectTypeIdStr(ec1)+" "+doc.getObjectTypeIdStr(ec2),
                            TLINK.TlinkType.UNDEF).reverse();
                    if(tt!= TLINK.TlinkType.UNDEF)
                        ep = new TemporalEventPair(e1, e2, tt, this);
                    else if(tt_reverse!= TLINK.TlinkType.UNDEF){
                        ep = new TemporalEventPair(e1, e2, tt_reverse, this);
                    }
                    else{
                        ep = new TemporalEventPair(e1, e2, TLINK.TlinkType.UNDEF, this);
                    }
                }
                else {
                    TLINK tt = doc.getTlink(ec1, ec2);
                    TLINK tt_reverse = doc.getTlink(ec2, ec1);
                    if (tt != null && !tt.getReducedRelType().equals(TLINK.TlinkType.UNDEF)) {
                        ep = new TemporalEventPair(e1, e2, tt.getReducedRelType(), this);
                    } else if (tt_reverse != null && !tt_reverse.getReducedRelType().equals(TLINK.TlinkType.UNDEF)) {
                        ep = new TemporalEventPair(e1, e2, tt_reverse.getReducedRelType().reverse(), this);
                    } else {
                        ep = new TemporalEventPair(e1, e2, TLINK.TlinkType.UNDEF, this);
                    }
                }
                if(newLabel&&ep.isRelInclude())
                    continue;
                setKeywords4EventPairs(ep,constants.connectivesSet,constants.modalVerbSet);
                eventPairs.add(ep);
            }
        }
    }
    private void extractEventPairsFromTextAnn(TextAnnotation ta){
        // assume no tlinks exist now
        int n = allSRLEvents.size();
        for(int i=0;i<n;i++){
            Event e1 = allSRLEvents.get(i);
            for(int j=i+1;j<n;j++){
                Event e2 = allSRLEvents.get(j);
                if(e2.getSentId()-e1.getSentId()>reliableRange)
                    continue;
                TemporalEventPair ep = new TemporalEventPair(e1,e2, TLINK.TlinkType.UNDEF,this);
                setKeywords4EventPairs(ep,constants.connectivesSet,constants.modalVerbSet);
                eventPairs.add(ep);
            }
        }
    }

    public void keepEventPairsOnlyInSent(int i){//get only dist=i pairs
        eventPairs = keepEventPairsOnlyInSent(eventPairs,i);
    }
    public void keepFullEventPairsOnlyInSent(int i){
        allEventPairs = keepEventPairsOnlyInSent(allEventPairs,i);
    }
    public List<TemporalEventPair> keepEventPairsOnlyInSent(List<TemporalEventPair> elist, int i){
        if(i<0)
            return elist;
        List<TemporalEventPair> newEventPairs = new ArrayList<>();
        for(TemporalEventPair ep:elist){
            if(ep.event2.getSentId()-ep.event1.getSentId()!=i)
                continue;
            newEventPairs.add(ep);
        }
        return newEventPairs;
    }
    public static List<TemporalEventPair> extractEventPairs_negVagSampling(List<TemporalEventPair> elist, double negVagSampling, boolean temp_or_causal, Random rng){
        List<TemporalEventPair> elist_sampled = new ArrayList<>();
        for(TemporalEventPair ep:elist){
            if(temp_or_causal) {
                if (ep.relation != TLINK.TlinkType.UNDEF)
                    elist_sampled.add(ep);
                else {
                    if (negVagSampling<=1) {
                        if(rng.nextDouble() <= negVagSampling)
                            elist_sampled.add(ep);
                    }
                    else{
                        double tmp = negVagSampling;
                        for(;tmp>1;tmp--){
                            elist_sampled.add(ep);
                        }
                        if(rng.nextDouble() <= tmp)
                            elist_sampled.add(ep);
                    }
                }
            }
            else{
                if (ep.causal_relation != ClinkType.UNDEF)
                    elist_sampled.add(ep);
                else {
                    if(negVagSampling<=1) {
                        if (rng.nextDouble() <= negVagSampling) {
                            elist_sampled.add(ep);
                        }
                    }
                    else{
                        double tmp = negVagSampling;
                        for(;tmp>1;tmp--){
                            elist_sampled.add(ep);
                        }
                        if(rng.nextDouble() <= tmp)
                            elist_sampled.add(ep);
                    }
                }
            }
        }
        return elist_sampled;
    }
    public List<TemporalEventPair> extractEventPairs_negVagSampling(double negVagSampling){
        return extractEventPairs_negVagSampling(negVagSampling,true);
    }
    public List<TemporalEventPair> extractEventPairs_negVagSampling(double negVagSampling, boolean temp_or_causal){
        return extractEventPairs_negVagSampling(eventPairs,negVagSampling,temp_or_causal,new Random());
    }
    public List<TemporalEventPair> extractEventPairs_negVagSampling(double negVagSampling, boolean temp_or_causal, int seed){
        Random random = new Random(seed);
        return extractEventPairs_negVagSampling(eventPairs,negVagSampling,temp_or_causal,random);
        /*List<TemporalEventPair> eventPairs_negVagSampling = new ArrayList<>();
        for(TemporalEventPair ep:eventPairs){
            if(temp_or_causal) {
                if (ep.relation != TLINK.TlinkType.UNDEF)
                    eventPairs_negVagSampling.add(ep);
                else {
                    if (random.nextDouble() <= negVagSampling) {
                        eventPairs_negVagSampling.add(ep);
                    }
                }
            }
            else{
                if (ep.causal_relation != ClinkType.UNDEF)
                    eventPairs_negVagSampling.add(ep);
                else {
                    if (random.nextDouble() <= negVagSampling) {
                        eventPairs_negVagSampling.add(ep);
                    }
                }
            }
        }
        return eventPairs_negVagSampling;*/
    }
    public void sortEventPairs(){
        sortEventPairs(eventPairs);
        sortEventPairs(allEventPairs);
    }
    public void sortEventPairs(List<TemporalEventPair> elist){
        elist.sort(new Comparator<TemporalEventPair>(){
                       public int compare(TemporalEventPair ep1, TemporalEventPair ep2) {
                           int sentDist1 = Math.abs(ep1.event1.getSentId()-ep1.event2.getSentId());
                           int sentDist2 = Math.abs(ep2.event1.getSentId()-ep2.event2.getSentId());
                           if(sentDist1!=sentDist2)
                               return sentDist1-sentDist2;
                           int tokDist1 = Math.abs(ep1.event1.getTokenId()-ep1.event2.getTokenId());
                           int tokDist2 = Math.abs(ep2.event1.getTokenId()-ep2.event2.getTokenId());
                           return tokDist1-tokDist2;
                       }
                   }
        );
    }

    public void setKeywords4EventPairs(TemporalEventPair ep,HashSet<String> connectivesSet,HashSet<String> modelVerbSet){
        int start = startTokInSent(ta,Math.min(ep.event1.getSentId(),ep.event2.getSentId()));
        int loc1 = Math.min(ep.event1.getTokenId(),ep.event2.getTokenId());
        int loc2 = Math.max(ep.event1.getTokenId(),ep.event2.getTokenId());
        int end = endTokInSent(ta,Math.max(ep.event1.getSentId(),ep.event2.getSentId()));
        ep.connectives_before = findKeywordsBetween(start,loc1,connectivesSet);
        ep.modelverbs_before = findKeywordsBetween(start,loc1,modelVerbSet);
        ep.connectives_between = findKeywordsBetween(loc1,loc2,connectivesSet);
        ep.modelverbs_between = findKeywordsBetween(loc1,loc2,modelVerbSet);
        ep.connectives_after = findKeywordsBetween(loc2,end,connectivesSet);
        ep.modelverbs_after = findKeywordsBetween(loc2,end,modelVerbSet);
    }
    private List<String> findKeywordsBetween(int start, int end, HashSet<String> keywords){
        String[] tokens = ta.getTokensInSpan(start,end);
        List<String> matches = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for(String t:tokens){
            sb.append(t.toLowerCase());
            sb.append(" ");
        }
        String text = sb.toString().trim();
        boolean found_connective = false;
        for(String str:keywords){
            if(text.contains(str)){
                found_connective = true;
                matches.add(str.toLowerCase());
            }
        }
        if(!found_connective)
            matches.add("N/A");
        return matches;
    }
    //@@temp solution
    public void addIsBeforeCorpusFeat(){
        if(myTempLangMdl!=null&&myTempLangMdl.tempLangMdl!=null) {
            for (TemporalEventPair ep : eventPairs) {
                ep.isBeforeCorpus(myTempLangMdl.tempLangMdl);
            }
        }
    }
    public void addOtherLabelCorpusFeat(){
        if(myTempLangMdl!=null&&myTempLangMdl.tempLangMdl!=null) {
            for (TemporalEventPair ep : eventPairs) {
                ep.readCorpusStats(myTempLangMdl.tempLangMdl);
            }
        }
    }
    public void genGraph(String fname){
        GraphJavaScript graph = new GraphJavaScript(fname);
        for(Event event:allSRLEvents){
            graph.addVertex(allSRLEvents.indexOf(event), vertex.EntityType.EVENT,event.getText());
        }
        List<vertex> V = graph.getVertexes();
        int n = allSRLEvents.size();
        for(int i=0;i<n;i++){
            for(int j=i+1;j<n;j++){
                if(relGraph[i][j]== TLINK.TlinkType.UNDEF)
                    continue;
                graph.addEdge(V.get(i),V.get(j), relGraph[i][j].toStringfull());
            }
        }
        graph.createJS();
    }
    public void extractPrepPhrase(){
        for(Event e:allSRLEvents){
            e.extractPPHead();
        }
        for(Event e:allEvents)
            e.extractPPHead();
        for(Event e:additionalEvents)
            e.extractPPHead();
        if(allTimexes==null||allTimexes.size()==0)
            extractAllTimexes();
        for(Timex t:allTimexes)
            t.extractPPHead();
    }
    public void addEmbeddings(){
        if(myTempLangMdl!=null&&myTempLangMdl.embeddings!=null){
            for (TemporalEventPair ep : eventPairs) {
                ep.addEmbedding(myTempLangMdl.embeddings);
            }
        }
    }

    public TemporalDocument getDoc(){
        return doc;
    }
    public Event getEventfromEIID(int eiid){
        for(Event e:allSRLEvents){
            if(e.getEiid()==eiid)
                return e;
        }
        return null;
    }
    public Event getEventAllfromEIID(int eiid){
        for(Event e:allEvents){
            if(e.getEiid()==eiid)
                return e;
        }
        return null;
    }

    public List<TemporalEventPair> getAdditionalEventPairs() {
        return additionalEventPairs;
    }

    public void addAdditionalEventPair(TemporalEventPair ep){
        additionalEventPairs.add(ep);
        int eiid1 = ep.event1.getEiid();
        int eiid2 = ep.event2.getEiid();
        if(getEventAllfromEIID(eiid1)==null){
            additionalEvents.add(ep.event1);
        }
        if(getEventAllfromEIID(eiid2)==null){
            additionalEvents.add(ep.event2);
        }
    }
    public Timex getTimexFromTID(int tid){
        for(Timex t:allTimexes){
            if(t.getTid()==tid)
                return t;
        }
        return null;
    }
    public void setRelTypefromEIID(int eiid1, int eiid2, TLINK.TlinkType tt){
        setRelTypefromIndex(allSRLEvents.indexOf(getEventfromEIID(eiid1)),allSRLEvents.indexOf(getEventfromEIID(eiid2)),tt);
    }
    public TLINK.TlinkType getRelTypefromEIID(int eiid1,int eiid2){
        return getRelTypefromIndex(allSRLEvents.indexOf(getEventfromEIID(eiid1)),allSRLEvents.indexOf(getEventfromEIID(eiid2)));
    }
    public void setRelTypefromEIIDFull(int eiid1, int eiid2, TLINK.TlinkType tt){
        setRelTypefromIndexFull(getEntityIdx(getEventfromEIID(eiid1)),getEntityIdx(getEventfromEIID(eiid2)),tt);
    }
    public TLINK.TlinkType getRelTypefromEIIDFull(int eiid1,int eiid2){
        return getRelTypefromIndexFull(getEntityIdx(getEventfromEIID(eiid1)),getEntityIdx(getEventfromEIID(eiid2)));
    }
    public void setRelTypefromIndex(int id1, int id2, TLINK.TlinkType tt){
        if(id1<0||id1>allSRLEvents.size()
                ||id2<0||id2>allSRLEvents.size())
            return;
        relGraph[id1][id2] = tt;
        relGraph[id2][id1] = tt.reverse();
        relGraph2eventPairs();
    }
    public TLINK.TlinkType getRelTypefromIndex(int id1,int id2){
        if(id1<0||id1>allSRLEvents.size()
                ||id2<0||id2>allSRLEvents.size())
            return TLINK.TlinkType.UNDEF;
        if(id1>id2)
            return relGraph[id2][id1].reverse();
        if(id1==id2)
            return TLINK.TlinkType.EQUAL;
        return relGraph[id1][id2];
    }
    public void setRelTypefromIndexFull(int id1, int id2, TLINK.TlinkType tt){
        if(id1<0||id1>allEvents.size()
                ||id2<0||id2>allEvents.size())
            return;
        relGraphFull[id1][id2] = tt;
        relGraphFull[id2][id1] = tt.reverse();
        relGraphFull2pairs();
    }
    public TLINK.TlinkType getRelTypefromIndexFull(int id1,int id2){
        if(id1<0||id1>allEvents.size()
                ||id2<0||id2>allEvents.size())
            return TLINK.TlinkType.UNDEF;
        if(id1>id2)
            return relGraphFull[id2][id1].reverse();
        if(id1==id2)
            return TLINK.TlinkType.EQUAL;
        return relGraphFull[id1][id2];
    }
    public void setRelTypefromEIID_additional(int eiid1, int eiid2, TLINK.TlinkType tt){
        for(TemporalEventPair ep:additionalEventPairs){
            if(ep.event1.getEiid()==eiid1&&ep.event2.getEiid()==eiid2){
                ep.relation = tt;
            }
            else if(ep.event1.getEiid()==eiid2&&ep.event2.getEiid()==eiid1){
                ep.relation = tt.reverse();
            }
        }
    }
    public TLINK.TlinkType getRelTypefromEIID_additional(int eiid1, int eiid2){
        for(TemporalEventPair ep:additionalEventPairs){
            if(ep.event1.getEiid()==eiid1&&ep.event2.getEiid()==eiid2){
                return ep.relation;
            }
            else if(ep.event1.getEiid()==eiid2&&ep.event2.getEiid()==eiid1){
                return ep.relation.reverse();
            }
        }
        return TLINK.TlinkType.UNDEF;
    }
    public void setCausalTypefromEIID(int eiid1,int eiid2, ClinkType ct){
        setCausalTypefromIndex(allSRLEvents.indexOf(getEventfromEIID(eiid1)),allSRLEvents.indexOf(getEventfromEIID(eiid2)),ct);
    }
    public ClinkType getCausalTypefromEIID(int eiid1,int eiid2){
        return getCausalTypefromIndex(allSRLEvents.indexOf(getEventfromEIID(eiid1)),allSRLEvents.indexOf(getEventfromEIID(eiid2)));
    }
    public void setCausalTypefromIndex(int id1,int id2, ClinkType ct){
        if(id1<0||id1>allSRLEvents.size()
                ||id2<0||id2>allSRLEvents.size()) {
            System.out.printf("getCausalTypefromIndex(%d,%d). But allSRLEvents.size()=%d\n", id1, id2, allSRLEvents.size());
            return;
        }
        /*if(!ClinkType.isNull(causalGraph[id1][id2]))
            System.out.printf("duplicate in %s: %s->%s, (e%s->e%s)\n",structID,id1,id2,allSRLEvents.get(id1).getEiid(),allSRLEvents.get(id2).getEiid());*/
        causalGraph[id1][id2] = ct;
        causalGraph[id2][id1] = ct.reverse();
    }
    public ClinkType getCausalTypefromIndex(int id1,int id2){
        if(id1<0||id1>allSRLEvents.size()
                ||id2<0||id2>allSRLEvents.size())
            return ClinkType.UNDEF;
        if(id1>id2)
            return causalGraph[id2][id1].reverse();
        if(id1==id2)
            return ClinkType.UNDEF;
        return causalGraph[id1][id2];
    }
    public static void setIdx2keep(HashMap<String, List<Integer>> idx2keep) {
        TemporalStructure.idx2keep = idx2keep;
    }
    public static void setNewRelAnn(HashMap<String, HashMap<String, TLINK.TlinkType>> newRelAnn) {
        TemporalStructure.newRelAnn = newRelAnn;
    }
    public int countClink(){
        int cnt = 0;
        int n = allSRLEvents.size();
        for(int i=0;i<n;i++){
            for(int j=i+1;j<n;j++){
                if(!ClinkType.isNull(causalGraph[i][j]))
                    cnt++;
            }
        }
        return cnt;
    }
    public int startTokInSent(int sentid){
        return startTokInSent(ta,sentid);
    }
    public static int startTokInSent(TextAnnotation ta, int sentid){
        if(sentid>=ta.getNumberOfSentences()||sentid<0)
            return -1;// sentid out of boundary
        int n = ta.getTokens().length;
        int i;
        for(i=0;i<n;i++){
            int tmp = ta.getSentenceId(i);
            if(tmp==sentid)
                break;
        }
        return i;
    }
    public static int endTokInSent(TextAnnotation ta, int sentid){
        int start = startTokInSent(ta,sentid);
        if(start==-1)
            return -1;
        return start+ta.getSentence(sentid).getTokens().length-1;
    }
    public String renderSent(int sentid){
        try {
            Sentence sent = ta.getSentence(sentid);
            String text = sent.toString();
            List<Event> events = new ArrayList<>();
            for (Event e : allSRLEvents) {
                if (e.getSentId() == sentid)
                    events.add(e);
            }
            if (events.size() == 0)
                return text;
            // get the tokenid of the first token in sentid
            String[] sentTokens = sent.getTokens().clone();
            int startId = startTokInSent(sentid);
            for (Event e : events) {
                int offset = e.getTokenId() - startId;
                String old = sentTokens[offset];
                sentTokens[offset] = String.format("<e%d:%s>", e.getEiid(), old);
            }
            StringBuilder sb = new StringBuilder();
            for (String str : sentTokens) {
                sb.append(str);
                sb.append(" ");
            }
            return sb.toString();
        }
        catch(Exception e){e.printStackTrace();return "";}
    }


    // Event-Timex
    public void extractAllTimexes(){
        if(doc==null||ta==null)
            return;
        Timex dct = tjc2timex(doc.getDocumentCreationTime(),ta,true);
        allTimexes.add(dct);
        for(TemporalJointChunk tjc:doc.getBodyTimexMentions()){
            Timex timex = tjc2timex(tjc,ta,false);
            allTimexes.add(timex);
        }
    }
    public void extractAllEventTimexPairs(boolean useSRLEvent){
        if(allTimexes==null||allTimexes.size()==0)
            extractAllTimexes();
        if(!useSRLEvent){
            for(Event e:allEvents) {
                EventChunk ec = doc.getBodyEventMentions().get(allEvents.indexOf(e));
                for(Timex t:allTimexes) {
                    if(!t.isDCT()&&Math.abs(e.getSentId()-t.getSentId())>reliableRange)
                        continue;

                    TemporalJointChunk tjc;
                    if(t.isDCT())
                        tjc = doc.getDocumentCreationTime();
                    else
                        tjc = doc.getBodyTimexMentions().get(allTimexes.indexOf(t)-1);
                    TLINK.TlinkType rel;
                    TLINK tt = doc.getTlink(ec, tjc);
                    TLINK tt_reverse = doc.getTlink(tjc, ec);
                    if (tt != null && !tt.getReducedRelType().equals(TLINK.TlinkType.UNDEF)) {
                        rel = tt.getReducedRelType();
                    } else if (tt_reverse != null && !tt_reverse.getReducedRelType().equals(TLINK.TlinkType.UNDEF)) {
                        rel = tt_reverse.getReducedRelType().reverse();
                    } else {
                        rel = TLINK.TlinkType.UNDEF;
                    }
                    TemporalEventTimexPair etPair = new TemporalEventTimexPair(e,t,rel,this);
                    setKeywords4EventTimexPairs(etPair,constants.connectivesSet,constants.modalVerbSet);
                    allEventTimexPairs.add(etPair);
                }
            }
        }
        else{
            // @@ to-do
        }
    }


    public void setRelGraphFullFromObjStr(String o1, String o2, TLINK.TlinkType tt){
        int id1 = o1.startsWith("e")?getEntityIdx(getEventAllfromEIID(Integer.valueOf(o1.substring(1))))
                :getEntityIdx(getTimexFromTID(Integer.valueOf(o1.substring(1))));
        int id2 = o2.startsWith("e")?getEntityIdx(getEventAllfromEIID(Integer.valueOf(o2.substring(1))))
                :getEntityIdx(getTimexFromTID(Integer.valueOf(o2.substring(1))));
        if(id1==-1||id2==-1)
            return;
        relGraphFull[id1][id2] = tt;
    }
    public void extractRelGraphFull(){
        int n_entity = allEvents.size()+allTimexes.size();
        relGraphFull = new TLINK.TlinkType[n_entity][n_entity];
        for(int i=0;i<n_entity;i++)
            for(int j=0;j<n_entity;j++)
                relGraphFull[i][j] = TLINK.TlinkType.UNDEF;
        for(TemporalEventPair ep:allEventPairs){
            if(!ep.isRelNull())
                relGraphFull[getEntityIdx(ep.event1)][getEntityIdx(ep.event2)] = ep.relation;
        }
        for(TemporalEventTimexPair etp:allEventTimexPairs){
            if(!etp.isRelNull())
                relGraphFull[getEntityIdx(etp.event)][getEntityIdx(etp.timex)] = etp.relation;
        }

        doc.orderTimexes();
        List<TLINK> ttlinks = doc.getTTlinks();
        for(TLINK tlnk:ttlinks){
            setRelGraphFullFromObjStr("t"+tlnk.getSourceId(),"t"+tlnk.getSourceId(),tlnk.getReducedRelType());
        }
    }

    public int getEntityIdx(Object obj){
        if(obj instanceof Event)
            return allEvents.indexOf(obj);
        if(obj instanceof Timex)
            return allEvents.size()+allTimexes.indexOf(obj);
        return -1;
    }

    private void setKeywords4EventTimexPairs(TemporalEventTimexPair ep, HashSet<String> connectivesSet,HashSet<String> modelVerbSet){
        if(ep.timex.isDCT())
            return;
        int start = startTokInSent(ta,Math.min(ep.event.getSentId(),ep.timex.getSentId()));
        int loc1 = Math.min(ep.event.getTokenId(),ep.timex.getTokenHeadId());
        int loc2 = Math.max(ep.event.getTokenId(),ep.timex.getTokenHeadId());
        int end = endTokInSent(ta,Math.max(ep.event.getSentId(),ep.timex.getSentId()));
        ep.connectives_before = findKeywordsBetween(start,loc1,connectivesSet);
        ep.modelverbs_before = findKeywordsBetween(start,loc1,modelVerbSet);
        ep.connectives_between = findKeywordsBetween(loc1,loc2,connectivesSet);
        ep.modelverbs_between = findKeywordsBetween(loc1,loc2,modelVerbSet);
        ep.connectives_after = findKeywordsBetween(loc2,end,connectivesSet);
        ep.modelverbs_after = findKeywordsBetween(loc2,end,modelVerbSet);
    }

    public static List<TemporalEventTimexPair> extractEventTimexPairs_negVagSampling(List<TemporalEventTimexPair> elist, double negVagSampling, Random rng){
        List<TemporalEventTimexPair> elist_sampled = new ArrayList<>();
        for(TemporalEventTimexPair ep:elist){
            if (ep.relation != TLINK.TlinkType.UNDEF)
                elist_sampled.add(ep);
            else {
                if (rng.nextDouble() <= negVagSampling) {
                    elist_sampled.add(ep);
                }
            }
        }
        return elist_sampled;
    }

    public List<TemporalEventTimexPair> keepEventTimexPairsOnlyInSent(List<TemporalEventTimexPair> etlist, int i){
        //i=-1-->EDCT
        //i>=0, ET with sentence distance = i
        List<TemporalEventTimexPair> newEventPairs = new ArrayList<>();
        for(TemporalEventTimexPair ep:etlist){
            if(i==-1){
                if(!ep.timex.isDCT())
                    continue;
            }
            else {
                if(ep.timex.isDCT())
                    continue;
                if (Math.abs(ep.event.getSentId() - ep.timex.getSentId()) != i)
                    continue;
            }
            newEventPairs.add(ep);
        }
        return newEventPairs;
    }

    public void keepEventTimexPairsOnlyInSent(int i){
        allEventTimexPairs = keepEventTimexPairsOnlyInSent(allEventTimexPairs,i);
    }

    public void printStat(){
        System.out.printf("Doc:%s, #eventPairs=%d, #allEventPairs=%d\n",structID,eventPairs.size(),allEventPairs.size());
        int[] cnt = new int[TLINK.TlinkType.tvalues().length];
        for(TemporalEventPair ep:allEventPairs){
            TLINK.TlinkType tt = ep.relation;
            cnt[tt.getTValueIdx()]++;
        }
        System.out.println("Stats of allEventPairs:");
        for(int k = 0;k<cnt.length;k++){
            System.out.printf("%10s=%5d\n", TLINK.TlinkType.tvalues()[k].toStringfull(),cnt[k]);
        }
    }
}
