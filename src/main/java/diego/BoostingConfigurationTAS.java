package diego;

import eu.cloudtm.Configuration.BoostingConfiguration;

/**
 * @author Diego Didona
 * @email didona@gsd.inesc-id.pt
 * @since 02/04/14
 */
public class BoostingConfigurationTAS extends BoostingConfiguration {

   private final static String arff = "conf/tas/Boosting_tas.properties";

   public BoostingConfigurationTAS() {
      super(arff);
   }

   public static BoostingConfiguration getInstance() {
      return (myself == null) ? myself = new BoostingConfigurationTAS() : myself;
   }
}
