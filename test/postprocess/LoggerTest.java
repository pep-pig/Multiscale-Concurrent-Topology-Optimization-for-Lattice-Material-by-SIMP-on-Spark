package postprocess;

import org.junit.Test;

import static org.junit.Assert.*;

public class LoggerTest {

    @Test
    public void write() {
        Logger logger = new Logger("C:/multiscaleOptimization");
        logger.write("i");
        logger.write("love");
        logger.write("you");
    }
}