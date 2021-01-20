package cs131.pa2.CarsTunnels;

import java.util.Comparator;

import cs131.pa2.Abstract.Vehicle;
/**
 * A comparator of vehicle for Priority Queue. 
 * @author Shumin Chen
 *
 */
public class vehicleComparator implements Comparator <Vehicle> {
	
	/**
	 * This method compares two vehicles. The vehicle with a greater priority number is "larger", 
	 * and will be put at the front of the queue
	 */
	public int compare(Vehicle v1, Vehicle v2) {
		if(v1.getPriority()>v2.getPriority()) {
			return -1;// -1 is the head, is put in the front
		}else if (v1.getPriority()<v2.getPriority()) {
			return 1;
		}else {
			return 0;
		}
	}
	
	
	
	
	
	
}