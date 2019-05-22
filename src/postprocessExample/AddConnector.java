package postprocessExample;

import postprocess.Connector;
import postprocess.DataLoaderAndPersist;
import postprocess.MacroVariables;
import postprocess.MicroVariables;

public class AddConnector {
    public static void main(String[] args) {
        Connector connector = new Connector();
        DataLoaderAndPersist dataLoader = new DataLoaderAndPersist("E://MultiscaleOptimization1");
        MacroVariables macroVariables = dataLoader.loadMacroVariables();
        macroVariables = reverse(macroVariables);
        MicroVariables microVariables = dataLoader.loadMicroVariables();
        microVariables = connector.addSurroundConnector(macroVariables,microVariables);
        dataLoader.saveVarialbles(macroVariables,microVariables,50);
    }
    private static MacroVariables reverse(MacroVariables macroVariables) {
        double[][] macroDensity = macroVariables.getMacroVariables();
        for (int i = 0; i < macroDensity.length; i++) {
            for (int j = 0; j < macroDensity[0].length; j++) {
                macroDensity[i][j] = 1 - macroDensity[i][j];
            }
        }
        return  new MacroVariables(macroDensity);
    }
}
