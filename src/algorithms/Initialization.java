package algorithms;

import org.apache.spark.api.java.function.PairFunction;
import org.jblas.DoubleMatrix;
import scala.Tuple2;
import scala.Tuple3;

import java.util.ArrayList;

public class Initialization implements PairFunction<Tuple2<Integer, Tuple3<ArrayList<DoubleMatrix>, DoubleMatrix, ArrayList<DoubleMatrix>>>, Integer, Tuple3<ArrayList<DoubleMatrix>, DoubleMatrix, ArrayList<DoubleMatrix>>>{
    private int cellModelNely;
    private int cellModelNelx;
    public Initialization(int cellModelNely,int cellModelNelx){
        this.cellModelNelx=cellModelNelx;
        this.cellModelNely=cellModelNely;
    }
    @Override
    public Tuple2<Integer, Tuple3<ArrayList<DoubleMatrix>, DoubleMatrix, ArrayList<DoubleMatrix>>> call(Tuple2<Integer, Tuple3<ArrayList<DoubleMatrix>, DoubleMatrix, ArrayList<DoubleMatrix>>> macroInputRDD) throws Exception {
        ArrayList<DoubleMatrix> microDensity = macroInputRDD._2._3();
        DoubleMatrix macroDensity = macroInputRDD._2._2();
        ArrayList<DoubleMatrix> C = macroInputRDD._2._1();
        for(int ele = 0;ele<microDensity.size();ele++){
            microDensity.set(ele,DoubleMatrix.ones(cellModelNely,cellModelNelx).mul(macroDensity.get(ele)));
        }
        return new Tuple2<>(1, new Tuple3<>(C, macroDensity, microDensity));
    }
}

