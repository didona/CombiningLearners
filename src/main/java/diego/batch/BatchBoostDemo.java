package diego.batch;

import eu.cloudtm.DataUtility.DataConverter;
import eu.cloudtm.autonomicManager.oracles.InputOracle;
import eu.cloudtm.boosting.Dataset;
import org.apache.log4j.PropertyConfigurator;
import weka.core.Instance;

/**
 * @author Diego Didona
 * @email didona@gsd.inesc-id.pt
 * @since 02/04/14
 */
public class BatchBoostDemo {

   public static void main(String[] args) throws Exception {
      // TODO code application logic here

      System.out.println("Hello World!");
      PropertyConfigurator.configure("conf/log4j.properties");

      //Boosting
      System.out.println("Updating arff");
      DataConverter.updateArff("conf/batch/dataset.arff");
      System.out.println("Generating dataset");
      Dataset d = new BatchDataSet("batch_csv"); //Boosting dataset
      System.out.println("Boosting");
      BatchBoosting b = new BatchBoosting();

      //Knn
           /*
           DataSets dataknn=new DataSets("csvfile");//Knn dataset
           Knearestneighbourg k=new Knearestneighbourg();

           //Probing

            ProbingDataSets dataP=new ProbingDataSets("csvfile"); //Probing dataset
            Probing P= new Probing();
            */
      //simple results
      for (Instance i : Dataset.DataBoosting) {
         double real = i.value(i.numAttributes() - 1);
         InputOracle io = DataConverter.FromInstancesToInputOracle(i);
         double pred = b.forecast(io).responseTime(0);
         System.out.println(real + "," + pred);
      }
      /*
      System.out.println(Dataset.DataBoosting);
      Instance i = Dataset.DataBoosting.instance(1);
      System.out.println(i);
      InputOracle io = DataConverter.FromInstancesToInputOracle(i);
      System.out.println(io);
      System.out.println("Target Value" + i.value(i.numAttributes() - 1));
      //System.out.println("KNN prediction "+(k.forecast(io).throughput(1)+k.forecast(io).throughput(0)));
      System.out.println("Boosting prediction" + (b.forecast(io).responseTime(0)));
      //System.out.println("Probing prediction"+(P.forecast(io).throughput(0)+P.forecast(io).throughput(1)));

      */
   }
}
