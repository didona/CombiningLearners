/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.probing;

import eu.cloudtm.Configuration.BoostingConfiguration;
import eu.cloudtm.Configuration.ProbingConfiguration;
import eu.cloudtm.DataUtility.DataConverter;
import eu.cloudtm.DataUtility.DataPrinting;
import eu.cloudtm.DataUtility.OutputsMap;
import eu.cloudtm.autonomicManager.commons.Param;
import eu.cloudtm.autonomicManager.oracles.InputOracle;
import eu.cloudtm.autonomicManager.oracles.Oracle;
import eu.cloudtm.autonomicManager.oracles.OutputOracle;
import eu.cloudtm.autonomicManager.oracles.exceptions.OracleException;
import eu.cloudtm.weka.Cubist.Cubist;
import org.apache.log4j.Logger;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

/**
 * @author ennio
 */
public class Probing implements Oracle {
   static Logger logger = Logger.getLogger(Probing.class.getName());
   private Oracle oracle;
   private Classifier classifier = null, regressor = null;
   private Instances BadSet, Set;
   private HashMap<Instance, OutputOracle> predictions;
   private boolean RegressorisBuilt = false;


   public Probing() throws OracleException, InstantiationException {

      if (ProbingDataSets.ARFFDataSet == null) {
         logger.error(Probing.class.getName() + "--" + "Datasets Not instanziated");
         throw new InstantiationException(Probing.class.getName() + "Datasets Not instanziated");
      }

      double badInstance[], tempbad[], both[];
      double goodInstance[], tempgood[];
      double Outputs[] = new double[4];
      double target, targetA, targetB, error;
      OutputOracle ValidationOutput;

      Instance BadInstance, GoodInstance;


      if (ProbingDataSets.predictionResults.keySet().size() == 1) {

         oracle = ProbingDataSets.predictionResults.keySet().iterator().next();
         predictions = ProbingDataSets.predictionResults.get(oracle);

      } else {

         throw new OracleException("Probing needs only one initial predictor");

      }

      try {

         target = this.RootMeanSquaredError(ProbingDataSets.ARFFDataSet, ProbingDataSets.ValidationSet, predictions, ProbingConfiguration.getInstance().getTarget());

         if (target > 100) {

            logger.info("Proobing: Tas Prediction Error greater than 100");
            Set = ExtendNominalInstances();
            BadSet = ExtendInstances();

            for (Entry<Instance, OutputOracle> e : predictions.entrySet()) {

               Method method = OutputOracle.class.getDeclaredMethod("throughput", int.class);
               targetA = (Double) method.invoke(e.getValue(), 0) + (Double) method.invoke(e.getValue(), 1);
               ValidationOutput = ProbingDataSets.ValidationSet.get(e.getKey());
               targetB = (Double) method.invoke(ValidationOutput, 0) + (Double) method.invoke(ValidationOutput, 1);

               error = Math.abs(targetB - targetA);

               if (error > 300) {

                  tempbad = new double[1];

                  tempbad[0] = Set.attribute("flag").indexOfValue("bad");
                  badInstance = DataPrinting.addTwoArray(e.getKey().toDoubleArray(), tempbad);

                  BadInstance = new DenseInstance(1, badInstance);
                  Set.add(BadInstance);

                  Outputs[0] = ValidationOutput.throughput(1) + ValidationOutput.throughput(0);
                  Outputs[1] = ValidationOutput.abortRate(1);
                  Outputs[2] = ValidationOutput.responseTime(0);
                  Outputs[3] = ValidationOutput.responseTime(1);

                  both = DataPrinting.addTwoArray(e.getKey().toDoubleArray(), Outputs);
                  Instance I = new DenseInstance(1, both);

                  BadSet.add(I);


               } else {


                  tempgood = new double[1];

                  tempgood[0] = Set.attribute("flag").indexOfValue("good");
                  goodInstance = DataPrinting.addTwoArray(e.getKey().toDoubleArray(), tempgood);

                  GoodInstance = new DenseInstance(1, goodInstance);
                  Set.add(GoodInstance);
               }


            }

            classifier = new J48();


            Set.setClassIndex(Set.numAttributes() - 1);
            classifier.buildClassifier(Set);
            logger.info("****Probing classifier added " + classifier.getClass().getName() + "******");

            regressor = (Classifier) ProbingConfiguration.getInstance().getSecondaryOracle().newInstance();

            logger.info("****Probing regressor added " + regressor.getClass().getName() + "******");

            logger.info("++++Proobing regressor built : number of Bad instances=  " + BadSet.size() + "number of good instances :" + (Set.size() - BadSet.size()) + "+++++");
         }


      } catch (Exception e) {

         throw new OracleException(e);

      }


   }


   @Override
   public OutputOracle forecast(InputOracle io) throws OracleException {
      try {

         Instance i = DataConverter.FromInputOracleToInstance(io);

         i.setDataset(Set);

         Double result;

         if ((classifier != null) && (regressor != null)) {


            result = classifier.classifyInstance(i);

            if (Set.attribute("flag").indexOfValue("good") == result) {

               return oracle.forecast(io);
            } else {

               logger.info("Probing :Bad point,using regressor ");
               return new ProbingOutputOracle(io, i);
            }


         }

         return oracle.forecast(io);

      } catch (Exception ex) {
         throw new OracleException(ex);
      }
   }


   private Instances ExtendNominalInstances() throws Exception {


      Instances NewData = new ConverterUtils.DataSource(BoostingConfiguration.getInstance().getOracleInputDescription()).getStructure();
      ArrayList attrs = new ArrayList();

      attrs.add("bad");
      attrs.add("good");

      Attribute nominal = new Attribute("flag", attrs);

      NewData.insertAttributeAt(nominal, NewData.numAttributes());


      return NewData;
   }

   private Instances ExtendInstances() throws Exception {

      Instances NewData = new ConverterUtils.DataSource(BoostingConfiguration.getInstance().getOracleInputDescription()).getStructure();

      NewData.insertAttributeAt(new Attribute("throughput"), NewData.numAttributes());
      NewData.insertAttributeAt(new Attribute("abortRate"), NewData.numAttributes());
      NewData.insertAttributeAt(new Attribute("responseTimeRO"), NewData.numAttributes());
      NewData.insertAttributeAt(new Attribute("responseTimeWO"), NewData.numAttributes());

      return NewData;
   }


   //compute the RMSE of some instances given the target predictions and the real target value
   private double RootMeanSquaredError(Instances inst, HashMap<Instance, OutputOracle> validations, HashMap<Instance, OutputOracle> predictions, String Parameter) throws Exception {
      double rmse;
      Double[] RMSe;
      double errorOutputRO;
      double errorOutputWO;
      double SEOutputRO = 0D;
      double SEOutputWO = 0D;
      Method method;
      OutputOracle outputValidationSet;
      OutputOracle outputOracle;
      StringTokenizer token;
      String outputname;

      for (Map.Entry<Instance, OutputOracle> entry : predictions.entrySet()) {
         for (int i = 0; i < inst.numInstances(); i++) {

            Instance ins = ProbingDataSets.InstancesMap.get(inst.instance(i).toStringNoWeight());

            outputValidationSet = validations.get(ins);

            outputOracle = entry.getValue();

            token = new StringTokenizer(Parameter);
            while (token.hasMoreTokens()) {

               outputname = token.nextToken();

               method = OutputOracle.class.getMethod(outputname, int.class);

               errorOutputRO = (Double) method.invoke(outputValidationSet, 0) - (Double) method.invoke(outputOracle, 0);
               errorOutputWO = (Double) method.invoke(outputValidationSet, 1) - (Double) method.invoke(outputOracle, 1);


               SEOutputRO = SEOutputRO + Math.pow(errorOutputRO, 2);
               // logger.info( "error on "+outputname+" = " +errorOutputRO );


               SEOutputWO = SEOutputWO + Math.pow(errorOutputWO, 2);
               // logger.info( "error on "+outputname+" = " +errorOutputWO );


            }

         }

      }
      RMSe = new Double[2];
      RMSe[0] = Math.sqrt(SEOutputRO) / inst.numInstances();
      RMSe[1] = Math.sqrt(SEOutputWO) / inst.numInstances();

      logger.info("Root-mean-squared-error:RESULT considering " + Parameter + ":" + "RMSERO =" + RMSe[0] + "  RMSEWO =" + RMSe[1]);
      rmse = RMSe[0] + RMSe[1];

      return rmse;
   }

   private class ProbingOutputOracle implements OutputOracle {

      InputOracle io;
      Instance instc;

      public ProbingOutputOracle(InputOracle i, Instance instnc) {

         io = i;
         instc = instnc;

      }

      @Override
      public double throughput(int i) {
         try {

            if (!(regressor instanceof Cubist) || !RegressorisBuilt) {
               logger.info("Probing :regressor is not Cubist or it has not yet built");
               int displacement = OutputsMap.throughput.getArffDispacement();
               BadSet.setClassIndex(BadSet.numAttributes() - displacement);
               regressor.buildClassifier(BadSet);
               RegressorisBuilt = true;
            } else
               ((Cubist) regressor).setTarget(OutputsMap.throughput.getTarget());


            double writeperc = (Double) io.getParam(Param.PercentageSuccessWriteTransactions);

            if (i == 0) {


               return regressor.classifyInstance(instc) * (1 - writeperc);
            } else {

               return regressor.classifyInstance(instc) * (writeperc);
            }
         } catch (Exception ex) {
            logger.error(this.getClass().getName() + ex);
            ex.printStackTrace();
            throw new RuntimeException("Error During Throughput forecasting");
         }

      }

      @Override
      public double abortRate(int i) {
         try {

            if (!(regressor instanceof Cubist) || !RegressorisBuilt) {
               int displacement = OutputsMap.abortRate.getArffDispacement();
               BadSet.setClassIndex(BadSet.numAttributes() - displacement);
               regressor.buildClassifier(BadSet);
               RegressorisBuilt = true;
            } else
               ((Cubist) regressor).setTarget(OutputsMap.abortRate.getTarget());


            if (i == 0) {

               return 0D;
            } else {

               return regressor.classifyInstance(instc);
            }
         } catch (Exception ex) {
            logger.error(this.getClass().getName() + ex);
            throw new RuntimeException("Error During AbortRate forecasting");
         }

      }

      @Override
      public double responseTime(int i) {
         try {


            if (i == 0) {

               if (!(regressor instanceof Cubist) || !RegressorisBuilt) {
                  int displacement = OutputsMap.responseTimeRO.getArffDispacement();
                  BadSet.setClassIndex(BadSet.numAttributes() - displacement);
                  regressor.buildClassifier(BadSet);
                  RegressorisBuilt = true;
               } else
                  ((Cubist) regressor).setTarget(OutputsMap.responseTimeRO.getTarget());

               return regressor.classifyInstance(instc);
            } else {

               if (!(regressor instanceof Cubist) || !RegressorisBuilt) {
                  int displacement = OutputsMap.responseTimeWO.getArffDispacement();
                  BadSet.setClassIndex(BadSet.numAttributes() - displacement);
                  regressor.buildClassifier(BadSet);
                  RegressorisBuilt = true;
               } else
                  ((Cubist) regressor).setTarget(OutputsMap.responseTimeWO.getTarget());

               return regressor.classifyInstance(instc);
            }
         } catch (Exception ex) {
            logger.error(this.getClass().getName() + ex);
            throw new RuntimeException("Error During ResponseTime forecasting");
         }

      }

      @Override
      public double getConfidenceThroughput(int i) {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public double getConfidenceAbortRate(int i) {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public double getConfidenceResponseTime(int i) {
         throw new UnsupportedOperationException("Not supported yet.");
      }


   }

}