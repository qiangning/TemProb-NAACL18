// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D4D4BCA0280441DF59340182294B6DA541DCEA23A2A61211E3E6531A3327DB5111DFB735D6C5D1ECBB4DD61C424D7C2C69955B32ECB5F6B4573693BA4DD9C8CA23DC0A6C7C86087129CFA4584A704D6B4AF7C1367678F3E7FF2F36E42F02231A8D90376F4FCF8172BC0F4B118679E16CE064D9580B223769B839380C7772D299690DB5423B9CC77A6D3D3E45D67FA4D23AE12087B116D6ECE77AF9AE302280FD826CB1FEF02B3EBDDC4D000000

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


public class ModalVerbs extends Classifier
{
  public ModalVerbs()
  {
    containingPackage = "lbjava";
    name = "ModalVerbs";
  }

  public String getInputType() { return "datastruct.TemporalEventPair"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof TemporalEventPair))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'ModalVerbs(TemporalEventPair)' defined on line 61 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    TemporalEventPair ep = (TemporalEventPair) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    Object[] modelVerbs = ep.modelverbs_between.toArray();
    for (int i = 0; i < modelVerbs.length; i++)
    {
      __id = "" + ("MODALVERB_BETWEEN:" + (String) modelVerbs[i]);
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
      System.err.println("Classifier 'ModalVerbs(TemporalEventPair)' defined on line 61 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "ModalVerbs".hashCode(); }
  public boolean equals(Object o) { return o instanceof ModalVerbs; }
}

