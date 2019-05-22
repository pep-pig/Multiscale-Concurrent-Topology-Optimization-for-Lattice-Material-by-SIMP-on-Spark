package sparkExample;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;

import java.util.ArrayList;
import java.util.List;

public class RDDLoopTest {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("MultiscaleOptimization").setMaster("local[4]");
        //SparkConf conf = new SparkConf().setAppName("ConectionTest").setMaster("spark://master:7077").set("spark.driver.host","115.156.249.7");
        //SparkConf conf = new SparkConf().setAppName("LocalModeTest").setMaster("local");
        JavaSparkContext jsc = new JavaSparkContext(conf);
        List<Integer> testList = new ArrayList<Integer>();
        for (int i = 0; i < 4; i++) {
            testList.add(i);
        }
        List<Integer> output;
        int iterateNum=0;
        Broadcast<BroadTest> broadTest = jsc.broadcast(new BroadTest());
        JavaRDD<Integer> inputRDD = jsc.parallelize(testList,4);
        inputRDD.cache();
        while(iterateNum<10){
            output = inputRDD.map(new AddFunction(broadTest)).collect();
            System.out.println(output);
            inputRDD = jsc.parallelize(output);
            iterateNum=iterateNum+1;
        }
    }
}


