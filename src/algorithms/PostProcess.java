package algorithms;

import org.jblas.DoubleMatrix;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class PostProcess  {
    public PostProcess(){

            this.dataWindowOfMacro = new DataWindow("Macro Convergence Curve");
            this.dataWindowOfMicro = new DataWindow("Micro Convergence Curve");
            this.plotWindow = new PlotWindow("Macro Grayscale");
            this.resultWindow = new ResultWindow("Real Structure");
        }

    DataWindow dataWindowOfMacro;
    DataWindow dataWindowOfMicro;
    PlotWindow plotWindow;
    ResultWindow resultWindow;

    public class DataWindow extends JFrame{

        private ArrayList<Double> energy = new ArrayList<Double>();
        private ArrayList<Double> volume = new ArrayList<Double>();
        double maxValue=1;
        int iteration = 0;
        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JPanel dataPanel;
        private javax.swing.JTextArea dataTextArea;
        private javax.swing.JScrollPane jScrollPane1;
        private DataWindow(String title){
            initComponents();
            setTitle(title);
        }
        public void paint(Graphics g){
            super.paint(g);
            //Define the plot region
            int yAxisStart = dataPanel.getY() +60;
            int yAxisEnd =dataPanel.getY()+dataPanel.getHeight()-10 ;
            int xAxisStart = dataPanel.getX() + 43;
            int xAxisEnd = dataPanel.getX() + dataPanel.getWidth() - 43;
            //compute the scale ratio
            double yScale = ((yAxisEnd - yAxisStart)/maxValue);
            int y2Scale = (int)((yAxisEnd - yAxisStart)/1.0);
            int xScale = (xAxisEnd - xAxisStart)/(energy.size());
            // Plot x axis and y1、y2 axis
            g.setColor(Color.BLACK);
            g.drawLine(xAxisStart, yAxisEnd, xAxisStart, yAxisStart);//Y1 axis
            g.drawLine(xAxisStart, yAxisEnd, xAxisEnd, yAxisEnd);//X axis
            g.drawLine(xAxisEnd,yAxisEnd,xAxisEnd,yAxisStart);//Y2 axis
            //Plot tick on x axis
            int min=0;
            int xTickNumber;
            int xTickInterval;
            if (energy.size()<8){
                xTickNumber = energy.size();
                xTickInterval =(int)Math.ceil((energy.size()/(float)xTickNumber))*xScale;
            }
            else {xTickNumber = 8;
                xTickInterval =(int)Math.ceil((energy.size()/(float)xTickNumber))*xScale;
            }
            for (int xTick = xAxisStart + xTickInterval; xTick < xAxisEnd; xTick += xTickInterval) {   // tick every 1 minute
                g.drawLine(xTick, yAxisEnd -5, xTick, yAxisEnd);
                min += xTickInterval/xScale;
                g.drawString(Integer.toString(min), xTick - (min < 10 ? 3 : 7) , yAxisEnd + 20);
            }
            //Plot tick on left y1 axis
            int yTickInterval;
            int yTickNumber;
            if (energy.size()<10){
                yTickNumber = energy.size();
                yTickInterval = (int)((maxValue/yTickNumber)*yScale);
            }
            else {yTickNumber = 8;
                yTickInterval = (int)((maxValue/yTickNumber)*yScale);
            }
            g.setColor(Color.BLUE);
            for (int yTick = yAxisEnd-yTickInterval; yTick>yAxisStart ; yTick -= yTickInterval) {         // tick every 200
                g.drawLine(xAxisStart, yTick, xAxisStart + 5, yTick);
                g.drawString(Integer.toString((int)((yAxisEnd-yTick)/yScale)), xAxisStart - 18 , yTick + 5);
            }
            //Plot tick on right y2 axis
            int y2TickInterval;
            int y2TickNumber=10;
            y2TickInterval = (int)Math.floor((1.0/y2TickNumber)*y2Scale);
            g.setColor(Color.RED);
            for (int y2Tick = yAxisEnd-y2TickInterval; y2Tick>yAxisStart ; y2Tick -= y2TickInterval) {         // tick every 200
                g.drawLine(xAxisEnd, y2Tick, xAxisEnd + 5, y2Tick);
                g.drawString(String.format("%.1f",((yAxisEnd-y2Tick)/(float)y2Scale)), xAxisEnd +8 , y2Tick + 5);
            }
            //Plot line
            int xStart = xAxisStart;
            int yStart = yAxisEnd-(int)(energy.get(0)*yScale);
            int y2Start =  yAxisEnd-(int)(volume.get(0)*y2Scale);
            for (int i = 1; i < energy.size(); i++) {
                int xEnd = (xStart + xScale);
                int yEnd = (int)(yAxisEnd- energy.get(i).intValue()*yScale);
                int y2End = (int)(yAxisEnd- volume.get(i)*y2Scale);
                g.setColor(Color.BLUE);

                g.drawLine(xStart, yStart, xEnd, yEnd);
                g.setColor(Color.RED);
                g.drawLine(xStart, y2Start, xEnd, y2End);
                xStart = xEnd;
                yStart = yEnd;
                y2Start = y2End;
            }
        }
        private void addData(double currentEnergy,double currentVolume){
            if (currentEnergy>maxValue){
                maxValue = currentEnergy;
            }
            iteration++;
            energy.add(currentEnergy);
            volume.add(currentVolume);
            dataTextArea.append("iteration:"+iteration+";  "+"energy = "+currentEnergy+";  "+"volume = "+currentVolume+'\n');
            dataTextArea.setCaretPosition(dataTextArea.getText().length());
            repaint();
        }
        //public void plotCrayscale(Graphics g){}
        /** This method is called from within the constructor to
         * initialize the form.
         * WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the Form Editor.
         */
        @SuppressWarnings("unchecked")
        //GEN-BEGIN:initComponents
        private void initComponents() {
            dataPanel = new javax.swing.JPanel();
            jScrollPane1 = new javax.swing.JScrollPane();
            dataTextArea = new javax.swing.JTextArea();

            dataPanel.setBackground(new java.awt.Color(255, 255, 255));
            dataPanel.setMinimumSize(new java.awt.Dimension(400, 250));
            dataPanel.setPreferredSize(new java.awt.Dimension(400, 250));
            getContentPane().add(dataPanel, java.awt.BorderLayout.CENTER);

            jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            jScrollPane1.setMinimumSize(new java.awt.Dimension(400, 100));
            jScrollPane1.setPreferredSize(new java.awt.Dimension(400, 100));

            dataTextArea.setColumns(20);
            dataTextArea.setEditable(false);
            dataTextArea.setRows(4);
            jScrollPane1.setViewportView(dataTextArea);
            getContentPane().add(jScrollPane1, java.awt.BorderLayout.SOUTH);
            pack();
        }// //GEN-END:initComponents
    }
    public void plotConvergenceCurve(DataWindow dataWindow,ArrayList<Double> energy, ArrayList<Double> volume){
        int size = energy.size();
        while(energy.size()==size){
            dataWindow.addData(energy.get(energy.size()-1),volume.get(volume.size()-1));
            size++;
        }
    }

    public class PlotWindow extends JFrame{
        int iteration = 0;
        int[][] grayValue;
        private javax.swing.JPanel plotPanel;
        private PlotWindow(String title){
            initComponents();
            setTitle(title);
        }

        private void setGrayValue(int[][] grayValue){
            this.grayValue = grayValue;
        }

        public void paint(Graphics g){
            super.paint(g);
            BufferedImage bufImg = convertRGBImage(grayValue);
            g.drawImage(bufImg,plotPanel.getX()+50,plotPanel.getY()+60,plotPanel.getX()+plotPanel.getWidth()-80,plotPanel.getY()+plotPanel.getHeight()-60,this);
        }

        public BufferedImage convertRGBImage(int[][] rgbValue){
            int height = rgbValue.length;
            int width = rgbValue[0].length;
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            //we either have to loop through all values, or convert to 1-d array
            for(int y=0; y< height; y++){
                for(int x=0; x< width; x++){
                    int rgb = new Color(rgbValue[y][x],rgbValue[y][x],rgbValue[y][x]).getRGB();
                    bufferedImage.setRGB(x,y,rgb);
                }
            }
            return bufferedImage;
        }
        @SuppressWarnings("unchecked")
        //GEN-BEGIN:initComponents
        private void initComponents() {
            plotPanel = new javax.swing.JPanel();
            plotPanel.setBackground(new java.awt.Color(255, 255, 255));
            plotPanel.setMinimumSize(new java.awt.Dimension(400, 250));
            plotPanel.setPreferredSize(new java.awt.Dimension(400, 250));
            getContentPane().add(plotPanel, java.awt.BorderLayout.CENTER);
            pack();
        }
    }
    public void plotGrayscale(PlotWindow plotWindow,double[][] density){
        int[][] grayValue = new int[density.length][density[0].length];
        for (int x = 0;x<density.length;x++){
            for(int y = 0;y<density[0].length;y++){
                grayValue[x][y] = (int)(density[x][y]*255);
            }
        }
        plotWindow.setGrayValue(grayValue);
        plotWindow.repaint();
    }

    public class ResultWindow extends JFrame{
        int iteration = 0;
        int numberOfCellY;

        ArrayList<DoubleMatrix> microDensity = new ArrayList<DoubleMatrix>();
        private javax.swing.JPanel plotPanel;
        private ResultWindow(String title){
            initComponents();
            setTitle(title);
        }

        private void setMicroDensity(ArrayList<DoubleMatrix> microDensity,int numberOfCellY){
            this.numberOfCellY = numberOfCellY;
            this.microDensity = microDensity;
        }

        public void paint(Graphics g){
            int xStart = plotPanel.getX()+50;
            int xEnd = plotPanel.getX()+plotPanel.getWidth()-80;
            int yStart = plotPanel.getY()+60;
            int yEnd = plotPanel.getY()+plotPanel.getHeight()-60;
            int rows = numberOfCellY;
            int columns =  microDensity.size()/numberOfCellY;
            int xInterval =(xEnd-xStart)/columns;
            //int yInterval = (yEnd-yStart)/rows;
            int yInterval = xInterval;
            super.paint(g);
            int k = 0;
            for(int j = 0;j<columns;j++){
                for(int i = 0;i<rows;i++){
                    BufferedImage bufImg = convertRGBImage(microDensity.get(k).mul(-1).add(1).mul(255).toIntArray2());
                    k++;
                    g.drawImage(bufImg,xStart,yStart,xInterval,yInterval,this);
                    yStart = yStart+yInterval+1;
                }
                xStart = xStart+yInterval+2;
                yStart = plotPanel.getY()+60;
            }
        }

        public BufferedImage convertRGBImage(int[][] rgbValue){
            int height = rgbValue.length;
            int width = rgbValue[0].length;
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            //we either have to loop through all values, or convert to 1-d array
            for(int y=0; y< height; y++){
                for(int x=0; x< width; x++){
                    int rgb = new Color(rgbValue[y][x],rgbValue[y][x],rgbValue[y][x]).getRGB();
                    bufferedImage.setRGB(x,y,rgb);
                }
            }
            return bufferedImage;
        }
        @SuppressWarnings("unchecked")
        //GEN-BEGIN:initComponents
        private void initComponents() {
            plotPanel = new javax.swing.JPanel();
            plotPanel.setBackground(new java.awt.Color(255, 255, 255));
            plotPanel.setMinimumSize(new java.awt.Dimension(400, 250));
            plotPanel.setPreferredSize(new java.awt.Dimension(400, 250));
            getContentPane().add(plotPanel, java.awt.BorderLayout.CENTER);
            pack();
        }
    }
    public void plotRealStructure(ResultWindow resultWindow,ArrayList<DoubleMatrix>microDensity,int numberOfCellY){
        ArrayList<DoubleMatrix> grayValueList = new ArrayList<DoubleMatrix>();
        for (int i=0;i<microDensity.size();i++){
            grayValueList.add(new DoubleMatrix(microDensity.get(i).toArray2()));
        }
        resultWindow.setMicroDensity(grayValueList,numberOfCellY);
        resultWindow.repaint();
    }
}

