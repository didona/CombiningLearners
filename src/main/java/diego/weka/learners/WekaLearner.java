package diego.weka.learners;

import diego.weka.incremental.DataSet;
import diego.weka.incremental.WekaDataSet;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public abstract class WekaLearner<P extends Classifier> extends Learner {

   protected P classifier;

   protected WekaLearner(DataSet d) throws Exception {
      super(d);
      this.classifier = buildAndTrainClassifier();
   }


   public WekaLearner(DataSet d, String features) throws Exception {
      super(d, features);
      this.classifier = buildAndTrainClassifier();
   }

   @Override
   protected DataSet filterTrainingSetWithFeatures(DataSet d, String features) throws Exception {
      return new WekaDataSet(filter(((WekaDataSet) d).getSamples(), features));
   }

   protected Instances filter(Instances i, String features) throws Exception {
      Remove remove = new Remove();
      remove.setInvertSelection(true);
      remove.setAttributeIndices(features);
      remove.setInputFormat(i);
      Instances filteredInput = Filter.useFilter(i, remove);
      filteredInput.setClassIndex(filteredInput.numAttributes() - 1);
      return filteredInput;
   }

   public final double mape(DataSet d) throws Exception {
      WekaDataSet wds = (WekaDataSet) d;
      Instance temp;
      double real, pred, err = 0;
      Instances filtered = filter(wds.getSamples(), this.features);
      for (int i = 0; i < filtered.numInstances(); i++) {
         temp = filtered.instance(i);
         // System.out.println(temp);
         pred = this.classifier.classifyInstance(temp);
         real = temp.value(temp.numAttributes() - 1);
         //System.out.println(pred + "  vs " + real);
         err += Math.abs(pred - real) / real;
      }
      return err / filtered.numInstances();
   }

   private final static boolean compareWithWeka = false;

   @Override
   public double wekaMeanAbsoluteRelativeError(DataSet d) throws Exception {
      if (true) {
         if (compareWithWeka) {
            WekaDataSet wds = (WekaDataSet) d;
            Instances filtered = filter(wds.getSamples(), this.features);
            Evaluation eval = new Evaluation(filtered);
            eval.evaluateModel(this.classifier, filtered);
            System.out.println(this.classifier.toString());
            System.out.println(eval.toSummaryString());
         }
         return mape(d);
      }
      WekaDataSet wds = (WekaDataSet) d;
      Instances filtered = filter(wds.getSamples(), this.features);
      System.out.println(filtered.toString());
      Evaluation eval = new Evaluation(filtered);
      eval.evaluateModel(this.classifier, filtered);
      System.out.println(this.classifier.toString());
      System.out.println(eval.toSummaryString());
      return eval.relativeAbsoluteError();
   }

   protected WekaDataSet trainingSet() {
      return (WekaDataSet) trainingSet;
   }

   protected Instances instances() {
      return trainingSet().getSamples();
   }

   protected abstract P buildAndTrainClassifier() throws Exception;
}
