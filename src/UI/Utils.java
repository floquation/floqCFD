package UI;

public class Utils {

	public static double minArray(double[][] array, boolean includeBoundary){
		double min = Double.MAX_VALUE;
		int counter_min = includeBoundary? 0 : 1;
		
		for(int i = counter_min; i<array.length-counter_min; i++){
			for(int j = counter_min; j<array[i].length-counter_min; j++){
				min = Math.min(array[i][j], min);
			}
		}
		
		return min;
	}

	public static double maxArray(double[][] array, boolean includeBoundary){
		double max = -Double.MAX_VALUE;
		int counter_min = includeBoundary? 0 : 1;
		
		for(int i = counter_min; i<array.length-counter_min; i++){
			for(int j = counter_min; j<array[i].length-counter_min; j++){
				max = Math.max(array[i][j], max);
			}
		}
		
		return max;
	}

}
