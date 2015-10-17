package newUI;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;


public class GraphicsLoop{

	private Canvas canvas;
	private ArrayList<Paintable> paintables;
	
	public GraphicsLoop(Canvas canvasIn){
		canvas = canvasIn;
		paintables = new ArrayList<Paintable>();
	}
	
	public void registerPaintable(Paintable p){
		paintables.add(p);
	}
	
	public void graphicsLooper(){
		while(true){
			//Paint CFD here
			Graphics g = canvas.getGraphics();
			clearScreen(g);

			for (Iterator<Paintable> it = paintables.iterator(); it.hasNext();){
			      it.next().paint(g);
			}
			
			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}  
	
	private void clearScreen(Graphics g) {
        g.setColor(canvas.getBackground());
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}
	
	
}
