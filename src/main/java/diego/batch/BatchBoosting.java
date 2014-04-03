package diego.batch;

import eu.cloudtm.Configuration.BoostingConfiguration;
import eu.cloudtm.DataUtility.DataConverter;
import eu.cloudtm.autonomicManager.oracles.InputOracle;
import eu.cloudtm.autonomicManager.oracles.OutputOracle;
import eu.cloudtm.autonomicManager.oracles.exceptions.OracleException;
import eu.cloudtm.boosting.Boosting;
import eu.cloudtm.boosting.Dataset;
import weka.classifiers.meta.AdditiveRegression;

/**
 * @author Diego Didona
 * @email didona@gsd.inesc-id.pt
 * @since 02/04/14
 */
public class BatchBoosting extends Boosting {

   public BatchBoosting() throws Exception {
      if (Dataset.DataBoosting == null)
         throw new Exception("Boosting Dataset not present");
      adR = new AdditiveRegression[1];

      String classifier = getConfig().getCombiner();
      String Iteration = getConfig().getIteration();

      String[] Options = {"-W", classifier, "-I", Iteration};


      for (int i = 0; i < adR.length; i++) {

         adR[i] = new AdditiveRegression();//new Ensemble());

         adR[i].setOptions(Options);
         System.out.println(adR[i]);
      }
   }


   @Override
   public OutputOracle forecast(InputOracle io) throws OracleException {

      try {
         inputOracle = io;
         instc = DataConverter.FromInputOracleToInstance(io);

         return new BatchBoostingOutputOracle();
      } catch (Exception ex) {
         logger.error("Error During Boosting forecasting" + ex);

         throw new OracleException(Boosting.class.getName() + ex);
      }
   }

   @Override
   protected BoostingConfiguration getConfig() {
      return BoostingConfigurationBatch.getInstance();
   }

   protected class BatchBoostingOutputOracle extends BoostingOutputOracle {
      @Override
      public double throughput(int i) {
         throw new UnsupportedOperationException("No throughput");
      }

      @Override
      public double abortRate(int i) {
         throw new UnsupportedOperationException("No abort rate");
      }

      @Override
      public double responseTime(int i) {
         try {


            if (i == 0) {

               if (!responsetimeRO)// the learner is generated only once for each target;
               {
                  //
                  //System.out.println(Dataset.DataBoosting);

                  Dataset.DataBoosting.setClassIndex(Dataset.DataBoosting.numAttributes() - 1);
                  //System.out.println(Dataset.DataBoosting.classAttribute().toString());
                  System.out.println("Building classifier");
                  adR[0].buildClassifier(Dataset.DataBoosting);
                  System.out.println(adR[0]);
                  responsetimeRO = true;
               }

               return adR[0].classifyInstance(instc);
            } else {
               throw new UnsupportedOperationException("Batch returns only resp time for RO");
            }
         } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(this.getClass().getName() + ex);
            throw new RuntimeException("Error During ResponseTime forecasting");
         }
      }
   }
}
