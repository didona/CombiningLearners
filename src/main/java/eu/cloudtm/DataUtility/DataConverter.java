/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.cloudtm.DataUtility;

import eu.cloudtm.autonomicManager.commons.EvaluatedParam;
import eu.cloudtm.autonomicManager.commons.ForecastParam;
import eu.cloudtm.autonomicManager.commons.Param;
import eu.cloudtm.autonomicManager.oracles.InputOracle;
import org.apache.log4j.Logger;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.converters.ConverterUtils.DataSource;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ennio email:ennio_torre@hotmail.it
 */
public class DataConverter {
   private static Logger logger = Logger.getLogger(DataConverter.class.getName());
   private static String arff = "conf/K-NN/dataset.arff";

   public static void updateArff(String n) {
      System.out.println("DataConverter: updating to " + n);
      arff = n;
   }

   public static DataInputOracle FromInstancesToInputOracle(Instance data) throws Exception {


      HashMap<Param, Object> param = new HashMap<Param, Object>();
      HashMap<EvaluatedParam, Object> evaluatedparam = new HashMap<EvaluatedParam, Object>();
      HashMap<ForecastParam, Object> forecastparam = new HashMap<ForecastParam, Object>();
      double[] instValue = data.toDoubleArray();
      Object ForecastValue, EvaluatedValue;
      String parameter;
      DataSource source = new DataSource(arff);

      for (int i = 0; i < source.getDataSet().numAttributes(); i++) {
         parameter = source.getDataSet().attribute(i).name();

         try {

            ForecastValue = ParameterClassConversion.ConvertTo(ForecastParam.valueOf(parameter), instValue[i]);

            forecastparam.put(ForecastParam.valueOf(parameter), ForecastValue);
         } catch (IllegalArgumentException e) {

            try {

               // System.out.println((EvaluatedParam.valueOf(parameter))+" ; "+value);
               EvaluatedValue = ParameterClassConversion.ConvertTo(EvaluatedParam.valueOf(parameter), instValue[i]);
               evaluatedparam.put(EvaluatedParam.valueOf(parameter), EvaluatedValue);
            } catch (IllegalArgumentException ef) {

               try {


                  param.put(Param.valueOf(parameter), instValue[i]);
                  //System.out.println((Param.valueOf(parameter))+" ; "+value);

               } catch (IllegalArgumentException ex) {
                  logger.warn("--" + ex.getMessage() + "--" + parameter + " is not a valid parameter");
                  throw new IllegalArgumentException(parameter + " is not a valid parameter");
               }
            }


         }
      }

      return new DataInputOracle(param, evaluatedparam, forecastparam);
   }


   public static Instance FromInputOracleToInstance(InputOracle input) throws Exception {

      DataSource source = new DataSource(arff);
      //DataSource source = new DataSource("conf/batch/dataset.arff");
      Instance inst = new DenseInstance(source.getStructure().numAttributes());
      double ParamValue;
      Number ForecastValue, EvaluatedParamValue;


      for (int i = 0; i < source.getStructure().numAttributes(); i++) {
         String parameter = source.getStructure().attribute(i).name();
         System.out.println(parameter + "_1");

         try {

            ParamValue = new Double(input.getParam(Param.valueOf(parameter)) + "");
            inst.setValue(source.getStructure().attribute(i), ParamValue);
            System.out.println(parameter + "_2");

         } catch (IllegalArgumentException e) {

            try {
               System.out.println(parameter + "_3");
               ForecastValue = ParameterClassConversion.ConvertTo(ForecastParam.valueOf(parameter), input.getForecastParam(ForecastParam.valueOf(parameter)));

               inst.setValue(source.getStructure().attribute(i), ForecastValue.doubleValue());

            } catch (IllegalArgumentException ef) {

               try {
                  System.out.println(parameter + "_4");
                  EvaluatedParamValue = ParameterClassConversion.ConvertTo(EvaluatedParam.valueOf(parameter), input.getEvaluatedParam(EvaluatedParam.valueOf(parameter)));

                  inst.setValue(source.getStructure().attribute(i), EvaluatedParamValue.doubleValue());
               } catch (IllegalArgumentException ex) {
                  logger.warn("--" + ex.getMessage() + "--" + parameter + " is not a valid parameter");
                  throw new IllegalArgumentException(parameter + " is not a valid parameter");
               }
            }

         }


      }

      //System.out.println(inst);

      return inst;

   }

   public static DataInputOracle FromInputOracleToDataInputOracle(InputOracle data) throws Exception {


      HashMap<Param, Object> param = new HashMap<Param, Object>();
      HashMap<EvaluatedParam, Object> evaluatedparam = new HashMap<EvaluatedParam, Object>();
      HashMap<ForecastParam, Object> forecastparam = new HashMap<ForecastParam, Object>();

      Object ForecastValue, EvaluatedValue;
      String parameter;
      DataSource source = new DataSource(arff);

      for (int i = 0; i < source.getDataSet().numAttributes(); i++) {
         parameter = source.getDataSet().attribute(i).name();

         try {

            ForecastValue = data.getForecastParam(ForecastParam.valueOf(parameter));

            forecastparam.put(ForecastParam.valueOf(parameter), ForecastValue);
         } catch (IllegalArgumentException e) {

            try {

               // System.out.println((EvaluatedParam.valueOf(parameter))+" ; "+value);
               EvaluatedValue = data.getEvaluatedParam(EvaluatedParam.valueOf(parameter));
               evaluatedparam.put(EvaluatedParam.valueOf(parameter), EvaluatedValue);
            } catch (IllegalArgumentException ef) {

               try {


                  param.put(Param.valueOf(parameter), data.getParam(Param.valueOf(parameter)));
                  //System.out.println((Param.valueOf(parameter))+" ; "+value);

               } catch (IllegalArgumentException ex) {
                  logger.warn("--" + ex.getMessage() + "--" + parameter + " is not a valid parameter");
                  throw new IllegalArgumentException(parameter + " is not a valid parameter");
               }
            }


         }
      }

      return new DataInputOracle(param, evaluatedparam, forecastparam);
   }


   static class DataInputOracle implements DataInputOracleInterface {

      private HashMap<Param, Object> param;
      private HashMap<EvaluatedParam, Object> evaluatedparam;
      private HashMap<ForecastParam, Object> forecastparam;

      public DataInputOracle(HashMap<Param, Object> param, HashMap<EvaluatedParam, Object> evaluatedparam, HashMap<ForecastParam, Object> forecastparam) {

         this.param = param;
         this.evaluatedparam = evaluatedparam;
         this.forecastparam = forecastparam;
         //System.out.println("data set created");
      }

      @Override
      public Object getParam(Param param) {

         return this.param.get(param);
      }

      @Override
      public Object getEvaluatedParam(EvaluatedParam ep) {

         return this.evaluatedparam.get(ep);
      }

      @Override
      public Object getForecastParam(ForecastParam fp) {

         return this.forecastparam.get(fp);
      }

      @Override
      public String toString() {

         String stringP = "", stringEP = "", stringFP = "";

         for (Map.Entry<Param, Object> entry : param.entrySet()) {

            stringP = stringP + entry.getKey() + ":" + entry.getValue() + "\n";

         }

         for (Map.Entry<EvaluatedParam, Object> entry : evaluatedparam.entrySet()) {

            stringEP = stringEP + entry.getKey() + ":" + entry.getValue() + "\n";

         }

         for (Map.Entry<ForecastParam, Object> entry : forecastparam.entrySet()) {

            stringFP = stringFP + entry.getKey() + ":" + entry.getValue() + "\n";

         }
         return stringP + stringEP + stringFP;

      }

   }

}
    

