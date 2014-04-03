package diego.weka.learners.M5Rules;

import diego.weka.incremental.DataSet;
import diego.weka.incremental.DataSetBuildException;
import diego.weka.incremental.WekaDataSet;
import diego.weka.learners.Learner;
import diego.weka.learners.WekaFeatureSelector;
import weka.classifiers.Classifier;
import weka.classifiers.rules.M5Rules;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class M5RFeatureSelector extends WekaFeatureSelector {

   public M5RFeatureSelector(WekaDataSet inputDS) {
      super(inputDS);
   }

   @Override
   protected Learner buildAndTrain(DataSet d) {
      try {
         return new M5RLearner(d);
      } catch (Exception e) {
         e.printStackTrace();  // TODO: Customise this generated block
         throw new DataSetBuildException(e.getMessage());
      }
   }

   @Override
   protected Classifier vanillaClassifier() throws Exception {
      M5Rules m5r = new M5Rules();
      m5r.setOptions(weka.core.Utils.splitOptions(M5RLearner.prettyParameters()));
      return m5r;
   }
}
