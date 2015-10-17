package newCFD;

import java.util.Arrays;

import CFD.DEBUG;

public class CFDProcessor implements Runnable {
	
	private CFDData data;
	private CFDStatus status;
	
	
	private double[][] u_new;
	private double[][] u_mid;
	private double[][] v_new;
	private double[][] v_mid;
	private double[][] P_mid;
	private double[][] Pcorr_mid;
	private double[][] Pcorr_new;
		
	private double error_u;
	private double error_v;
	private double error_P;
	
	private int i_PRef = 1;
	private int j_PRef = 1;
	
	
	public CFDProcessor(CFDData dataIn, CFDStatus statusIn) {
		data = dataIn;
		status = statusIn;
		initialize();
	}
	
	public void initialize(){
		
		//Parameters:		
		u_new = new double[data.u.length][data.u[0].length];
		u_mid = new double[data.u.length][data.u[0].length];
		v_new = new double[data.v.length][data.v[0].length];
		v_mid = new double[data.v.length][data.v[0].length];
		P_mid = new double[data.P.length][data.P[0].length];
		Pcorr_new = new double[data.P.length][data.P[0].length];
		Pcorr_mid = new double[data.P.length][data.P[0].length];
		
		//Clone data / Initial guess:
		for(int i = 0; i<data.u.length; i++){
			u_new[i] = data.u[i].clone();
			for(int j = 1; j<data.u[i].length-1; j++){
				u_new[i][j] = 1;
			}
		}
		for(int i = 0; i<data.v.length; i++){
			v_new[i] = data.v[i].clone();
		}
		for(int i = 0; i<data.P.length; i++){
			P_mid[i] = data.P[i].clone();
		}
		//Initial guess for pressure correction = 0 for arbitrary situation... TODO: Better guess?
		
		applyBC(1);
		applyBC(2);
		
		//Clone data to mid-value-arrays:
		for(int i = 0; i<data.u.length; i++){
			u_mid[i] = u_new[i].clone();
		}
		for(int i = 0; i<data.v.length; i++){
			v_mid[i] = v_new[i].clone();
		}
		//Pressure is already cloned, because it is all-zero.
		
		
	}
	
	public void dispose(){
		u_new = null;
		u_mid = null;
		v_new = null;
		v_mid = null;
		Pcorr_new = null;
		Pcorr_mid = null;
	}

	@Override
	public void run() { //time advances	
		boolean continueLooping = true;
		error_u = data.convCritOuterIt+1;
		error_v = 0;
		error_P = 0;
		double[][][] Au = this.initA(data.u);
		double[][][] Av = this.initA(data.v);
		double[][][] Ap = this.initA(data.P);
		double[][] Qu=initQ(data.u);
		double[][] Qv=initQ(data.v);
		double[][] Qp=initQ(data.P);
		int itNumber = 0;
		//Outer loop: BuildA() and BuildQ()
		while(continueLooping && 
				(itNumber < data.maxNumOuterIt && Math.max(error_u,Math.max(error_v,error_P)) > data.convCritOuterIt)	){
			itNumber++;
			
			//Solve for u*
			for(int inIt = 0; inIt<data.numInnerIt_U; inIt++){
				buildAu(Au);
				buildQu(Qu);
				solveEquation(Au,Qu,u_mid,u_new);
				applyBC(1); //u
				
				error_u = 0;
				for(int i = 1; i<u_mid.length-1; i++){
					for(int j = 1; j<u_mid[i].length-1; j++){
						error_u = Math.max(error_u, Math.abs((u_new[i][j]-u_mid[i][j])));
					}
				}
				for(int i = 0; i<u_mid.length; i++){
					u_mid[i] = u_new[i].clone();
				}
			} //End Inner Iteration for U*
			
			//Solve for v*
			for(int inIt = 0; inIt<data.numInnerIt_V; inIt++){
				buildAv(Av);
				buildQv(Qv);
				solveEquation(Av,Qv,v_mid,v_new);
				applyBC(2); //v
				
				error_v = 0;
				for(int i = 1; i<v_mid.length-1; i++){
					for(int j = 1; j<v_mid[i].length-1; j++){
						error_v = Math.max(error_v, Math.abs((v_new[i][j]-v_mid[i][j])));
					}
				}
				for(int i = 0; i<v_mid.length; i++){
					v_mid[i] = v_new[i].clone();
				}
			} //End Inner Iteration for V*
			
			ensureGlobMassCons();
			
			//Solve for P'
			for(int inIt = 0; inIt<data.numInnerIt_P; inIt++){
				buildAp(Ap,Au,Av);
				buildQp(Qp);
				solveEquation(Ap,Qp,Pcorr_mid,Pcorr_new);
				applyBC(3); //P'
				
//				System.out.println("probe  for P':" + Pcorr_new[5][5]);
//
//				System.out.println(Arrays.toString(u_new[5]));
//				System.out.println(Arrays.toString(Pcorr_new[1]));
//				System.out.println(Arrays.toString(Pcorr_new[5]));
				
				//p' has no unique solution. Ensure one by keeping P fixed at one reference point (i_PRef,j_PRef):
				for(int i = 0; i<Pcorr_new.length; i++){
					for(int j = 0; j<Pcorr_new[i].length; j++){
//						System.out.println("P: (i,j) [aP; aN, aS, aW, aE][Q] = (" + i + "," + j + ") [" + A[i][j][0] + "; " + A[i][j][1] + "," + A[i][j][2] + "," + A[i][j][3] + ","+ A[i][j][4] + "][" + Q[i][j] + "]; -> Pcorr_new = " + Pcorr_new[i][j]);
						Pcorr_mid[i][j] = Pcorr_new[i][j] - Pcorr_new[i_PRef][j_PRef];
					}
				}
				
//				System.out.println("probe  for P2':" + Pcorr_mid[5][5]);
			} //End Inner Iteration for P
			
			//Correct the velocities:
			for(int i = 1; i<data.u.length-1; i++){
				for(int j = 1; j<data.u[i].length-1; j++){
					u_mid[i][j] += -data.centerdy[j]/Au[i][j][0]*(Pcorr_mid[i+1][j]-Pcorr_mid[i][j]) * data.underRelax_U;
				}
			}
			for(int i = 1; i<data.v.length-1; i++){
				for(int j = 1; j<data.v[i].length-1; j++){
					v_mid[i][j] += -data.centerdx[i]/Av[i][j][0]*(Pcorr_mid[i][j+1]-Pcorr_mid[i][j]) * data.underRelax_V;
				}
			}
			
			//Correct the pressure:
			for(int i = 0; i<P_mid.length; i++){
				for(int j = 0; j<P_mid[i].length; j++){
					P_mid[i][j] += Pcorr_mid[i][j] * data.underRelax_P;
				}
			}	
			error_P = 0;
			for(int i = 1; i<Pcorr_mid.length-1; i++){
				for(int j = 1; j<Pcorr_mid[i].length-1; j++){
					error_P = Math.max(error_P,Math.abs(Pcorr_mid[i][j]));
					
				}
			}
			
			if(DEBUG.DEBUG && DEBUG.DEBUGlevel>=1)
				System.out.println("error_inf {u,v,P} = (" + itNumber + "){\t" + error_u + ",\t" + error_v + ",\t" + error_P + "\t}");
		} //Time-step has finished!
		
		//Exiting thread...
				
		//Clone data to database:
		for(int i = 0; i<data.u.length; i++){
			data.u[i] = u_mid[i].clone();
		}
		for(int i = 0; i<data.v.length; i++){
			data.v[i] = v_mid[i].clone();
		}
		for(int i = 0; i<data.P.length; i++){
			data.P[i] = P_mid[i].clone();
		}
		
		if(DEBUG.DEBUG && DEBUG.DEBUGlevel<1)
			System.out.println("(CFDProcessor) Processor has finished in " + itNumber + " iterations.");
		status.currentStatus = CFDStatus.status.idle;
		
		
		System.out.println(Arrays.toString(data.u[data.u.length-1]));
		
		Thread.currentThread().interrupt();
	
	}
	
	/**
	 * Solves the equation "Ax=Q" in the form: "a_p*x_p^new = - sum_nb a_nb*x_nb^old + Q_p"
	 * 
	 * @param A is a matrix containing the coefficients of the equation [point P.i][point P.j][5 neighbours: P, north, south, west, east]
	 * @param Q is a vector describing source terms (independent of neighbours) [point P.i][point P.j]
	 * @param x0 is the old vector of the quantity we are solving [point P.i][point P.j]
	 * @param x1 is the new vector of the quantity we are solving [point P.i][point P.j]
	 */
	private void solveEquation(double[][][] A, double[][] Q, double[][] x0, double[][] x1){
		//for each cell P, solve "a_p*x_p^new = - sum_nb a_nb*x_nb^old + Q_p"
		for(int i = 1; i<x0.length-1; i++){
			for(int j = 1; j<x0[i].length-1; j++){
				x1[i][j] /*x(P)*/ =
						(
							Q[i][j] //source terms
						-
							x0[i][j+1]*A[i][j][1] //north
						-
							x0[i][j-1]*A[i][j][2] //south
						-
							x0[i-1][j]*A[i][j][3] //west
						-
							x0[i+1][j]*A[i][j][4] //east
						)/A[i][j][0]; //center (a_p)
			}
		}
	}
	
	/**
	 * Initiates the coefficient matrix for the given phi.
	 * 
	 * @return A (double[][][]): the coefficient matrix, with boundary values initiated.
	 * 			This may be reused in the "buildA"-method.
	 */
	private double[][][] initA(double[][] phi){
		double[][][] A = new double[phi.length][phi[0].length][5];

		// Init boundary cells. These values will drop out of the equations, but are still used.
		// That gives problems in case the value is 0, since x/0-x/0 = NaN, rather than 0.
		for(int i = 1; i<phi.length-1; i++){
			A[i][0][0] = 1;
			A[i][phi[0].length-1][0] = 1;
		}
		for(int j = 1; j<phi[0].length-1; j++){
			A[0][j][0] = 1;
			A[phi.length-1][j][0] = 1;
		}
		
		return A;
	}
	
	/**
	 * Initiates the source vector for the given phi.
	 * 
	 * @return Q (double[][]): the source vector, with boundary values initiated.
	 * 			This may be reused in the "buildQ"-method.
	 */
	private double[][] initQ(double[][] phi){
		double[][] Q = new double[phi.length][phi[0].length];
		return Q;
	}
	
	/**
	 * Updates the coefficient matrix for the horizontal velocity, u.
	 * 
	 * @param A (intent: out) (double[][][]) is a matrix containing the coefficients of the equation for u </br>
	 * 			--> A[point P.i][point P.j][5 neighbours: P, north, south, west, east] </br>
	 * 			It should be created using the initA() method.
	 * 
	 */
	private void buildAu(double[][][] A){
		
		for(int i = 1; i<data.u.length-1; i++){
			for(int j = 1; j<data.u[i].length-1; j++){
				
				// EAST:
				A[i][j][4] = Math.min(0, 0.5d * data.rho * data.centerdy[j] * (u_mid[i][j] + u_mid[i+1][j])); //Convection
				A[i][j][4] += -2*data.mu*data.centerdy[j]/data.centerdx[i+1]; //Diffusion
				
				// WEST:
				A[i][j][3] = Math.min(0, -0.5d * data.rho * data.centerdy[j] * (u_mid[i][j] + u_mid[i-1][j])); //Convection
				A[i][j][3] += -2*data.mu*data.centerdy[j]/data.centerdx[i]; //Diffusion
				
				// NORTH:
				A[i][j][1] = Math.min(0, 0.5d * data.rho * data.staggereddx[i] * (v_mid[i][j] + v_mid[i+1][j])); //Convection
				A[i][j][1] += -data.mu*data.staggereddx[i]/data.staggereddy[j]; //Diffusion
				
				// SOUTH:
				A[i][j][2] = Math.min(0, -0.5d * data.rho * data.staggereddx[i] * (v_mid[i][j-1] + v_mid[i+1][j-1])); //Convection
				A[i][j][2] += -data.mu*data.staggereddx[i]/data.staggereddy[j-1]; //Diffusion
				
				// CENTER:
				double at = data.rho*(data.centerdy[j]*data.staggereddx[i])/data.dt;				
				A[i][j][0] = -(A[i][j][1] + A[i][j][2] + A[i][j][3] + A[i][j][4]) + at;
			}
		}
		
	}
	
	/**
	 * Creates the source vector for the horizontal velocity, u.
	 * 
	 * @param Q (intent:out) double[][] is a vector containing the source terms for u.
	 * 			--> Q[point P.i][point P.j]
	 */
	private void buildQu(double[][] Q){		
		for(int i = 1; i<data.u.length-1; i++){
			for(int j = 1; j<data.u[i].length-1; j++){

				double at = data.rho*(data.centerdy[j]*data.staggereddx[i])/data.dt;	
				double Qt = at * data.u[i][j];
				double Qp = -(P_mid[i+1][j]-P_mid[i][j])*data.centerdy[j];
				double Qd = data.mu*(v_mid[i+1][j]-v_mid[i][j]) - data.mu*(v_mid[i+1][j-1]-v_mid[i][j-1]);
				
				Q[i][j] = Qt + Qp + Qd;
			}
		}
	}
	
	/**
	 * Updates the coefficient matrix for the horizontal velocity, v.
	 * 
	 * @param A (intent: out) (double[][][]) is a matrix containing the coefficients of the equation for v </br>
	 * 			--> A[point P.i][point P.j][5 neighbours: P, north, south, west, east] </br>
	 * 			It should be created using the initA() method.
	 * 
	 */
	private void buildAv(double[][][] A){

		for(int i = 1; i<data.v.length-1; i++){
			for(int j = 1; j<data.v[i].length-1; j++){
				
				// EAST:
				A[i][j][4] = Math.min(0, 0.5d * data.rho * data.staggereddy[j] * (u_mid[i][j] + u_mid[i][j+1])); //Convection
				A[i][j][4] += -data.mu*data.staggereddy[j]/data.staggereddx[i]; //Diffusion
				
				// WEST:
				A[i][j][3] = Math.min(0, -0.5d * data.rho * data.staggereddy[j] * (u_mid[i-1][j] + u_mid[i-1][j+1])); //Convection
				A[i][j][3] += -data.mu*data.staggereddy[j]/data.staggereddx[i-1]; //Diffusion
				
				// NORTH:
				A[i][j][1] = Math.min(0, 0.5d * data.rho * data.centerdx[i] * (v_mid[i][j] + v_mid[i][j+1])); //Convection
				A[i][j][1] += -2*data.mu*data.centerdx[i]/data.centerdy[j+1]; //Diffusion
				
				// SOUTH:
				A[i][j][2] = Math.min(0, -0.5d * data.rho * data.centerdx[i] * (v_mid[i][j-1] + v_mid[i][j])); //Convection
				A[i][j][2] += -2*data.mu*data.centerdx[i]/data.centerdy[j]; //Diffusion
				
				// CENTER:
				double at = data.rho*(data.centerdx[i]*data.staggereddy[j])/data.dt;				
				A[i][j][0] = -(A[i][j][1] + A[i][j][2] + A[i][j][3] + A[i][j][4]) + at;
			}
		}
		
	}

	/**
	 * Creates the source vector for the vertical velocity, v.
	 * 
	 * @param Q (intent:out) double[][] is a vector containing the source terms for v.
	 * 			--> Q[point P.i][point P.j]
	 */
	private void buildQv(double[][] Q){
		for(int i = 1; i<data.v.length-1; i++){
			for(int j = 1; j<data.v[i].length-1; j++){

				double at = data.rho*(data.centerdx[i]*data.staggereddy[j])/data.dt;		
				double Qt = at * data.v[i][j];
				double Qp = -(P_mid[i][j+1]-P_mid[i][j])*data.centerdx[i];
				double Qd = data.mu*(u_mid[i][j+1]-u_mid[i][j]) - data.mu*(u_mid[i-1][j+1]-u_mid[i-1][j]);
				
				Q[i][j] = Qt + Qp + Qd;
			}
		}
	}
	
	/**
	 * Updates the coefficient matrix for the pressure correction, p'.
	 * 
	 * @param Ap (intent: out) (double[][][]) is a matrix which will contain the coefficients of the equation for p' </br>
	 * 			--> A[point P.i][point P.j][5 neighbours: P, north, south, west, east] </br>
	 * 			It should be created using the initA() method.
	 * @param Au is the coefficient matrix for the horizontal velocity, u
	 * @param Av is the coefficient matrix for the vertical velocity, v
	 * 
	 * @return double[][][] is a matrix containing the coefficients of the equation for p'
	 * 			--> A[point P.i][point P.j][5 neighbours: P, north, south, west, east]
	 */
	private void buildAp(double[][][] Ap, double[][][] Au, double[][][] Av){
				
		for(int i = 1; i<data.P.length-1; i++){
			for(int j = 1; j<data.P[i].length-1; j++){
				
				// EAST:
				Ap[i][j][4] = -data.rho * data.centerdy[j] * data.centerdy[j] / Au[i][j][0];
				
				// WEST:
				Ap[i][j][3] = -data.rho * data.centerdy[j] * data.centerdy[j] / Au[i-1][j][0];
				
				// NORTH:
				Ap[i][j][1] = -data.rho * data.centerdx[i] * data.centerdx[i] / Av[i][j][0];
				
				// SOUTH:
				Ap[i][j][2] = -data.rho * data.centerdx[i] * data.centerdx[i] / Av[i][j-1][0];
				
				// CENTER:
				Ap[i][j][0] = -(Ap[i][j][1] + Ap[i][j][2] + Ap[i][j][3] + Ap[i][j][4]);

			}
		}
		
	}
	
	/**
	 * Creates the source vector for the pressure correction, P'.
	 * 
	 * @param Q (intent:out) double[][] is a vector containing the source terms for P'.
	 * 			--> Q[point P.i][point P.j]
	 */
	private void buildQp(double[][] Q){
		for(int i = 1; i<data.P.length-1; i++){
			for(int j = 1; j<data.P[i].length-1; j++){
				double massflux =
						+	data.rho * u_mid[i][j] * data.centerdy[j]
						- 	data.rho * u_mid[i-1][j] * data.centerdy[j]
						+	data.rho * v_mid[i][j] * data.centerdx[i]
						-	data.rho * v_mid[i][j-1] * data.centerdx[i];
				
				Q[i][j] = -massflux;
			}
		}
	}
	
	private void applyBC(int whatBC){
		//TODO: Set some BC properly
		if(whatBC == 1){ //u
			for(int j = 0; j<u_new[0].length; j++){
				u_new[0][j] = 1;	//WEST: Inflow
				u_new[u_new.length-1][j] = u_new[u_new.length-2][j];  //EAST: Outflow: Zero gradient
			}
			for(int i=0; i<u_new.length;i++){
				u_new[i][0] = -u_new[i][1]; // SOUTH
				u_new[i][u_new[i].length-1] = -u_new[i][u_new[i].length-2]; // NORTH
			}
		}else if(whatBC == 2){ //v
			for(int j = 0; j<v_new[0].length; j++){
				v_new[v_new.length-1][j] = v_new[v_new.length-2][j];  //EAST: Outflow: Zero gradient
			}
			for(int i = 0; i<v_new.length*0.2; i++){
				//v_new[i][v_new[i].length-1] = -3; //NORTH
			}			
		}else if(whatBC == 3){ //p'
			for(int i = 0; i<Pcorr_new.length; i++){
				Pcorr_new[i][0] = Pcorr_new[i][1];  //SOUTH: Zero gradient: WALL
				Pcorr_new[i][Pcorr_new[i].length-1] = Pcorr_new[i][Pcorr_new[i].length-2];  //NORTH: Zero gradient: WALL
			}
//			double d_w;
//			double d_e;
			for(int j = 0; j<Pcorr_new[0].length; j++){
				Pcorr_new[0][j] = Pcorr_new[1][j]; //WEST: Zero gradient: INLET
				Pcorr_new[Pcorr_new.length-1][j] = Pcorr_new[Pcorr_new.length-2][j]; //EAST: Zero gradient: OUTLET
				
//				d_w = data.centerdy[j] / aP_u[Pcorr_new.length-3][j];
//				d_e = data.centerdy[j] / aP_u[Pcorr_new.length-2][j];
//				Pcorr_new[Pcorr_new.length-1][j] = Pcorr_new[Pcorr_new.length-2][j] + d_w/d_e * (Pcorr_new[Pcorr_new.length-2][j] - Pcorr_new[Pcorr_new.length-3][j]); //EAST: Extrapolation: OUTLET
			}
//			for(int j = 0; j<P_mid[0].length; j++){
//				P_mid[P_mid.length-1][j] = P_mid[P_mid.length-2][j]; //EAST: zero gradient: OUTLET
//			}			
		}else if(whatBC == 4){
			
		}
		
	}
	
	private void ensureGlobMassCons(){
		//TODO: Define inlet/outlet properly.
		
		//Find the massflux in/out
		double massflux_in = 0;
		double massflux_out = 0;
		for(int j = 1; j<u_mid[0].length-1; j++){ //note: ghost cells (walls) not taken into account
			//Inlet: WEST
			massflux_in += data.rho*u_mid[0][j]*data.centerdy[j];
			
			//Outlet: EAST
			massflux_out += data.rho*u_mid[u_mid.length-1][j]*data.centerdy[j];
		}
		
		//Correct mass conservation by changing the velocity at the outlet
		double correction = massflux_in / massflux_out;

		if(DEBUG.DEBUG && DEBUG.DEBUGlevel>=2)
			System.out.println("global mass cons. correction = " + correction);
		
		for(int j = 1; j<u_mid[0].length-1; j++){			
			//Outlet: EAST
			u_mid[u_mid.length-1][j] *= correction;
		}
	}

}







