package algorithms;
import org.jblas.DoubleMatrix;
import static org.jblas.MatrixFunctions.sqrt;
public class OC {
    public static DoubleMatrix oc(double nelx,double nely,DoubleMatrix density,double volumeFactor,DoubleMatrix dc,double move,double upperLimit,double lowerLimit,int iteration){
        double L1 = 0;
        double L2 = 1E5;
        double lMid;
        double volume = volumeFactor*nelx*nely;
        move = Math.max(Math.pow(0.96,(double)iteration),0.001);
        //System.out.println("current iteration's move is "+ move);
        DoubleMatrix newDensity = new DoubleMatrix(density.getRows(),density.getColumns());
        while(L2-L1>1E-4){
            lMid = 0.5*(L2+L1);
            //TODO move should be dynamicly tuned
            newDensity =(density.add(move).min(density.mul(sqrt(dc.mul(-1).div(lMid))))).min(upperLimit).max(density.sub(move)).max(lowerLimit);
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
