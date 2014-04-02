package eu.cloudtm.boosting;

import csv.CsvReader;
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

   protected double[] additionalStats(CsvReader reader) {
      double[] Outputs = new double[1];
      Outputs[0] = reader.responseTime(0);
      return Outputs;
   }

   protected Instances ExtendInstances() throws Exception {

      Instances NewData = new ConverterUtils.DataSource(BC.getOracleInputDescription()).getStructure();

      NewData.insertAttributeAt(new Attribute("responseTimeRO"), NewData.numAttributes());

      return NewData;
   }
}
