package diego.weka.learners.smoReg;

import diego.weka.incremental.DataSet;
import diego.weka.learners.WekaLearner;
import weka.classifiers.Classifier;
import weka.classifiers.functions.SMOreg;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0 http://weka.sourceforge.net/doc.dev/weka/classifiers/functions/SMOreg.html
 */
public class SmoRegLearner extends WekaLearner {
   private static final long C = 8;
   private static final long normalize = 0;
   private static final String function = "\"weka.classifiers.functions.supportVector.RegSMOImproved -L 0.001 -W 1 -P 1.0E-12 -T 0.001 -V\"";
   //private static final String kernel = "\"weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0\"";
   private static final String kernel = "\"weka.classifiers.functions.supportVector.NormalizedPolyKernel -C 250007 -E 2.0\"";


   /*
   Note: The classifier (in our example tree) should not be trained when handed over to the crossValidateModel method.
   Why? If the classifier does not abide to the Weka convention that a classifier must be re-initialized every time the
   vanillaClassifier method is called (in other words: subsequent calls to the vanillaClassifier method always return the
   same results), you will get inconsistent and worthless results. The crossValidateModel takes care of training and
   evaluating the classifier. (It creates a copy of the original classifier that you hand over to the crossValidateModel
   for each run of the cross-validation.)
   source http://weka.wikispaces.com/Use+WEKA+in+your+Java+code
    */
   public SmoRegLearner(DataSet d, String features) throws Exception {
      super(d, features);
      /*WekaDataSet wds = (WekaDataSet) trainingSet;
      Instances filteredInput = filter(wds.getSamples());
      WekaDataSet filteredDS = new WekaDataSet(filteredInput);
      this.trainingSet = filteredDS;
      trace(filteredInput.toString());
      this.smOreg = new SMOreg();
      // set options
      //smOreg.setOptions(weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-12 -N 0 -V -1 -W 1 -K weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0"));
      smOreg.setOptions(weka.core.Utils.splitOptions("-C 1.0 -N 0 -I \"weka.classifiers.functions.supportVector.RegSMOImproved -L 0.001 -W 1 -P 1.0E-12 -T 0.001 -V\" -K \"weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0\""));

      //eval.crossValidateModel(smOreg, filteredInput, 10, new Random(1));
      smOreg.vanillaClassifier(filteredInput);
      Evaluation eval = new Evaluation(filteredInput);
      eval.evaluateModel(smOreg, filteredInput);
      info(eval.toSummaryString());
      info(smOreg.toString());
      this.ten_fold_error = eval.relativeAbsoluteError();
      */
   }

   public SmoRegLearner(DataSet d) throws Exception {
      super(d);
      /*
      WekaDataSet wds = (WekaDataSet) trainingSet;

      SMOreg smOreg = new SMOreg();
      // set options
      smOreg.setOptions(weka.core.Utils.splitOptions("-C 1.0 -N 0 -I \"weka.classifiers.functions.supportVector.RegSMOImproved -L 0.001 -W 1 -P 1.0E-12 -T 0.001 -V\" -K \"weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0\""));
      smOreg.vanillaClassifier(wds.getSamples());
      trace(wds.getSamples().toSummaryString());
      info(smOreg.toString());
      Evaluation eval = new Evaluation(wds.getSamples());
      eval.crossValidateModel(smOreg, wds.getSamples(), 2, new Random(1));
      info(eval.toSummaryString());
      this.ten_fold_error = eval.relativeAbsoluteError();
      */
   }

   @Override
   protected Classifier buildAndTrainClassifier() throws Exception {
      SMOreg smOreg = new SMOreg();
      //smOreg.setOptions(weka.core.Utils.splitOptions("-C 1.0 -N 0 -I \"weka.classifiers.functions.supportVector.RegSMOImproved -L 0.001 -W 1 -P 1.0E-12 -T 0.001 -V\" -K \"weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0\""));
      smOreg.setOptions(weka.core.Utils.splitOptions(prettyParameters()));
      smOreg.buildClassifier(instances());
      return smOreg;
   }


   static String prettyParameters() {
      return "-C " + C + " -N " + normalize + " -I " + function + " -K " + kernel;
   }
}
