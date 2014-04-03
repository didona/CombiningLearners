package diego.mains;

import batching.offline.BatchingPaoloAnalyticalOracle;
import diego.batch.BatchDataSet;
import eu.cloudtm.DataUtility.DataConverter;
import eu.cloudtm.autonomicManager.oracles.InputOracle;
import eu.cloudtm.autonomicManager.oracles.OutputOracle;
import eu.cloudtm.boosting.Dataset;
import org.apache.log4j.PropertyConfigurator;
import weka.core.Instance;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * @author Diego Didona
 * @email didona@gsd.inesc-id.pt
 * @since 02/04/14
 */
public class BatchMain {

   public static void main(String[] args) throws Exception {
      System.out.println("Hello World!");
      PropertyConfigurator.configure("conf/log4j.properties");
      System.out.println("Going to create dataset");
      DataConverter.updateArff("conf/batch/dataset.arff");
      Dataset d = new BatchDataSet("batch_csv"); //Boosting dataset
      System.out.println("Now using Paolo Batch");
      PrintWriter pw = new PrintWriter(new FileWriter(new File("conf/batch_out.csv")));
      int instances = Dataset.DataBoosting.numInstances();
      pw.println("Real,Real,Pred");
      Instance instance;
      double real, pred;
      for (int i = 0; i < instances; i++) {
         instance = Dataset.DataBoosting.instance(i);
         InputOracle io = DataConverter.FromInstancesToInputOracle(instance);
         try {
            //Here you update the quality of the model
            BatchingPaoloAnalyticalOracle.optimalValues();
            OutputOracle oo = new BatchingPaoloAnalyticalOracle().forecast(io);
            real = instance.value(instance.numAttributes() - 1);
            pred = oo.responseTime(0);
            pw.println(real + "," + real + "," + pred);
            System.out.println("Real " + real + " vs " + pred);
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
      pw.close();
   }
}
