package algorithms;

import org.apache.spark.Partitioner;
public class MicroOutputRDDPartitioner extends Partitioner {
    private int  parallism;
    private int[] flagNumber;
    MicroOutputRDDPartitioner(int cores, int elementNumber){
        this.parallism = cores*3;
        int interval = elementNumber/this.parallism;
        flagNumber = new int[this.parallism];
        for (int i = (flagNumber.length-1),j=0;i>=0;j++,i--){
            flagNumber[i] = elementNumber-interval*j;
        }
    }
    public int numPartitions() {
        return parallism;
    }

    public int getPartition(Object key) {
        int partitionID=0;
        for (int flag:flagNumber){
            if((Integer)key>flag){
                partitionID++;
            }
            else{
                break;
            }
        }
        return partitionID;
    }
}
