package diego.knn;

import eu.cloudtm.DataUtility.DataConverter;
import eu.cloudtm.Knn.DataSets;
import eu.cloudtm.autonomicManager.oracles.InputOracle;
import eu.cloudtm.autonomicManager.oracles.Oracle;
import eu.cloudtm.autonomicManager.oracles.OutputOracle;
import eu.cloudtm.autonomicManager.oracles.exceptions.OracleException;
import org.apache.log4j.Logger;
import weka.core.DistanceFunction;
import weka.core.Instances;
import weka.core.neighboursearch.LinearNNSearch;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author Diego Didona
 * @email didona@gsd.inesc-id.pt
 * @since 03/04/14
 */
public class KNNLearner_D extends Learner_D implements Oracle {
   static Logger logger = Logger.getLogger(KNNLearner_D.class);
   private LinearNNSearch KNN;
   private int numNeighbours;
   private String targetFeature;
   private double cutoff;

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


      numNeighbours = Integer.parseInt(config.getknearestneighbourg());
      targetFeature = config.getTarget();
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


   @Override
   public OutputOracle forecast(InputOracle io) throws OracleException {

      double AVGrmse = Double.MAX_VALUE;
      Oracle best = null;

      try {

         Instances filtered = filterInstancesIfNeeded(m_Training);
         KNN.setInstances(filtered);

         m_TestSet = DataConverter.FromInputOracleToInstance(io);
         logger.info("PREDICT TARGET VALUE FOR  : " + m_TestSet.toStringNoWeight());
         System.out.println(m_TestSet);
         Neighbourshood = KNN.kNearestNeighbours(m_TestSet, numNeighbours);//>k neighbours are returned if there are more than one neighbours at the kth boundary.
         distances = KNN.getDistances();


         RMSE = computeRMSE(this.targetFeature);


         double actual;
         for (Map.Entry<Oracle, Double[]> entry : RMSE.entrySet()) {


            // actual=(entry.getValue()[0]*VarianceRMSE(entry.getValue())[0]+entry.getValue()[1]*VarianceRMSE(entry.getValue())[1])/2;
            actual = (entry.getValue()[0] + entry.getValue()[1]) / 2;
            if (actual <= AVGrmse) {
               AVGrmse = actual;
               best = entry.getKey();

            }
         }

         logger.info("ORACLE SELECTED FOR PREDICTION : " + best.toString().split("@")[0]);

         return best.forecast(io);

      } catch (Exception ex) {
         logger.error("forecast error  " + ex);
         throw new OracleException(ex);
      }
   }


   protected boolean considerInstances(int i) {
      return distances[i] < cutoff;
   }
}
