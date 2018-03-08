package util;

import edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet;
import edu.illinois.cs.cogcomp.nlp.classifier.lbj.perceptron.LearningObj;
/**
 * Created by qning2 on 1/21/17.
 */
public interface ScoringFunc<LearningObj> {
    ScoreSet scores(LearningObj obj);
    String discreteValue(LearningObj obj);
}
