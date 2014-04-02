/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.boosting;

import CsvOracles.params.CsvRgParams;
import csv.CsvReader;
import eu.cloudtm.Configuration.BoostingConfiguration;
import eu.cloudtm.Configuration.Configuration;
import eu.cloudtm.DataUtility.DataConverter;
import eu.cloudtm.DataUtility.DataPrinting;
import eu.cloudtm.autonomicManager.oracles.Oracle;
import eu.cloudtm.autonomicManager.oracles.OutputOracle;
import org.apache.log4j.Logger;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;


/**
 * @author ennio
 */
public class Dataset implements eu.cloudtm.Dataset.Dataset {
   static Logger logger = Logger.getLogger(Dataset.class.getName());

   public static Instances DataBoosting;
   protected BoostingConfiguration BC = boostConfig();

   protected BoostingConfiguration boostConfig() {
      return BoostingConfiguration.getInstance();
   }


   public Dataset(String Directory_path) throws Exception {


      int numFiles = 0;
      //double[] Outputs = new double[4];
      double[] both;
      logger.info("//******************************************");
      logger.info("          Start of Boosting Dataset Creation         ");
      logger.info("*******************************************//");

      try {

         DataBoosting = ExtendInstances();


         File dir = new File(Directory_path);
         if (!dir.exists())
            throw new IOException(dir + " for dataset does not exist");
         for (File nextdir : dir.listFiles()) {
            if (nextdir.isDirectory()) {
               for (File csv : nextdir.listFiles()) {
                  if (!csv(csv)) {
                     continue;
                  } else {
                     //System.out.println("Parsing " + csv.getAbsolutePath());
                     CsvReader reader = new CsvReader(new CsvRgParams(csv.getPath()));

                     Instance i = DataConverter.FromInputOracleToInstance(reader);
                     //System.out.println(Arrays.toString(i.toDoubleArray()));
                     double[] Outputs = additionalStats(reader);
//                     Outputs[0] = reader.throughput(0) + reader.throughput(1);
//                     Outputs[1] = reader.abortRate(1);
//                     Outputs[2] = reader.responseTime(0);
//                     Outputs[3] = reader.responseTime(1);

                     both = DataPrinting.addTwoArray(i.toDoubleArray(), Outputs);
                     Instance I = new DenseInstance(1, both);
                     //System.out.println(Arrays.toString(I.toDoubleArray()));
                     DataBoosting.add(I);
                     numFiles++;

                  }
               }
            }
         }
         DataPrinting.PrintDataSet(DataBoosting);
      } catch (Exception e) {

         logger.error("Dataset Creation Failed " + e);
         e.printStackTrace();

      } finally {
         logger.info("//******************************************");
         logger.info("  Dataset Created For Boosting" + numFiles + " File Readed");
         logger.info("******************************************//");
      }

   }


   protected double[] additionalStats(CsvReader reader) {
      double[] Outputs = new double[4];
      Outputs[0] = reader.throughput(0) + reader.throughput(1);
      Outputs[1] = reader.abortRate(1);
      Outputs[2] = reader.responseTime(0);
      Outputs[3] = reader.responseTime(1);
      return Outputs;
   }


   private static boolean csv(File f) {

      return f.toString().endsWith("csv");
   }

   protected Instances ExtendInstances() throws Exception {

      Instances NewData = new ConverterUtils.DataSource(BC.getOracleInputDescription()).getStructure();

      NewData.insertAttributeAt(new Attribute("throughput"), NewData.numAttributes());
      NewData.insertAttributeAt(new Attribute("abortRate"), NewData.numAttributes());
      NewData.insertAttributeAt(new Attribute("responseTimeRO"), NewData.numAttributes());
      NewData.insertAttributeAt(new Attribute("responseTimeWO"), NewData.numAttributes());

      return NewData;
   }

   @Override
   public Instances getARFFDataSet() {
      return DataBoosting;
   }


   @Override
   public Configuration getConfiguration() {
      return BC;
   }

   @Override
   public HashMap<Instance, OutputOracle> getValidationSet() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public HashMap<Oracle, HashMap<Instance, OutputOracle>> getPredictiontionSet() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public HashMap<String, Instance> getInstancesMap() {
      throw new UnsupportedOperationException("Not supported yet.");
   }


}
