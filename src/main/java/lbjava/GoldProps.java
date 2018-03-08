// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D7FC14B0280401500EFB2F0120569427F8A678809EA24ED4424272B50B55677BE21EF7F65B2050BE6BCCCBD7F13590D76546865834BD459AA6B3DE6647BEA555639C384A94B41A04D97887D872E86490957E50A3B9C00B5BB1F968850E7D462BDDE357DB8024A180BBCD448B4BA0EAD902E7F71FB1295B9BAD9036E9D24D42531C942839306636C0E483CB6C07E2A04803AEE411AF9BDC7263F9AD328F1CFBA2FF5A2F5059FC5D5AEBF375C265E878CF13AF71E67580E468100000

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


public class GoldProps extends Classifier
{
  public GoldProps()
  {
    containingPackage = "lbjava";
    name = "GoldProps";
  }

  public String getInputType() { return "datastruct.TemporalEventPair"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof TemporalEventPair))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'GoldProps(TemporalEventPair)' defined on line 155 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    TemporalEventPair ep = (TemporalEventPair) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    String[] prop1 = ep.event1.getTACP();
    int i = 0;
    for (; i < prop1.length; i++)
    {
      __id = "" + ("E1_" + i + ":" + prop1[i]);
      __value = "true";
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    }
    String[] prop2 = ep.event2.getTACP();
    for (i = 0; i < prop2.length; i++)
    {
      __id = "" + ("E2_" + i + ":" + prop2[i]);
      __value = "true";
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    }
    for (i = 0; i < prop1.length; i++)
    {
      __id = "" + ("E1_E2_" + i + ":" + prop1[i] + ":" + prop2[i]);
      __value = "true";
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof TemporalEventPair[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'GoldProps(TemporalEventPair)' defined on line 155 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "GoldProps".hashCode(); }
  public boolean equals(Object o) { return o instanceof GoldProps; }
}

