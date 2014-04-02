/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.weka.ensemble;

import eu.cloudtm.DataUtility.DataPrinting;
import eu.cloudtm.DataUtility.OutputsMap;
import eu.cloudtm.autonomicManager.oracles.exceptions.OracleException;
import eu.cloudtm.weka.Cubist.Cubist;
import org.apache.log4j.Logger;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.MultipleClassifiersCombiner;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMOreg;
import weka.core.Capabilities;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ennio
 */
public class EnsembleML extends MultipleClassifiersCombiner {
   static Logger logger = Logger.getLogger(Ensemble.class.getName());


   protected AbstractClassifier classifier;
   private final static int counter = 0;
   private static HashMap<String, Integer> register = new HashMap();
   private String Target = null;
   private ArrayList<Classifier> clList;
   Instances i;

   private final static boolean batch = true;

   static {
      if (!batch) {
         int i = 0;
         for (OutputsMap m : OutputsMap.values()) {

            register.put(m.toString(), counter);
            logger.info(m.toString() + "added to Ensembe class");
            System.out.println(m.toString() + "added to Ensembe class");
         }
      } else {

         register.put("AvgGetsPerWrTransaction", 0);
         System.out.println(register.toString());
      }

   }


   public EnsembleML() {

      super();
      AbstractClassifier[] cl = {new SMOreg(), new Cubist()};
      this.setClassifiers(cl);
   }

   @Override
   public void buildClassifier(Instances i) throws Exception {
      try {
         this.i = i;
         getCapabilities().testWithFail(i);
         Target = i.classAttribute().name();
         testonTarget();
         System.out.println("Fetching " + Target);

         int temp = register.get(Target);
         System.out.println("temp " + temp);
         if ((temp < getClassifiers().length)) {
            //set the classifier from the list
            String Options[] = {"-I"};
            classifier = (AbstractClassifier) this.getClassifier(temp);
            System.out.println("PRE EnsembleML classifier "+classifier);
            if (classifier instanceof MultilayerPerceptron)
               ((MultilayerPerceptron) classifier).setNormalizeAttributes(false);
            //classifier.setOptions(Options);
            classifier.buildClassifier(i);
            System.out.println("POST EnsembleML classifier "+classifier);
            temp++;
            logger.info("@" + Ensemble.class + "  " + classifier.getClass().getName() + " instance has been generated " +
                              "for" + Target + " prediction @");
            System.out.println("@" + this.getClass() + "  " + classifier.getClass().getName() + " instance has been generated " +
                                     "for" + Target + " prediction @");

         } else {
            //if Iterations>num classifiers instances set the last in the list as classifier
            /*int CLindex=getClassifiers().length-1;
            classifier=classifier=this.getClassifier(CLindex);
            classifier.buildClassifier(i);
            temp++;*/
            logger.info("@" + Ensemble.class + "  " + classifier.getClass().getName() + " instance has been generated " +
                              "for" + Target + " prediction @");
         }

         register.put(Target, temp);
         logger.info("@ " + classifier.getClass().getName() + " classifier built @");

      } catch (OracleException e) {
         e.printStackTrace();
         throw new Exception(e);

      } catch (Exception e) {
         e.printStackTrace();
         throw new Exception(e);
      }

   }

   @Override
   public double classifyInstance(Instance instnc) throws Exception {
      double Outputs[] = new double[4];
      double both[] = DataPrinting.addTwoArray(instnc.toDoubleArray(), Outputs);
      Instance I = new DenseInstance(1, both);
      return classifier.classifyInstance(I);// instnc.value(instnc.numAttributes()-1);
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
   public void setOptions(String[] s) {

      int i = 0;
      for (String d : s) {

         if (d.equals("target")) {
            Target = s[i + 1];
            break;
         }
         i++;
      }

   }

   private void testonTarget() throws Exception {

      if (Target == null)
         throw new Exception(Ensemble.class.getName() + "target not set");
   }

   private String getTarget(Instances i) throws Exception {

      String tar;
      tar = i.classAttribute().name();
      tar = OutputsMap.valueOf(tar).getTarget();
      //tar=tar.substring(0,tar.length()-3);
      return tar;
   }

   @Override
   public String toString() {

      String info = "Ensemble Info Resume \n";
      for (Map.Entry<String, Integer> e : register.entrySet())
         info += "[ For-> " + e.getKey() + " prediction " + e.getKey() + " oracle inst. ]\n";

      return info;
   }
}
