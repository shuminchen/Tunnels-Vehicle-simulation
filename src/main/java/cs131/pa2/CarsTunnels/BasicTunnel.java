package cs131.pa2.CarsTunnels;
/**
 * Shumin Chen
 */
import java.util.LinkedList;

import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;
import cs131.pa2.Abstract.Log.Event;
import cs131.pa2.Abstract.Log.EventType;

/**
 * 
 * The class for the Basic Tunnel, extending Tunnel.
 * @author cs131a
 *
 */
public class BasicTunnel extends Tunnel{
	/**
	 * A list that keep track of the vehicle within the tunnel
	 */
	private LinkedList <Vehicle> vehicleList ;
	
	
	/**
	 * Creates a new instance of a basic tunnel with the given name
	 * @param name the name of the basic tunnel
	 */
	public BasicTunnel(String name) {
		super(name);
		this.vehicleList= new LinkedList <Vehicle>();
	}
	/**
	 * Add vehicle in this tunnel
	 */
	@Override
	protected synchronized boolean tryToEnterInner(Vehicle vehicle) {
		if (this.vehicleList.isEmpty()) {
			vehicleList.add(vehicle);
			return true;
		}else {
			if(vehicle instanceof Car ) {
				// if there is sled in the tunnel, car cannot enter
				if(vehicleList.get(0) instanceof Sled){
					return false;
				}else if(this.vehicleList.size()<3) {
					if(vehicleList.get(0).getDirection().equals(vehicle.getDirection())) {
						this.vehicleList.add(vehicle);
						return true;
					}
				}
			}else if(vehicle instanceof Ambulance ) {
				// check if there is already an ambulance in the tunnel
				for (Vehicle v : vehicleList) {
					if (v instanceof Ambulance) {
						return false;
					} 
				}
				vehicleList.add(vehicle);
				return true;
			}
		}
		return false;
	}

	
	
	/**
	 * remove vehicle from this tunnel
	 */
	@Override
	public synchronized void  exitTunnelInner(Vehicle vehicle) {
		for (int i=0;i< this.vehicleList.size();i++) {
			if(this.vehicleList.get(i).equals(vehicle)) {
				this.vehicleList.remove(i);
				break;
			}
		}	
	}
	/**
	 * 
	 * @return the vehicleList of this instance
	 */
	public LinkedList<Vehicle> getVehicleList(){
		return this.vehicleList;
	}
	
}
