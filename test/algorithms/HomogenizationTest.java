package algorithms;

import org.jblas.DoubleMatrix;
import org.junit.Assert;

import java.lang.reflect.Method;
import java.util.Map;

public class HomogenizationTest {
    @org.junit.Test
    public void testHomogenizeWithTowMaterials() {
    double[][] xx = new double[80][80];
    double[] lambdaArray = new double[]{0.1,0.2,0.1,0.2,0.1,0.2};
    double[] muArray = new double[]{1,2,1,2,1,2};
    DoubleMatrix lambda = new DoubleMatrix(40,40);
    DoubleMatrix mu = new DoubleMatrix(40,40);
    for (int j=0;j<40;j++){
        for(int k=0;k<40;k++){
            //int i = (int)(Math.random()*lambda.length);
            //lambda.put(j,k,lambdaArray[i]);
            //mu.put(j,k,muArray[i]);
            lambda.put(j,k,0.578);
            mu.put(j,k,0.3846);
        }
    }
    DoubleMatrix CH = Homogenization.homogenize(1,1,lambda,mu);
    CH.print();
    }
    Method computeKeAndFe = null;
    @org.junit.Before
    public void setUp() throws Exception {
        //设置私有方法可见
        computeKeAndFe = Homogenization.class.getDeclaredMethod("computeKeAndFe",double.class,double.class);
        computeKeAndFe.setAccessible(true);
    }

    @org.junit.Test
    public void testComputeKeAndFe() throws Exception {
        double a = (double)(1.0/6.0);
        double b = (double)(1.0/6.0);
        Homogenization homogenization = new Homogenization();
        Map keAndFeMatrix = (Map) computeKeAndFe.invoke(homogenization,a,b);
        DoubleMatrix ke = ((DoubleMatrix)keAndFeMatrix.get("keLambda")).mmul(0.33).add(((DoubleMatrix)keAndFeMatrix.get("keMu")).mmul(0.3846));
        System.out.println("keLambda:");
        System.out.println(keAndFeMatrix.get("keLambda"));
        System.out.println("keMu:");
        ((DoubleMatrix)keAndFeMatrix.get("keMu")).print();
        System.out.println("feLambda:");
        ((DoubleMatrix)keAndFeMatrix.get("feMu")).print();
        System.out.println("feMu:");
        ((DoubleMatrix)keAndFeMatrix.get("feLambda")).print();
    }

    @org.junit.Test
    public void testHomogenizeWithArbitraryMaterials(){
    }
}