// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000B2A4D4CC98E85507DCD4A4D4949CCCB47FC00D8094DCD28CF2A4CC17D2B4DCB29084CCC22584D20D450B1D558A65849CF2D4AC945582250B50A89E5A7A6902BE3D4B6582E4DCB260A43C91A0A580E8F6269468E51716159864110556D200F043F38B97000000

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


public class EmbeddingIP extends Classifier
{
  public EmbeddingIP()
  {
    containingPackage = "lbjava";
    name = "EmbeddingIP";
  }

  public String getInputType() { return "datastruct.TemporalEventPair"; }
  public String getOutputType() { return "real[]"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof TemporalEventPair))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'EmbeddingIP(TemporalEventPair)' defined on line 139 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    TemporalEventPair ep = (TemporalEventPair) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    int __featureIndex = 0;
    double __value;

    double r = ep.getEmbeddingIP();
    __value = r;
    __result.addFeature(new RealArrayStringFeature(this.containingPackage, this.name, "", __value, __featureIndex++, 0));
    __value = r * r;
    __result.addFeature(new RealArrayStringFeature(this.containingPackage, this.name, "", __value, __featureIndex++, 0));
    __value = Math.sqrt(r);
    __result.addFeature(new RealArrayStringFeature(this.containingPackage, this.name, "", __value, __featureIndex++, 0));

    for (int __i = 0; __i < __result.featuresSize(); ++__i)
      __result.getFeature(__i).setArrayLength(__featureIndex);

    return __result;
  }

  public double[] realValueArray(Object __example)
  {
    return classify(__example).realValueArray();
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof TemporalEventPair[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'EmbeddingIP(TemporalEventPair)' defined on line 139 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "EmbeddingIP".hashCode(); }
  public boolean equals(Object o) { return o instanceof EmbeddingIP; }
}

