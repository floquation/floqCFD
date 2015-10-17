package newCFD;

public class CFDData {
		
	public double[] centerPointsX; 		//list of cell centers for T and P						[i]
	public double[] centerPointsY; 		//list of cell centers for T and P						[j]
	public double[] staggeredPointsUX; 	//list of cell centers for u-velocity					[i]
	public double[] staggeredPointsVY; 	//list of cell centers for v-velocity					[j]
	public double[] centerdx; 			//width of cell											[i]
	public double[] centerdy; 			//height of cell										[j]
	public double[] staggereddx; 		//x-distance between cells / width of staggered cell	[i]
	public double[] staggereddy; 		//y-distance between cells / height of staggered cell	[j]
	
	public double[][] u; //velocity: x		[i][j]
	public double[][] v; //velocity: y		[i][j]
	public double[][] T; //temperature		[i][j]
	public double[][] P; //pressure			[i][j]
	
	public double dt; //timestep
	public double maxIt; //TODO: UNUSED //Maximum number of outer iterations
	public double numInnerIt_U = 1; //Number of inner iterations
	public double numInnerIt_V = 1; //Number of inner iterations
	public double numInnerIt_P = 20; //Number of inner iterations
	public double maxNumOuterIt = 1000; //Number of outer iterations
	public double convCritOuterIt = 1E-4; //Convergence criterion for the outer iterations. Each quantity must satisfy this criterion.

	public double underRelax_P = 0.01;
	public double underRelax_U = 1.5;//1.1-underRelax_P;
	public double underRelax_V = 1.5;//1.1-underRelax_P;
	
	public double rho; //Density
	public double mu; //Viscosity
	
}
