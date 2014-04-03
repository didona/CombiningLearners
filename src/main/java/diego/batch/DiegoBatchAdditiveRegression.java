package diego.batch;

import batching.BatchingOutputOracle;
import eu.cloudtm.DataUtility.DataConverter;
import eu.cloudtm.autonomicManager.oracles.InputOracle;
import eu.cloudtm.autonomicManager.oracles.Oracle;
import eu.cloudtm.autonomicManager.oracles.OutputOracle;
import eu.cloudtm.autonomicManager.oracles.exceptions.OracleException;
import eu.cloudtm.boosting.Dataset;
import weka.classifiers.meta.AdditiveRegression;
import weka.core.Instance;

/**
 * @author Diego Didona
 * @email didona@gsd.inesc-id.pt
 * @since 02/04/14
 */
public class DiegoBatchAdditiveRegression implements Oracle {

   protected Instance instc;
   protected InputOracle inputOracle;
   protected AdditiveRegression[] adR;

   private String classifier = "weka.classifiers.rules.M5Rules", iterations = "10";

   public DiegoBatchAdditiveRegression(String classifier, String iterations) throws Exception {
      this.classifier = classifier;
      this.iterations = iterations;
      build();
   }

   private void build() throws Exception {
      if (Dataset.DataBoosting == null)
         throw new Exception("Boosting Dataset not present");
      adR = new AdditiveRegression[1];


      String[] Options = {"-W", classifier, "-I", iterations};


      for (int i = 0; i < adR.length; i++) {

         adR[i] = new AdditiveRegression();//new Ensemble());

         adR[i].setOptions(Options);
      }

      Dataset.DataBoosting.setClassIndex(Dataset.DataBoosting.numAttributes() - 1);
      adR[0].buildClassifier(Dataset.DataBoosting);
      //System.out.println(adR[0]);
   }

   public DiegoBatchAdditiveRegression() throws Exception {
      build();
   }


   @Override
   public OutputOracle forecast(InputOracle inputOracle) throws OracleException {
      try {
         instc = DataConverter.FromInputOracleToInstance(inputOracle);
         return new BatchingOutputOracle(adR[0].classifyInstance(instc));
      } catch (Exception e) {
         throw new OracleException(e.getMessage());
      }
   }
}
