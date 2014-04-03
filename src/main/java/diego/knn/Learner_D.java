package diego.knn;

import eu.cloudtm.Knn.DataSets;
import eu.cloudtm.autonomicManager.oracles.Oracle;
import eu.cloudtm.autonomicManager.oracles.OutputOracle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
   protected HashMap<Oracle, Double> RMSE;
   protected Instances Neighbourshood;
   protected double[] distances;
   protected double[][] SquaredErrors;

   private final static Log logger = LogFactory.getLog(Learner_D.class);

   private enum targets {
      Throughput(-1), WriteThroughput(1), ReadThroughput(0), ReadResponseTime(0), WriteResponseTime(1), ReadAbortProb(0), WriteAbortProb(1);
      private int clazz;

      targets(int clazz) {
         this.clazz = clazz;
      }

   }

   protected Instances filterInstancesIfNeeded(Instances toFilter) {
      //With new samples we need to do nothing
      //In case, we can re-integrate Ennio's methods for RD and Isolation level
      return toFilter;
   }

   /**
    * Only one parameter among throughput, writeResponseTime, readResponseTime, writeThroughput, readThroughput,
    * abortRate
    */

   //I would not choose a learner basing on the average among the errors on different features!
   //Every feature should have its own learner
   protected HashMap<Oracle, Double> computeRMSE(String Parameter) throws Exception {
      HashMap<Oracle, Double> rmse = new HashMap<Oracle, Double>();
      double RMSe;


      OutputOracle outputValidationSet;
      OutputOracle outputOracle;

      SquaredErrors = new double[2][Neighbourshood.numInstances()];

      targets targetParameter = targets.valueOf(Parameter);

      for (Map.Entry<Oracle, HashMap<Instance, OutputOracle>> entry : DataSets.predictionResults.entrySet()) {
         double consideredInstances = 0;
         double err = 0D;
         for (int i = 0; i < Neighbourshood.numInstances(); i++) {

            //Get the i-th instance from the neighborhood
            Instance inst = DataSets.InstancesMap.get(Neighbourshood.instance(i).toStringNoWeight());
            //Take the output from the validationSet (i.e., I guess, the REAL value)
            outputValidationSet = DataSets.ValidationSet.get(inst);
            //Take the --cached-- output of the Oracle
            outputOracle = entry.getValue().get(inst);

            logger.info("Instance :" + inst + "\n" + "validationOutput= " + outputValidationSet + "\n" + "Oracle Output= " + outputOracle + "\n" + "Distance= " + distances[i]);


            if (considerInstances(i)) {
               consideredInstances++;
               err += computePredictionError(targetParameter, outputValidationSet, outputOracle);
            }

         }
         RMSe = err / consideredInstances;
         logger.info("RESULT considering " + Parameter + " of  " + entry.getKey().toString().split("@")[0] + ":" + "RMSE =" + RMSe);
         rmse.put(entry.getKey(), RMSe);
      }
      return rmse;
   }

   private static double rootError(double a, double b) {
      return Math.pow(a - b, 2);
   }

   private double computePredictionError(targets target, OutputOracle real, OutputOracle pred) {
      switch (target) {
         case Throughput: {
            double r = real.throughput(0) + real.throughput(1);
            double p = pred.throughput(0) + pred.throughput(1);
            return rootError(r, p);
         }
         case ReadThroughput: {
            double r = real.throughput(0);
            double p = pred.throughput(0);
            return rootError(r, p);
         }
         case WriteThroughput: {
            double r = real.throughput(1);
            double p = pred.throughput(1);
            return rootError(r, p);
         }
         case ReadResponseTime: {
            double r = real.responseTime(0);
            double p = pred.responseTime(0);
            return rootError(r, p);
         }
         case WriteResponseTime: {
            double r = real.responseTime(1);
            double p = pred.responseTime(1);
            return rootError(r, p);
         }
         case WriteAbortProb: {
            double r = real.abortRate(1);
            double p = pred.abortRate(1);
            return rootError(r, p);
         }
         default: {
            throw new IllegalArgumentException(target + " is not a valid target. Possible values are " + Arrays.toString(targets.values()));
         }
      }
   }

   protected abstract boolean considerInstances(int i);
}
