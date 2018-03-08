// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// discrete BiasTerm4Causal(TemporalEventPair ep) <- BiasTerm

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


public class BiasTerm4Causal extends Classifier
{
  private static final BiasTerm __BiasTerm = new BiasTerm();

  public BiasTerm4Causal()
  {
    containingPackage = "lbjava";
    name = "BiasTerm4Causal";
  }

  public String getInputType() { return "datastruct.TemporalEventPair"; }
  public String getOutputType() { return "discrete"; }

  public Feature featureValue(Object __example)
  {
    if (!(__example instanceof TemporalEventPair))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'BiasTerm4Causal(TemporalEventPair)' defined on line 191 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Feature __result;
    __result = __BiasTerm.featureValue(__example);
    return __result;
  }

  public FeatureVector classify(Object __example)
  {
    return new FeatureVector(featureValue(__example));
  }

  public String discreteValue(Object __example)
  {
    return featureValue(__example).getStringValue();
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof TemporalEventPair[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'BiasTerm4Causal(TemporalEventPair)' defined on line 191 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "BiasTerm4Causal".hashCode(); }
  public boolean equals(Object o) { return o instanceof BiasTerm4Causal; }
}

