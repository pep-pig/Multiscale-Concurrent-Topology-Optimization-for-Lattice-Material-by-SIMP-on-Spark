package algorithms;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFunction;
import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;
import org.jcp.xml.dsig.internal.dom.DOMUtils;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import scala.Int;
import scala.Tuple2;
import scala.Tuple3;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Optimizer {

    Optimizer(){
    }
    public void compute(PostProcess postProcess) {
        SparkConf conf = new SparkConf().setAppName("MultiscaleOptimization").setMaster("local[4]");
//        SparkConf conf = new SparkConf().setAppName("MultiscaleOptimization").setMaster("spark://master:7077").set("spark.driver.host","115.156.249.7")
//                .setJars(new String[]{"H:\\OneDrive\\毕业论文\\multiscaleOptimization-Spark\\out\\artifacts\\multiscaleOptimization_jar\\multiscaleOptimization.jar"});
        JavaSparkContext jsc = new JavaSparkContext(conf);
        FiniteElementAnalysis fem = new FiniteElementAnalysis();
        //Parameter.init(fem);
        long start = System.currentTimeMillis();
        fem.iteration = 0;
        double change = 1.0;
        DoubleMatrix oldMacroDensity ;
        //prepare input data
        Tuple3<ArrayList<DoubleMatrix>,DoubleMatrix,ArrayList<DoubleMatrix>> data = fem.variableInit();
        ArrayList<Tuple2<Integer,Tuple3<ArrayList<DoubleMatrix>,DoubleMatrix,ArrayList<DoubleMatrix>>>> inputData = new ArrayList<>();
        inputData.add(new Tuple2<>(1, data));
        oldMacroDensity = inputData.get(0)._2._2();
        JavaPairRDD<Integer,Tuple3<ArrayList<DoubleMatrix>,DoubleMatrix,ArrayList<DoubleMatrix>>> macroInputRDD = jsc.parallelizePairs(inputData,1);
        while(fem.iteration<fem.macroStopIteration && change>fem.macroStopChangeValue){
            if(fem.iteration<fem.microOptimizationStartIteration){
                List<Tuple2<Integer,Tuple3<ArrayList<DoubleMatrix>, DoubleMatrix,ArrayList<DoubleMatrix>>>> nextInput = macroInputRDD.mapToPair(new SingleMacroOptimization()).collect();
                postProcess.plotGrayscale(postProcess.plotWindow, nextInput.get(0)._2._2().mmul(-1).add(1).toArray2());
                postProcess.plotRealStructure(postProcess.resultWindow, nextInput.get(0)._2._3(), fem.nely);
                change = MatrixFunctions.abs(nextInput.get(0)._2._2().sub(oldMacroDensity).max());
                macroInputRDD = jsc.parallelizePairs(nextInput);
                fem.iteration++;
            }else{
                JavaPairRDD<Integer, Tuple3<DoubleMatrix, Double, DoubleMatrix>> microInputRDD = macroInputRDD.flatMapToPair(new MacroOptimization());
                JavaPairRDD<Integer, Tuple3<DoubleMatrix, Double, DoubleMatrix>> parallelizedMicroInputRDD = microInputRDD.repartitionAndSortWithinPartitions(new microOutputRDDPartitioner(fem.cpu, fem.nelx * fem.nely));
                JavaPairRDD<Integer, Tuple3<DoubleMatrix, Double, DoubleMatrix>> parallelizedMicroOutputRDD = parallelizedMicroInputRDD.mapToPair(new MicroOptimization());
                List<Tuple2<Integer, Tuple3<DoubleMatrix, Double, DoubleMatrix>>> iterationResult = parallelizedMicroOutputRDD.collect();
                ArrayList<Tuple2<Integer, Tuple3<ArrayList<DoubleMatrix>, DoubleMatrix, ArrayList<DoubleMatrix>>>> nextIteratioInputData = merge(iterationResult, fem.nely, fem.nelx);
                postProcess.plotGrayscale(postProcess.plotWindow, nextIteratioInputData.get(0)._2._2().mmul(-1).add(1).toArray2());
                postProcess.plotRealStructure(postProcess.resultWindow, nextIteratioInputData.get(0)._2._3(), fem.nely);
                change = MatrixFunctions.abs(nextIteratioInputData.get(0)._2._2().sub(oldMacroDensity).max());
                macroInputRDD = jsc.parallelizePairs(nextIteratioInputData, 1);
                fem.iteration++;
            }
        }
        jsc.close();
    }
    private ArrayList<Tuple2<Integer, Tuple3<ArrayList<DoubleMatrix>, DoubleMatrix, ArrayList<DoubleMatrix>>>> merge(List<Tuple2<Integer, Tuple3<DoubleMatrix, Double, DoubleMatrix>>> iterationResult,int nely,int nelx) {
        ArrayList<Tuple2<Integer, Tuple3<ArrayList<DoubleMatrix>, DoubleMatrix, ArrayList<DoubleMatrix>>>> nextIterationInputData = new ArrayList<>();
        ArrayList<DoubleMatrix> materialMatrixC = new ArrayList<DoubleMatrix>(iterationResult.size());
        DoubleMatrix macroDensity = new DoubleMatrix(nely,nelx);
        ArrayList<DoubleMatrix> microDensity = new ArrayList<DoubleMatrix>(iterationResult.size());
        for (int ele = 0;ele<iterationResult.size();ele++){
            materialMatrixC.add(iterationResult.get(ele)._2._1());
            macroDensity.put(ele,iterationResult.get(ele)._2._2());
            microDensity.add(iterationResult.get(ele)._2._3());
        }
        Tuple3<ArrayList<DoubleMatrix>, DoubleMatrix, ArrayList<DoubleMatrix>> input = new Tuple3<>(materialMatrixC, macroDensity, microDensity);
        nextIterationInputData.add(new Tuple2<>(1, input));
        return nextIterationInputData;
    }
}

