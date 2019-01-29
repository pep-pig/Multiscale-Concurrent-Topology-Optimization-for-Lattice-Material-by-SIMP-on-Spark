package algorithms;

import org.jblas.DoubleMatrix;
import org.junit.Test;


import java.util.Map;

public class FiniteElementMethodTest {
    @Test
    public void testMicroBoundary(){
        FiniteElementAnalysis fem = new FiniteElementAnalysis();
        Parameter.init(fem);
        fem.setMacroMeshModel(fem.length,fem.height, fem.nelx, fem.nely);
        fem.setMicroMeshModel(fem.cellModel.length, fem.cellModel.height, fem.cellModel.nelx, fem.cellModel.nely);
        //step2 generate material model
        fem.initialMicroMaterialModel(fem.cellModel.lambda, fem.cellModel.mu,fem.volf,fem.cellModel.penal);
        fem.initialMacroMaterialModel(fem.penal);
        DoubleMatrix K = fem.assemblyMicroElementStiffnessMatrix(fem.microDensity.get(0), fem.cellModel.penal, fem.cellModel.nodeNumberMatrix);
        Map<String, DoubleMatrix> boundaryConditions = fem.microLinearIntepolatingBoundary(0,new DoubleMatrix());
        Map<String, DoubleMatrix> linearSystem = fem.implementBoundaryConditions(K, boundaryConditions.get("loadConstrains"), boundaryConditions.get("displacementConstrains"));
        DoubleMatrix microU = fem.solve(linearSystem.get("K"), linearSystem.get("F"));
        System.out.println("finished");
    }
}
