package diego.knn;

import eu.cloudtm.Configuration.KnnConfiguration;

/**
 * @author Diego Didona
 * @email didona@gsd.inesc-id.pt
 * @since 03/04/14
 */
public class KNNConfig extends KnnConfiguration {
   private final static String conf = "conf/diego/K-NN/KNN.properties";

   public KNNConfig(String file) {
      super(file);
   }

   public KNNConfig() {
      super(conf);
   }

   public static KnnConfiguration getInstance() {
      return (myself == null) ? myself = new KNNConfig() : myself;

   }

}
