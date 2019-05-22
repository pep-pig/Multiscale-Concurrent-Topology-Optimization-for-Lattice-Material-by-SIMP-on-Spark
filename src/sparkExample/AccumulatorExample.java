package sparkExample;

import algorithms.ComplianceAccumulatorV2;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AccumulatorExample implements Serializable {
    public static void main(String[] args){
        SparkConf conf = new SparkConf().setAppName("MultiscaleOptimization").setMaster("local[4]");
        JavaSparkContext jsc = new JavaSparkContext(conf);
        ComplianceAccumulatorV2 complianceAccumulatorV2 = new ComplianceAccumulatorV2();
        jsc.sc().register(complianceAccumulatorV2,"testAccumulator");
        List<Integer> testList = new ArrayList<Integer>();
        for (int i = 0; i < 4; i++) {
            testList.add(i);
        }
        JavaRDD<Integer> inputRDD = jsc.parallelize(testList,4);
        List<Integer> output  = inputRDD.map((Function<Integer, Integer>) integer -> {
            String string = "i love you";
            complianceAccumulatorV2.add(string);
            return integer*integer;
        }).collect();
        System.out.println(complianceAccumulatorV2.value());
    }
}
