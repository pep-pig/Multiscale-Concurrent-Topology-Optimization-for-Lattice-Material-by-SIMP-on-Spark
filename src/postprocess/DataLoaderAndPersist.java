package postprocess;

import algorithms.FiniteElementAnalysis;
import algorithms.MicroOutputRDDPartitioner;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.sql.BatchUpdateException;

public class DataLoaderAndPersist {
    private String path;
    public DataLoaderAndPersist(String directoryPath){
        this.path =directoryPath;
        File file = new File(this.path);
        if(file.exists()){
            System.out.println("directory is already exists:"+directoryPath);
        }
        else if (!file.mkdirs()){
            System.out.println("failed to create directory with path:"+directoryPath);
        }
    }
    public MacroVariables loadMacroVariables(){
        Object object = loadObject(path+System.getProperty("file.separator")+"macro.txt");
        return (MacroVariables)object;
    }
    public MicroVariables loadMicroVariables(){
        Object object = loadObject(path+System.getProperty("file.separator")+"micro.txt");
        return (MicroVariables)object;
    }
    public Object loadObject(String fileName){

        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(fileName));
            Object object = objectInputStream.readObject();
            return object;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public  void saveVarialbles(MacroVariables macroVariables,MicroVariables microVariables,int ID){

        saveObject(macroVariables,path+System.getProperty("file.separator")+"macro.txt");
        saveObject(microVariables,path+System.getProperty("file.separator")+"micro.txt");
        saveImg(macroVariables,path+System.getProperty("file.separator")+"macro"+ID,microVariables,path+System.getProperty("file.separator")+"micro"+ID);
    }
    public void saveObject(Object o, String fileName){
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(fileName));
            objectOutputStream.writeObject(o);
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void saveImg(MacroVariables macroVariables, String macroFileName, MicroVariables microVariables,String microFileName){
        //prepare macro-Img matrix
        int macroWidth = macroVariables.getMacroVariables()[0].length;
        int macroHeight = macroVariables.getMacroVariables().length;
        int[] macroData = new int[macroWidth*macroHeight];
        for(int i=0;i<macroHeight;i++){
            for(int j = 0;j<macroWidth;j++){
                macroData[i*macroWidth+j]=new Color((int)(macroVariables.getMacroVariables()[i][j]*255),(int)(macroVariables.getMacroVariables()[i][j]*255),(int)(macroVariables.getMacroVariables()[i][j]*255)).getRGB();
            }
        }
        //prepare micro-Img matrix
        int cellWidth = microVariables.getMicroVariables().get(0)[0].length;
        int cellHeight = microVariables.getMicroVariables().get(0).length;
        int microWidth = macroWidth*cellWidth;
        int microHeight = macroHeight*cellHeight;
        double[][] microDensity = new double[microHeight][microWidth];
        int[] microData = new int[microWidth*microHeight];
        for (int i=0;i<macroWidth;i++){
            for(int j=0;j<macroHeight;j++){
                for(int n=0;n<cellHeight;n++){
                    for(int m=0;m<cellWidth;m++){
                        microDensity[j*cellHeight+n][i*cellWidth+m]=microVariables.getMicroVariables().get(i*macroHeight+j)[n][m];
                    }
                }
            }
        }
        for(int i=0;i<microHeight;i++){
            for(int j=0;j<microWidth;j++){
                microData[i*microWidth+j]=new Color((int)(microDensity[i][j]*255),(int)(microDensity[i][j]*255),(int)(microDensity[i][j]*255)).getRGB();
            }
        }
        //save image
        BufferedImage macroBfImg = new BufferedImage(macroWidth,macroHeight,BufferedImage.TYPE_INT_BGR);
        macroBfImg.setRGB(0,0,macroWidth,macroHeight,macroData,0,macroWidth);
        BufferedImage microBfImg = new BufferedImage(microWidth,microHeight,BufferedImage.TYPE_INT_BGR);
        microBfImg.setRGB(0,0,microWidth,microHeight,microData,0,microWidth);
        try {
            File macroFile= new File(macroFileName+".png");
            File microFile = new File(microFileName+".png");
            ImageIO.write((RenderedImage)macroBfImg,"PNG", macroFile);
            ImageIO.write((RenderedImage)microBfImg,"PNG",microFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
