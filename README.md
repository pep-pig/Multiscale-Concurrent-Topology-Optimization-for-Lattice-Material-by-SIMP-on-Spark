# Multiscale-Concurrent-Topology-Optimization-for-Lattice-Material
## Mutiscale-Optimization
* Multiscale optimization of lattice materials is a hot topic in the field of structural topology optimization,the picture below shows the difference of topology optimization between continuum structure and lattice material
<p align="center">
<img src="Imgs/Img1.png" width="460"/>
</p>

## Distributed Parallel Optimization Method
* Our distributed parallel optimization is based on spark,the DAG is as follows:
<p align="center">
<img src="Imgs/Img6.png" width="460"/>
</p>
<p align="center">
<img src="Imgs/Img7.png" width="460"/>
</p>

## Numerical Example
* Boundary conditions of cantilevel beam and the topology result
<p align="center">
<img src="Imgs/Img2.png" width="460"/>
</p>
<p align="center">
<img src="Imgs/Img3.png" width="460"/>
</p>
* Boundary conditions of MMB and the topology result
<p align="center">
<img src="Imgs/Img4.png" width="460"/>
</p>
<p align="center">
<img src="Imgs/Img5.png" width="460"/>
</p>

# How to use?
*step1: you should have a spark enviroment*<br>
*step2: config the input in the parameter.xml which include mesh mode,boundary condition,cpu numbers to use,path for data writing and more.*<br>
*step3: Packaging the java code as jar file ,and submit to spark . There are two kinds of running mode of spark program.*<br>
* `--local mode`:in this mode,you can run the program on windows or linux 
* `--cluster mode`:in this mode ,you can run the program only on linux 
