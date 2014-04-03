package diego.knn;

import diego.weka.incremental.DataSet;
import diego.weka.incremental.WekaDataSet;
import eu.cloudtm.DataUtility.DataConverter;
import eu.cloudtm.DataUtility.DatasetOutputOracle;
import eu.cloudtm.Knn.DataSets;
import eu.cloudtm.Knn.Learner;
import eu.cloudtm.autonomicManager.oracles.InputOracle;
import eu.cloudtm.autonomicManager.oracles.Oracle;
import eu.cloudtm.autonomicManager.oracles.OutputOracle;
import eu.cloudtm.autonomicManager.oracles.exceptions.OracleException;
import org.apache.log4j.Logger;
import weka.core.DistanceFunction;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.LinearNNSearch;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Diego Didona
 * @email didona@gsd.inesc-id.pt
 * @since 03/04/14
 */
public class KNNLearner_D extends Learner implements Oracle {
   protected Instances baseLearnersTrainingSet = null;
   protected Instances metaLearnerTrainingSet = null;
   protected Instance m_TestSet = null;
   protected HashMap<Oracle, Double> RMSE;
   protected Instances Neighbourshood;
   protected double[] distances;
   static Logger logger = Logger.getLogger(KNNLearner_D.class);
   private LinearNNSearch KNN;
   private int numNeighbours;
   private String targetFeature;
   private double cutoff;

   private ArrayList<Oracle> oracles = new ArrayList<Oracle>();
   private HashMap<Oracle, HashMap<Instance, OutputOracle>> baseLearnersOutput = new HashMap<Oracle, HashMap<Instance, OutputOracle>>();
   private HashMap<Instance, OutputOracle> metaLearnerRealValues = new HashMap<Instance, OutputOracle>();

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

      targets targetParameter = targets.valueOf(Parameter);

      for (Map.Entry<Oracle, HashMap<Instance, OutputOracle>> entry : DataSets.predictionResults.entrySet()) {
         double consideredInstances = 0;
         double err = 0D;
         for (int i = 0; i < Neighbourshood.numInstances(); i++) {

            //Get the i-th instance from the neighborhood
            //Instance inst = DataSets.InstancesMap.get(Neighbourshood.instance(i).toStringNoWeight());
            Instance inst = Neighbourshood.get(i);
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

   public KNNLearner_D(DataSet trainingSet, String featuresList) throws Exception {

      KNNConfig config = (KNNConfig) KNNConfig.getInstance();

      numNeighbours = Integer.parseInt(config.getknearestneighbourg());
      targetFeature = config.getTarget();
      cutoff = Double.parseDouble(config.getCutoff());
      KNN = new LinearNNSearch();

      DistanceFunction distFunc = buildDistanceFunction(config);
      KNN.setDistanceFunction(distFunc);

      generateTrainingSets(trainingSet);

      train();

   }


   private void train() {
      computeMetaLeanerValues();
      trainBaseLearners();
   }

   private void computeMetaLeanerValues() {
      for (Instance i : metaLearnerTrainingSet) {
         InputOracle io = DataConverter.FromInstancesToInputOracle(i);

      }
   }




   private void generateTrainingSets(DataSet trainingSet) {
      DataSet meta = trainingSet.subDataSet(20);
      trainingSet.remove(meta);
      this.baseLearnersTrainingSet = ((WekaDataSet) trainingSet).getSamples();
      this.metaLearnerTrainingSet = ((WekaDataSet) meta).getSamples();
   }

   @Override
   public OutputOracle forecast(InputOracle io) throws OracleException {

      double AVGrmse = Double.MAX_VALUE;
      Oracle best = null;

      try {

         Instances filtered = filterInstancesIfNeeded(baseLearnersTrainingSet);
         KNN.setInstances(filtered);

         m_TestSet = DataConverter.FromInputOracleToInstance(io);
         logger.info("PREDICT TARGET VALUE FOR  : " + m_TestSet.toStringNoWeight());
         System.out.println(m_TestSet);
         //Pick the nearest neighbours
         Neighbourshood = KNN.kNearestNeighbours(m_TestSet, numNeighbours);//>k neighbours are returned if there are more than one neighbours at the kth boundary.
         //Pick the distances of the nearest
         distances = KNN.getDistances();


         RMSE = computeRMSE(this.targetFeature);


         double actual;
         for (Map.Entry<Oracle, Double> entry : RMSE.entrySet()) {

            actual = entry.getValue();
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


   private OutputOracle fromInstanceToOutputOracle(Instance i){
      DatasetOutputOracle dat=new DatasetOutputOracle();
      dat.
   }
}
