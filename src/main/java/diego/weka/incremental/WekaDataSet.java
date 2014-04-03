package diego.weka.incremental;

import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.RemovePercentage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Random;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class WekaDataSet extends DataSet {

   Instances samples;
   private final static boolean allowDuplicates = true;


   public WekaDataSet(File f) {
      super(f);
   }

   public Instances getSamples() {
      return samples;
   }

   public WekaDataSet(Instances instances) {
      this.samples = instances;
   }

   @Override
   protected void buildDataSet() throws DataSetBuildException {
      try {
         BufferedReader reader = new BufferedReader(
               new FileReader(f.getAbsolutePath()));
         samples = new Instances(reader);
         //Set the target feature to be the last one
         samples.setClassIndex(samples.numAttributes() - 1);
         reader.close();
      } catch (Exception e) {
         throw new DataSetBuildException(e.getMessage());
      }
   }

   private boolean contains(Instance in) {
      for (int i = 1; i < samples.numInstances(); i++) {
         if (samples.instance(i).equals(in))
            return true;
      }
      return false;
   }

   protected void merge(DataSet dataSe) {
      WekaDataSet dataSet = (WekaDataSet) dataSe;
      int s = dataSet.samples.numInstances();
      Instance instance;
      for (int i = 0; i < s; i++) {
         instance = dataSet.samples.instance(i);
         if (!contains(instance))
            samples.add(instance);
      }
   }

   protected WekaDataSet subDataSet(int percentage) {

      //r.setNoReplacement(true);
      try {

         samples.randomize(new Random(System.nanoTime()));
         RemovePercentage removePercentage = new RemovePercentage();
         removePercentage.setPercentage(100 - percentage);
         /*Randomize randomize = new Randomize();
         randomize.setInputFormat(samples);
         randomize.setRandomSeed(new Random(100000).nextInt());
         Instances newI = Filter.useFilter(samples, randomize);  */
         //System.out.println("POST " + samples);
         removePercentage.setInputFormat(samples);
         WekaDataSet wd = new WekaDataSet(Filter.useFilter(samples, removePercentage));
         //System.out.println(wd.size());
         //System.out.println(wd.toString());
         return wd;
      } catch (Exception e) {
         e.printStackTrace();
         throw new DataSetBuildException(e.getMessage());
      }

   }

   @Override
   protected void remove(DataSet d) {
     WekaDataSet w = (WekaDataSet) d;
      Instance instanceJ;
      // it's important to iterate from last to first, because when we remove
      // an instance, the rest shifts by one position.
      for (int j = 0; j < w.samples.numInstances(); j++) {
         instanceJ = w.samples.instance(j);
         this.remove(instanceJ);
      }
   }

   private void remove(Instance instanceJ) {
      Instance instanceI;
      for (int i = samples.numInstances() - 1; i >= 0; i--) {
         instanceI = samples.instance(i);
         trace("Comparing " + instanceI + " with " + instanceJ);
         if (instanceI.toString().equals(instanceJ.toString())) {
            trace("Equals!");
            samples.delete(i);
            if (allowDuplicates) return;
         }
      }
   }

   public String toStringAll() {
      return "WekaDataSet{" +
            "samples=" + samples.toString() +
            '}';
   }

   public String toString() {
      return Collections.list(samples.enumerateInstances()).toString();
   }

   @Override
   protected long size() {
      return this.samples.numInstances();
   }

   public void dumpSet(String toFile) {
      BufferedWriter writer = null;
      try {
         File to = new File(toFile);

         writer = new BufferedWriter(new FileWriter(to));
         for (int i = 0; i < samples.numInstances(); i++) {
            writer.write(samples.instance(i).toString() + "\n");
         }

      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         if (writer != null)
            try {
               writer.close();
            } catch (IOException e) {
               e.printStackTrace();  // TODO: Customise this generated block
            }
      }
   }


}
