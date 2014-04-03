package diego.mains;

import diego.tas.TasDataSet;
import eu.cloudtm.DataUtility.DataConverter;
import eu.cloudtm.autonomicManager.oracles.InputOracle;
import eu.cloudtm.autonomicManager.oracles.OutputOracle;
import eu.cloudtm.boosting.Dataset;
import org.apache.log4j.PropertyConfigurator;
import tasOracle.common.TasOracle;
import weka.core.Instance;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * @author Diego Didona
 * @email didona@gsd.inesc-id.pt
 * @since 01/04/14
 */
public class TasMain {

   public static void main(String[] args) throws Exception {
      System.out.println("Hello World!");
      PropertyConfigurator.configure("conf/log4j.properties");

      //Boosting
      System.out.println("Going to create dataset");
      Dataset d = new TasDataSet("newTasData"); //Boosting dataset
      System.out.println("Now using tas");
      PrintWriter pw = new PrintWriter(new FileWriter(new File("conf/out.csv")));

      int instances = Dataset.DataBoosting.numInstances();
      double[] predTH = new double[instances];
      double[] realTH = new double[instances];

      Instance instance;
      double real, pred;
      pw.println("Real,Real,Pred");
      for (int i = 0; i < instances; i++) {
         instance = Dataset.DataBoosting.instance(i);
         InputOracle io = DataConverter.FromInstancesToInputOracle(instance);
         try {
            OutputOracle oo = new TasOracle().forecast(io);
            real = instance.value(instance.numAttributes() - 4);
            pred = oo.throughput(0) + oo.throughput(1);
            predTH[i] = pred;
            realTH[i] = real;
            pw.println(real + "," + real + "," + pred);
            System.out.println("Real " + realTH[i - 1] + " vs " + predTH[i - 1]);
         } catch (Exception e) {
            //e.printStackTrace();
         }
      }

      pw.close();
   }
}
