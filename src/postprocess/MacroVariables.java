package postprocess;

import org.jblas.DoubleMatrix;

import java.io.Serializable;
import java.util.ArrayList;

public class MacroVariables implements Serializable {
    private static final long serialVersionUID = 19920803L;
    private double[][] macroVariables;
    private int iteration=0;
    public MacroVariables(){}
    public MacroVariables(double[][] macroVariables){
        this.macroVariables=macroVariables;
    }
    public MacroVariables(double[][] macroVariables,int iteration){
        this.iteration = iteration;
        this.macroVariables=macroVariables;
    }
    public void setMacroVariables(double[][] macroDensity){
        this.macroVariables = macroDensity;
    }
    public double[][] getMacroVariables(){
        return macroVariables;
    }
    public int getIteration(){
        return iteration;
    }
    public MacroVariables reverseMacroVariable(){
        this.macroVariables = new DoubleMatrix(macroVariables).mul(-1).add(1).toArray2();
        return this;
    }

}
