package util;
import edu.illinois.cs.cogcomp.lbjava.learn.SupportVectorMachine;

public class ParamLBJ {
    public static double[] learningRates = new double[] { 0.005,0.01};
    public static double[] thicknesses = new double[] { 2.0,3.0};
    public static double[] negVagSamplingRates = new double[] {0.3,0.5,0.7,0.9};
    public static int[] learningRounds = new int[] { 100,200,400};

    public static double eeLearningRate = 0.001;
    public static double eeThickness = 1;

}
