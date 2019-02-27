package algorithms;

import org.apache.spark.api.java.function.Function;
import org.jblas.DoubleMatrix;

import java.util.HashMap;
import java.util.Map;

public class MicroFiniteElementAnalysis extends FiniteElementAnalysis {


    public  DoubleMatrix assemblyMicroElementStiffnessMatrix(DoubleMatrix density){
        DoubleMatrix K = new DoubleMatrix((cellModel.nelx+1)*(cellModel.nely+1)*2,(cellModel.nelx+1)*(cellModel.nely+1)*2);
        DoubleMatrix ke = calculateMicroElementStiffness();
        for(int i=0;i<cellModel.nodeNumberMatrix.getRows();i++){
            DoubleMatrix elementNode = cellModel.nodeNumberMatrix.getRow(i).sub(1);
            put(elementNode.toIntArray(),elementNode.toIntArray(),ke.mul(Math.pow(density.get(i),cellModel.penal)),K);
        }
        return K;
    }
    //Given displacement in four corner obtained in fem finite element analysis
    public Map<String,DoubleMatrix> microBoundary(DoubleMatrix Ue){
        Map<String,DoubleMatrix> boundaryConditions = new HashMap<String,DoubleMatrix>();
        DoubleMatrix displacementConstrains = new DoubleMatrix(8,2);
        int[] nodeList = {(cellModel.nely+1)*2-1,(cellModel.nely+1)*2,(cellModel.nelx+1)*(cellModel.nely+1)*2-1,(cellModel.nelx+1)*(cellModel.nely+1)*2,(cellModel.nelx)*(cellModel.nely+1)*2+1,(cellModel.nelx)*(cellModel.nely+1)*2+2,1,2};
        for (int i=0;i<8;i++){
            displacementConstrains.put(i,0,nodeList[i]);
            displacementConstrains.put(i,1,Ue.get(i));
        }
        boundaryConditions.put("loadConstrains",null);
        boundaryConditions.put("displacementConstrains",displacementConstrains);
        return boundaryConditions;

    }
    //TODO merge the two method into one
    public double getMicroElementEnergy(int microElementNumber,DoubleMatrix microDensity,DoubleMatrix U){
        DoubleMatrix ke = calculateMicroElementStiffness();
        DoubleMatrix Ue = getElementDisplacement(microElementNumber,U,cellModel.nodeNumberMatrix);
        return Ue.transpose().mmul(ke.mul(Math.pow(microDensity.get(microElementNumber),cellModel.penal))).mmul(Ue).get(0);
    }
    public  double getMicroElementEnergyDerivative(int microElementNumber,DoubleMatrix microDensity,DoubleMatrix U){
        DoubleMatrix ke = calculateMicroElementStiffness();
        DoubleMatrix Ue = getElementDisplacement(microElementNumber,U,cellModel.nodeNumberMatrix);
        return Ue.transpose().mmul(ke.mul(Math.pow(microDensity.get(microElementNumber),cellModel.penal-1)*(-cellModel.penal))).mmul(Ue).get(0);
    }
    public  DoubleMatrix calculateMicroElementStiffness(){
        double a = cellModel.length/cellModel.nelx;
        double b = cellModel.height/cellModel.nely;
        DoubleMatrix CLambda = new DoubleMatrix(new double[][]{{1,1,0},{1,1,0},{0,0,0}});
        DoubleMatrix CMu = new DoubleMatrix(new double[][]{{2,0,0},{0,2,0},{0,0,1}});
        double lambda = cellModel.lambda;
        double mu = cellModel.mu;
        DoubleMatrix C = CLambda.mul(lambda).add(CMu.mul(mu));
        return gaussIntegration(a,b,C);
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

}
