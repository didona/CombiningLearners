package diego.weka.learners.M5Rules;

import diego.weka.incremental.DataSet;
import diego.weka.learners.WekaLearner;
import weka.classifiers.Classifier;
import weka.classifiers.rules.M5Rules;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class M5RLearner extends WekaLearner {

   private final static long M = 4;

   public M5RLearner(DataSet d) throws Exception {
      super(d);
   }

   public M5RLearner(DataSet d, String features) throws Exception {
      super(d, features);
   }

   @Override
   protected Classifier buildAndTrainClassifier() throws Exception {
      M5Rules m5r = new M5Rules();
      m5r.setOptions(weka.core.Utils.splitOptions(prettyParameters()));
      m5r.buildClassifier(instances());
      return m5r;
   }

   static String prettyParameters() {
      return "-M " + M;
   }
}
