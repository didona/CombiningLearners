package diego.mains;

import batching.offline.BatchingPaoloAnalyticalOracle;
import diego.batch.BatchDataSet;
import diego.batch.DiegoBatchAdditiveRegression;
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
public class ARMain {

   public static void main(String[] args) throws Exception {
      String[] learners = new String[]{"weka.classifiers.rules.M5Rules", "weka.classifiers.functions.SMOreg"};
      String[] iterations = new String[]{"1", "2", "3","4","5"};

      PropertyConfigurator.configure("conf/log4j.properties");
      System.out.println("Going to create dataset");
      DataConverter.updateArff("conf/batch/dataset.arff");
      Dataset d = new BatchDataSet("batch_csv"); //Boosting dataset
      for (String l : learners) {
         for (String i : iterations) {
            errorWith(l, i);
         }
      }
   }


   private static void errorWith(String learner, String iterations) throws Exception {

      //System.out.println("Now using Paolo Batch");
      int instances = Dataset.DataBoosting.numInstances();
      Instance instance;
      double real, pred, pred2;
      double[] realM = new double[instances], amM = new double[instances], mlM = new double[instances];
      double sumAM = 0, sumML = 0;

      DiegoBatchAdditiveRegression dbar = new DiegoBatchAdditiveRegression(learner, iterations);
      for (int i = 0; i < instances; i++) {
         instance = Dataset.DataBoosting.instance(i);
         InputOracle io = DataConverter.FromInstancesToInputOracle(instance);
         try {
            //Here you update the quality of the model
            BatchingPaoloAnalyticalOracle.optimalValues();
            OutputOracle oo = new BatchingPaoloAnalyticalOracle().forecast(io);
            real = instance.value(instance.numAttributes() - 1);
            pred = oo.responseTime(0);
            pred2 = dbar.forecast(io).responseTime(0);
            //System.out.println("Real " + real + " vs " + pred + " vs " + pred2);
            sumML += err(real, pred2);
            sumAM += err(real, pred);
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
      System.out.println("Leaner " + learner + " iterations " + iterations + "\nAM error " + sumAM / instances + " ML error " + sumML / instances);

   }

   private static void all() throws Exception {
      System.out.println("Hello World!");
      PropertyConfigurator.configure("conf/log4j.properties");
      System.out.println("Going to create dataset");
      DataConverter.updateArff("conf/batch/dataset.arff");
      Dataset d = new BatchDataSet("batch_csv"); //Boosting dataset
      //System.out.println("Now using Paolo Batch");
      PrintWriter pw = new PrintWriter(new FileWriter(new File("conf/AR_batch_out.csv")));
      int instances = Dataset.DataBoosting.numInstances();
      pw.println("Real,Real,AM,ML");
      Instance instance;
      double real, pred, pred2;
      double[] realM = new double[instances], amM = new double[instances], mlM = new double[instances];


      DiegoBatchAdditiveRegression dbar = new DiegoBatchAdditiveRegression();
      for (int i = 0; i < instances; i++) {
         instance = Dataset.DataBoosting.instance(i);
         InputOracle io = DataConverter.FromInstancesToInputOracle(instance);
         try {
            //Here you update the quality of the model
            BatchingPaoloAnalyticalOracle.optimalValues();
            OutputOracle oo = new BatchingPaoloAnalyticalOracle().forecast(io);
            real = instance.value(instance.numAttributes() - 1);
            pred = oo.responseTime(0);
            pred2 = dbar.forecast(io).responseTime(0);
            pw.println(real + "," + real + "," + pred + ", " + pred2);
            //System.out.println("Real " + real + " vs " + pred + " vs " + pred2);
            mlM[i] = err(real, pred2);
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
      pw.close();
   }

   private static double err(double r, double p) {
      return Math.abs(r - p) / r;
   }
}
