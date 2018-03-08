// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000B2A4D4CC98E8550FC267A4D4BCF2A45FCC37ECF2A282D2E8F2B4C2ACC4C29CCCFC3D8094DCD28CF2A4CC17D2B4DCB29084CCC22584D20D450B1D558A6582E4DCB2E455034D3384150D20A8AE52B1A28EB2860481AD061032D4CD236D05DFEB985291A75C585452A18FD420A68A500012B98A20FA000000

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


public class IsBeforeInCorpus_variation extends Classifier
{
  public IsBeforeInCorpus_variation()
  {
    containingPackage = "lbjava";
    name = "IsBeforeInCorpus_variation";
  }

  public String getInputType() { return "datastruct.TemporalEventPair"; }
  public String getOutputType() { return "real[]"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof TemporalEventPair))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'IsBeforeInCorpus_variation(TemporalEventPair)' defined on line 105 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    TemporalEventPair ep = (TemporalEventPair) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    int __featureIndex = 0;
    double __value;

    __value = 1.0d * ep.c1 / (ep.c1 + ep.c2) * ep.c1 / (ep.c1 + ep.c2);
    __result.addFeature(new RealArrayStringFeature(this.containingPackage, this.name, "", __value, __featureIndex++, 0));
    __value = Math.sqrt(1.0d * ep.c1 / (ep.c1 + ep.c2));
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
      System.err.println("Classifier 'IsBeforeInCorpus_variation(TemporalEventPair)' defined on line 105 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "IsBeforeInCorpus_variation".hashCode(); }
  public boolean equals(Object o) { return o instanceof IsBeforeInCorpus_variation; }
}

