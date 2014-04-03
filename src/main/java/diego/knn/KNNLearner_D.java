package diego.knn;

import eu.cloudtm.Configuration.KnnConfiguration;
import eu.cloudtm.Knn.DataSets;
import eu.cloudtm.Knn.Learner;
import eu.cloudtm.autonomicManager.oracles.Oracle;
import org.apache.log4j.Logger;
import weka.core.DistanceFunction;
import weka.core.neighboursearch.LinearNNSearch;

import java.lang.reflect.Method;

/**
 * @author Diego Didona
 * @email didona@gsd.inesc-id.pt
 * @since 03/04/14
 */
public class KNNLearner_D extends Learner implements Oracle {
   static Logger logger = Logger.getLogger(KNNLearner_D.class);
   private LinearNNSearch KNN;
   private int NumNeighbours;
   private String ConsideredOutOracle;
   private double cutoff;

   private String[] getconfiguration() {

      String[] param = new String[5];
      param[0] = KnnConfiguration.getInstance().getDistanceFunction();
      param[1] = KnnConfiguration.getInstance().getIsNormalized();
      param[2] = KnnConfiguration.getInstance().getknearestneighbourg();
      param[3] = KnnConfiguration.getInstance().getTarget();
      param[4] = KnnConfiguration.getInstance().getCutoff();

      return param;


   }

   private String[] buildOptions(KNNConfig config) {
      return new String[]{config.getIsNormalized()};
   }

   private DistanceFunction buildDistanceFunction(KNNConfig config) throws Exception {
      Method method;
      Class c = Class.forName("weka.core." + config.getDistanceFunction());
      Object distanceFunc = c.newInstance();
      method = distanceFunc.getClass().getMethod("setOptions", String[].class);
      method.invoke(distanceFunc, (Object) buildOptions(config));
      return (DistanceFunction) distanceFunc;
   }


   public KNNLearner_D() throws Exception {

      KNNConfig config = (KNNConfig) KNNConfig.getInstance();


      NumNeighbours = Integer.parseInt(config.getknearestneighbourg());
      ConsideredOutOracle = config.getTarget();
      cutoff = Double.parseDouble(config.getCutoff());
      KNN = new LinearNNSearch();

      DistanceFunction distFunc = buildDistanceFunction(config);
      KNN.setDistanceFunction(distFunc);

      if (DataSets.ARFFDataSet != null) {
         this.m_Training = DataSets.ARFFDataSet;
      } else {
         logger.error("--" + "Datasets Not instanziated");
         throw new InstantiationException("Datasets Not instanziated");
      }
   }


}
