package algorithms;

import com.google.common.math.DoubleMath;
import org.apache.spark.Partitioner;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.broadcast.Broadcast;
import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;
import scala.Tuple2;
import scala.Tuple3;
import scala.xml.PrettyPrinter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import static org.jblas.MatrixFunctions.sqrt;

public class MacroOptimization extends MacroFiniteElementAnalysis implements PairFlatMapFunction<Tuple2<Integer, Tuple3<ArrayList<DoubleMatrix>,DoubleMatrix,ArrayList<DoubleMatrix>>>, Integer, Tuple3<DoubleMatrix,Double,DoubleMatrix>> {
    MacroOptimization(){
    }
    public Iterator<Tuple2<Integer, Tuple3<DoubleMatrix,Double,DoubleMatrix>>> call(Tuple2<Integer, Tuple3<ArrayList<DoubleMatrix>,DoubleMatrix,ArrayList<DoubleMatrix>>> macroInputRDD) throws Exception {
        Tuple3<DoubleMatrix,DoubleMatrix,ArrayList<DoubleMatrix>> microInput = simp(macroInputRDD._2());
        ArrayList<Tuple2<Integer, Tuple3<DoubleMatrix,Double,DoubleMatrix>>> microInputRDD=new ArrayList<Tuple2<Integer, Tuple3<DoubleMatrix,Double,DoubleMatrix>>>();
        for(int i = 0;i<microInput._3().size();i++){
            microInputRDD.add(new Tuple2<Integer, Tuple3<DoubleMatrix,Double,DoubleMatrix>>(
                    i,new Tuple3<DoubleMatrix,Double,DoubleMatrix>(
                            getElementDisplacement(i,microInput._1(),nodeNumberMatrix),microInput._2().get(i),microInput._3().get(i))));
        }
        return microInputRDD.iterator();
    }
    public Tuple3 simp(Tuple3<ArrayList<DoubleMatrix>,DoubleMatrix,ArrayList<DoubleMatrix>> macroInputRDD){
        ArrayList<DoubleMatrix> microDensity = macroInputRDD._3();
        DoubleMatrix macroDensity = macroInputRDD._2();
        ArrayList<DoubleMatrix> materialMatrixC = macroInputRDD._1();
        //reinitiate micro Density instead of using the result of last iteration
        //1. compute fem element stiffness K
        DoubleMatrix K = assemblyMacroElementStiffnessMatrix(macroDensity,materialMatrixC);
        //2. add boundary conditions
        Map<String, DoubleMatrix> boundaryCondition ;
        if (boundaryConditions == 1){
            boundaryCondition = boundaryCondition1();
        }
        else if (boundaryConditions==2){
            boundaryCondition = boundaryCondition2();
        }
        else{
            boundaryCondition = boundaryCondition3();
        }
        Map<String, DoubleMatrix> linearSystem = implementBoundaryConditions(K, boundaryCondition.get("loadConstrains"), boundaryCondition.get("displacementConstrains"));
        //3. Solve fem linearSystem
        DoubleMatrix macroU = solve(linearSystem.get("K"), linearSystem.get("F"));
        //4. update fem macroDensity
        double macroEnergy = 0;
        DoubleMatrix macroEnergyDerivative = new DoubleMatrix(nely, nelx);
        for (int eln = 0; eln < nelx * nely; eln++) {
            macroEnergy += getMacroElementEnergy(eln, macroU,materialMatrixC,macroDensity);
            macroEnergyDerivative = macroEnergyDerivative.put(eln, getMacroElementEnergyDerivative(eln, macroU,materialMatrixC,macroDensity));
        }
        macroEnergyDerivative = macroFilter(macroEnergyDerivative,macroDensity);
        macroDensity = oc(macroEnergyDerivative,macroDensity);
        double volumeFactor = macroDensity.sum()/(nelx*nely);
        System.out.println("MacroIteration:"+iteration+";  macroEnergy:"+macroEnergy+";  volumeFactor:"+volumeFactor);
        return new Tuple3<DoubleMatrix,DoubleMatrix,ArrayList<DoubleMatrix>>(macroU,macroDensity,microDensity);
    }
    public DoubleMatrix macroFilter(DoubleMatrix macroDc,DoubleMatrix macroDensity){
        DoubleMatrix modifiedEnergyDerivative = new DoubleMatrix(nely,nelx);
        double eleLength = length/nelx;
        double[][] dist = new double[][]{{eleLength*Math.sqrt(2),eleLength,eleLength*Math.sqrt(2)},
                {eleLength,0,eleLength},
                {eleLength*Math.sqrt(2),eleLength,eleLength*Math.sqrt(2)},
        };
        for(int i=0;i<nelx;i++){
            for(int j=0;j<nely;j++){
                double modifiedDemoninator = 0;
                double modifiedNumerator = 0;
                for(int m=i-1, k=0;m<=i+1;m++,k++){
                    for(int n=j-1,l=0;n<=j+1;n++,l++ ){
                        if (m<0 || n<0 || m>=nelx || n>=nely){
                            continue;
                        }
                        modifiedDemoninator += (filterRadius-dist[l][k])*(macroDensity.get(n,m))*(macroDc.get(n,m));
                        modifiedNumerator+=filterRadius-dist[l][k];
                    }
                }
                modifiedEnergyDerivative.put(j,i,modifiedDemoninator/(modifiedNumerator*macroDensity.get(j,i)));
            }
        }
        return modifiedEnergyDerivative;
    }
    public DoubleMatrix oc(DoubleMatrix dc,DoubleMatrix density){
        double L1 = 0;
        double L2 = 1E5;
        double lMid;
        double volume = volf*nelx*nely;
        DoubleMatrix newDensity = new DoubleMatrix(density.getRows(),density.getColumns());
        while(L2-L1>1E-6){
            lMid = 0.5*(L2+L1);
            newDensity =(density.add(macroOcMove).min(density.mul(sqrt(dc.mul(-1).div(lMid))))).min(macroOcDensityUpperLimit).max(density.sub(macroOcMove)).max(macroOcDensityLowerLimit);
            if(newDensity.sum()-volume>0){
                L1 = lMid;
            }
            else{
                L2 = lMid;
            }
        }
        return  newDensity;
    }
}
