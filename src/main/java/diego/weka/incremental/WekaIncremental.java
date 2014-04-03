package diego.weka.incremental;

import diego.weka.ModelResult;
import diego.weka.learners.Learner;
import diego.weka.learners.M5Rules.M5RFeatureSelector;
import diego.weka.learners.M5Rules.M5RLearner;
import diego.weka.learners.MultiLayerPerceptron.MLPFeatureSelector;
import diego.weka.learners.MultiLayerPerceptron.MLPLearner;
import diego.weka.learners.WekaFeatureSelector;
import diego.weka.learners.smoReg.SmoRegLearner;
import diego.weka.learners.smoReg.SmoRegWekaFeatureSelector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class WekaIncremental {
   final private File baseTrainingSetFile; //arff file with immutable training set
   final private File testSetFile; //arff file with test set
   final private int percentage; //percentage of test set to be added to trainingSet
   final private boolean removeFromTest; //if true, remove the selected from test
   private final static Log log = LogFactory.getLog(WekaIncremental.class);
   private final static boolean trace = log.isTraceEnabled();
   private final static boolean info = log.isInfoEnabled();
   private final static boolean warn = log.isWarnEnabled();

   private DataSet trainingSet;
   private DataSet testSet;
   private String features;

   private WL wl = WL.SMOREG;
   private final static boolean onlyDumpFiles = false;
   private static int epoch = 0;

   public WekaIncremental(File baseTrainingSetFile, File testSetFile, int percentage, boolean removeFromTest, String wl) {
      this.baseTrainingSetFile = baseTrainingSetFile;
      this.testSetFile = testSetFile;
      this.percentage = percentage;
      this.removeFromTest = removeFromTest;
      this.wl = WL.valueOf(wl);
      warn(this.wl.toString());
   }

   public final ModelResult buildModel() {
      generateTrainTestSet();
      if (onlyDumpFiles) {
         return new ModelResult(0, 0);
      }
      this.features = performFeatureSelection(trainingSet);
      warn("Final features are " + features);
      Learner r = trainLearner(trainingSet, features);
      info("Training error " + r.getTen_fold_error());
      try {
         warn("ERR " + r.wekaMeanAbsoluteRelativeError(testSet));
         return new ModelResult(r.wekaMeanAbsoluteRelativeError(testSet), testSet.size());
      } catch (Exception e) {
         e.printStackTrace();
      }
      return null;
   }


   private void generateTrainTestSet() {
      info("Creating base training set " + baseTrainingSetFile.getAbsolutePath());
      DataSet train = buildDataSet(baseTrainingSetFile);
      train.buildDataSet();
      trace(train.toString());
      info("Creating test set  " + testSetFile.getAbsolutePath());
      DataSet _test = buildDataSet(testSetFile);
      _test.buildDataSet();
      trace(_test.toString());
      info("Creating additional training set with percentage " + percentage);
      DataSet additionalTrain = _test.subDataSet(percentage);
      trace(additionalTrain.toString());
      info("Going to merge base training with the additional one");
      train.merge(additionalTrain);
      trace(train.toString());
      //If I give all the test as train, I want to evaluate on all the test set, otherwise I have an empty bucket
      if (!(percentage == 100) && removeFromTest) {
         info("Going to remove additional training samples from the test set");
         _test.remove(additionalTrain);
         trace(_test.toString());
      }
      this.trainingSet = train;
      this.testSet = _test;
      if (onlyDumpFiles) {
         ((WekaDataSet) trainingSet).dumpSet("data/cubist/train_" + percentage + "_" + epoch + ".data");
         ((WekaDataSet) testSet).dumpSet("data/cubist/test_" + percentage + "_" + epoch + ".data");
         if (percentage == 100) epoch++;
      }
      warn("Training set has size " + trainingSet.size() + " test set " + testSet.size());
   }

   private String performFeatureSelection(DataSet d) {
      WekaFeatureSelector fs;
      WekaDataSet wd = (WekaDataSet) d;
      switch (wl) {
         case SMOREG:
            fs = new SmoRegWekaFeatureSelector(wd);
            break;
         case MLP:
            fs = new MLPFeatureSelector(wd);
            break;
         case M5P:
            fs = new M5RFeatureSelector(wd);
            break;
         default:
            throw new IllegalArgumentException(wl + " not supported");
      }

      String features = fs.select();
      info("Selected features " + features);
      return features;
   }

   protected Learner trainLearner(DataSet d, String features) {
      try {
         switch (wl) {
            case SMOREG:
               return new SmoRegLearner(d, features);
            case MLP:
               return new MLPLearner(d, features);
            case M5P:
               return new M5RLearner(d, features);
            default:
               throw new IllegalArgumentException(wl + " not supported");
         }


      } catch (Exception e) {
         e.printStackTrace();  // TODO: Customise this generated block
      }
      return null;
   }

   protected DataSet buildDataSet(File f) {
      return new WekaDataSet(f);
   }

   private void info(String s) {
      if (info)
         log.info(s);
   }

   private void trace(String s) {
      if (trace)
         log.trace(s);
   }

   private void warn(String s) {
      if (warn)
         log.warn(s);
   }

   private enum WL {
      SMOREG, MLP, M5P, KNN
   }
}
