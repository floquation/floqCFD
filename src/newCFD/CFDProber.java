package newCFD;

public class CFDProber {
	
	private CFDData data;
	
	public CFDProber(CFDData dataIn){
		data = dataIn;
	}
	
	public double[][] getParameter(CFDParametersEnum param){
		switch(param){
			case P: return data.P;
			case T: return data.T;
			case U: return data.u;
			case V: return data.v;
			case massCons: return getMassConsMatrix();
			default: return null;		
		}
	}
	
	private double[][] getMassConsMatrix(){
		double[][] m = new double[data.P.length][data.P[0].length];
		
		double massCons = 0;
		for(int i = 1; i<m.length-1; i++){
			for(int j = 1; j<m[i].length-1; j++){
				m[i][j] = 
						-	data.rho * data.u[i][j] * data.meshCenter.cellHeight[j] 		//out
						+ 	data.rho * data.u[i-1][j] * data.meshCenter.cellHeight[j] 	//in
						-	data.rho * data.v[i][j] * data.meshCenter.cellWidth[i] 		//out
						+	data.rho * data.v[i][j-1] * data.meshCenter.cellWidth[i] 	//in
						;
				massCons += m[i][j];
			}
		}
		
		if(DEBUG.DEBUG && DEBUG.DEBUGlevel >=1){
			System.out.println("Global mass conservation: " + massCons);
		}
		return m;
	}
	
	public double[] getStaggeredMeshUX(){
		return data.meshStaggerU.cellX;
	}
	public double[] getStaggeredMeshVY(){
		return data.meshStaggerV.cellY;
	}
	
	public double[] getCenterMeshX(){
		return data.meshCenter.cellX;
	}
	public double[] getCenterMeshY(){
		return data.meshCenter.cellY;
	}
	
	

}
