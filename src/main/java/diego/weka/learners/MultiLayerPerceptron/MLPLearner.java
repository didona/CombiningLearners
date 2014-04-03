package diego.weka.learners.MultiLayerPerceptron;

import diego.weka.incremental.DataSet;
import diego.weka.learners.WekaLearner;
import weka.classifiers.Classifier;
import weka.classifiers.functions.MultilayerPerceptron;

/**
 * // TODO: Document this
 *
 * @author Diego Didona
 * @since 4.0 http://weka.sourceforge.net/doc.dev/weka/classifiers/functions/MultilayerPerceptron.html
 */
public class MLPLearner extends WekaLearner {

   private final static long epochs = 1000;
   private final static double learningRate = 0.3;
   private final static double momentum = 0.2;
   private final static Object hiddenLayers = "a";
   private final static long validationThreshold = 20;
   private final static long validationSetSizePercentage = 0;

   public MLPLearner(DataSet d) throws Exception {
      super(d);
   }

   public MLPLearner(DataSet d, String features) throws Exception {
      super(d, features);
   }

   @Override
   protected Classifier buildAndTrainClassifier() throws Exception {
      MultilayerPerceptron mlp = new MultilayerPerceptron();
      mlp.setOptions(weka.core.Utils.splitOptions(prettyParameters()));
      mlp.buildClassifier(instances());
      return mlp;
   }

   static String prettyParameters() {
      return "-L " + learningRate + " -M " + momentum + " -N " + epochs + " -V " + validationSetSizePercentage + " -S 0 -E " + validationThreshold + " -H " + hiddenLayers;
   }
}
