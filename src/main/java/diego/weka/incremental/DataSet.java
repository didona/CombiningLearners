package diego.weka.incremental;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public abstract class DataSet {

   protected File f;
   protected final static Log log = LogFactory.getLog(DataSet.class);
   private final static boolean trace = log.isTraceEnabled();
   private final static boolean info = log.isInfoEnabled();

   public DataSet(File f) {
      this.f = f;
   }

   protected DataSet() {
   }

   protected abstract void buildDataSet();

   //Merge two sets together, i.e.,
   protected abstract void merge(DataSet dataSet);

   protected abstract WekaDataSet subDataSet(int percentage);

   protected abstract void remove(DataSet d);

   protected abstract long size();

   protected final void info(String s) {
      if (info)
         log.info(s);
   }

   protected final void trace(String s) {
      if (trace)
         log.trace(s);
   }

}
