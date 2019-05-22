package sparkExample;

import java.io.Serializable;

public class BroadTest implements Serializable {
    BroadTest(){
        System.out.println("初始化");
    }
    private int i = 1;
    public int getI(){
        return i;
    }
}
