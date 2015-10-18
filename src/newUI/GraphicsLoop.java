package newUI;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;


public class GraphicsLoop{

	private Canvas canvas;
	private BufferedImage buffer;
	private ArrayList<Paintable> paintables;
	
	public GraphicsLoop(Canvas canvasIn){
		canvas = canvasIn;
		paintables = new ArrayList<Paintable>();
		buffer = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
	}
	
	public void registerPaintable(Paintable p){
		paintables.add(p);
	}
	
	public void graphicsLooper(){
		while(true){
			//Paint CFD here
			Graphics g_buffer = buffer.getGraphics();
			Graphics g_canvas = canvas.getGraphics();

			clearScreen(g_buffer);
			for (Iterator<Paintable> it = paintables.iterator(); it.hasNext();){
			      it.next().paint(g_buffer);
			}
			
			g_canvas.drawImage(buffer, 0, 0, canvas.getWidth(), canvas.getHeight(), null);
			
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
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}
	
	
}
