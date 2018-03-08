// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000052E81CA02C030144F756F051412E1CBB7A65192826125CB68D6BBA40739D094C2AF7F6AA7A973CCC0C83BF6A9650C2322B12233A4C0FA46099FC8301723F01E9AAA67BEE9C50988C7C99C7EE0D640A0B18DF61A6B89A7A8E418E29C1C6ACA0A79729FF36775DA0A11FE96CC676A4A0E22312FD82E05853A32DF1FA9A43BD453DD5225BEB9FD98566B8AEC89B8935932D549050FB83FE857D5B000000

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


public class local_ee_corpus_allLabels_gold$$1 extends Classifier
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
  private static final GoldProps __GoldProps = new GoldProps();

  public local_ee_corpus_allLabels_gold$$1()
  {
    containingPackage = "lbjava";
    name = "local_ee_corpus_allLabels_gold$$1";
  }

  public String getInputType() { return "datastruct.TemporalEventPair"; }
  public String getOutputType() { return "mixed%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof TemporalEventPair))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'local_ee_corpus_allLabels_gold$$1(TemporalEventPair)' defined on line 251 of local_ee.lbj received '" + type + "' as input.");
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
    __result.addFeatures(__GoldProps.classify(__example));
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof TemporalEventPair[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'local_ee_corpus_allLabels_gold$$1(TemporalEventPair)' defined on line 251 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "local_ee_corpus_allLabels_gold$$1".hashCode(); }
  public boolean equals(Object o) { return o instanceof local_ee_corpus_allLabels_gold$$1; }

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
    result.add(__GoldProps);
    return result;
  }
}

