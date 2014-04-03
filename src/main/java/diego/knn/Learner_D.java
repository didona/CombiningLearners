package diego.knn;

import eu.cloudtm.Knn.DataSets;
import eu.cloudtm.autonomicManager.oracles.Oracle;
import eu.cloudtm.autonomicManager.oracles.OutputOracle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import weka.core.Instance;
import weka.core.Instances;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author Diego Didona
 * @email didona@gsd.inesc-id.pt
 * @since 03/04/14
 */
public abstract class Learner_D {
   protected Instances m_Training = null;
   protected String m_TestSetFile = null;
   protected Instance m_TestSet = null;
   protected String m_TrainingFile = null;
   protected HashMap<Oracle, Double[]> RMSE;
   protected Instances Neighbourshood;
   protected double[] distances;
   protected double[][] SquaredErrors;


   private final static Log logger = LogFactory.getLog(Learner_D.class);


   protected Instances filterInstancesIfNeeded(Instances toFilter) {
      //With new samples we need to do nothing
      //In case, we can re-integrate Ennio's methods for RD and Isolation level
      return toFilter;
   }

   /**
    * Valid value for Parameter are: throughput abortRate responseTime everyone separated by a space
    */

   //I would not choose a learner basing on the average among the errors on different features!
   //Every feature should have its own learner
   protected HashMap<Oracle, Double[]> computeRMSE(String Parameter) throws Exception {
      HashMap<Oracle, Double[]> rmse = new HashMap<Oracle, Double[]>();
      Double[] RMSe;
      double errorOutputRO;
      double errorOutputWO;

      Method method;
      OutputOracle outputValidationSet;
      OutputOracle outputOracle;
      StringTokenizer token;
      String outputname;

      SquaredErrors = new double[2][Neighbourshood.numInstances()];

      for (Map.Entry<Oracle, HashMap<Instance, OutputOracle>> entry : DataSets.predictionResults.entrySet()) {
         double consideredInstances = 0;
         double SEOutputRO = 0D;
         double SEOutputWO = 0D;
         for (int i = 0; i < Neighbourshood.numInstances(); i++) {

            //Get the i-th instance from the neighborhood
            Instance inst = DataSets.InstancesMap.get(Neighbourshood.instance(i).toStringNoWeight());
            //Take the output from the validationSet (i.e., I guess, the REAL value)
            outputValidationSet = DataSets.ValidationSet.get(inst);
            //Take the --cached-- output of the Oracle
            outputOracle = entry.getValue().get(inst);

            logger.info("Instance :" + inst + "\n" + "validationOutput= " + outputValidationSet + "\n" + "Oracle Output= " + outputOracle + "\n" + "Distance= " + distances[i]);
            token = new StringTokenizer(Parameter);

            while (token.hasMoreTokens()) {

               outputname = token.nextToken();

               method = OutputOracle.class.getMethod(outputname, int.class);

               errorOutputRO = (Double) method.invoke(outputValidationSet, 0) - (Double) method.invoke(outputOracle, 0);
               errorOutputWO = (Double) method.invoke(outputValidationSet, 1) - (Double) method.invoke(outputOracle, 1);

               if (considerInstances(i)) {//does not consider in the accuracy computation  points too distant
                  consideredInstances++;
                  SEOutputRO = SEOutputRO + Math.pow(errorOutputRO, 2);
                  logger.info("error on " + outputname + "RO prediction for " + entry.getKey().toString().split("@")[0] + " = " + errorOutputRO);


                  SEOutputWO = SEOutputWO + Math.pow(errorOutputWO, 2);
                  logger.info("error on " + outputname + "WO prediction for " + entry.getKey().toString().split("@")[0] + " = " + errorOutputWO);


                  SquaredErrors[0][i] = SEOutputRO;
                  SquaredErrors[1][i] = SEOutputWO;


               } else {
                  logger.info("error on " + outputname + "WO prediction for " + entry.getKey().toString().split("@")[0] + " = " + errorOutputWO);
                  logger.info("error on " + outputname + "RO prediction for " + entry.getKey().toString().split("@")[0] + " = " + errorOutputRO);
                  logger.info("point not considered during accuracy computation due to the high AVG contribution (" + distances[i] + "> 1* ");
               }

            }

         }
         RMSe = new Double[2];
         RMSe[0] = Math.sqrt(SEOutputRO) / consideredInstances;
         RMSe[1] = Math.sqrt(SEOutputWO) / consideredInstances;
         logger.info("RESULT considering " + Parameter + " of  " + entry.getKey().toString().split("@")[0] + ":" + "RMSERO =" + RMSe[0] + "  RMSEWO =" + RMSe[1]);
         rmse.put(entry.getKey(), RMSe);
      }
      return rmse;
   }


   protected abstract boolean considerInstances(int i);


}
