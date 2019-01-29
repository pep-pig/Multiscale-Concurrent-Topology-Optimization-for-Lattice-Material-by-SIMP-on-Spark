package algorithms;

import org.jblas.DoubleMatrix;
import org.jblas.Solve;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import static java.lang.Math.sqrt;

public class FiniteElementAnalysis {
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
    CellModel cellModel = new CellModel();
    DoubleMatrix macroU;
    //materialModelData
    DoubleMatrix macroDensity;
    ArrayList<DoubleMatrix>microDensity = new ArrayList<DoubleMatrix>();
    ArrayList<DoubleMatrix> C = new ArrayList<DoubleMatrix>();

    //Solution data
    ArrayList<Double> macroEnergy = new ArrayList<Double>();
    ArrayList<Double> [] microEnergy ;
    ArrayList<Double>macroVolume = new ArrayList<Double>();
    ArrayList<Double> [] microVolume;

    //Simp parameter
    double macroStopChangeValue;
    int macroStopIteration;
    double microStopChangeValue;
    int microStopIteration;

    //Oc parameter
    double macroOcMove;
    double macroOcDensityUpperLimit;
    double macroOcDensityLowerLimit;
    double microOcMove;
    double microOcDensityUpperLimit;
    double microOcDensityLowerLimit;

    public FiniteElementAnalysis(){
            }

    public  void setMacroMeshModel(double length, double height, int nelx, int nely) {
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
        this.nodeNumberMatrix = firstNodeNumbersOfElements.repmat(1, 8).add(new DoubleMatrix(1, 8, nodeNumberOffset).repmat(elementAmount, 1));
        this.length = length;
        this.height = height;
        this.nelx = nelx;
        this.nely = nely;
    }

    public void setMicroMeshModel(double length, double height, int nelx, int nely){
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
        cellModel.nodeNumberMatrix = firstNodeNumbersOfElements.repmat(1, 8).add(new DoubleMatrix(1, 8, nodeNumberOffset).repmat(elementAmount, 1));
        cellModel.length = length;
        cellModel.height = height;
        cellModel.nelx = nelx;
        cellModel.nely = nely;
    }

    public void initialMicroMaterialModel(double lambda, double mu,double volf,double penal) {
        cellModel.lambda = lambda;
        cellModel.mu = mu;
        cellModel.penal = penal;
        for (int ele = 0;ele<nelx*nely;ele++){
            microDensity.add(DoubleMatrix.ones(cellModel.nely,cellModel.nelx).mul(volf));
        }
    }

    public void reInitMicroDensity(){
        for (int ele = 0;ele<nelx*nely;ele++){
            microDensity.set(ele,DoubleMatrix.ones(cellModel.nely,cellModel.nelx).mul(macroDensity.get(ele)));
        }
    }
    public  void initialMacroMaterialModel(double penal) {
        double elx = cellModel.length/cellModel.nelx;
        double ely = cellModel.height/cellModel.nely;
        this.penal = penal;
        macroDensity = DoubleMatrix.ones(nely,nelx).mul(volf);
        for(int i = 0; i < nely*nelx; i++) {
            C.add(Homogenization.homogenize(elx,ely,microDensity.get(i).mul(cellModel.lambda),microDensity.get(i).mul(cellModel.mu)));
        }
    }

    public void updateMacroMaterialModel(){
        double elx = cellModel.length/cellModel.nelx;
        double ely = cellModel.height/cellModel.nely;
        for(int i = 0;i<nely*nelx; i++){
            C.set(i,Homogenization.homogenize(elx,ely,microDensity.get(i).mul(cellModel.lambda),microDensity.get(i).mul(cellModel.mu)));
        }
    }
    public  DoubleMatrix calculateMicroElementStiffness(int elementNubmer){
        double a = cellModel.length/cellModel.nelx;
        double b = cellModel.height/cellModel.nely;
        DoubleMatrix CLambda = new DoubleMatrix(new double[][]{{1,1,0},{1,1,0},{0,0,0}});
        DoubleMatrix CMu = new DoubleMatrix(new double[][]{{2,0,0},{0,2,0},{0,0,1}});
        double lambda = cellModel.lambda;
        double mu = cellModel.mu;
        DoubleMatrix C = CLambda.mul(lambda).add(CMu.mul(mu));
        return gaussIntegration(a,b,C);
    }

    public  DoubleMatrix calculateMacroElementStiffness(int elementNumber){
        double a = length/nelx;
        double b = height/nely;
        return gaussIntegration(a,b,C.get(elementNumber));
    }

    public  DoubleMatrix gaussIntegration(double a,double b,DoubleMatrix C){
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

    public  DoubleMatrix assemblyMacroElementStiffnessMatrix(DoubleMatrix density, double penalty, DoubleMatrix nodeNumberMatrix){
        DoubleMatrix K = new DoubleMatrix((nelx+1)*(nely+1)*2,(nelx+1)*(nely+1)*2);
        for(int i=0;i<nodeNumberMatrix.getRows();i++){
            DoubleMatrix ke = calculateMacroElementStiffness(i);
            DoubleMatrix elementNode = nodeNumberMatrix.getRow(i).sub(1);
            put(elementNode.toIntArray(),elementNode.toIntArray(),ke.mul(Math.pow(density.get(i),penalty)),K);
           //put(elementNode.toIntArray(),elementNode.toIntArray(),ke,K);
        }
        return K;
    }
    //TODO micro element stiffness just need be caculated once
    public  DoubleMatrix assemblyMicroElementStiffnessMatrix(DoubleMatrix density, double penalty, DoubleMatrix nodeNumberMatrix){
        DoubleMatrix K = new DoubleMatrix((cellModel.nelx+1)*(cellModel.nely+1)*2,(cellModel.nelx+1)*(cellModel.nely+1)*2);
        for(int i=0;i<nodeNumberMatrix.getRows();i++){
            DoubleMatrix ke = calculateMicroElementStiffness(i);
            DoubleMatrix elementNode = nodeNumberMatrix.getRow(i).sub(1);
            put(elementNode.toIntArray(),elementNode.toIntArray(),ke.mul(Math.pow(density.get(i),penalty)),K);
        }
        return K;
    }

    public  void put(int[] rindices,int[] cindices,DoubleMatrix src,DoubleMatrix des){
        for (int i=0;i<src.getColumns();i++){
            for(int j=0;j<src.getRows();j++){
                des.data[cindices[i]*des.rows+rindices[j]]+=src.data[i*src.rows+j];
            }
        }
    }

    //cantilever beam with load in the bottom right corner
    public Map<String,DoubleMatrix> boundaryCondition1(double force){
        Map<String,DoubleMatrix> boundaryConditions = new HashMap<String,DoubleMatrix>();
        DoubleMatrix loadConstrains = new DoubleMatrix(1,2);
        DoubleMatrix displacementConstrains = new DoubleMatrix((nely+1)*2,2);
        for (int nodeNumber=1;nodeNumber<=(nely+1)*2;nodeNumber++){
            displacementConstrains.put(nodeNumber-1,0,nodeNumber);
            displacementConstrains.put(nodeNumber-1,1,0);
        }
        loadConstrains.put(0,0,(nelx+1)*(nely+1)*2);
        loadConstrains.put(0,1,force);
        boundaryConditions.put("loadConstrains",loadConstrains);
        boundaryConditions.put("displacementConstrains",displacementConstrains);
        return boundaryConditions;
    }
    //cantilever beam with load in the center of right edge,if use this you should make sure the nubmer of element
    //in y direction is even,so that the load can implement in the center of the right edge
    public Map<String,DoubleMatrix> boundaryCondition2(double force){
        Map<String,DoubleMatrix> boundaryConditions = new HashMap<String,DoubleMatrix>();
        DoubleMatrix loadConstrains = new DoubleMatrix(1,2);
        DoubleMatrix displacementConstrains = new DoubleMatrix((nely+1)*2,2);
        for (int nodeNumber=1;nodeNumber<=(nely+1)*2;nodeNumber++){
            displacementConstrains.put(nodeNumber-1,0,nodeNumber);
            displacementConstrains.put(nodeNumber-1,1,0);
        }
        loadConstrains.put(0,0,(nelx+1)*(nely+1)*2-(nely/2)*2);
        loadConstrains.put(0,1,force);
        boundaryConditions.put("loadConstrains",loadConstrains);
        boundaryConditions.put("displacementConstrains",displacementConstrains);
        return boundaryConditions;
    }
    //MMB,if you use this boundaryConditions you should make sure that the number of element in x direction
    //is even
    public Map<String,DoubleMatrix> boundaryCondition3(double force){
        Map<String,DoubleMatrix> boundaryConditions = new HashMap<String,DoubleMatrix>();
        DoubleMatrix loadConstrains = new DoubleMatrix(1,2);
        DoubleMatrix displacementConstrains = new DoubleMatrix(4,2);

        displacementConstrains.put(0,0,(nely+1)*2-1);
        displacementConstrains.put(0,1,0);
        displacementConstrains.put(1,0,(nely+1)*2);
        displacementConstrains.put(1,1,0);
        displacementConstrains.put(2,0,(nelx+1)*(nely+1)*2-1);
        displacementConstrains.put(2,1,0);
        displacementConstrains.put(3,0,(nelx+1)*(nely+1)*2);
        displacementConstrains.put(3,1,0);

        loadConstrains.put(0,0,((nelx)/2-1)*(nely+1)*2+2);
        loadConstrains.put(0,1,force);
        boundaryConditions.put("loadConstrains",loadConstrains);
        boundaryConditions.put("displacementConstrains",displacementConstrains);
        return boundaryConditions;
    }
    //Given displacement in four corner obtained in fem finite element analysis
    public Map<String,DoubleMatrix> microBoundary(int macroEle,DoubleMatrix U){
        DoubleMatrix Ue = getElementDisplacement(macroEle,U,nodeNumberMatrix);
        //DoubleMatrix Ue = new DoubleMatrix(new double[]{1,1,-1,1,-1,-1,1,-1});
        //DoubleMatrix Ue = new DoubleMatrix(new double[]{0,0,1,-1,1,1,0,0});
        Map<String,DoubleMatrix> boundaryConditions = new HashMap<String,DoubleMatrix>();
        DoubleMatrix loadConstrains = null;
        DoubleMatrix displacementConstrains = new DoubleMatrix(8,2);
        int[] nodeList = {(cellModel.nely+1)*2-1,(cellModel.nely+1)*2,(cellModel.nelx+1)*(cellModel.nely+1)*2-1,(cellModel.nelx+1)*(cellModel.nely+1)*2,(cellModel.nelx)*(cellModel.nely+1)*2+1,(cellModel.nelx)*(cellModel.nely+1)*2+2,1,2};
        for (int i=0;i<8;i++){
            displacementConstrains.put(i,0,nodeList[i]);
            displacementConstrains.put(i,1,Ue.get(i));

        }
        boundaryConditions.put("loadConstrains",loadConstrains);
        boundaryConditions.put("displacementConstrains",displacementConstrains);
        return boundaryConditions;

    }
    public Map<String,DoubleMatrix> microLinearIntepolatingBoundary(int macroEle,DoubleMatrix U){
        DoubleMatrix Ue = getElementDisplacement(macroEle,U,nodeNumberMatrix);
        //DoubleMatrix Ue = new DoubleMatrix(new double[]{0,0,1,-1,1,1,0,0});
        Map<String,DoubleMatrix> boundaryConditions = new HashMap<String,DoubleMatrix>();
        DoubleMatrix loadConstrains = null;
        DoubleMatrix displacementConstrains = new DoubleMatrix((cellModel.nelx*2)*4,2);
        double linearUe[] = linearIntepolate(Ue.toArray());
            for (int i =0;i<cellModel.nelx;i++){
            displacementConstrains.put(i*2,0, (i+1)*(cellModel.nely+1)*2-1);
            displacementConstrains.put(i*2,1,linearUe[i*2]);
            displacementConstrains.put(i*2+1,0,  (i+1)*(cellModel.nely+1)*2);
            displacementConstrains.put(i*2+1,1,linearUe[i*2+1]);

            displacementConstrains.put(i*2+cellModel.nelx*2,0, (cellModel.nelx+1)*(cellModel.nely+1)*2-(2*i+1));
            displacementConstrains.put(i*2+cellModel.nelx*2,1,linearUe[i*2+cellModel.nelx*2]);
            displacementConstrains.put(i*2+1+cellModel.nelx*2,0, (cellModel.nelx+1)*(cellModel.nely+1)*2-2*i);
            displacementConstrains.put(i*2+1+cellModel.nelx*2,1,linearUe[i*2+1+cellModel.nelx*2]);

            displacementConstrains.put(i*2+cellModel.nelx*2*2,0, (cellModel.nelx+1)*2*(cellModel.nelx-i)+1);
            displacementConstrains.put(i*2+cellModel.nelx*2*2,1,linearUe[i*2+cellModel.nelx*2*2]);
            displacementConstrains.put(i*2+1+cellModel.nelx*2*2,0, (cellModel.nelx+1)*2*(cellModel.nelx-i)+2);
            displacementConstrains.put(i*2+1+cellModel.nelx*2*2,1,linearUe[i*2+1+cellModel.nelx*2*2]);

            displacementConstrains.put(i*2+cellModel.nelx*2*3,0, i*2+1);
            displacementConstrains.put(i*2+cellModel.nelx*2*3,1,linearUe[i*2+cellModel.nelx*2*3]);
            displacementConstrains.put(i*2+1+cellModel.nelx*2*3,0, i*2+2);
            displacementConstrains.put(i*2+1+cellModel.nelx*2*3,1,linearUe[i*2+1+cellModel.nelx*2*3]);
        }
        boundaryConditions.put("loadConstrains",loadConstrains);
        boundaryConditions.put("displacementConstrains",displacementConstrains);
        return boundaryConditions;
    }
    public double[] linearIntepolate(double[] Ue){
        double[] linearUe = new double[(cellModel.nelx*2)*4];
        for (int i =0;i<cellModel.nelx;i++){
            linearUe[i*2] = Ue[0]+(Ue[2]-Ue[0])/cellModel.nelx*i;
            linearUe[i*2+1]= Ue[1]+(Ue[3]-Ue[1])/cellModel.nelx*i;
            linearUe[i*2+cellModel.nelx*2]= Ue[2]+(Ue[4]-Ue[2])/cellModel.nelx*i;
            linearUe[i*2+1+cellModel.nelx*2]= Ue[3]+(Ue[5]-Ue[3])/cellModel.nelx*i;
            linearUe[i*2+cellModel.nelx*2*2]= Ue[4]+(Ue[6]-Ue[4])/cellModel.nelx*i;
            linearUe[i*2+1+cellModel.nelx*2*2]= Ue[5]+(Ue[7]-Ue[5])/cellModel.nelx*i;
            linearUe[i*2+cellModel.nelx*2*3]= Ue[6]+(Ue[0]-Ue[6])/cellModel.nelx*i;
            linearUe[i*2+1+cellModel.nelx*2*3]= Ue[7]+(Ue[1]-Ue[7])/cellModel.nelx*i;
        }
        return linearUe;
    }
    public  Map<String,DoubleMatrix> implementBoundaryConditions(DoubleMatrix K, DoubleMatrix loadConstrains, DoubleMatrix displacementConstrains){
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

    public  DoubleMatrix solve(DoubleMatrix K,DoubleMatrix F){
        return Solve.solvePositive(K,F);
    }

    public double getMacroElementEnergy(int elementNumber,DoubleMatrix U){
        DoubleMatrix ke = calculateMacroElementStiffness(elementNumber);
        DoubleMatrix Ue = getElementDisplacement(elementNumber,U,nodeNumberMatrix);
        return Ue.transpose().mmul(ke.mul(Math.pow(macroDensity.get(elementNumber),penal))).mmul(Ue).get(0);
    }

    public  double getMacroElementEnergyDerivative(int elementNumber,DoubleMatrix U){
        DoubleMatrix ke = calculateMacroElementStiffness(elementNumber);
        DoubleMatrix Ue = getElementDisplacement(elementNumber,U,nodeNumberMatrix);
        return Ue.transpose().mmul(ke.mul(Math.pow(macroDensity.get(elementNumber),penal-1)*(-penal))).mmul(Ue).get(0);

    }

    public double getMicroElementEnergy(int microElementNumber,int macroElementNumber,DoubleMatrix U){
        DoubleMatrix ke = calculateMicroElementStiffness(microElementNumber);
        DoubleMatrix Ue = getElementDisplacement(microElementNumber,U,cellModel.nodeNumberMatrix);
        return Ue.transpose().mmul(ke.mul(Math.pow(microDensity.get(macroElementNumber).get(microElementNumber),cellModel.penal))).mmul(Ue).get(0);
    }

    public  double getMicroElementEnergyDerivative(int microElementNumber,int macroElementNumber,DoubleMatrix U){
        DoubleMatrix ke = calculateMicroElementStiffness(microElementNumber);
        DoubleMatrix Ue = getElementDisplacement(microElementNumber,U,cellModel.nodeNumberMatrix);
        return Ue.transpose().mmul(ke.mul(Math.pow(microDensity.get(macroElementNumber).get(microElementNumber),cellModel.penal-1)*(-cellModel.penal))).mmul(Ue).get(0);
    }

    private  DoubleMatrix getElementDisplacement(int elementNumber,DoubleMatrix U,DoubleMatrix nodeNumberMatrix){
        return U.get(nodeNumberMatrix.getRow(elementNumber).sub(1).toIntArray(),0);
    }
}
