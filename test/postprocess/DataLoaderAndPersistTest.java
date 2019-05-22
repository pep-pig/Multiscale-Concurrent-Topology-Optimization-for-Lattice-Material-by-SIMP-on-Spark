package postprocess;

import org.apache.hadoop.yarn.webapp.hamlet.Hamlet;
import org.jblas.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.DATA_CONVERSION;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class DataLoaderAndPersistTest {
    private DataLoaderAndPersist dataLoaderAndPersist;
    @Before
    public void setUp() throws Exception {
        dataLoaderAndPersist = new DataLoaderAndPersist("C://optimization");
    }

    @Test
    public void loadMacroVariables() {
        MacroVariables macroVariables = dataLoaderAndPersist.loadMacroVariables();
        double[][] macroDensity = macroVariables.getMacroVariables();
        System.out.println(macroDensity[0][0]);
    }

    @Test
    public void loadMicroVariables() {
        MacroVariables macroVariables = dataLoaderAndPersist.loadMacroVariables();
        double[][] macroDensity = macroVariables.getMacroVariables();
        System.out.println(macroDensity);
    }

    @Test
    public void saveVarialbles() {
        double[][] macroDensity = new double[][]{{0.1,0.2},{0.3,0.4}};

        double[][] cellDensity = new double[2][2];
        ArrayList<double[][]> microDensity = new ArrayList<>(4);
        for (int i=0;i<4;i++){
            for(int n=0;n<2;n++){
                for(int m=0;m<2;m++){
                    cellDensity[n][m] = Random.nextDouble();
                }
            }
            microDensity.add(cellDensity);
        }
        MacroVariables macroVariables = new MacroVariables(macroDensity);
        MicroVariables microVariables = new MicroVariables();
        microVariables.setMacroVariables(microDensity);
        dataLoaderAndPersist.saveVarialbles(macroVariables,microVariables,1);
    }

    @Test
    public void savaImg(){

    }
}