package diego.weka.learners.smoReg;

import diego.weka.incremental.DataSet;
import diego.weka.incremental.DataSetBuildException;
import diego.weka.incremental.WekaDataSet;
import diego.weka.learners.Learner;
import diego.weka.learners.WekaFeatureSelector;
import weka.classifiers.Classifier;
import weka.classifiers.functions.SMOreg;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class SmoRegWekaFeatureSelector extends WekaFeatureSelector {

   public SmoRegWekaFeatureSelector(WekaDataSet inputDS) {
      super(inputDS);
   }

   @Override
   protected Learner buildAndTrain(DataSet d) {
      try {
         return new SmoRegLearner(d);
      } catch (Exception e) {
         e.printStackTrace();  // TODO: Customise this generated block
         throw new DataSetBuildException(e.getMessage());
      }
   }

   @Override
   protected Classifier vanillaClassifier() throws Exception {
      SMOreg base = new SMOreg();
      base.setOptions(weka.core.Utils.splitOptions(SmoRegLearner.prettyParameters()));
      return base;
   }
}
