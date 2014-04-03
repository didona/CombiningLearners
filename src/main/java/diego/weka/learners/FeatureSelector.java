package diego.weka.learners;

import diego.weka.incremental.DataSet;
import diego.weka.learners.smoReg.SmoRegLearner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;

/**
 * @author Diego Didona
 * @email didona@gsd.inesc-id.pt
 * @since 12/03/14
 */
public abstract class FeatureSelector<D extends DataSet> {
   protected final static Log log = LogFactory.getLog(SmoRegLearner.class);
   protected final static boolean trace = log.isTraceEnabled();
   protected final static boolean info = log.isInfoEnabled();
   protected static File testInputFile = new File("data/weka/train.arff");
   protected D dataset;

   public FeatureSelector(D dataset) {
      this.dataset = dataset;
   }

   protected void info(String s) {
      if (info)
         log.info(s);
   }

   protected void trace(String s) {
      if (trace)
         log.trace(s);
   }
}
