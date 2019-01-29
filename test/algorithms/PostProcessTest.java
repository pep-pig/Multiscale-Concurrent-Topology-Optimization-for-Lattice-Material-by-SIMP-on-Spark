package algorithms;

import org.jblas.DoubleMatrix;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;

public class PostProcessTest {

    @Test
    public void plotConvergenceCurve() {

        final PostProcess postProcess = new PostProcess();
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                postProcess.dataWindowOfMacro.setVisible(true);
            }
        });
        Thread thread = new Thread(){
            ArrayList<Double> energy = new ArrayList<Double>();
            ArrayList<Double> volume = new ArrayList<Double>();
            public void run(){
                for(int i=0;i<200;i++){
                    int y1 = new Random().nextInt(400);
                    double y2 = new Random().nextInt(10)/10.0;
                    energy.add((double)y1);
                    volume.add(y2);
                    postProcess.plotConvergenceCurve(postProcess.dataWindowOfMacro,energy,volume);
                    try{
                        Thread.sleep(1000L);
                    }
                    catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.run();
    }


    @Test
    public void plotGrayscale() {
        final PostProcess postProcess = new PostProcess();
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                postProcess.plotWindow.setVisible(true);
            }
        });
        Thread thread = new Thread(){
            double[][] density = new double[40][40];
            public void run(){
                while(true) {
                for(int i=0;i<40;i++){
                    for (int j = 0;j<40;j++) {
                        density[i][j] = new Random().nextInt(10) / 10.0;
                    }
                }
                postProcess.plotGrayscale(postProcess.plotWindow,density);
                try{
                        Thread.sleep(200L);
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
            };
        thread.run();
    }

    @Test
    public void resultStructure() {
        final PostProcess postProcess = new PostProcess();
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                postProcess.resultWindow.setVisible(true);
            }
        });
        Thread thread = new Thread(){
            ArrayList<DoubleMatrix> microDensity = new ArrayList<DoubleMatrix>();
            double[][] density = new double[40][40];
            public void run(){
                    for(int k=0;k<16;k++){
                        for(int i=0;i<40;i++){
                            for (int j = 0;j<40;j++) {
                                density[i][j] = new Random().nextInt(10)/10.0;
                            }
                        }
                        microDensity.add(new DoubleMatrix(density));
                    }
                    postProcess.plotRealStructure(postProcess.resultWindow,microDensity,2);
                    try{
                        Thread.sleep(20000000L);
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }

        };
        thread.run();
    }

}
