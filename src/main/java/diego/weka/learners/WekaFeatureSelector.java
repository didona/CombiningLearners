package diego.weka.learners;

import diego.weka.incremental.DataSet;
import diego.weka.incremental.DataSetBuildException;
import diego.weka.incremental.WekaDataSet;
import diego.weka.learners.smoReg.SmoRegWekaFeatureSelector;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.attributeSelection.SubsetEvaluator;
import weka.attributeSelection.WrapperSubsetEval;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMOreg;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Random;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public abstract class WekaFeatureSelector extends FeatureSelector<WekaDataSet> {


   private final static boolean backwardSearch = true;


   public static void main(String[] args) throws Exception {
      new SmoRegWekaFeatureSelector(new WekaDataSet(new Instances(new BufferedReader(new FileReader(testInputFile))))).select();
   }

   public WekaFeatureSelector(WekaDataSet dataset) {
      super(dataset);
   }

   public String select() {
      return wekaClassifierSearchFS();

   }

   /*
   This uses mariaFS filter
    */
   @Deprecated
   private String wekaFilterFS() {
      weka.filters.supervised.attribute.AttributeSelection filter = new weka.filters.supervised.attribute.AttributeSelection();  // package weka.filters.supervised.attribute!
      CfsSubsetEval eval = new CfsSubsetEval();

      //search.setVerbose(true);
      BestFirst search = new BestFirst();

      //search.setSearchBackwards(true);
      filter.setEvaluator(eval);
      filter.setSearch(search);
      Instances data = dataset.getSamples();
      try {
         filter.setInputFormat(data);
         // generate new data
         Instances newData = Filter.useFilter(data, filter);
         trace("filtered data\n" + newData);
         SMOreg base = new SMOreg();
         base.setOptions(Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-12 -N 0 -V -1 -W 1 -K weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0"));
         Evaluation evaluation = new Evaluation(newData);
         evaluation.crossValidateModel(base, newData, 10, new Random(1));
         info(evaluation.toSummaryString());
         Thread.sleep(1000);
      } catch (Exception e) {
         return null;
      }

      return null;
   }

   private boolean wrapper = true;

   private String wekaClassifierSearchFS() {
      Instances data = (dataset).getSamples();
      int numAttr = data.numAttributes();
      SubsetEvaluator subsetEvaluator;
      try {
         //The base classifier which I want to use eventually
         Classifier base = vanillaClassifier();
         //The search method
         GreedyStepwise search = new GreedyStepwise();
         search.setSearchBackwards(backwardSearch);
         //The wrapper to perform the search
         WrapperSubsetEval classifier = new WrapperSubsetEval();
         //ClassifierSubsetEval classifier = new ClassifierSubsetEval();
         classifier.setFolds(10);
         classifier.buildEvaluator(data);
         classifier.setClassifier(base);


         // base.setOptions(Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-12 -N 0 -V -1 -W 1 -K weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0"));
         classifier.setClassifier(base);
         //classifier.setEvaluator(cse);
         info("Performing attributes search with " + search.toString());
         int[] setAttrib_wrp_nbc = search.search(classifier, data);
         //System.out.println("Search finished " + search.toString());
         /*
         It seems that the returned list has 0 as base, whereas the rest of the APIs take 1 as base
         so I add 1 to each feature

         In the end, I have to add the target feature, otherwise it gets removed and everything
         gets  screwed up!
          */
         for (int i = 0; i < setAttrib_wrp_nbc.length; i++) {
            setAttrib_wrp_nbc[i]++;
         }
         String ret = Arrays.toString(setAttrib_wrp_nbc);
         ret = ret.replace("[", "");
         ret = ret.replace("]", "");
         ret = ret.replace(" ", "");
         ret = ret.concat("," + numAttr);
         return ret;
      } catch (Exception e) {
         e.printStackTrace();  // TODO: Customise this generated block
      }

      return null;
   }

   protected abstract Classifier vanillaClassifier() throws Exception;

   protected abstract Learner buildAndTrain(DataSet data) throws DataSetBuildException;
}
