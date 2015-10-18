package newCFD;

public class CFDMesher {

	/**
	 * Uses CFDObjects and CFDDomain.
	 * Generates a mesh and writes it to CFDData: x, y, nb
	 * 
	 * Nx and Ny are the number of CELLS
	 * 
	 * Domain is currently assumed to be between 0 and 1 in both directions.
	 */
	public static boolean generateMesh(CFDData data, int Nx, int Ny){
		//Add ghost points:
		Nx+=2;
		Ny+=2;
		
		//Initialize matrix sizes:
		data.u = new double[Nx-1][Ny];
		data.v = new double[Nx][Ny-1];
		data.P = new double[Nx][Ny];
		data.T = new double[Nx][Ny];
		double[] centerPointsX = new double[Nx];
		double[] centerPointsY = new double[Ny];
		double[] staggeredPointsUX = new double[Nx-1];
		double[] staggeredPointsVY = new double[Ny-1];
		double[] centerdx = new double[Nx];
		double[] centerdy = new double[Ny];
		double[] staggereddx = new double[Nx-1];
		double[] staggereddy = new double[Ny-1];
		
		//Make an uniform mesh with rectangular elements
		//We iterate over each cell, meaning there is 1 corner more than N for each dimension.
		double lastX;
		double lastY;
		
		lastY = (-1d)/(Ny-2);
		for(int j = 0; j<Ny; j++){
			centerdy[j] = 1d/(Ny-2);
			centerPointsY[j] = lastY + 0.5d * centerdy[j];
			
			//Staggered grid:
			if(j<Ny-1){
				staggereddy[j] = 1d/(Ny-2);
				staggeredPointsVY[j] = lastY + centerdy[j];
			}
			
			//Update y of current row
			lastY += centerdy[j];
		}

		lastX = (-1d)/(Nx-2);
		for(int i = 0; i<Nx; i++){
			centerdx[i] = 1d/(Nx-2);
			centerPointsX[i] = lastX + 0.5d * centerdx[i];
			
			//Staggered grid:
			if(i<Nx-1){
				staggereddx[i] = 1d/(Nx-2);
				staggeredPointsUX[i] = lastX + centerdx[i];
			}
			
			//Update x of current column
			lastX += centerdx[i];
		}
		
		data.set_centerPointsX(centerPointsX);
		data.set_centerPointsY(centerPointsY);
		data.set_staggeredPointsUX(staggeredPointsUX);
		data.set_staggeredPointsVY(staggeredPointsVY);
		data.set_centerdx(centerdx);
		data.set_centerdy(centerdy);
		data.set_staggereddx(staggereddx);
		data.set_staggereddy(staggereddy);
		
		System.out.println("(CFDMesher) Mesh generated.");
		
		return true; //Mesh generation succeeded
	}

}
