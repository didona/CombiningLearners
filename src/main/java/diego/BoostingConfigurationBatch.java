package diego;

import eu.cloudtm.Configuration.BoostingConfiguration;

/**
 * @author Diego Didona
 * @email didona@gsd.inesc-id.pt
 * @since 02/04/14
 */
public class BoostingConfigurationBatch extends BoostingConfiguration {

   private final static String arff =  "conf/batch/Boosting_batch.properties";

   public BoostingConfigurationBatch() {
      super(arff);
   }

   public static BoostingConfiguration getInstance() {
      return (myself == null) ? myself = new BoostingConfigurationBatch() : myself;
   }
}
