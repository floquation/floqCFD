package newCFD;

public class Point{
	public double x = 0;
	public double y = 0;
	
	public static Point computeCenter(Point[] points){
		int size = points.length;
		Point output = new Point();
		for(int i = 0; i<size; i++){
			output.x+=points[i].x;
			output.y+=points[i].y;
		}
		output.x/=size;
		output.y/=size;
		return output;
	}
	
	public String toString(){
		return "("+x+","+y+")";
	}
}