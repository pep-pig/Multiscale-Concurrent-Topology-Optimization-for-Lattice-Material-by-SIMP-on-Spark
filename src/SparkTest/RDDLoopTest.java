package SparkTest;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RDDLoopTest {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("ConectionTest").setMaster("spark://master:7077").setJars(new String[]{"H:\\OneDrive\\毕业论文\\multiscaleOptimization-Spark\\out\\artifacts\\multiscaleOptimization_jar\\multiscaleOptimization.jar"}).set("spark.driver.host","115.156.249.7");
        //SparkConf conf = new SparkConf().setAppName("ConectionTest").setMaster("spark://master:7077").set("spark.driver.host","115.156.249.7");
        //SparkConf conf = new SparkConf().setAppName("LocalModeTest").setMaster("local");
        JavaSparkContext jsc = new JavaSparkContext(conf);
        List<Integer> testList = new ArrayList<Integer>();
        for (int i = 0; i < 10; i++) {
            testList.add(i);
        }
        List<Integer> output;
        int iterateNum=0;
        JavaRDD<Integer> inputRDD = jsc.parallelize(testList,1);
        inputRDD.cache();
        while(iterateNum<10){
            output = inputRDD.map(new Function<Integer, Integer>() {
                public Integer call(Integer v1) throws Exception {
                    int v2 = v1+2;
                    System.out.println("V2 is "+v2);
                    return v1 + 1;
                }
            }).collect();
            System.out.println(output);
            inputRDD = jsc.parallelize(output);
            iterateNum=iterateNum+1;
        }
    }
}


