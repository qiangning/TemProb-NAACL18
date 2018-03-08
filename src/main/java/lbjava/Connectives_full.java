// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000059E814B028040158FFAC0140B2294874DA349C60D543C4AB44889D85B16BAC83A011DF7F6F691511D968973F6EDBFE0AA9C9091780145A536ECAA3C62D2AD2B4190E5AEA82B25678A97599220CAD289C80E601DEFC6CABDD14EFC7289A9BB3D312DD361511A3C5DC882BBA0BC703B3805A914917F8D73362DF807A44D74E391DD6BD2353D0AE60160144168238469B199EC5E22A85A7300B14CA9949E3A5DF08DAAD1870C4D2AF07FF849EEB36665032DF149EEF6CC9D22191F75A47F513FEF0BDC3D05648100000

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


public class Connectives_full extends Classifier
{
  public Connectives_full()
  {
    containingPackage = "lbjava";
    name = "Connectives_full";
  }

  public String getInputType() { return "datastruct.TemporalEventPair"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof TemporalEventPair))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'Connectives_full(TemporalEventPair)' defined on line 49 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    TemporalEventPair ep = (TemporalEventPair) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    Object[] connectives = ep.connectives_before.toArray();
    for (int i = 0; i < connectives.length; i++)
    {
      __id = "" + ("CONNECTIVE_BEFORE:" + (String) connectives[i]);
      __value = "true";
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    }
    Object[] connectives2 = ep.connectives_after.toArray();
    for (int i = 0; i < connectives2.length; i++)
    {
      __id = "" + ("CONNECTIVE_AFTER:" + (String) connectives2[i]);
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
      System.err.println("Classifier 'Connectives_full(TemporalEventPair)' defined on line 49 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "Connectives_full".hashCode(); }
  public boolean equals(Object o) { return o instanceof Connectives_full; }
}

