// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000059F81CA63C030144F7560341C6C4D4DA3AD9C1A4809BA1A9B9135C42B1752C19C84A4F252FFED59DD694D12180ABC0BBF64CEBDB47B3B4E9E90B13DFEB2B6607B23A5D94FECB43AD5CB5A3E06C6BDFAF394BFAA5961434285C33EB0F6EDA4DD5DD060E65E852F623A085E9574E7BFABAA2E4A484DE12979F252E06C226E90613552BE9477EF387296A90F781055C8A44A8CFF075758BA90774AD112A57EFE11712492A139A09A8096CA5D2B99D0453820EDE98A4C99FDF3F0171E12E2D36CB6E744E7D04C36602E69188BB6026E6026E607EF698EB49716C100000

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


public class GoldPropsConjunctions extends Classifier
{
  public GoldPropsConjunctions()
  {
    containingPackage = "lbjava";
    name = "GoldPropsConjunctions";
  }

  public String getInputType() { return "datastruct.TemporalEventPair"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof TemporalEventPair))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'GoldPropsConjunctions(TemporalEventPair)' defined on line 172 of local_ee.lbj received '" + type + "' as input.");
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
      int j = i + 1;
      for (; j < prop1.length; j++)
      {
        __id = "" + ("E1_" + i + "_" + j + ":" + prop1[i] + ":" + prop1[j]);
        __value = "true";
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
      }
    }
    String[] prop2 = ep.event2.getTACP();
    for (i = 0; i < prop2.length; i++)
    {
      int j = i + 1;
      for (; j < prop2.length; j++)
      {
        __id = "" + ("E2_" + i + "_" + j + ":" + prop2[i] + ":" + prop2[j]);
        __value = "true";
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
      }
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof TemporalEventPair[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'GoldPropsConjunctions(TemporalEventPair)' defined on line 172 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "GoldPropsConjunctions".hashCode(); }
  public boolean equals(Object o) { return o instanceof GoldPropsConjunctions; }
}

