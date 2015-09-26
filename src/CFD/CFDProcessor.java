package CFD;

import java.util.Arrays;

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
	
	private double[][] aP_u;
	private double[][] aP_v;
	
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
		//Coefficient required in the pressure correction equation:
		aP_u = new double[data.u.length][data.u[0].length];
		aP_v = new double[data.v.length][data.v[0].length];
		for(int i = 0; i<aP_u.length; i++){
			for(int j = 0; j<aP_u[i].length; j++){
				aP_u[i][j] = 1;
			}
		}
		for(int i = 0; i<aP_v.length; i++){
			for(int j = 0; j<aP_v[i].length; j++){
				aP_v[i][j] = 1;
			}
		}
		
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
		aP_u = null;
		aP_v = null;
	}

	@Override
	public void run() { //time advances
		boolean continueLooping = true;
		
		error_u = data.convCritOuterIt+1;
		error_v = 0;
		error_P = 0;
		int itNumber = 0;
		//Outer loop
		while(continueLooping && 
				(itNumber < data.maxNumOuterIt && Math.max(error_u,Math.max(error_v,error_P)) > data.convCritOuterIt)	){
			itNumber++;
			
		//Inner loop: U*-velocity
			//First update the coefficients: 			//TODO: outside loop
			
			//<<Algorithm>>:
			//1) Use u_old, u_mid, v_mid and P_mid to find u_new
			//2) Write u_new to u_mid
			//3) Repeat
			for(int inIt = 0; inIt<data.numInnerIt_U; inIt++){
				//Step 1:
				if(DEBUG.DEBUG && DEBUG.DEBUGlevel>=3)System.out.println("<<Inner iterations U: " + inIt + ">>");
				for(int i = 1; i<data.u.length-1; i++){
					for(int j = 1; j<data.u[i].length-1; j++){
						double aE = 0;
						aE += Math.min(0, 0.5d * data.rho * data.centerdy[j] * (u_mid[i][j] + u_mid[i+1][j])); //Convection
						//System.out.println("convective aE = " + aE);
						aE += -2*data.mu*data.centerdy[j]/data.centerdx[i+1]; //Diffusion
						double aW = 0;
						aW += Math.min(0, -0.5d * data.rho * data.centerdy[j] * (u_mid[i][j] + u_mid[i-1][j])); //Convection
						//if(DEBUG.DEBUG)System.out.println("convective aW = " + aW);
						aW += -2*data.mu*data.centerdy[j]/data.centerdx[i]; //Diffusion
						double aN = 0;
						aN += Math.min(0, 0.5d * data.rho * data.staggereddx[i] * (v_mid[i][j] + v_mid[i+1][j])); //Convection
						aN += -data.mu*data.staggereddx[i]/data.staggereddy[j]; //Diffusion
						double aS = 0;
						aS += Math.min(0, -0.5d * data.rho * data.staggereddx[i] * (v_mid[i][j-1] + v_mid[i+1][j-1])); //Convection
						aS += -data.mu*data.staggereddx[i]/data.staggereddy[j-1]; //Diffusion
						double at = data.rho*(data.centerdy[j]*data.staggereddx[i])/data.dt;
						//if(DEBUG.DEBUG)System.out.println(at);
						double Qt = at * data.u[i][j];
						double Qp = -(P_mid[i+1][j]-P_mid[i][j])*data.centerdy[j];
						double Qd = data.mu*(v_mid[i+1][j]-v_mid[i][j]) - data.mu*(v_mid[i+1][j-1]-v_mid[i][j-1]);
						
						
						double aP = -(aN + aS + aW + aE) + at;
						aP_u[i][j] = aP;
						u_new[i][j] = (Qt+Qp+Qd-(aN*u_mid[i][j+1]+aS*u_mid[i][j-1]+aE*u_mid[i+1][j]+aW*u_mid[i-1][j]))/aP;
						//u_new[i][j] = data.u[i][j];
						
						//if(DEBUG.DEBUG)System.out.println("(i,j) [aN, aS, aW, aE; aP][Qt, Qp, Qd] = (" + i + "," + j + ") [" + aN + "," + aS + "," + aW + "," + aE + "; "+ aP + "][" + Qt + ", " + Qp + ", " + Qd + "]; -> u_new = " + u_new[i][j]);
					} //End j
				} //End i

				applyBC(1); //u

				//Step 2: I need not do anything, because u_mid and u_new point to the same memory.	
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
						
		//Inner loop: V*-velocity
			//First update the coefficients: 			//TODO: outside loop
			
			//<<Algorithm>>:
			//1) Use u_old, u_mid, v_mid and P_mid to find v_new
			//2) Write v_new to v_mid
			//3) Repeat
			for(int inIt = 0; inIt<data.numInnerIt_V; inIt++){
				//Step 1:
				if(DEBUG.DEBUG && DEBUG.DEBUGlevel>=3)System.out.println("<<Inner iterations V: " + inIt + ">>");
				for(int i = 1; i<data.v.length-1; i++){
					for(int j = 1; j<data.v[i].length-1; j++){
						double aE = 0;
						aE += Math.min(0, 0.5d * data.rho * data.staggereddy[j] * (u_mid[i][j] + u_mid[i][j+1])); //Convection
						aE += -data.mu*data.staggereddy[j]/data.staggereddx[i]; //Diffusion
						double aW = 0;
						aW += Math.min(0, -0.5d * data.rho * data.staggereddy[j] * (u_mid[i-1][j] + u_mid[i-1][j+1])); //Convection
						aW += -data.mu*data.staggereddy[j]/data.staggereddx[i-1]; //Diffusion
						double aN = 0;
						aN += Math.min(0, 0.5d * data.rho * data.centerdx[i] * (v_mid[i][j] + v_mid[i][j+1])); //Convection
						aN += -2*data.mu*data.centerdx[i]/data.centerdy[j+1]; //Diffusion
						double aS = 0;
						aS += Math.min(0, -0.5d * data.rho * data.centerdx[i] * (v_mid[i][j-1] + v_mid[i][j])); //Convection
						aS += -2*data.mu*data.centerdx[i]/data.centerdy[j]; //Diffusion
						double at = data.rho*(data.centerdx[i]*data.staggereddy[j])/data.dt;
						double Qt = at * data.v[i][j];
						double Qp = -(P_mid[i][j+1]-P_mid[i][j])*data.centerdx[i];
						double Qd = data.mu*(u_mid[i][j+1]-u_mid[i][j]) - data.mu*(u_mid[i-1][j+1]-u_mid[i-1][j]);
						
						
						double aP = -(aN + aS + aW + aE) + at;
						aP_v[i][j] = aP;
						v_new[i][j] = (Qt+Qp+Qd-(aN*v_mid[i][j+1]+aS*v_mid[i][j-1]+aE*v_mid[i+1][j]+aW*v_mid[i-1][j]))/aP;
						//v_new[i][j] = data.v[i][j]; //TODO: Activate v
						
						//System.out.println("(i,j) [aN, aS, aW, aE; aP] = (" + i + "," + j + ") [" + aN + "," + aS + "," + aW + "," + aE + "; "+ aP + "];");
					} //End j
				} //End i
				
				applyBC(2); //v
				
				//Step 2: I need not do anything, because u_mid and u_new point to the same memory.	
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
		
		//Global Mass Conservation
			ensureGlobMassCons();
			
		//Inner loop: P
			//First update the coefficients: 			//TODO: outside loop
			
			//<<Algorithm>>:
			//1) Use u_old, u_mid, v_mid and P_mid to find P_new
			//2) Write P_new to P_mid
			//3) Repeat
			for(int inIt = 0; inIt<data.numInnerIt_P; inIt++){
				//Step 1:
				if(DEBUG.DEBUG && DEBUG.DEBUGlevel>=3)System.out.println("<<Inner iterations P: " + inIt + ">>");
				for(int i = 1; i<data.P.length-1; i++){
					for(int j = 1; j<data.P[i].length-1; j++){
						double aE = -data.rho * data.centerdy[j] * data.centerdy[j] / aP_u[i][j];
						double aW = -data.rho * data.centerdy[j] * data.centerdy[j] / aP_u[i-1][j];
						double aN = -data.rho * data.centerdx[i] * data.centerdx[i] / aP_v[i][j];
						double aS = -data.rho * data.centerdx[i] * data.centerdx[i] / aP_v[i][j-1];
						double massflux =
								+	data.rho * u_mid[i][j] * data.centerdy[j]
								- 	data.rho * u_mid[i-1][j] * data.centerdy[j]
								+	data.rho * v_mid[i][j] * data.centerdx[i]
								-	data.rho * v_mid[i][j-1] * data.centerdx[i];
						
						//P_mid[i][j] = -i; //TEST OK: negative dP/dx and Pcorr off will give expected result.
						
						double aP = -(aN + aS + aW + aE);
						//Correct the pressure:
						Pcorr_new[i][j] = ( -massflux-(aN*Pcorr_mid[i][j+1]+aS*Pcorr_mid[i][j-1]+aE*Pcorr_mid[i+1][j]+aW*Pcorr_mid[i-1][j]) )/aP;

						if(DEBUG.DEBUG && DEBUG.DEBUGlevel>=3)
							System.out.println("P: (i,j) [aN, aS, aW, aE; aP][Q] = (" + i + "," + j + ") [" + aN + "," + aS + "," + aW + "," + aE + "; "+ aP + "][" + massflux + "]; -> Pcorr_new = " + Pcorr_new[i][j]);
					} //End j
				} //End i

				applyBC(3);	//p'
				
				//Step 2: I need not do anything, because P_mid and P_new point to the same memory.		
//				for(int i = 0; i<P_mid.length; i++){
//					Pcorr_mid[i] = Pcorr_new[i].clone();
//				}
				//p' has no unique solution. Ensure one by keeping P fixed at one reference point (i_PRef,j_PRef):
				for(int i = 0; i<Pcorr_new.length; i++){
					for(int j = 0; j<Pcorr_new[i].length; j++){
						Pcorr_mid[i][j] = Pcorr_new[i][j] - Pcorr_new[i_PRef][j_PRef];
					}
				}	
			} //End Inner Iteration for P
						
			//Correct the velocities:
			for(int i = 1; i<data.u.length-1; i++){
				for(int j = 1; j<data.u[i].length-1; j++){
					u_mid[i][j] += -data.centerdy[j]/aP_u[i][j]*(Pcorr_mid[i+1][j]-Pcorr_mid[i][j]) * data.underRelax_U;
				}
			}
			for(int i = 1; i<data.v.length-1; i++){
				for(int j = 1; j<data.v[i].length-1; j++){
					v_mid[i][j] += -data.centerdx[i]/aP_v[i][j]*(Pcorr_mid[i][j+1]-Pcorr_mid[i][j]) * data.underRelax_V;
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
			//applyBC();

			
			try {
				Thread.sleep(0);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(DEBUG.DEBUG && DEBUG.DEBUGlevel>=1)
				System.out.println("error_inf {u,v,P} = (" + itNumber + "){\t" + error_u + ",\t" + error_v + ",\t" + error_P + "\t}");
		} //Time-step has finished!
		
		//Exiting thread...
		
		//applyBC();
		//ensureGlobMassCons();
		
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
		/*try { //Kill the computational thread: it is done.
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		throw new RuntimeException("(CFDProcessor): This message cannot be shown. If it is shown, the thread is not killed??");*/
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







