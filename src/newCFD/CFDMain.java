package newCFD;

/**
 * 
 * Starting point of the CFD code.
 * First use this class to set the required variables, then call its run() method.
 * The run() method then returns the IO-interface (CFDProber) of the CFD code, or null if an error occurred.
 * 
 * @author Kevin van As
 */
public class CFDMain{
	
	private CFDProber prober;
	private CFDData data;
	private CFDStatus status;
	private CFDProcessor proc;
	
	public CFDMain(){
		data = new CFDData();
		prober = new CFDProber(data);
		status = new CFDStatus();
	}

	public CFDProber run() {
		if(status.hasAMesh && status.isInitialized){
			proc = new CFDProcessor(data, status);
			return prober;
		}else{
			System.out.println("(CFDMain) CFD was not correctly initialized. The errors are:");
			if (data.rho == 0)
				System.out.println("(CFDMain) data.rho = " + data.rho);
			if (data.dt == 0)
				System.out.println("(CFDMain) data.dt = " + data.dt);
			if (data.meshCenter.cellX == null)
				System.out.println("(CFDMain) data.meshCenter.cellX == null!");
			if (data.meshCenter.cellY == null)
				System.out.println("(CFDMain) data.meshCenter.cellY == null!");
			if (data.meshCenter.cellWidth == null)
				System.out.println("(CFDMain) data.meshCenter.cellWidth == null!");
			if (data.meshCenter.cellHeight == null)
				System.out.println("(CFDMain) data.meshCenter.cellHeight == null!");
			if (data.meshStaggerU.cellX == null)
				System.out.println("(CFDMain) data.meshStaggerU.cellX == null!");
			if (data.meshStaggerV.cellY == null)
				System.out.println("(CFDMain) data.meshStaggerV.cellY == null!");
			if (data.meshStaggerU.cellWidth == null)
				System.out.println("(CFDMain) data.meshStaggerU.cellWidth == null!");
			if (data.meshStaggerV.cellHeight == null)
				System.out.println("(CFDMain) data.meshStaggerV.cellHeight == null!");
			if (data.P == null)
				System.out.println("(CFDMain) data.P == null!");
			if (data.T == null)
				System.out.println("(CFDMain) data.T == null!");
			if (data.u == null)
				System.out.println("(CFDMain) data.u == null!");
			if (data.v == null)
				System.out.println("(CFDMain) data.v == null!");
			return null;
		}
	}

	public boolean doTimeStep() {
		if(status.currentStatus == CFDStatus.status.iterating){
			return false;
		}
		status.currentStatus = CFDStatus.status.iterating;
		new Thread(proc).start();
		//System.out.println("number of active threads = " + Thread.activeCount());
		return true;
	}
	public boolean doTimeStep(double dt) {
		data.dt = dt;	
		return doTimeStep();
	}
	
	
	public void setDensity(double rhoIn){
		data.rho = rhoIn;
		checkWhetherInitialized();
	}
	public void setViscosity(double muIn){
		data.mu = muIn;
		checkWhetherInitialized();
	}
	//...
	
	/**
	 * Try to generate a mesh using a maximum of (Nx,Ny) interior cells.
	 * @param Nx
	 * @param Ny
	 */
	public boolean generateMesh(int Nx, int Ny){
		if(CFDMesher.generateMesh(data, Nx, Ny)){
			status.hasAMesh = true;
			checkWhetherInitialized();
			return true;
		}
		status.hasAMesh = false;
		return false; //Failed
	}
	
	private void checkWhetherInitialized(){
		if (
				data.rho != 0 && data.dt != 0 && data.maxIt != 0
				&& data.meshCenter.cellX != null && data.meshCenter.cellY != null
				&& data.meshCenter.cellWidth != null && data.meshCenter.cellHeight != null 
				&& data.meshStaggerU.cellX != null && data.meshStaggerV.cellY != null
				&& data.meshStaggerU.cellWidth != null && data.meshStaggerV.cellHeight != null 
				&& data.P != null && data.T != null
				&& data.u != null && data.v != null){
			status.isInitialized = true;
		}else{			
			status.isInitialized = false;
		}
	}

	public void setTimeStep(double dt) {
		data.dt = dt;	
		checkWhetherInitialized();	
	}
	
	public void setMaxIter(int maxIt){
		data.maxIt = maxIt;
		checkWhetherInitialized();
	}
	
	public void setNumInnerIter(int it_u, int it_v, int it_P){
		data.numInnerIt_U = Math.max(1, it_u);
		data.numInnerIt_V = Math.max(1, it_v);
		data.numInnerIt_P = Math.max(1, it_P);
	}
	

}
