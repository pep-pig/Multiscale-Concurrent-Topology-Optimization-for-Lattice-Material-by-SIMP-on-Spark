package algorithms;

import org.apache.spark.Partitioner;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;
import scala.Tuple2;
import scala.Tuple3;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.jblas.MatrixFunctions.sqrt;

public class MicroOptimization extends MicroFiniteElementAnalysis implements PairFunction<Tuple2<Integer, Tuple3<DoubleMatrix,Double,DoubleMatrix>>, Integer, Tuple3<DoubleMatrix,Double,DoubleMatrix>> {

    public MicroOptimization(){
        System.out.println("初始化MicroOptimization");
    }
    public Tuple2<Integer, Tuple3<DoubleMatrix,Double,DoubleMatrix>> call(Tuple2<Integer, Tuple3<DoubleMatrix,Double,DoubleMatrix>> microInputRDD) throws Exception {
        int eleNum = microInputRDD._1;
        DoubleMatrix Ue = microInputRDD._2._1();
        double volf = microInputRDD._2._2();
        DoubleMatrix newDensity = microInputRDD._2._3();
        double microChange = 1.0;
        DoubleMatrix oldMicroDensity;
        int iteration = 0;
        while (microChange >microStopChangeValue && iteration<microStopIteration) {
            iteration++;
            oldMicroDensity = newDensity;
            //Step1 compute fem element stiffness K
            DoubleMatrix K = assemblyMicroElementStiffnessMatrix(newDensity);
            //step2 add boundary conditions,there is only displacement constrain,which is contained in macroU
            Map<String, DoubleMatrix> boundaryConditions = microBoundary(Ue);
            Map<String, DoubleMatrix> linearSystem = implementBoundaryConditions(K, boundaryConditions.get("loadConstrains"), boundaryConditions.get("displacementConstrains"));
            //step3 solve micro linearSystem
            DoubleMatrix microU = solve(linearSystem.get("K"), linearSystem.get("F"));
            //step4 update micro microDensity by simp
            double microEnergy = 0;
            DoubleMatrix microEnergyDerivative = new DoubleMatrix(cellModel.nely, cellModel.nelx);
            for (int microEle = 0; microEle < cellModel.nelx * cellModel.nely; microEle++) {
                microEnergy += getMicroElementEnergy(microEle,oldMicroDensity, microU);
                microEnergyDerivative = microEnergyDerivative.put(microEle, getMicroElementEnergyDerivative(microEle,oldMicroDensity,microU));
            }
            microEnergyDerivative = microFilter(oldMicroDensity,microEnergyDerivative);
            newDensity = oc(oldMicroDensity,microEnergyDerivative,volf);
            double volumeFactor = newDensity.sum()/(cellModel.nelx*cellModel.nely);
            //System.out.println("    microIteration:"+iteration+";  microEnergy:"+microEnergy+";  volumeFactor:"+volumeFactor);
            microChange = MatrixFunctions.abs(newDensity.sub(oldMicroDensity)).max();
        }
        double elx = cellModel.length/cellModel.nelx;
        double ely = cellModel.height/cellModel.nely;
        DoubleMatrix materialMatrixC = Homogenization.homogenize(elx,ely,newDensity.mul(cellModel.lambda),newDensity.mul(cellModel.mu)).div(volf);
        return new Tuple2<Integer, Tuple3<DoubleMatrix,Double,DoubleMatrix>>(eleNum,new Tuple3<DoubleMatrix, Double, DoubleMatrix>(materialMatrixC,volf,newDensity));
    }
    /*
   microFilter:to avoid checkboard
    */
    public DoubleMatrix microFilter(DoubleMatrix microDensity, DoubleMatrix energyDerivative){
        DoubleMatrix modifiedEnergyDerivative = new DoubleMatrix(cellModel.nely,cellModel.nelx);
        double eleLength = cellModel.length/cellModel.nelx;
        double[][] dist = new double[][]{{eleLength*Math.sqrt(2),eleLength,eleLength*Math.sqrt(2)},
                {eleLength,0,eleLength},
                {eleLength*Math.sqrt(2),eleLength,eleLength*Math.sqrt(2)},
        };

        for(int i=0;i<cellModel.nelx;i++){
            for(int j=0;j<cellModel.nely;j++){
                double modifiedDemoninator = 0;
                double modifiedNumerator = 0;
                for(int m=i-1, k=0;m<=i+1;m++,k++){
                    for(int n=j-1,l=0;n<=j+1;n++,l++ ){
                        if (m<0 || n<0 || m>=cellModel.nelx || n>=cellModel.nely){
                            continue;
                        }
                        modifiedDemoninator += (cellModel.filterRadius-dist[l][k])*(microDensity.get(n,m))*(energyDerivative.get(n,m));
                        modifiedNumerator+=cellModel.filterRadius-dist[l][k];
                    }
                }
                modifiedEnergyDerivative.put(j,i,modifiedDemoninator/(modifiedNumerator*microDensity.get(j,i)));
            }
        }
        return modifiedEnergyDerivative;
    }
    public  DoubleMatrix oc(DoubleMatrix density,DoubleMatrix dc,double vol){
        double L1 = 0;
        double L2 = 1E5;
        double lMid;
        double volume = vol*cellModel.nelx*cellModel.nely;
        DoubleMatrix newDensity = new DoubleMatrix(density.getRows(),density.getColumns());
        while(L2-L1>1E-6){
            lMid = 0.5*(L2+L1);
            newDensity =(density.add(microOcMove).min(density.mul(sqrt(dc.mul(-1).div(lMid))))).min(microOcDensityUpperLimit).max(density.sub(microOcMove)).max(microOcDensityLowerLimit);
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
