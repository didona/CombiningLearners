package diego.batch;

import batching.offline.BatchingPaoloAnalyticalOracle;
import eu.cloudtm.DataUtility.DataConverter;
import eu.cloudtm.DataUtility.OutputsMap;
import eu.cloudtm.autonomicManager.oracles.InputOracle;
import org.apache.log4j.Logger;
import weka.classifiers.AbstractClassifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

/**
 * @author Diego Didona
 * @email didona@gsd.inesc-id.pt
 * @since 02/04/14
 */
public class BatchingClassifier extends AbstractClassifier {
   static Logger logger = Logger.getLogger(eu.cloudtm.weka.Tas.Tas.class.getName());
   private String Target, methodTarg;

   public BatchingClassifier() {
      super();
   }

   @Override
   public double[] distributionForInstance(Instance instnc) throws Exception {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Capabilities getCapabilities() {
      Capabilities cap = super.getCapabilities();

      cap.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
      cap.enable(Capabilities.Capability.MISSING_VALUES);
      cap.enable(Capabilities.Capability.NUMERIC_CLASS);

      return cap;

   }

   @Override
   public void buildClassifier(Instances i) throws Exception {
      try {
         getCapabilities().testWithFail(i);

         Target = i.classAttribute().name();
         testonTarget();

         methodTarg = OutputsMap.valueOf(Target).getTarget();

         logger.info("Batch CLASSIFIER built");

      } catch (Exception e) {
         throw new Exception(e);
      }

   }

   @Override
   public double classifyInstance(Instance instnc) throws Exception {

      InputOracle inst = DataConverter.FromInstancesToInputOracle(instnc);
      BatchingPaoloAnalyticalOracle oracle = new BatchingPaoloAnalyticalOracle();
      return (oracle.forecast(inst)).getConfidenceResponseTime(0);

   }

   private void testonTarget() throws Exception {

      if (Target == null)
         throw new Exception(this.getClass().toString() + "target not set");

   }

   public void setTarget(String target) {
      Target = target;
   }
}
