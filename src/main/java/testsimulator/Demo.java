/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testsimulator;

import eu.cloudtm.DataUtility.DataConverter;
import eu.cloudtm.Knn.DataSets;
import eu.cloudtm.Knn.Knearestneighbourg;
import eu.cloudtm.autonomicManager.oracles.InputOracle;
import eu.cloudtm.boosting.Dataset;
import eu.cloudtm.boosting.Boosting;
import eu.cloudtm.probing.Probing;
import eu.cloudtm.probing.ProbingDataSets;
import org.apache.log4j.PropertyConfigurator;
import weka.core.Instance;

/**
 *
 * @author ennio
 */
public class Demo {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        
        System.out.println( "Hello World!" );
        PropertyConfigurator.configure("conf/log4j.properties");
        
        //Boosting
        
        Dataset d=new Dataset("newTasData"); //Boosting dataset
       System.out.println("Boosting");
        Boosting b=new Boosting();
        
        //Knn
        /*
        DataSets dataknn=new DataSets("csvfile");//Knn dataset
        Knearestneighbourg k=new Knearestneighbourg();
        
        //Probing
        
         ProbingDataSets dataP=new ProbingDataSets("csvfile"); //Probing dataset
         Probing P= new Probing();
         */
         //simple results
         
         Instance i=Dataset.DataBoosting.instance(5);
         InputOracle io=DataConverter.FromInstancesToInputOracle(i);
    
         System.out.println("Target Value"+i.value(i.numAttributes()-4));
         //System.out.println("KNN prediction "+(k.forecast(io).throughput(1)+k.forecast(io).throughput(0)));
         System.out.println("Boosting prediction"+(b.forecast(io).throughput(0)+b.forecast(io).throughput(1)));
         //System.out.println("Probing prediction"+(P.forecast(io).throughput(0)+P.forecast(io).throughput(1)));
         
         
    }
}
