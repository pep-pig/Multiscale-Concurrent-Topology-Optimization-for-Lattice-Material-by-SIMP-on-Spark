package algorithms;

public class Main {
    public static void main(String[] args) {
        final SIMP simpOptimizer = new SIMP(args[0]);
        final PostProcess postProcess = new PostProcess(simpOptimizer.fem.nelx,simpOptimizer.fem.nely,
                simpOptimizer.fem.cellModel.nelx,simpOptimizer.fem.cellModel.nely);
        Thread computeThread = new Thread(new Runnable() {
            public void run() {
                FiniteElementAnalysis result = simpOptimizer.macroSimp(postProcess);
            }
        });
        computeThread.start();
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                postProcess.resultWindow.setVisible(true);
                postProcess.plotWindow.setVisible(true);
            }
        });
    }
}
