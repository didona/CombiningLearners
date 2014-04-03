package diego.weka.learners;

import diego.weka.incremental.DataSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public abstract class Learner {

   protected double ten_fold_error;
   protected String features;
   protected DataSet trainingSet;
   protected final static Log log = LogFactory.getLog(Learner.class);
   private final static boolean trace = log.isTraceEnabled();
   private final static boolean info = log.isInfoEnabled();


   public double getTen_fold_error() {
      return ten_fold_error;
   }


   public Learner(DataSet d) {
      this.trainingSet = d;
   }

   public Learner(DataSet d, String features) throws Exception {
      this.trainingSet = filterTrainingSetWithFeatures(d, features);
      this.features = features;
   }

   public abstract double wekaMeanAbsoluteRelativeError(DataSet d) throws Exception;

   public abstract double mape(DataSet d) throws Exception;

   protected abstract DataSet filterTrainingSetWithFeatures(DataSet d, String features) throws Exception;

   protected void info(String s) {
      if (info)
         log.info(s);
   }

   protected void trace(String s) {
      if (trace)
         log.trace(s);
   }

}
