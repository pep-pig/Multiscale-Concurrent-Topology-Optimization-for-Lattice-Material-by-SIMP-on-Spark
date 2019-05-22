package algorithms;

import org.apache.hadoop.yarn.webapp.hamlet.Hamlet;
import org.jblas.DoubleMatrix;
import org.jblas.Solve;
import scala.Tuple3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.sqrt;

public  class FiniteElementAnalysis {
    FiniteElementAnalysis(){
        long startInit=System.currentTimeMillis();
        Parameter.init(this);
        long endInit=System.currentTimeMillis();
        //System.out.println("initTime:"+(endInit-startInit));
    }
    public class CellModel{
        double length;
        double height;
        int nelx;
        int nely;
        double lambda;
        double mu;
        double penal;
        double filterRadius;
        DoubleMatrix nodeNumberMatrix;
    }
    //meshModelData
    double length;
    double height;
    int nelx;
    int nely;
    double penal;
    double volf;
    double filterRadius;
    double force;
    int boundaryConditions;
    DoubleMatrix nodeNumberMatrix;
    FiniteElementAnalysis.CellModel cellModel = new FiniteElementAnalysis.CellModel();
    DoubleMatrix macroU;

    //Solution data
    ArrayList<Double> macroEnergy = new ArrayList<Double>();
    ArrayList<Double> [] microEnergy ;
    ArrayList<Double>macroVolume = new ArrayList<Double>();
    ArrayList<Double> [] microVolume;
    int iteration;
    //Simp parameter
    double macroStopChangeValue;
    int macroStopIteration;
    double microStopChangeValue;
    int microStopIteration;
    int microOptimizationStartIteration;
    //MacroOcFunction parameter
    double macroOcMove;
    double macroOcDensityUpperLimit;
    double macroOcDensityLowerLimit;
    double microOcMove;
    double microOcDensityUpperLimit;
    double microOcDensityLowerLimit;
    //postprocess
    String path;
    double densityThreshold;
    //parallism setting
    int cpu;
    public DoubleMatrix gaussIntegration(double a, double b, DoubleMatrix C){
        double[] integrationPointx = new double[]{(-1/sqrt(3)),(1/sqrt(3))};
        double[] integrationPointy = new double[]{(-1/sqrt(3)),(1/sqrt(3))};
        DoubleMatrix ke = new DoubleMatrix(8,8);
        for (double t :integrationPointx)
        {
            for (double s:integrationPointy){
                double detJ = a*b,wi=1,wj=1;
                double weight = wi*wj*detJ;
                double[] data = new double[]{0,b*(t-1)/4,b*(t+1)/4,a*(s-1)/4,a*(s+1)/4};
                DoubleMatrix B = new DoubleMatrix(new double[][] {{data[1],data[0],-data[1],data[0],data[2],data[0],-data[2],data[0]},
                        {data[0],data[3],data[0],-data[4],data[0],data[4],data[0],-data[3]},
                        {data[3],data[1],-data[4],-data[1],data[4],data[2],-data[3],-data[2]}}).mul(1/(a*b));
                //Element Matrix
                ke = ke.add(B.transpose().mmul(C).mmul(B).mul(weight));
            }
        }
        return ke;
    }

    public  void put(int[] rindices,int[] cindices,DoubleMatrix src,DoubleMatrix des){
        for (int i=0;i<src.getColumns();i++){
            for(int j=0;j<src.getRows();j++){
                des.data[cindices[i]*des.rows+rindices[j]]+=src.data[i*src.rows+j];
            }
        }
    }

    public Map<String,DoubleMatrix> implementBoundaryConditions(DoubleMatrix K, DoubleMatrix loadConstrains, DoubleMatrix displacementConstrains){
        Map<String,DoubleMatrix> linearSystem = new HashMap<String, DoubleMatrix>();
        DoubleMatrix F = DoubleMatrix.zeros(K.getRows(),1);
        for(int i =0;i<displacementConstrains.getRows();i++){
            int index = (int)displacementConstrains.get(i,0)-1;
            double value = displacementConstrains.get(i,1);
            F = F.put(index,K.get(index,index)*1E23*value);
            K = K.put(index,index,(K.get(index,index)*1E23));
        }
        if (loadConstrains!=null){
            for(int i = 0;i<loadConstrains.getRows();i++){
                int index = (int)loadConstrains.get(i,0)-1;
                double value = loadConstrains.get(i,1);
                F = F.put(index,value);
            }}
        linearSystem.put("F",F);
        linearSystem.put("K",K);
        return linearSystem;
    }
    public   DoubleMatrix getElementDisplacement(int elementNumber,DoubleMatrix U,DoubleMatrix nodeNumberMatrix){
        return U.get(nodeNumberMatrix.getRow(elementNumber).sub(1).toIntArray(),0);
    }
    public  DoubleMatrix solve(DoubleMatrix K,DoubleMatrix F){
        return Solve.solveSymmetric(K,F);
    }

    public  void setMacroMeshModel() {
        //generate node number ordered by column ,and x-diretion first ,y-direction second;
        int elementAmount = nelx * nely;
        double[] order;
        order = new double[(1 + nely) * (1 + nelx)];
        for (int i = 1; i <= (1 + nely) * (1 + nelx); i++) {
            order[i - 1] = i;
        }
        DoubleMatrix nodeLocation = new DoubleMatrix(1 + nely, 1 + nelx, order);
        DoubleMatrix firstNodeNumbersOfElements = nodeLocation.getRange(0, nely, 0, nelx).reshape(elementAmount, 1).mul(2).add(1);
        double[] nodeNumberOffset = {0, 1, 2 * nely + 2, 2 * nely + 3, 2 * nely,
                2 * nely + 1, -2, -1};
        nodeNumberMatrix = firstNodeNumbersOfElements.repmat(1, 8).add(new DoubleMatrix(1, 8, nodeNumberOffset).repmat(elementAmount, 1));

    }

    public void setMicroMeshModel(){
        int elementAmount = cellModel.nelx * cellModel.nely;
        double[] order;
        order = new double[(1 + cellModel.nely) * (1 + cellModel.nelx)];
        for (int i = 1; i <= (1 + cellModel.nely) * (1 + cellModel.nelx); i++) {
            order[i - 1] = i;
        }
        DoubleMatrix nodeLocation = new DoubleMatrix(1 + cellModel.nely, 1 + cellModel.nelx, order);
        DoubleMatrix firstNodeNumbersOfElements = nodeLocation.getRange(0, cellModel.nely, 0, cellModel.nelx).reshape(elementAmount, 1).mul(2).add(1);
        double[] nodeNumberOffset = {0, 1, 2 * cellModel.nely + 2, 2 * cellModel.nely + 3, 2 * cellModel.nely,
                2 * cellModel.nely + 1, -2, -1};
        cellModel.nodeNumberMatrix = firstNodeNumbersOfElements.repmat(1, 8).add(new DoubleMatrix(1, 8, nodeNumberOffset).repmat(elementAmount, 1));
    }
    //Initiate material data which will be convert to SparkRDD
    public Tuple3<ArrayList<DoubleMatrix>,DoubleMatrix, ArrayList<DoubleMatrix>> variableInit(){
        double elx = cellModel.length/cellModel.nelx;
        double ely = cellModel.height/cellModel.nely;
        DoubleMatrix macroDensity;
        ArrayList<DoubleMatrix> microDensity = new ArrayList<DoubleMatrix>();
        ArrayList<DoubleMatrix> materialMatrixC = new ArrayList<DoubleMatrix>();
        macroDensity = DoubleMatrix.ones(nely,nelx).mul(volf);
        for (int ele = 0;ele<nelx*nely;ele++){
            microDensity.add(DoubleMatrix.ones(cellModel.nely,cellModel.nelx).mul(volf));
        }
        //TODO
        DoubleMatrix ce = Homogenization.homogenize(elx,ely,microDensity.get(0).mul(cellModel.lambda),microDensity.get(0).mul(cellModel.mu)).div(Math.pow(macroDensity.get(0),penal));
        for(int i = 0; i < nely*nelx; i++) {
            materialMatrixC.add(new DoubleMatrix(ce.toArray2()));
        }
        return new Tuple3<ArrayList<DoubleMatrix>, DoubleMatrix, ArrayList<DoubleMatrix>>(materialMatrixC,macroDensity,microDensity);
    }
}

