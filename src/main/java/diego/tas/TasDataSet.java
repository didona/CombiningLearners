package diego.tas;

import eu.cloudtm.Configuration.BoostingConfiguration;
import eu.cloudtm.boosting.Dataset;

/**
 * @author Diego Didona
 * @email didona@gsd.inesc-id.pt
 * @since 02/04/14
 */
public class TasDataSet extends Dataset {

   public TasDataSet(String Directory_path) throws Exception {
      super(Directory_path);
   }

   protected BoostingConfiguration boostConfig() {
      return BoostingConfigurationTAS.getInstance();
   }

}
