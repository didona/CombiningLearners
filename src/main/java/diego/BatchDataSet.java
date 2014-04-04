package diego;

import csv.CsvReader;
import eu.cloudtm.Configuration.BoostingConfiguration;
import eu.cloudtm.boosting.Dataset;
import weka.core.Instances;

/**
 * @author Diego Didona
 * @email didona@gsd.inesc-id.pt
 * @since 02/04/14
 */
@Deprecated
//Now the dataSet generated for the batch is exactly the same as the one used for tas
public class BatchDataSet extends Dataset {

   public BatchDataSet(String Directory_path) throws Exception {
      super(Directory_path);

   }

   protected BoostingConfiguration boostConfig() {
      return BoostingConfigurationBatch.getInstance();
   }

   protected double[] additionalStats(CsvReader reader) {
      return super.additionalStats(reader);
      /*
      double[] Outputs = new double[1];
      Outputs[0] = ((Number) reader.getParam(Param.AvgGetsPerWrTransaction)).doubleValue();
      return Outputs;
      */

   }

   protected Instances ExtendInstances() throws Exception {
      /*
      Instances NewData = new ConverterUtils.DataSource(BC.getOracleInputDescription()).getStructure();

      NewData.insertAttributeAt(new Attribute("LocalUpdateTxTotalResponseTime"), NewData.numAttributes());

      System.out.println("Extended " + NewData);
      return NewData;
      */

      //I guess that, in order to maintain compatibility, I have to expand in the same way
      return super.ExtendInstances();
   }

}
