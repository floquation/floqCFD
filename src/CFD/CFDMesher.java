package CFD;

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
		data.centerPointsX = new double[Nx];
		data.centerPointsY = new double[Ny];
		data.staggeredPointsUX = new double[Nx-1];
		data.staggeredPointsVY = new double[Ny-1];
		data.centerdx = new double[Nx];
		data.centerdy = new double[Ny];
		data.staggereddx = new double[Nx-1];
		data.staggereddy = new double[Ny-1];
		
		//Make an uniform mesh with rectangular elements
		//We iterate over each cell, meaning there is 1 corner more than N for each dimension.
		double lastX;
		double lastY;
		
		lastY = (-1d)/(Ny-2);
		for(int j = 0; j<Ny; j++){
			data.centerdy[j] = 1d/(Ny-2);
			data.centerPointsY[j] = lastY + 0.5d * data.centerdy[j];
			
			//Staggered grid:
			if(j<Ny-1){
				data.staggereddy[j] = 1d/(Ny-2);
				data.staggeredPointsVY[j] = lastY + data.centerdy[j];
			}
			
			//Update y of current row
			lastY += data.centerdy[j];
		}

		lastX = (-1d)/(Nx-2);
		for(int i = 0; i<Nx; i++){
			data.centerdx[i] = 1d/(Nx-2);
			data.centerPointsX[i] = lastX + 0.5d * data.centerdx[i];
			
			//Staggered grid:
			if(i<Nx-1){
				data.staggereddx[i] = 1d/(Nx-2);
				data.staggeredPointsUX[i] = lastX + data.centerdx[i];
			}
			
			//Update x of current column
			lastX += data.centerdx[i];
		}
		
		System.out.println("(CFDMesher) Mesh generated.");
		
		return true; //Mesh generation succeeded
	}

}
