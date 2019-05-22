package sparkExample;


import org.apache.spark.api.java.function.Function;
import org.apache.spark.broadcast.Broadcast;

public class AddFunction implements Function<Integer, Integer>{
    public Broadcast<BroadTest> broadTest;
    AddFunction(Broadcast<BroadTest> broadTest){
        this.broadTest = broadTest;
    }
    public Integer call(Integer v1)throws Exception{
        int v2=v1+2;
        System.out.println("V2 is "+v2);
        return v1+broadTest.getValue().getI();
    }
}