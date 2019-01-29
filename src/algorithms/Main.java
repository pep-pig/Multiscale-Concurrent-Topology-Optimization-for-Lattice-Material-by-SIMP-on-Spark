package algorithms;

public class Main {
    public static void main(String[] args) {
        final SIMP simpOptimizer = new SIMP();
        final PostProcess postProcess = new PostProcess();
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
