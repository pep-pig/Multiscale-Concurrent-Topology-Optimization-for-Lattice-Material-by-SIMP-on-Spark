package algorithms;

import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MicroOptimize implements Runnable {
    public static int num = 0;
    public int  macroEle;
    public FiniteElementAnalysis fem;
    public DoubleMatrix macroU;
    private Lock lock = new ReentrantLock();
    public MicroOptimize(int macroEle, FiniteElementAnalysis fem, DoubleMatrix macroU){
        this.macroEle = macroEle;
        this.fem = fem;
        this.macroU = macroU;
    }
    public void run() {
        double microChange = 1.0;
        DoubleMatrix oldMicroDensity;
        int iteration = 0;
        fem.microEnergy[macroEle] = new ArrayList<Double>();
        fem.microVolume[macroEle] = new ArrayList<Double>();
        while (microChange >fem.microStopChangeValue && iteration<fem.microStopIteration) {
            iteration++;
            oldMicroDensity = fem.microDensity.get(macroEle);
            //step1 compute fem element stiffness K
            DoubleMatrix K = fem.assemblyMicroElementStiffnessMatrix(fem.microDensity.get(macroEle), fem.cellModel.penal, fem.cellModel.nodeNumberMatrix);
            //step2 add boundary conditions,there is only displacement constrain,which is contained in macroU
            //Map<String, DoubleMatrix> boundaryConditions = fem.microLinearIntepolatingBoundary(macroEle,macroU);
            Map<String, DoubleMatrix> boundaryConditions = fem.microBoundary(macroEle,macroU);
            Map<String, DoubleMatrix> linearSystem = fem.implementBoundaryConditions(K, boundaryConditions.get("loadConstrains"), boundaryConditions.get("displacementConstrains"));
            //step3 solve micro linearSystem
            DoubleMatrix microU = fem.solve(linearSystem.get("K"), linearSystem.get("F"));
            //step4 update micro microDensity by simp
            double microEnergy = 0;
            DoubleMatrix microEnergyDerivative = new DoubleMatrix(fem.cellModel.nely, fem.cellModel.nelx);
            for (int microEle = 0; microEle < fem.cellModel.nelx * fem.cellModel.nely; microEle++) {
                microEnergy += fem.getMicroElementEnergy(microEle,macroEle, microU);
                microEnergyDerivative = microEnergyDerivative.put(microEle, fem.getMicroElementEnergyDerivative(microEle,macroEle, microU));
            }
            fem.microEnergy[macroEle].add(microEnergy);
            microEnergyDerivative = microFilter(macroEle,fem,microEnergyDerivative);
            //TODO 每一次微观迭代尝试重新初始化密度
            fem.microDensity.set(macroEle,OC.oc(fem.cellModel.nelx,fem.cellModel.nely,fem.microDensity.get(macroEle),fem.macroDensity.get(macroEle),microEnergyDerivative,fem.microOcMove,fem.microOcDensityUpperLimit,fem.microOcDensityLowerLimit));
            double volumeFactor = fem.microDensity.get(macroEle).sum()/(fem.cellModel.nelx*fem.cellModel.nely);
            fem.microVolume[macroEle].add(volumeFactor);
            //System.out.println("    microIteration:"+iteration+";  microEnergy:"+microEnergy+";  volumeFactor:"+volumeFactor);
            microChange = MatrixFunctions.abs(fem.microDensity.get(macroEle).sub(oldMicroDensity)).max();
        }
        double elx = fem.cellModel.length/fem.cellModel.nelx;
        double ely = fem.cellModel.height/fem.cellModel.nely;
        fem.C.set(macroEle,Homogenization.homogenize(elx,ely,fem.microDensity.get(macroEle).mul(fem.cellModel.lambda),fem.microDensity.get(macroEle).mul(fem.cellModel.mu)));
        lock.lock();
            num++;
        lock.unlock();
    }
    /*
    microFilter:to avoid checkboard
     */
    public DoubleMatrix microFilter(int macroEle,FiniteElementAnalysis fem, DoubleMatrix energyDerivative){
        DoubleMatrix modifiedEnergyDerivative = new DoubleMatrix(fem.cellModel.nely,fem.cellModel.nelx);
        double eleLength = fem.cellModel.length/fem.cellModel.nelx;
        double[][] dist = new double[][]{{eleLength*Math.sqrt(2),eleLength,eleLength*Math.sqrt(2)},
                {eleLength,0,eleLength},
                {eleLength*Math.sqrt(2),eleLength,eleLength*Math.sqrt(2)},
        };

        for(int i=0;i<fem.cellModel.nelx;i++){
            for(int j=0;j<fem.cellModel.nely;j++){
                double modifiedDemoninator = 0;
                double modifiedNumerator = 0;
                for(int m=i-1, k=0;m<=i+1;m++,k++){
                    for(int n=j-1,l=0;n<=j+1;n++,l++ ){
                        if (m<0 || n<0 || m>=fem.cellModel.nelx || n>=fem.cellModel.nely){
                            continue;
                        }
                        modifiedDemoninator += (fem.cellModel.filterRadius-dist[l][k])*(fem.microDensity.get(macroEle).get(n,m))*(energyDerivative.get(n,m));
                        modifiedNumerator+=fem.cellModel.filterRadius-dist[l][k];
                    }
                }
                modifiedEnergyDerivative.put(j,i,modifiedDemoninator/(modifiedNumerator*fem.microDensity.get(macroEle).get(j,i)));
            }
        }
        return modifiedEnergyDerivative;
    }
}
