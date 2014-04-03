package diego.mains;

import org.apache.log4j.PropertyConfigurator;
import weka.ModelResult;
import weka.incremental.WekaIncremental;

import java.io.File;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class WekaMain {

   private final static String base = "data/weka/gsd/throughput/train.arff";
   private final static String test = "data/weka/1K_5put.arff";

   public static void main(String[] args) {
      PropertyConfigurator.configure("conf/log4j.properties");
      //weka.core.logging.Logger.log(weka.core.logging.Logger.Level.ALL,
      //"Logging started");
      String baseTrain = args[1];
      String baseTest = args[2];
      String ml = args[0];
      File base = new File(baseTrain);
      //File test = new File("data/weka/test.arff");
      File test = new File(baseTest);
      double fold = 3;
      double run = 10;
      double percentage;
      final boolean removeFromTest = true;
      double[][] result = new double[(int) run][(int) (fold + 1)];
      double[][] numSamples = new double[(int) run][(int) (fold + 1)];
      double res;
      boolean first = true;
      for (int r = 0; r < run; r++) {
         for (int i = 0; i < fold; i++) {     //don't do the 100%
            //only do the first fold once!
            if (i == 0) {
               if (first) {
                  first = false;
               } else {
                  continue;
               }
            }
            //for (int i = 1; i < (int) fold; i++) {
            percentage = i * 120 / fold;
            System.out.println("Percentage " + percentage);
            WekaIncremental wi = new WekaIncremental(base, test, (int) percentage, removeFromTest, ml);
            ModelResult mr = wi.buildModel();
            result[r][i] = mr.getError();
            numSamples[r][i] = mr.getNumTestSamples();
         }
      }

      for (int i = 0; i <= fold; i++) {
         for (int r = 1; r < run; r++) {
            result[0][i] += result[r][i];
            numSamples[0][i] += numSamples[r][i];
         }
      }

      for (int i = 0; i <= fold; i++) {
         System.out.println("Fold " + i + " error " + result[0][i] + " for samples " + numSamples[0][i]);
      }
   }
}
