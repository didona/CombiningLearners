package eu.cloudtm.boosting;

import csv.CsvReader;

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

}
