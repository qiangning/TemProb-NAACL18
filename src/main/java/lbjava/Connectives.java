// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D4D81CA028048148F55601205429ECA67829D3471D029A38888ADF9D68DEACAF76448FEEDED2F430FDC7CCC55E8DA126A5121DA415BC272A1DDC9E9386357F26225C7CA5A10D0E12E53EB8CA9785BA821DEFD7C6D6F1C2045D01FB98450CA776C4DF17DB8073D60EA45C0965FD44632E5E640D39AE8FE69BFEB76F76425321C942B435194E783B8A6F22FB801968E0C78B72632557ED271A0952240B971548913FF0ABB054C0AD000000

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


public class Connectives extends Classifier
{
  public Connectives()
  {
    containingPackage = "lbjava";
    name = "Connectives";
  }

  public String getInputType() { return "datastruct.TemporalEventPair"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof TemporalEventPair))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'Connectives(TemporalEventPair)' defined on line 41 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    TemporalEventPair ep = (TemporalEventPair) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    Object[] connectives = ep.connectives_between.toArray();
    for (int i = 0; i < connectives.length; i++)
    {
      __id = "" + ("CONNECTIVE_BETWEEN:" + (String) connectives[i]);
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
      System.err.println("Classifier 'Connectives(TemporalEventPair)' defined on line 41 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "Connectives".hashCode(); }
  public boolean equals(Object o) { return o instanceof Connectives; }
}

