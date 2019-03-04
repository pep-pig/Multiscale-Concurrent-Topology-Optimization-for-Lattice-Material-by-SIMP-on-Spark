package algorithms;

import com.aparapi.Kernel;
import com.aparapi.Range;
import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;



import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class SIMP {
    /*
    macroSimp:use Homogenization method to homogenize a cell and compute the effective
    properties,then you can regard each cell as solid with different material properties,the
    microDensity means the volume proportion.If microDensity equals one ,it means the cell if solid,and if
    microDensity equals zeros it means the cell is empty,if microDensity is between 0-1,it means the cell is partly
    empty
     */
    //public int macroDataReadable = 1;
    public int microDataReadable = 1;
    public boolean computeFinished = false;
    public int macroEle;
    public FiniteElementAnalysis fem = new FiniteElementAnalysis();

    public SIMP(){
        Parameter.init(fem);
    }

    public  FiniteElementAnalysis macroSimp(PostProcess postProcess) {
        //step1 generate mesh model
        fem.setMacroMeshModel(fem.length,fem.height, fem.nelx, fem.nely);
        fem.setMicroMeshModel(fem.cellModel.length, fem.cellModel.height, fem.cellModel.nelx, fem.cellModel.nely);
        //step2 generate material model
        fem.initialMicroMaterialModel(fem.cellModel.lambda, fem.cellModel.mu,fem.volf,fem.cellModel.penal);
        fem.initialMacroMaterialModel(fem.penal);
        //step3 update macroDensity
        double macroChange = 1.0;
        double macroStopCondition = fem.macroStopChangeValue;
        double force = fem.force;
        int iteration=0;
        DoubleMatrix oldMacroDensity;

        ThreadPoolExecutor executor = new ThreadPoolExecutor(fem.cpu+1,fem.cpu+1,200,TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(fem.nelx*fem.nely));
        long start = System.currentTimeMillis();
        while (macroChange > macroStopCondition && iteration<fem.macroStopIteration) {
            fem.microEnergy = new ArrayList[fem.nelx*fem.nely];
            fem.microVolume = new ArrayList[fem.nelx*fem.nely];
            iteration++;
            oldMacroDensity = fem.macroDensity;
            //step3.1 compute fem element stiffness K
            DoubleMatrix K = fem.assemblyMacroElementStiffnessMatrix(fem.macroDensity, fem.penal, fem.nodeNumberMatrix);
            //step3.2 add boundary conditions
            Map<String, DoubleMatrix> boundaryConditions;
            if (fem.boundaryConditions == 1){
                boundaryConditions = fem.boundaryCondition1(force);
            }
            else if (fem.boundaryConditions==2){
                boundaryConditions = fem.boundaryCondition2(force);
            }
            else{
                boundaryConditions = fem.boundaryCondition3(force);
            }

            Map<String, DoubleMatrix> linearSystem = fem.implementBoundaryConditions(K, boundaryConditions.get("loadConstrains"), boundaryConditions.get("displacementConstrains"));
            //step3.3 solve fem linearSystem
            final DoubleMatrix macroU = fem.solve(linearSystem.get("K"), linearSystem.get("F"));
            fem.macroU = macroU;
            //step3.4 update fem microDensity by simp
            double macroEnergy = 0;
            DoubleMatrix macroEnergyDerivative = new DoubleMatrix(fem.nely, fem.nelx);
            for (int eln = 0; eln < fem.nelx * fem.nely; eln++) {
                macroEnergy += fem.getMacroElementEnergy(eln, macroU);
                macroEnergyDerivative = macroEnergyDerivative.put(eln, fem.getMacroElementEnergyDerivative(eln, macroU));
            }
            fem.macroEnergy.add(macroEnergy);
            //As there is a few number of element in fem and the material properties are differ from each element,so we
            //don't need to avoid the check board.
            //TODO we can also try to add filter in fem ,and to explore the difference .
            macroEnergyDerivative = macroFilter(fem,macroEnergyDerivative);

            fem.macroDensity = OC.oc(fem.nelx,fem.nely,fem.macroDensity,fem.volf,macroEnergyDerivative,fem.macroOcMove,fem.macroOcDensityUpperLimit,fem.macroOcDensityLowerLimit,iteration);
            double volumeFactor = fem.macroDensity.sum()/(fem.nelx*fem.nely);
            fem.macroVolume.add(volumeFactor);
            System.out.println("macroIteration:"+iteration+"start;  macroEnergy:"+macroEnergy+";  volumeFactor:"+volumeFactor);
            macroChange = MatrixFunctions.abs(fem.macroDensity.sub(oldMacroDensity)).max();
            //step4 update microDensity for each cell
            if(iteration>fem.microOptimizationStartIteration) {
                if((iteration-fem.microOptimizationStartIteration)==1){
                    fem.reInitMicroDensity();
                }
                Kernel kernel = new Kernel() {
                    @Override
                    public void run() {
                        int ele = getGlobalId();
                        MicroOptimize microOptimize = new MicroOptimize(ele, fem, macroU);
                        microOptimize.start();
                    }
                };
                Range range = Range.create(fem.nelx * fem.nely);
                kernel.execute(range);
            }
            postProcess.plotGrayscale(postProcess.plotWindow,fem.macroDensity.mmul(-1).add(1).toArray2());
            postProcess.plotRealStructure(postProcess.resultWindow,fem.microDensity,fem.nely);
            System.out.println("macroIteration finished;  updating macro material properties by homogenize");
        }
        computeFinished = true;
        long end = System.currentTimeMillis();
        System.out.println("time elapsed:"+(end-start));
        executor.shutdown();
        return fem;
    }

    public DoubleMatrix macroFilter(FiniteElementAnalysis fem, DoubleMatrix energyDerivative){
        DoubleMatrix modifiedEnergyDerivative = new DoubleMatrix(fem.nely,fem.nelx);
        double eleLength = fem.length/fem.nelx;
        double[][] dist = new double[][]{{eleLength*Math.sqrt(2),eleLength,eleLength*Math.sqrt(2)},
                {eleLength,0,eleLength},
                {eleLength*Math.sqrt(2),eleLength,eleLength*Math.sqrt(2)},
        };
        for(int i=0;i<fem.nelx;i++){
            for(int j=0;j<fem.nely;j++){
                double modifiedDemoninator = 0;
                double modifiedNumerator = 0;
                for(int m=i-1, k=0;m<=i+1;m++,k++){
                    for(int n=j-1,l=0;n<=j+1;n++,l++ ){
                        if (m<0 || n<0 || m>=fem.nelx || n>=fem.nely){
                            continue;
                        }
                        modifiedDemoninator += (fem.filterRadius-dist[l][k])*(fem.macroDensity.get(n,m))*(energyDerivative.get(n,m));
                        modifiedNumerator+=fem.filterRadius-dist[l][k];
                    }
                }
                modifiedEnergyDerivative.put(j,i,modifiedDemoninator/(modifiedNumerator*fem.macroDensity.get(j,i)));
            }
        }
        return modifiedEnergyDerivative;
    }
}
