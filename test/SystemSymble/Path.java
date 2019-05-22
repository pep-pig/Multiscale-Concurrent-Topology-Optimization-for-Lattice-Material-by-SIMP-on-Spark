package SystemSymble;

import org.junit.Test;

public class Path {
    @Test
    public void pathSeparatorTest(){
        System.out.println(System.getProperty("path.separator"));
        System.out.println(System.getProperty("file.separator"));
    }
}
