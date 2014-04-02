package diego;

import eu.cloudtm.DataUtility.DataPrinting;
import eu.cloudtm.weka.ensemble.EnsembleML;
import weka.core.DenseInstance;
import weka.core.Instance;

/**
 * @author Diego Didona
 * @email didona@gsd.inesc-id.pt
 * @since 02/04/14
 */
public class BatchEnsembleML extends EnsembleML {

   @Override
   public double classifyInstance(Instance instnc) throws Exception {
      double Outputs[] = new double[1];
      double both[] = DataPrinting.addTwoArray(instnc.toDoubleArray(), Outputs);
      Instance I = new DenseInstance(1, both);
      //System.out.println("instc " + instnc);
      //System.out.println("I " + I);
      // return classifier.classifyInstance(I);// instnc.value(instnc.numAttributes()-1);
      return classifier.classifyInstance(instnc);// instnc.value(instnc.numAttributes()-1);
   }

}
