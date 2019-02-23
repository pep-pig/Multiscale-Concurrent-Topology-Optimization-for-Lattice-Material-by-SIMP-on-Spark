package algorithms;

import org.jblas.DoubleMatrix;
import org.jblas.Solve;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.sqrt;

/**
 * this is a homogenization method to get the effective material properties for
 * 2D problem including plain strain and plain stress.
 * @author fengjb
 * @version 1.0
 */
public class Homogenization {
    /**
     * @param lx Unit cell length in x-direction
     * @param ly Unit cell length in y-direction
     * @param lambda Lame's first parameter for both materials. Two entries.
     * @param mu Lame's second parameter for both material. Two entries.
     * @param phi Angle between horizontal and vertical cell wall. Degrees.
     * @param x Material indicator matrix. Size used to determine number of elements in x and y direction
     */
    public static DoubleMatrix homogenize(double lx,double ly,DoubleMatrix lambda,DoubleMatrix mu){
        /*step1:
        initialize
         */
        DoubleMatrix CH = DoubleMatrix.zeros(3,3);
        int elementAmountY = lambda.getRows();
        int elementAmountX = lambda.getColumns();
        int elementAmount = elementAmountX*elementAmountY;

        /*step2:
        generate element stiffness matrix and element load matrix
        Stiffness matrix consists of two parts. one belong to lambda and
        one belong to mu. Same goes for load vector
         */
        double dx = lx/elementAmountX;
        double dy = ly/elementAmountY;
        Map<String,Object> keAndFeMatrix = computeKeAndFe(dx/2,dy/2);

        /*step3:
        Generate finite element model
         */
        double[] order;
        order = new double[(1+elementAmountY)*(1+elementAmountX)];
        for(int i =1;i<=(1+elementAmountY)*(1+elementAmountX);i++){order[i-1]=i;}
        DoubleMatrix nodeLocation = new DoubleMatrix(1+elementAmountY,1+elementAmountX,order);
        DoubleMatrix firstNodeNumbersOfElements = nodeLocation.getRange(0,elementAmountY,0,elementAmountX).reshape(elementAmount,1).mul(2).add(1);
        double[] nodeNumberOffset = {0,1,2*elementAmountY+2,2*elementAmountY+3,2*elementAmountY,
                2*elementAmountY+1,-2,-1};
        DoubleMatrix nodeNumberMatrix = firstNodeNumbersOfElements.repmat(1,8).add(new DoubleMatrix(1,8,nodeNumberOffset).repmat(elementAmount,1));

        /*step4:
        Impose periodic bound conditions:symmetrical displacement conditions
         */
        int nodeAmount = (elementAmountX+1)*(elementAmountY+1);
        int nodeAmountAfterBoundaryConditions = elementAmountX*elementAmountY;
        double[] nodeOrder;
        nodeOrder = new double[nodeAmountAfterBoundaryConditions];
        for(int i=1;i<=nodeAmountAfterBoundaryConditions;i++){nodeOrder[i-1]=i;}
        DoubleMatrix nodeLocationAfterBoundaryConditions = DoubleMatrix.zeros(elementAmountY+1,elementAmountX+1);
        int[] rindices,cindices;
        rindices = new int[elementAmountY];
        cindices = new int[elementAmountX];
        for(int i=0;i<elementAmountX;i++){cindices[i]=i;}
        for(int i=0;i<elementAmountY;i++){rindices[i]=i;}
        nodeLocationAfterBoundaryConditions.put(rindices,cindices,new DoubleMatrix(1,nodeAmountAfterBoundaryConditions,nodeOrder).reshape(elementAmountY,elementAmountX));
        //Extend with a mirror of the top border
        nodeLocationAfterBoundaryConditions.putRow(elementAmountY,nodeLocationAfterBoundaryConditions.getRow(0));
        //Extend with a mirror of the left border
        nodeLocationAfterBoundaryConditions.putColumn(elementAmountX,nodeLocationAfterBoundaryConditions.getColumn(0));
        //Generate nodeMatrix after implementing the boundary conditions
        DoubleMatrix nodeNumberVector = DoubleMatrix.zeros(2*nodeAmount,1);
        rindices = new int[nodeAmount];
        cindices = new int[]{0};
        for(int i=0;i<nodeAmount;i+=1){rindices[i]=2*i;}
        nodeNumberVector.put(rindices,cindices,nodeLocationAfterBoundaryConditions.reshape(nodeAmount,1).mul(2).sub(1));
        for(int i=0;i<nodeAmount;i+=1){rindices[i]+=1;}
        nodeNumberVector.put(rindices,cindices,nodeLocationAfterBoundaryConditions.reshape(nodeAmount,1).mul(2));
        DoubleMatrix nodeNumberMatrixAfterBoundaryConditions = DoubleMatrix.zeros(nodeNumberMatrix.getRows(),nodeNumberMatrix.getColumns());
        for(int i =0;i<elementAmount;i++){
            nodeNumberMatrixAfterBoundaryConditions.putRow(i,nodeNumberVector.getRows(nodeNumberMatrix.getRow(i).sub(1).toIntArray()).transpose());
        }
        int numberOfDegree = 2*nodeAmountAfterBoundaryConditions;

        /*step5:
        Assemble stiffness matrix and load vector
         */
        //Material properties in the different elements
        //generate element stifness matrix and vector load
        DoubleMatrix K = DoubleMatrix.zeros(numberOfDegree,numberOfDegree);
        DoubleMatrix F = DoubleMatrix.zeros(numberOfDegree,3);
        //Assembly
        for(int i=0;i<nodeNumberMatrixAfterBoundaryConditions.getRows();i++){
            DoubleMatrix ke = ((DoubleMatrix) keAndFeMatrix.get("keLambda")).mul(lambda.get(i)).add(((DoubleMatrix)keAndFeMatrix.get("keMu")).mul(mu.get(i)));
            DoubleMatrix fe = ((DoubleMatrix) keAndFeMatrix.get("feLambda")).mul(lambda.get(i)).add(((DoubleMatrix)keAndFeMatrix.get("feMu")).mul(mu.get(i)));
            DoubleMatrix elementNode = nodeNumberMatrixAfterBoundaryConditions.getRow(i).sub(1);
            put(elementNode.toIntArray(),elementNode.toIntArray(),ke,K);
            put(elementNode.toIntArray(),new int[]{0,1,2},fe,F);
        }

        /*step6:
        Solve,To avoid rigid displacement , constrain the degree of first node
         */
        int[] indices;
        indices = new int[numberOfDegree-2];
        for (int i=0;i<(numberOfDegree-2);i++){indices[i]=i+2;}
        DoubleMatrix microDisplacement = DoubleMatrix.zeros(numberOfDegree,3);
        //TODO discard the origin K to save the memory
        K = K.get(indices,indices);
        F = F.get(indices,new int[]{0,1,2});
        microDisplacement.put(indices,new int[]{0,1,2},Solve.solvePositive(K,F));

        /*step7:
        Homegenization
         */
        //solve the displacement generated by intial strain,as it has no thing to do with the material properties. so the
        //exact ratio does not matter
        DoubleMatrix initialMicroDisplacement = DoubleMatrix.zeros(8,3);
        DoubleMatrix ke = ((DoubleMatrix) keAndFeMatrix.get("keLambda")).add((DoubleMatrix)keAndFeMatrix.get("keMu"));
        DoubleMatrix fe = ((DoubleMatrix) keAndFeMatrix.get("feLambda")).add((DoubleMatrix)keAndFeMatrix.get("feMu"));
        int[] index = new int[]{2,4,5,6,7};
        initialMicroDisplacement.put(index,new int[]{0,1,2},Solve.solvePositive(ke.get(index,index),fe.get(index,new int[]{0,1,2})));
        //caculate the effective material properties
        for (int i=0;i<=2;i++)
        {
            for (int j=0;j<=2;j++)
            {
                double ch=0;
                for (int k = 0;k<elementAmount;k++)
                {
                    ke = ((DoubleMatrix) keAndFeMatrix.get("keLambda")).mul(lambda.get(k)).add(((DoubleMatrix)keAndFeMatrix.get("keMu")).mul(mu.get(k)));
                    DoubleMatrix chi0I = initialMicroDisplacement.getColumn(i);
                    DoubleMatrix chiI = microDisplacement.getColumn(i).get(nodeNumberMatrixAfterBoundaryConditions.getRow(k).sub(1).toIntArray(),0);
                    DoubleMatrix chi0J = initialMicroDisplacement.getColumn(j);
                    DoubleMatrix chiJ = microDisplacement.getColumn(j).get(nodeNumberMatrixAfterBoundaryConditions.getRow(k).sub(1).toIntArray(),0);
                    ch += (chi0I.sub(chiI)).transpose().mmul(ke).mmul((chi0J.sub(chiJ))).get(0);
                }
                CH.put(i,j,ch);
            }
        }
        CH = CH.div(lx*ly);
        return CH;
    }

    private static Map<String,Object> computeKeAndFe(double a, double b){
        Map<String,Object> keAndFeMatrix = new HashMap<String, Object>();
        DoubleMatrix keLambda = DoubleMatrix.zeros(8,8);
        DoubleMatrix keMu = DoubleMatrix.zeros(8,8);
        DoubleMatrix feLambda = DoubleMatrix.zeros(8,3);
        DoubleMatrix feMu = DoubleMatrix.zeros(8,3);
        DoubleMatrix CLambda = new DoubleMatrix(new double[][]{{1,1,0},{1,1,0},{0,0,0}});
        DoubleMatrix CMu = new DoubleMatrix(new double[][]{{2,0,0},{0,2,0},{0,0,1}});
        //Numerical integation of ke by gaussian integartion
        double[] integrationPointx = new double[]{(-1/sqrt(3)),(1/sqrt(3))};
        double[] integrationPointy = new double[]{(-1/sqrt(3)),(1/sqrt(3))};
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
                keLambda = keLambda.add(B.transpose().mmul(CLambda).mmul(B).mul(weight));
                keMu = keMu.add(B.transpose().mmul(CMu).mmul(B).mul(weight));
                //Element Load
                feLambda = feLambda.add(B.transpose().mmul(CLambda).mul(weight).mmul(DoubleMatrix.eye(3)));
                feMu = feMu.add(B.transpose().mmul(CMu).mul(weight).mmul(DoubleMatrix.eye(3)));
            }
        }
        keAndFeMatrix.put("keLambda",keLambda);
        keAndFeMatrix.put("keMu",keMu);
        keAndFeMatrix.put("feMu",feMu);
        keAndFeMatrix.put("feLambda",feLambda);
        return keAndFeMatrix;
    }
    private static void put(int[] rindices,int[] cindices,DoubleMatrix src,DoubleMatrix des){
        for (int i=0;i<src.getColumns();i++){
            for(int j=0;j<src.getRows();j++){
                des.data[cindices[i]*des.rows+rindices[j]]+=src.data[i*src.rows+j];
            }
        }
    }
}
