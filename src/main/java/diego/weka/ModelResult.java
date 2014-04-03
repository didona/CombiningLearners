package diego.weka;

/**
 * @author Diego Didona
 * @email didona@gsd.inesc-id.pt
 * @since 11/03/14
 */
public class ModelResult {
   private double error;
   private double numTestSamples;

   public ModelResult(double error, double numTestSamples) {
      this.error = error;
      this.numTestSamples = numTestSamples;
   }

   public double getNumTestSamples() {
      return numTestSamples;
   }

   public double getError() {
      return error;
   }
}
