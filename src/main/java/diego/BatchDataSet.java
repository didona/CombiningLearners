package diego;

import csv.CsvReader;
import eu.cloudtm.Configuration.BoostingConfiguration;
import eu.cloudtm.autonomicManager.commons.Param;
import eu.cloudtm.boosting.Dataset;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

/**
 * @author Diego Didona
 * @email didona@gsd.inesc-id.pt
 * @since 02/04/14
 */
public class BatchDataSet extends Dataset {

   public BatchDataSet(String Directory_path) throws Exception {
      super(Directory_path);

   }

   protected BoostingConfiguration boostConfig() {
      return BoostingConfigurationBatch.getInstance();
   }

   protected double[] additionalStats(CsvReader reader) {
      double[] Outputs = new double[1];
      Outputs[0] = ((Number) reader.getParam(Param.AvgGetsPerWrTransaction)).doubleValue();
      return Outputs;
   }

   protected Instances ExtendInstances() throws Exception {
      System.out.println("=?");
      Instances NewData = new ConverterUtils.DataSource(BC.getOracleInputDescription()).getStructure();

      NewData.insertAttributeAt(new Attribute("AvgGetsPerWrTransaction"), NewData.numAttributes());

      System.out.println("Extended " + NewData);
      return NewData;
   }
}
