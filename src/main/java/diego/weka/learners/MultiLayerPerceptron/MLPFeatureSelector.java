package diego.weka.learners.MultiLayerPerceptron;

import diego.weka.incremental.DataSet;
import diego.weka.incremental.DataSetBuildException;
import diego.weka.incremental.WekaDataSet;
import diego.weka.learners.Learner;
import diego.weka.learners.WekaFeatureSelector;
import weka.classifiers.Classifier;
import weka.classifiers.functions.MultilayerPerceptron;
/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class MLPFeatureSelector extends WekaFeatureSelector {

   public MLPFeatureSelector(WekaDataSet inputDS) {
      super(inputDS);
   }

   @Override
   protected Learner buildAndTrain(DataSet d) {
      try {
         return new MLPLearner(d);
      } catch (Exception e) {
         e.printStackTrace();  // TODO: Customise this generated block
         throw new DataSetBuildException(e.getMessage());
      }
   }

   @Override
   protected Classifier vanillaClassifier() throws Exception {
      MultilayerPerceptron mlp = new MultilayerPerceptron();
      mlp.setOptions(weka.core.Utils.splitOptions(MLPLearner.prettyParameters()));
      return mlp;
   }
}
