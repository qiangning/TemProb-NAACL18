// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000052ACBBA0201301641E7599265058858DB597925C58906B23EEEF5C06233B42161D7B73067F1C9395F3897349C7294F40681E0BD88CB871947D516D641D24856747CD3D945A6449C188B79B86D6182AFB16F7E877E0476733C4D4754D0473F952D305E5DDC291CF5364F3F76378B75A57000000

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


public class local_ee$$1 extends Classifier
{
  private static final BiasTerm __BiasTerm = new BiasTerm();
  private static final SentDist __SentDist = new SentDist();
  private static final TokenDist __TokenDist = new TokenDist();
  private static final POS __POS = new POS();
  private static final Connectives __Connectives = new Connectives();
  private static final ModalVerbs __ModalVerbs = new ModalVerbs();
  private static final SameSynSet __SameSynSet = new SameSynSet();

  public local_ee$$1()
  {
    containingPackage = "lbjava";
    name = "local_ee$$1";
  }

  public String getInputType() { return "datastruct.TemporalEventPair"; }
  public String getOutputType() { return "mixed%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof TemporalEventPair))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'local_ee$$1(TemporalEventPair)' defined on line 216 of local_ee.lbj received '" + type + "' as input.");
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
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof TemporalEventPair[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'local_ee$$1(TemporalEventPair)' defined on line 216 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "local_ee$$1".hashCode(); }
  public boolean equals(Object o) { return o instanceof local_ee$$1; }

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
    return result;
  }
}

