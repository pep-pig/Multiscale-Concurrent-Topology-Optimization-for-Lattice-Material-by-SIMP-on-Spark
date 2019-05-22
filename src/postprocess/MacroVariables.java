package postprocess;

import java.io.Serializable;
import java.util.ArrayList;

public class MacroVariables implements Serializable {
    private static final long serialVersionUID = 19920803L;
    private double[][] macroVariables;
    public MacroVariables(){}
    public MacroVariables(double[][] macroVariables){
        this.macroVariables=macroVariables;
    }
    public void setMacroVariables(double[][] macroDensity){
        this.macroVariables = macroDensity;
    }
    public double[][] getMacroVariables(){
        return macroVariables;
    }
}
