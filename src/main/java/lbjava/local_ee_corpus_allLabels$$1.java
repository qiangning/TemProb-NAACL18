// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000052D813B02C034148FFACB1A2824C1CDD9A5D1A0A858417D2FADE901C42F2421B8EFB735D9EEB3EEE8376ED8973465626B3003C4213CB2D0C6DE58748D455578D6F071422BD3F20F9B36319016747CD35D6835F88E419E29C9C4ACA8A7972CFF1BBB9654D887F892B950941D55666B774C1B0B6670DF1FA15A9D6AA1F09886D73FBFF576BAAECC9B89F2056F278F55A000000

package lbjava;

import datastruct.Event;
import datastruct.TemporalEventPair;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import java.lang.Object;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import util.ParamLBJ;
import util.constants;


public class local_ee_corpus_allLabels$$1 extends Classifier
{
  private static final BiasTerm __BiasTerm = new BiasTerm();
  private static final SentDist __SentDist = new SentDist();
  private static final TokenDist __TokenDist = new TokenDist();
  private static final POS __POS = new POS();
  private static final Connectives __Connectives = new Connectives();
  private static final ModalVerbs __ModalVerbs = new ModalVerbs();
  private static final SameSynSet __SameSynSet = new SameSynSet();
  private static final IsBeforeInCorpus __IsBeforeInCorpus = new IsBeforeInCorpus();
  private static final CorpusStats __CorpusStats = new CorpusStats();

  public local_ee_corpus_allLabels$$1()
  {
    containingPackage = "lbjava";
    name = "local_ee_corpus_allLabels$$1";
  }

  public String getInputType() { return "datastruct.TemporalEventPair"; }
  public String getOutputType() { return "mixed%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof TemporalEventPair))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'local_ee_corpus_allLabels$$1(TemporalEventPair)' defined on line 244 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    FeatureVector __result;
    __result = new FeatureVector();
    __result.addFeature(__BiasTerm.featureValue(__example));
    __result.addFeature(__SentDist.featureValue(__example));
    __result.addFeature(__TokenDist.featureValue(__example));
    __result.addFeatures(__POS.classify(__example));
    __result.addFeatures(__Connectives.classify(__example));
    __result.addFeatures(__ModalVerbs.classify(__example));
    __result.addFeature(__SameSynSet.featureValue(__example));
    __result.addFeatures(__IsBeforeInCorpus.classify(__example));
    __result.addFeatures(__CorpusStats.classify(__example));
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof TemporalEventPair[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'local_ee_corpus_allLabels$$1(TemporalEventPair)' defined on line 244 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "local_ee_corpus_allLabels$$1".hashCode(); }
  public boolean equals(Object o) { return o instanceof local_ee_corpus_allLabels$$1; }

  public java.util.LinkedList getCompositeChildren()
  {
    java.util.LinkedList result = new java.util.LinkedList();
    result.add(__BiasTerm);
    result.add(__SentDist);
    result.add(__TokenDist);
    result.add(__POS);
    result.add(__Connectives);
    result.add(__ModalVerbs);
    result.add(__SameSynSet);
    result.add(__IsBeforeInCorpus);
    result.add(__CorpusStats);
    return result;
  }
}

