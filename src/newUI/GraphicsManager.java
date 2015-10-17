package newUI;
import java.awt.Canvas;
import java.awt.Color;

import javax.swing.JFrame;

import newCFD.CFDProber;


public class GraphicsManager implements Runnable{

	Canvas canvas;
	JFrame frame;
	GraphicsLoop gLoop;
	
	public GraphicsManager(){
		frame = new JFrame();
		canvas = new Canvas();
		canvas.setSize(1000,600);
		frame.setTitle("CFD Tester");
		frame.setBackground(Color.black);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add("Center", canvas);
		frame.pack();
		frame.setVisible(true);
		//canvas.addMouseListener(this);
		
		gLoop = new GraphicsLoop(canvas);
	}
	
	public void initCFD(CFDProber cfd){
		System.out.println("initCFD");
		FluidRegion fr = new FluidRegion(cfd);
		fr.setX(50);
		fr.setY(50);
		fr.setW(canvas.getWidth()-100);
		fr.setH(canvas.getHeight()-100);
		gLoop.registerPaintable(fr);
	}
	
	@Override
	public void run() {
		System.out.println("run graphics");
		gLoop.graphicsLooper();
	}


	
}
