/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.boosting;


import eu.cloudtm.Configuration.BoostingConfiguration;
import eu.cloudtm.DataUtility.DataConverter;
import eu.cloudtm.DataUtility.OutputsMap;
import eu.cloudtm.autonomicManager.commons.Param;
import eu.cloudtm.autonomicManager.oracles.InputOracle;
import eu.cloudtm.autonomicManager.oracles.Oracle;
import eu.cloudtm.autonomicManager.oracles.OutputOracle;
import eu.cloudtm.autonomicManager.oracles.exceptions.OracleException;
import eu.cloudtm.weka.ensemble.Ensemble;
import weka.classifiers.meta.AdditiveRegression;
import weka.core.Instance;

/**
 *
 * @author ennio
 */
public class Boosting implements Oracle {

    static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Boosting.class.getName()); 
    private Instance instc;
    private InputOracle inputOracle;
    private AdditiveRegression[] adR;
  
    private boolean throughput=false,abort=false,
            responsetimeRO=false,responsetimeWO=false;
    
   public Boosting() throws Exception{

       if(Dataset.DataBoosting==null)
           throw new Exception("Boosting Dataset not present");
       adR=new AdditiveRegression[4];
      
       String  classifier=BoostingConfiguration.getInstance().getCombiner();
       String Iteration=BoostingConfiguration.getInstance().getIteration();
       
       String []Options={"-W",classifier,"-I",Iteration};
       
       
       for(int i=0;i<adR.length;i++){
       
           adR[i]=new AdditiveRegression();//new Ensemble());
       
           adR[i].setOptions(Options);
       }
       
   }
    
    @Override
    public OutputOracle forecast(InputOracle io) throws OracleException {
       
        try {
            inputOracle=io;
            instc=DataConverter.FromInputOracleToInstance(io);
            
            return new BoostingOutputOracle();
        } catch (Exception ex) {
           logger.error("Error During Boosting forecasting"+ ex);
        
           throw new OracleException(Boosting.class.getName()+ex);
        }
    }
   
    
    
    
    private class BoostingOutputOracle implements OutputOracle{

        @Override
        public double throughput(int i) {
            try {
                
                if(!throughput)// the learner is generated only once for each target;
                {
                    Dataset.DataBoosting.setClassIndex(Dataset.DataBoosting.numAttributes()- OutputsMap.throughput.getArffDispacement());
                    adR[0].buildClassifier(Dataset.DataBoosting);
                    throughput=true;
                    logger.info("-----Boosting-----");
                    logger.info("-----Oracle created-----");
                }
                double writeperc=(Double)inputOracle.getParam(Param.PercentageSuccessWriteTransactions);
               
                if (i==0){
            
                    return adR[0].classifyInstance(instc)*(1-writeperc);
                }
            
                else{
            
                    return adR[0].classifyInstance(instc)*(writeperc);
                }
            } catch (Exception ex) {
                logger.error(this.getClass().getName()+ex);
                ex.printStackTrace();
               throw new RuntimeException("Error During Throughput forecasting");
            }
            
        }

        @Override
        public double abortRate(int i) {
            try {
                
                if(!abort)// the learner is generated only once for each target;
                {
                    Dataset.DataBoosting.setClassIndex(Dataset.DataBoosting.numAttributes()- OutputsMap.abortRate.getArffDispacement());
                    adR[1].buildClassifier(Dataset.DataBoosting);
                    abort=true;
                }
                
                
               
                if (i==0){
            
                    return 0D;
                }
            
                else{
            
                    return adR[1].classifyInstance(instc);
                }
            } catch (Exception ex) {
                logger.error(this.getClass().getName()+ex);
               throw new RuntimeException("Error During AbortRate forecasting");
            }
            
        }

        @Override
        public double responseTime(int i) {
            try {
                
                               
                if (i==0){
                    
                    if(!responsetimeRO)// the learner is generated only once for each target;
                {
                    Dataset.DataBoosting.setClassIndex(Dataset.DataBoosting.numAttributes()- OutputsMap.responseTimeRO.getArffDispacement());
                    adR[2].buildClassifier(Dataset.DataBoosting);
                    responsetimeRO=true;
                }
                    
                    return adR[2].classifyInstance(instc);
                }
            
                else{
            
                     if(!responsetimeWO)// the learner is generated only once for each target;
                {
                    Dataset.DataBoosting.setClassIndex(Dataset.DataBoosting.numAttributes()- OutputsMap.responseTimeWO.getArffDispacement());
                    adR[3].buildClassifier(Dataset.DataBoosting);
                    responsetimeWO=true;
                }
                    
                    return adR[3].classifyInstance(instc);
                }
            } catch (Exception ex) {
                logger.error(this.getClass().getName()+ex);
               throw new RuntimeException("Error During ResponseTime forecasting");
            }
            
        }

        @Override
        public double getConfidenceThroughput(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public double getConfidenceAbortRate(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public double getConfidenceResponseTime(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    
    
    }
    
    
    
}
