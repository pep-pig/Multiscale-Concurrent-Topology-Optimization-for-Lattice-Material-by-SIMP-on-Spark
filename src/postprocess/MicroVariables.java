package postprocess;

import org.jblas.DoubleMatrix;


import java.io.Serializable;
import java.util.ArrayList;

public class MicroVariables implements Serializable {
    private static final long serialVersionUID = 19920803L;
    private ArrayList<double[][]> microVariables = new ArrayList<>();
    public MicroVariables(){}

    public MicroVariables(ArrayList<DoubleMatrix> microVariables){
        for (DoubleMatrix microVariable : microVariables) {
            this.microVariables.add(microVariable.toArray2());
        }
    }

    public void setMicroVariables(ArrayList<double[][]> microDensity) {
        this.microVariables = microDensity;
    }

    public ArrayList<double[][]> getMicroVariables(){
        return microVariables;
    }

    public MicroVariables reverseMicroVariable(){
        for(int i = 0;i<microVariables.size();i++){
            microVariables.set(i,new DoubleMatrix(microVariables.get(i)).mul(-1).add(1).toArray2());
        }
        return this;
    }
}
