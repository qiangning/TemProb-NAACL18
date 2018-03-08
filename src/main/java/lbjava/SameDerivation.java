// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000B49CC2E4E2A4D2945580E4CCD45794D2ACC2B4C29CCCFC3D8094DCD28CF2A4CC17D2B4DCB29084CCC22584D20D450B1D558A658CC4350D84D20DB2645D0A9A904920A945A5497A0A41CE8EBEA1FE2EA14E916E812E9EFE76519EA1CA46DA05BA09A935C9A87499F9F38555D200FFD3242259000000

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


public class SameDerivation extends Classifier
{
  public SameDerivation()
  {
    containingPackage = "lbjava";
    name = "SameDerivation";
  }

  public String getInputType() { return "datastruct.TemporalEventPair"; }
  public String getOutputType() { return "discrete"; }


  public FeatureVector classify(Object __example)
  {
    return new FeatureVector(featureValue(__example));
  }

  public Feature featureValue(Object __example)
  {
    String result = discreteValue(__example);
    return new DiscretePrimitiveStringFeature(containingPackage, name, "", result, valueIndexOf(result), (short) allowableValues().length);
  }

  public String discreteValue(Object __example)
  {
    if (!(__example instanceof TemporalEventPair))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'SameDerivation(TemporalEventPair)' defined on line 88 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    TemporalEventPair ep = (TemporalEventPair) __example;

    if (ep.sameDerivation())
    {
      return "SAME_DERIVATION:YES";
    }
    else
    {
      return "SAME_DERIVATION:NO";
    }
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof TemporalEventPair[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'SameDerivation(TemporalEventPair)' defined on line 88 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "SameDerivation".hashCode(); }
  public boolean equals(Object o) { return o instanceof SameDerivation; }
}

