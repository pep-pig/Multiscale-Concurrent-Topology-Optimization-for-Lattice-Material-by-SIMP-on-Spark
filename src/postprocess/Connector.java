package postprocess;

import org.jblas.DoubleMatrix;

import java.util.ArrayList;

public class Connector {
    public MicroVariables addSurroundConnector(MacroVariables macroVariables, MicroVariables microVariables){
        double[][] macroDensity = macroVariables.getMacroVariables();
        ArrayList<double[][]> microDensity = microVariables.getMicroVariables();
        MicroVariables newMicroVariables = new MicroVariables();
        int cellHeight = microDensity.get(0).length;
        int cellWidth = microDensity.get(0)[0].length;
        for(int i= 0;i<macroDensity[0].length;i++){
            for(int j = 0;j<macroDensity.length;j++){
                if(macroDensity[j][i]<0.101){
                    microDensity.set(i*macroDensity.length+j,new DoubleMatrix(cellHeight,cellWidth).add(1).toArray2());
                }
                else{
                    double[][] newElementMicroDensity = microDensity.get(i*macroDensity.length+j);
                    for (int k = 0;k<newElementMicroDensity.length;k++){
                        newElementMicroDensity[0][k] = 0;
                        newElementMicroDensity[newElementMicroDensity.length-1][k] = 0;
                        newElementMicroDensity[k][0] = 0;
                        newElementMicroDensity[k][newElementMicroDensity.length-1] = 0;
                        microDensity.set(i*macroDensity.length+j,newElementMicroDensity);
                    }
                }
            }
        }
        newMicroVariables.setMacroVariables(microDensity);
        return newMicroVariables;
    }
    public MicroVariables addCornerConnector(MacroVariables macroVariables, MicroVariables microVariables){
        int cornerConnectorLength = (int)Math.sqrt(0.025*microVariables.getMicroVariables().get(0).length);
        double[][] macroDensity = macroVariables.getMacroVariables();
        ArrayList<double[][]> microDensity = microVariables.getMicroVariables();
        MicroVariables newMicroVariables = new MicroVariables();
        int cellHeight = microDensity.get(0).length;
        int cellWidth = microDensity.get(0)[0].length;
        int cellLength = cellHeight;
        for(int i= 0;i<macroDensity[0].length;i++){
            for(int j = 0;j<macroDensity.length;j++){
                if(macroDensity[j][i]<0.101){
                    microDensity.set(i*macroDensity.length+j,new DoubleMatrix(cellHeight,cellWidth).add(1).toArray2());
                }
                else{
                    double[][] newElementMicroDensity = microDensity.get(i*macroDensity.length+j);
                    for (int m = 0;m<cornerConnectorLength;m++){
                        for(int n = 0;n<cornerConnectorLength;n++){
                            //first corner,start from(0,0),end with(cornerConectorLengh-1，cornerConectorLengh-1)
                            newElementMicroDensity[m][n] = 0;
                            //second corner,start from(length-cornerConectorLength,0),end with(Length-1,cornerConectorLength-1)
                            newElementMicroDensity[cellLength-cornerConnectorLength+n][m] = 0;
                            //third corner,start from(0,length-cornerConectorLength),end with(cornerConectorLength-1,length-1)
                            newElementMicroDensity[m][cellLength-cornerConnectorLength+n] = 0;
                            //fourth corner,start from(length-cornerConectorLength,length-cornerConectorLenght) end with(length-1,length-1)
                            newElementMicroDensity[cellLength-cornerConnectorLength+m][cellLength-cornerConnectorLength+n] = 0;
                            microDensity.set(i*macroDensity.length+j,newElementMicroDensity);
                        }
                    }
                }
            }
        }
        newMicroVariables.setMacroVariables(microDensity);
        return newMicroVariables;
    }

    public MicroVariables densityFilter(MacroVariables macroVariables,MicroVariables microVariables,double threshold){
        double[][] macroDensity = macroVariables.getMacroVariables();
        ArrayList<double[][]> microDensity = microVariables.getMicroVariables();
        MicroVariables newMicroVariables = new MicroVariables();
        int cellHeight = microDensity.get(0).length;
        int cellWidth = microDensity.get(0)[0].length;
        for(int i= 0;i<macroDensity[0].length;i++){
            for(int j = 0;j<macroDensity.length;j++) {
                if (macroDensity[j][i] < threshold) {
                    microDensity.set(i * macroDensity.length + j, new DoubleMatrix(cellHeight, cellWidth).add(1).toArray2());
                }
            }
        }
        newMicroVariables.setMacroVariables(microDensity);
        return newMicroVariables;
    }
}


