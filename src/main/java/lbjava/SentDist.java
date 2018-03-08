// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000B49CC2E4E2A4D2945580E4DCB2179CC2E21D8094DCD28CF2A4CC17D230A840426661924A6186A28D8EA245B2466A92864A618E5A2846C84F2D35B404A9C3354343514751062E68822E6B6BA060A904D904B2A4B82F4149066B8550726E6AA295B24DA24A6E417A29B64B12653ADF3F05C46C097FD4CCBA40B20024006D190AFEFF000000

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


public class SentDist extends Classifier
{
  public SentDist()
  {
    containingPackage = "lbjava";
    name = "SentDist";
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
      System.err.println("Classifier 'SentDist(TemporalEventPair)' defined on line 14 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    TemporalEventPair ep = (TemporalEventPair) __example;

    if (ep.event2.getSentId() - ep.event1.getSentId() == 0)
    {
      return "SentDist:Same";
    }
    else
    {
      if (ep.event2.getSentId() - ep.event1.getSentId() == 1)
      {
        return "SentDist:One";
      }
      else
      {
        return "SentDist:Many";
      }
    }
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof TemporalEventPair[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'SentDist(TemporalEventPair)' defined on line 14 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "SentDist".hashCode(); }
  public boolean equals(Object o) { return o instanceof SentDist; }
}

