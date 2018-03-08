// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D8E81CA02C04C044F7524148692619E1DAE1A2E6F4A45AA871119DAD45756DD5943A022EFBBB8E54151D358942333FA25DCA9091B5036B594D374A2B955D74DADF91EEF0694A617243C319A800F0104FAD0718CBCD1EA971B48DBDA0F1E18EBB37477D7AB764985B52C88D6A442FCE709083D0EB23C0ACD3772173A7F41019643B1EDAB578160EA3A143D02873EC789E86E2A81CA6022BCB01D5F0240F7AC4ACC62872BF24D21AB0C474C40EAF9003E732495332DFF081FF42C43B9982EB106CFA487DB1D6447EEA97100000

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


public class ModalVerbs_full extends Classifier
{
  public ModalVerbs_full()
  {
    containingPackage = "lbjava";
    name = "ModalVerbs_full";
  }

  public String getInputType() { return "datastruct.TemporalEventPair"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof TemporalEventPair))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'ModalVerbs_full(TemporalEventPair)' defined on line 69 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    TemporalEventPair ep = (TemporalEventPair) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    Object[] modelVerbs = ep.modelverbs_before.toArray();
    for (int i = 0; i < modelVerbs.length; i++)
    {
      __id = "" + ("MODALVERB_BEFORE:" + (String) modelVerbs[i]);
      __value = "true";
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    }
    Object[] modelVerbs2 = ep.modelverbs_after.toArray();
    for (int i = 0; i < modelVerbs2.length; i++)
    {
      __id = "" + ("MODALVERB_AFTER:" + (String) modelVerbs2[i]);
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
      System.err.println("Classifier 'ModalVerbs_full(TemporalEventPair)' defined on line 69 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "ModalVerbs_full".hashCode(); }
  public boolean equals(Object o) { return o instanceof ModalVerbs_full; }
}

