// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000055C8BCA02C030154F756615141228EED59A54BB0B60254791263324072F029A55FFED4DAB0DDC5E2776EC1B6E98A7204EB5542115680238F4F03E46BEDD0113A45445E2A82693D60D60F15159D3ABE8B231103CC0653785B159A1C869188C79DA94D1386CFD0DD859F15C7C4C06395A8D676A7C4C0E0E5B2A316CB4EE249541F27207877EB745A74CAAA5E9BAAFF0497DB31DF2DFD167E943F8E346BDB101583F6F36D000000

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


public class local_ee_pp_poswin_conn_corr_allE0$$1 extends Classifier
{
  private static final BiasTerm __BiasTerm = new BiasTerm();
  private static final SentDist __SentDist = new SentDist();
  private static final TokenDist __TokenDist = new TokenDist();
  private static final POS_corr __POS_corr = new POS_corr();
  private static final Connectives __Connectives = new Connectives();
  private static final ModalVerbs __ModalVerbs = new ModalVerbs();
  private static final SameSynSet __SameSynSet = new SameSynSet();
  private static final PPHead __PPHead = new PPHead();
  private static final POS_IN_WIN __POS_IN_WIN = new POS_IN_WIN();
  private static final Connectives_full __Connectives_full = new Connectives_full();
  private static final ModalVerbs_full __ModalVerbs_full = new ModalVerbs_full();
  private static final GoldProps __GoldProps = new GoldProps();

  public local_ee_pp_poswin_conn_corr_allE0$$1()
  {
    containingPackage = "lbjava";
    name = "local_ee_pp_poswin_conn_corr_allE0$$1";
  }

  public String getInputType() { return "datastruct.TemporalEventPair"; }
  public String getOutputType() { return "mixed%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof TemporalEventPair))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'local_ee_pp_poswin_conn_corr_allE0$$1(TemporalEventPair)' defined on line 311 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    FeatureVector __result;
    __result = new FeatureVector();
    __result.addFeature(__BiasTerm.featureValue(__example));
    __result.addFeature(__SentDist.featureValue(__example));
    __result.addFeature(__TokenDist.featureValue(__example));
    __result.addFeatures(__POS_corr.classify(__example));
    __result.addFeatures(__Connectives.classify(__example));
    __result.addFeatures(__ModalVerbs.classify(__example));
    __result.addFeature(__SameSynSet.featureValue(__example));
    __result.addFeatures(__PPHead.classify(__example));
    __result.addFeatures(__POS_IN_WIN.classify(__example));
    __result.addFeatures(__Connectives_full.classify(__example));
    __result.addFeatures(__ModalVerbs_full.classify(__example));
    __result.addFeatures(__GoldProps.classify(__example));
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof TemporalEventPair[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'local_ee_pp_poswin_conn_corr_allE0$$1(TemporalEventPair)' defined on line 311 of local_ee.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "local_ee_pp_poswin_conn_corr_allE0$$1".hashCode(); }
  public boolean equals(Object o) { return o instanceof local_ee_pp_poswin_conn_corr_allE0$$1; }

  public java.util.LinkedList getCompositeChildren()
  {
    java.util.LinkedList result = new java.util.LinkedList();
    result.add(__BiasTerm);
    result.add(__SentDist);
    result.add(__TokenDist);
    result.add(__POS_corr);
    result.add(__Connectives);
    result.add(__ModalVerbs);
    result.add(__SameSynSet);
    result.add(__PPHead);
    result.add(__POS_IN_WIN);
    result.add(__Connectives_full);
    result.add(__ModalVerbs_full);
    result.add(__GoldProps);
    return result;
  }
}

