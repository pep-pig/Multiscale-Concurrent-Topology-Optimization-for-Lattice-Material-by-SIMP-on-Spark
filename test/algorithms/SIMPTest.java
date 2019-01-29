package algorithms;

import org.junit.Test;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import static org.junit.Assert.*;

public class SIMPTest {
    @Test
    public void macroSimp1() {
        final SIMP simpOptimizer = new SIMP();
        final PostProcess postProcess = new PostProcess();
        Thread computeThread = new Thread(new Runnable() {
            public void run() {
                FiniteElementAnalysis result = simpOptimizer.macroSimp(postProcess);
            }
        });

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                postProcess.resultWindow.setVisible(true);
                postProcess.plotWindow.setVisible(true);
            }
        });
        // when debug run should be used ,when in Main start should be used;
        computeThread.run();
    }
}