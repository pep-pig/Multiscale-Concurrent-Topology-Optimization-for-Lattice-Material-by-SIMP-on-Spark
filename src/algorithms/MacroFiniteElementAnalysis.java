package algorithms;

import org.apache.spark.api.java.function.Function;
import org.jblas.DoubleMatrix;
import scala.Tuple2;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MacroFiniteElementAnalysis extends FiniteElementAnalysis  {
    public MacroFiniteElementAnalysis(){
            }

    public void updateMacroMaterialModel(ArrayList<DoubleMatrix> C,ArrayList<DoubleMatrix>microDensity){
        double elx = cellModel.length/cellModel.nelx;
        double ely = cellModel.height/cellModel.nely;
        for(int i = 0;i<nely*nelx; i++){
            C.set(i,Homogenization.homogenize(elx,ely,microDensity.get(i).mul(cellModel.lambda),microDensity.get(i).mul(cellModel.mu)));
        }
    }
    public  DoubleMatrix assemblyMacroElementStiffnessMatrix(DoubleMatrix macroDensity,ArrayList<DoubleMatrix> C){
        DoubleMatrix K = new DoubleMatrix((nelx+1)*(nely+1)*2,(nelx+1)*(nely+1)*2);
        for(int i=0;i<nodeNumberMatrix.getRows();i++){
            DoubleMatrix ke = calculateMacroElementStiffness(i,C);
            DoubleMatrix elementNode = nodeNumberMatrix.getRow(i).sub(1);
            put(elementNode.toIntArray(),elementNode.toIntArray(),ke.mul(Math.pow(macroDensity.get(i),penal)),K);
        }
        return K;
    }
    //cantilever beam with load in the bottom right corner
    public Map<String,DoubleMatrix> boundaryCondition1(){
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
    public Map<String,DoubleMatrix> boundaryCondition2(){
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
    public Map<String,DoubleMatrix> boundaryCondition3(){
        Map<String,DoubleMatrix> boundaryConditions = new HashMap<String,DoubleMatrix>();
        DoubleMatrix loadConstrains = new DoubleMatrix(1,2);
        DoubleMatrix displacementConstrains = new DoubleMatrix(3,2);

        displacementConstrains.put(0,0,(nely+1)*2-1);
        displacementConstrains.put(0,1,0);
        displacementConstrains.put(1,0,(nely+1)*2);
        displacementConstrains.put(1,1,0);
        displacementConstrains.put(2,0,(nelx+1)*(nely+1)*2);
        displacementConstrains.put(2,1,0);

        loadConstrains.put(0,0,((nelx)/2)*(nely+1)*2+2);
        loadConstrains.put(0,1,force);
        boundaryConditions.put("loadConstrains",loadConstrains);
        boundaryConditions.put("displacementConstrains",displacementConstrains);
        return boundaryConditions;
    }

    //TODO merge the two method
    public double getMacroElementEnergy(int elementNumber,DoubleMatrix U,ArrayList<DoubleMatrix> C,DoubleMatrix macroDensity){
        DoubleMatrix ke = calculateMacroElementStiffness(elementNumber,C);
        DoubleMatrix Ue = getElementDisplacement(elementNumber,U,nodeNumberMatrix);
        return Ue.transpose().mmul(ke.mul(Math.pow(macroDensity.get(elementNumber),penal))).mmul(Ue).get(0);
    }

    public  double getMacroElementEnergyDerivative(int elementNumber,DoubleMatrix U,ArrayList<DoubleMatrix> C,DoubleMatrix macroDensity){
        DoubleMatrix ke = calculateMacroElementStiffness(elementNumber,C);
        DoubleMatrix Ue = getElementDisplacement(elementNumber,U,nodeNumberMatrix);
        return Ue.transpose().mmul(ke.mul(Math.pow(macroDensity.get(elementNumber),penal-1)*(-penal))).mmul(Ue).get(0);
    }
    public  DoubleMatrix calculateMacroElementStiffness(int elementNumber,ArrayList<DoubleMatrix> C){
        double a = length/nelx;
        double b = height/nely;
        return gaussIntegration(a,b,C.get(elementNumber));
    }


    public DoubleMatrix getMacroU(Tuple2<Integer, Tuple2<ArrayList<DoubleMatrix>, DoubleMatrix>> MatAndDensity) throws Exception {
        ArrayList<DoubleMatrix> materialData = MatAndDensity._2._1();
        DoubleMatrix macroDensityData = MatAndDensity._2._2();
        DoubleMatrix K = assemblyMacroElementStiffnessMatrix(macroDensityData,materialData);
        Map<String, DoubleMatrix> boundaryCondition;
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
        //step3.3 solve fem linearSystem
        DoubleMatrix macroU = solve(linearSystem.get("K"), linearSystem.get("F"));
        return macroU;
    }
}


