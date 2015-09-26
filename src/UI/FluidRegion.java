package UI;

import java.awt.Color;
import java.awt.Graphics;

import CFD.CFDParametersEnum;
import CFD.CFDProber;

public class FluidRegion implements Paintable{

	private int xS=0,yS=0,wS=0,hS=0; //paint region; screen coordinates
	private CFDParametersEnum showableData = CFDParametersEnum.U;
	private int textPaintInterval_i = 5;
	private int textPaintInterval_j = 2;
	private boolean paintCenterPoint = false;
	
	CFDProber cfd;
	
	public FluidRegion(CFDProber cfdIn){
		cfd = cfdIn;
	}
	
	@Override
	public void paint(Graphics g) {
		switch(showableData){
		case massCons:
		case P: 
		case T: //Center mesh
			paintCenter(g);
			break;
		case U:	//Stagger-X mesh
			paintStaggerX(g);			
			break;
		case V: //Stagger-Y mesh
			paintStaggerY(g);				
			break;
		default: throw new RuntimeException("Not all cases have been specified."); //Cannot occur.
		}

		//Outline of domain:
		g.setColor(Color.RED);
		g.drawRect(xS, yS, wS, hS);
	}
	
	private void paintCenter(Graphics g){
		//Velocity:
		double[][] dataToBeShown = cfd.getParameter(showableData);
		double[] domain = new double[]{Utils.minArray(dataToBeShown,true),Utils.maxArray(dataToBeShown,true)};
		double deltaDomain = domain[1]-domain[0];
		float f;
		float fR;
		float fG;
		float fB;
		//Mesh:
		double[] centermeshX = cfd.getCenterMeshX();
		double[] centermeshY = cfd.getCenterMeshY();
		double[] staggermeshUX = cfd.getStaggeredMeshUX();
		double[] staggermeshVY = cfd.getStaggeredMeshVY();
		int[] xpoints = new int[4];
		int[] ypoints = new int[4];
				
		//Display mesh & velocity profile
		for(int i = 0; i<dataToBeShown.length; i++){ //Iterate over cells
			for(int j = 0; j<dataToBeShown[i].length; j++){ //Iterate over cells
				//Obtain corners of current cell:
				if(i==0){
					xpoints[0] = xS+wS*(-1);
					xpoints[3] = xS+wS*(-1);				
				}else{
					xpoints[0] = xS+(int)(wS*staggermeshUX[i-1]);
					xpoints[3] = xS+(int)(wS*staggermeshUX[i-1]);
				}
				if(i==dataToBeShown.length-1){
					xpoints[1] = xS+wS*(2);
					xpoints[2] = xS+wS*(2);
				}else{
					xpoints[1] = xS+(int)(wS*staggermeshUX[i]);
					xpoints[2] = xS+(int)(wS*staggermeshUX[i]);					
				}
				if(j==0){
					ypoints[0] = hS+yS-hS*(-1);
					ypoints[1] = hS+yS-hS*(-1);
				}else{
					ypoints[0] = hS+yS-(int)(hS*staggermeshVY[j-1]);
					ypoints[1] = hS+yS-(int)(hS*staggermeshVY[j-1]);					
				}
				if(j == dataToBeShown[i].length-1){
					ypoints[2] = hS+yS-hS*(2);
					ypoints[3] = hS+yS-hS*(2);
				}else{
					ypoints[2] = hS+yS-(int)(hS*staggermeshVY[j]);
					ypoints[3] = hS+yS-(int)(hS*staggermeshVY[j]);
				}
				
				//Colors:
				f = (float) ((dataToBeShown[i][j]-domain[0])/deltaDomain);
				if(f>0.5){
					fR = 2*f-1;
					fG = -2*f+2;
					fB = 0;
				}else{
					fR = 0;
					fG = 2*f;
					fB = -2*f+1;
				}
				if(fR>1)fR=1;
				if(fR<0)fR=0;
				if(fG>1)fG=1;
				if(fG<0)fG=0;
				if(fB>1)fB=1;
				if(fB<0)fB=0;
				
				//Parameter profile:
				g.setColor(new Color(fR,fG,fB));
				g.fillPolygon(xpoints, ypoints, 4);
				//Mesh:
				g.setColor(Color.BLACK);
				g.drawPolygon(xpoints, ypoints, 4);	
			}
		}
				
		//Centers points:
		g.setColor(Color.ORANGE);
		for(int i = 0; i<centermeshX.length; i++){
			for(int j = 0; j<centermeshY.length; j++){
				if(paintCenterPoint)g.fillOval(xS+(int)(wS*centermeshX[i]), hS+yS-(int)(hS*centermeshY[j]), 3, 3);
				if(i%textPaintInterval_i == 0 && j%textPaintInterval_j == 0){
					String s = String.format("%6.3e",dataToBeShown[i][j]);
					g.drawString(s, xS+(int)(wS*centermeshX[i]), hS+yS-(int)(hS*centermeshY[j]));
				}
			}
		}
	}

	private void paintStaggerX(Graphics g){
		//Velocity:
		double[][] dataToBeShown = cfd.getParameter(showableData);
		double[] domain = new double[]{Utils.minArray(dataToBeShown,true),Utils.maxArray(dataToBeShown,true)};
		double deltaDomain = domain[1]-domain[0];
		float f;
		float fR;
		float fG;
		float fB;
		//Mesh:
		double[] centermeshX = cfd.getCenterMeshX();
		double[] centermeshY = cfd.getCenterMeshY();
		double[] staggermeshUX = cfd.getStaggeredMeshUX();
		double[] staggermeshVY = cfd.getStaggeredMeshVY();
		int[] xpoints = new int[4];
		int[] ypoints = new int[4];
				
		//Display mesh & velocity profile
		for(int i = 0; i<dataToBeShown.length; i++){ //Iterate over cells
			for(int j = 0; j<dataToBeShown[i].length; j++){ //Iterate over cells
				//Obtain corners of current cell:
				xpoints[0] = xS+(int)(wS*centermeshX[i]);
				xpoints[3] = xS+(int)(wS*centermeshX[i]);
				xpoints[1] = xS+(int)(wS*centermeshX[i+1]);
				xpoints[2] = xS+(int)(wS*centermeshX[i+1]);	
				if(j==0){
					ypoints[0] = hS+yS-hS*(-1);
					ypoints[1] = hS+yS-hS*(-1);
				}else{
					ypoints[0] = hS+yS-(int)(hS*staggermeshVY[j-1]);
					ypoints[1] = hS+yS-(int)(hS*staggermeshVY[j-1]);					
				}
				if(j == dataToBeShown[i].length-1){
					ypoints[2] = hS+yS-hS*(2);
					ypoints[3] = hS+yS-hS*(2);
				}else{
					ypoints[2] = hS+yS-(int)(hS*staggermeshVY[j]);
					ypoints[3] = hS+yS-(int)(hS*staggermeshVY[j]);
				}
				
				//Colors:
				f = (float) ((dataToBeShown[i][j]-domain[0])/deltaDomain);
				if(f>0.5){
					fR = 2*f-1;
					fG = -2*f+2;
					fB = 0;
				}else{
					fR = 0;
					fG = 2*f;
					fB = -2*f+1;
				}
				if(fR>1)fR=1;
				if(fR<0)fR=0;
				if(fG>1)fG=1;
				if(fG<0)fG=0;
				if(fB>1)fB=1;
				if(fB<0)fB=0;
				
				//Parameter profile:
				g.setColor(new Color(fR,fG,fB));
				g.fillPolygon(xpoints, ypoints, 4);
				//Mesh:
				g.setColor(Color.BLACK);
				g.drawPolygon(xpoints, ypoints, 4);	
			}
		}
		
		//Centers points:
		g.setColor(Color.ORANGE);
		for(int i = 0; i<staggermeshUX.length; i++){
			for(int j = 0; j<centermeshY.length; j++){
				if(paintCenterPoint)g.fillOval(xS+(int)(wS*staggermeshUX[i]), hS+yS-(int)(hS*centermeshY[j]), 3, 3);
				if(i%textPaintInterval_i == 0 && j%textPaintInterval_j == 0){
					String s = String.format("%6.3e",dataToBeShown[i][j]);
					g.drawString(s, xS+(int)(wS*staggermeshUX[i]), hS+yS-(int)(hS*centermeshY[j]));
				}
			}
		}
	}

	private void paintStaggerY(Graphics g){
		//Velocity:
		double[][] dataToBeShown = cfd.getParameter(showableData);
		double[] domain = new double[]{Utils.minArray(dataToBeShown,true),Utils.maxArray(dataToBeShown,true)};
		double deltaDomain = domain[1]-domain[0];
		float f;
		float fR;
		float fG;
		float fB;
		//Mesh:
		double[] centermeshX = cfd.getCenterMeshX();
		double[] centermeshY = cfd.getCenterMeshY();
		double[] staggermeshUX = cfd.getStaggeredMeshUX();
		double[] staggermeshVY = cfd.getStaggeredMeshVY();
		int[] xpoints = new int[4];
		int[] ypoints = new int[4];
				
		//Display mesh & velocity profile
		for(int i = 0; i<dataToBeShown.length; i++){ //Iterate over cells
			for(int j = 0; j<dataToBeShown[i].length; j++){ //Iterate over cells
				//Obtain corners of current cell:
				if(i==0){
					xpoints[0] = xS+wS*(-1);
					xpoints[3] = xS+wS*(-1);				
				}else{
					xpoints[0] = xS+(int)(wS*staggermeshUX[i-1]);
					xpoints[3] = xS+(int)(wS*staggermeshUX[i-1]);
				}
				if(i==dataToBeShown.length-1){
					xpoints[1] = xS+wS*(2);
					xpoints[2] = xS+wS*(2);
				}else{
					xpoints[1] = xS+(int)(wS*staggermeshUX[i]);
					xpoints[2] = xS+(int)(wS*staggermeshUX[i]);					
				}
				ypoints[0] = hS+yS-(int)(hS*centermeshY[j]);
				ypoints[1] = hS+yS-(int)(hS*centermeshY[j]);		
				ypoints[2] = hS+yS-(int)(hS*centermeshY[j+1]);
				ypoints[3] = hS+yS-(int)(hS*centermeshY[j+1]);
				
				//Colors:
				f = (float) ((dataToBeShown[i][j]-domain[0])/deltaDomain);
				if(f>0.5){
					fR = 2*f-1;
					fG = -2*f+2;
					fB = 0;
				}else{
					fR = 0;
					fG = 2*f;
					fB = -2*f+1;
				}
				if(fR>1)fR=1;
				if(fR<0)fR=0;
				if(fG>1)fG=1;
				if(fG<0)fG=0;
				if(fB>1)fB=1;
				if(fB<0)fB=0;
				
				//Parameter profile:
				g.setColor(new Color(fR,fG,fB));
				g.fillPolygon(xpoints, ypoints, 4);
				//Mesh:
				g.setColor(Color.BLACK);
				g.drawPolygon(xpoints, ypoints, 4);	
			}
		}
		
		//Centers points:
		g.setColor(Color.ORANGE);
		for(int i = 0; i<centermeshX.length; i++){
			for(int j = 0; j<staggermeshVY.length; j++){
				if(paintCenterPoint)g.fillOval(xS+(int)(wS*centermeshX[i]), hS+yS-(int)(hS*staggermeshVY[j]), 3, 3);
				if(i%textPaintInterval_i == 0 && j%textPaintInterval_j == 0){
					String s = String.format("%6.3e",dataToBeShown[i][j]);
					g.drawString(s, xS+(int)(wS*centermeshX[i]), hS+yS-(int)(hS*staggermeshVY[j]));
				}
			}
		}
	}

	
	@Override
	public void setX(int x) {
		xS = x;		
	}
	@Override
	public void setY(int y) {
		yS = y;		
	}
	@Override
	public void setW(int w) {
		wS = w;
	}
	@Override
	public void setH(int h) {
		hS = h;		
	}
	
	
}
