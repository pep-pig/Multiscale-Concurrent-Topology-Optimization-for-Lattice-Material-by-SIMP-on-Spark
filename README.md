# Multiscale-Concurrent-Optimization
Multiscale concurrent optimization on spark 
## Serial version
　　1.This version contain three benchmark,boundary condition 1 and 2 are cantilever beams and boundary condition 3 is MMB.</br>
　　2.Optimization method in macro and micro are both SIMP method.</br>
　　3.The effective material properties is computed by homogenization method.</br>
　　4.All parameters you can change can be configured in file parameter.xml.</br>
## Parallel version
　　1. parallel version use java multithread to accelerate the computing. </br>
　　2. you can according the number of cpu in your computer to set the number of computing thread in parameter.xml.</br>
## GPU version
　　1. we mainly use a JAVA GPU COMPUTING LIB: [Aparapi](https://github.com/Syncleus/aparapi) to reallize GPU computting ,but we did't success for we can not establish the enviroment correctly.</br>
　　2. So if you have some experience about Aparapi or Opencl or CUDA ,maybe you can try to accellerate the computing by GPU</br>
## Spark version
　　1.Spark is a distributed parallel computing frame. If you have a spark cluster, you can try to use this version to accellerate your computing if your model is large.
 
## NOTE:you should use Solve.solve(K,F),instead of Solve.solvePositive(K,F),because Solve.solveSymmetry(K,F) is much faster.
