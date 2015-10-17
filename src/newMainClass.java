import newCFD.CFDMain;
import newCFD.CFDProber;
import newUI.GraphicsManager;


public class newMainClass {
		
	public static void main(String[] args){
		//Debug
		CFD.DEBUG.DEBUG = true;
		CFD.DEBUG.DEBUGlevel = 3;
		
		//CFD
		CFDMain cfd = new CFDMain();
		cfd.generateMesh(50, 50);
		cfd.setDensity(1);
		cfd.setViscosity(1);
		cfd.setTimeStep(2);
		cfd.setMaxIter(200000);
		CFDProber cfdProber = cfd.run();
		if(cfdProber == null)throw new RuntimeException("Incorrectly initialized CFD");
		GraphicsManager g = new GraphicsManager();
		g.initCFD(cfdProber);
		
		//Graphics
		new Thread(g).start();
		
		while(true){
			boolean b = cfd.doTimeStep();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
