/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.Configuration;

import org.apache.log4j.Logger;

/**
 * @author ennio
 */
public class BoostingConfiguration extends Configuration {
   static Logger logger = Logger.getLogger(BoostingConfiguration.class.getName());
   protected static BoostingConfiguration myself;


   public BoostingConfiguration() {

      super("conf/Boosting/Boosting.properties");
   }

   public BoostingConfiguration(String conf) {
      super(conf);
   }

   public String getIteration() {

      String o = defaultProps.getProperty("Iterations");
      checkvalue(o);
      return o;
   }


   public String getCombiner() {

      String o = defaultProps.getProperty("combiner");
      checkvalue(o);
      return o;

   }

   public String getTarget() {

      String o = defaultProps.getProperty("Target");
      checkvalue(o);
      return o;

   }

   public static BoostingConfiguration getInstance() {
      return (myself == null) ? myself = new BoostingConfiguration() : myself;
   }


}

