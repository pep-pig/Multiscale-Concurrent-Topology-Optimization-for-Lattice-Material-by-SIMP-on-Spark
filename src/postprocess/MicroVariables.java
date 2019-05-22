package postprocess;

import org.jblas.DoubleMatrix;


import java.io.Serializable;
import java.util.ArrayList;

public class MicroVariables implements Serializable {
    private static final long serialVersionUID = 19920803L;
    private ArrayList<double[][]> microVariables = new ArrayList<>();
    public MicroVariables(){}
    public MicroVariables(ArrayList<DoubleMatrix> microVariables){
        for (int i=0;i<microVariables.size();i++){
            this.microVariables.add(microVariables.get(i).mul(-1).add(1).toArray2());
        }
    }


    public void setMacroVariables(ArrayList<double[][]> microDensity) {
        this.microVariables = microDensity;
    }

    public ArrayList<double[][]> getMicroVariables(){
        return (ArrayList<double[][]>) microVariables;
    }
}
