package postprocess;

import java.io.*;

public  class Logger {
    public  FileOutputStream logger;
    public Logger(String directoryPath){
        try {
            File file = new File(directoryPath+System.getProperty("file.separator")+"log.txt");
            if(!file.exists()){
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            logger = new FileOutputStream(directoryPath+System.getProperty("file.separator")+"log.txt",true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public  void write(String content){
        try {
            logger.write(content.getBytes());
            logger.write(System.getProperty("line.separator").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void close(){
        if(logger!=null){
            try {
                logger.flush();
                logger.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
