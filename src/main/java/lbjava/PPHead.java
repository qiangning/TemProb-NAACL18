// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D7F813B02C030158FFAC9101A14CA4B365792818E26D04D5C9A40D7A61A6DA94AE22EF7735780E01ACD87FDB77FDD9B1372D0B893949504D93A30EEDF34BA65CB0D959AA1D48E93D6694F6238EC088984A692BE244EBDC81D2CDA36C8C621F51656F5FD6C61E49195D306DE349603496834DC582A958B836C3705DA98859BAC917EE4B6487A9D0F5BF58DFF4E40BAC772CB5E1545CCBFC780DA3C620F27FFF4BB9F2E9CE4F2F95100000

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


public class PPHead extends Classifier
{
  public PPHead()
  {
    containingPackage = "lbjava";
    name = "PPHead";
  }

  public String getInputType() { return "datastruct.TemporalEventPair"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof TemporalEventPair))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'PPHead(TemporalEventPair)' defined on line 118 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    TemporalEventPair ep = (TemporalEventPair) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    __id = "" + ("E1_PP_HEAD:" + ep.event1.getPp_head());
    __value = "true";
    __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    __id = "" + ("E2_PP_HEAD:" + ep.event2.getPp_head());
    __value = "true";
    __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    if (!ep.event1.getPp_head().equals("N/A"))
    {
      if (ep.event1.getPp_head().equals(ep.event2.getPp_head()))
      {
        __id = "E1E2_SAME_PP_HEAD:YES";
        __value = "true";
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
      }
      else
      {
        __id = "E1E2_SAME_PP_HEAD:NO";
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
      System.err.println("Classifier 'PPHead(TemporalEventPair)' defined on line 118 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "PPHead".hashCode(); }
  public boolean equals(Object o) { return o instanceof PPHead; }
}

