// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000057EC14B0280401500EFB2F0F4AC254ADDA5B3A70F2BD2525144C22535C249ACEE22148FFDB1301AB47A1ED73F816EA6DF5C150A3D91A73B335A2378255C51D3BB6D5DF82A7A628EADA3057902F91ED8F21825CA9D6E436A45E4A9DF8662169DC23D242E6DA34CC28C1B4E12442C7BC353E9019ED61B731140F650223CF7CA6C449ECFE414B3AE57BE36B98C9AFAA8365F7BFA8E516AEF081E3DCB2B3010E000000

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


public class POS_IN_WIN extends Classifier
{
  public POS_IN_WIN()
  {
    containingPackage = "lbjava";
    name = "POS_IN_WIN";
  }

  public String getInputType() { return "datastruct.TemporalEventPair"; }
  public String getOutputType() { return "discrete[]"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof TemporalEventPair))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'POS_IN_WIN(TemporalEventPair)' defined on line 129 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    TemporalEventPair ep = (TemporalEventPair) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    int __featureIndex = 0;
    String __value;

    Event e1 = ep.event1;
    Event e2 = ep.event2;
    int i = 0;
    for (; i < 3; i++)
    {
      __value = "" + ("PREV_" + i + "_POS:" + e1.getPrevPos(i));
      __result.addFeature(new DiscreteArrayStringFeature(this.containingPackage, this.name, "", __value, valueIndexOf(__value), (short) 0, __featureIndex++, 0));
      __value = "" + ("NEXT_" + i + "_POS:" + e1.getNextPos(i));
      __result.addFeature(new DiscreteArrayStringFeature(this.containingPackage, this.name, "", __value, valueIndexOf(__value), (short) 0, __featureIndex++, 0));
    }

    for (int __i = 0; __i < __result.featuresSize(); ++__i)
      __result.getFeature(__i).setArrayLength(__featureIndex);

    return __result;
  }

  public String[] discreteValueArray(Object __example)
  {
    return classify(__example).discreteValueArray();
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof TemporalEventPair[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'POS_IN_WIN(TemporalEventPair)' defined on line 129 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "POS_IN_WIN".hashCode(); }
  public boolean equals(Object o) { return o instanceof POS_IN_WIN; }
}

