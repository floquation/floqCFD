package newCFD;

public class CFDData {
	
	public CFDData(){
		meshCenter = new Mesh();
		meshStaggerU = new Mesh();
		meshStaggerV = new Mesh();
	}
	
	/** DO NOT MODIFY OUTSIDE THE OWNING CLASS </br> It is public for speed of access. The user is responsible for not altering it. **/
	public Mesh meshCenter;
	/** DO NOT MODIFY OUTSIDE THE OWNING CLASS </br> It is public for speed of access. The user is responsible for not altering it. **/
	public Mesh meshStaggerU;
	/** DO NOT MODIFY OUTSIDE THE OWNING CLASS </br> It is public for speed of access. The user is responsible for not altering it. **/
	public Mesh meshStaggerV;
	
	/**
	 * The Mesh class holds information about the cell:
	 * 
	 * @param cellX is the center x-coordinate of the cell [i]
	 * @param cellY is the center y-coordinate of the cell [j]
	 * @param cellWidth is the width of the cell (in the x-dir.) [i]
	 * @param cellHeight is the height of the cell (in the y-dir.) [j]
	 * @param cellHorDist is the distance between the center of the current cell and its neighbour (in the x-dir.) [i]
	 * @param cellVertDist is the distance between the center of the current cell and its neighbour (in the y-dir.) [j]
	 * 
	 * @author Kevin van As
	 */
	public class Mesh{
		double[] cellX;
		double[] cellY;
		
		double[] cellWidth;
		double[] cellHeight;
		
		double[] cellHorDist;
		double[] cellVertDist;
	}
	
	private void updateMeshReferences(){
		//meshCenter
		meshCenter.cellX = centerPointsX;
		meshCenter.cellY = centerPointsY;
		meshCenter.cellWidth = centerdx;
		meshCenter.cellHeight = centerdy;
		meshCenter.cellHorDist = staggereddx;
		meshCenter.cellVertDist = staggereddy;
		
		//meshStaggerU
		meshStaggerU.cellX = staggeredPointsUX;
		meshStaggerU.cellY = centerPointsY;
		meshStaggerU.cellWidth = staggereddx;
		meshStaggerU.cellHeight = centerdy;
		meshStaggerU.cellHorDist = centerdx;
		meshStaggerU.cellVertDist = staggereddy;
		
		//meshStaggerV
		meshStaggerV.cellX = centerPointsX;
		meshStaggerV.cellY = staggeredPointsVY;
		meshStaggerV.cellWidth = centerdx;
		meshStaggerV.cellHeight = staggereddy;
		meshStaggerV.cellHorDist = staggereddx;
		meshStaggerV.cellVertDist = centerdy;
	}
	
	public void set_centerPointsX(double[] in){
		centerPointsX = in;
		this.updateMeshReferences();
	}
	public void set_centerPointsY(double[] in){
		centerPointsY = in;
		this.updateMeshReferences();
	}
	public void set_staggeredPointsUX(double[] in){
		staggeredPointsUX = in;
		this.updateMeshReferences();
	}
	public void set_staggeredPointsVY(double[] in){
		staggeredPointsVY = in;
		this.updateMeshReferences();
	}
	public void set_centerdx(double[] in){
		centerdx = in;
		this.updateMeshReferences();
	}
	public void set_centerdy(double[] in){
		centerdy = in;
		this.updateMeshReferences();
	}
	public void set_staggereddx(double[] in){
		staggereddx = in;
		this.updateMeshReferences();
	}
	public void set_staggereddy(double[] in){
		staggereddy = in;
		this.updateMeshReferences();
	}
	
	private double[] centerPointsX; 		//list of cell centers for T and P						[i]
	private double[] centerPointsY; 		//list of cell centers for T and P						[j]
	private double[] staggeredPointsUX; 	//list of cell centers for u-velocity					[i]
	private double[] staggeredPointsVY; 	//list of cell centers for v-velocity					[j]
	private double[] centerdx; 				//width of cell											[i]
	private double[] centerdy; 				//height of cell										[j]
	private double[] staggereddx; 			//x-distance between cells / width of staggered cell	[i]
	private double[] staggereddy; 			//y-distance between cells / height of staggered cell	[j]
	
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
