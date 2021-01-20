package cs131.pa2.CarsTunnels;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;
import cs131.pa2.Abstract.Log.Log;

/**
 * The preemptive priority scheduler assigns vehicles to tunnels based on their priority and supports ambulances.
 * It extends the Tunnel class.
 * @author cs131a
 *
 */
public class PreemptivePriorityScheduler extends Tunnel{
	private Collection <Tunnel> basicTunnels ;
	private Log log;
	/**
	 * A Priority Queue that stores the vehicles waiting to enter the tunnel
	 */
	private PriorityQueue <Vehicle> waitQueue;
	/**
	 * A lock for this class to prevent race condition
	 */
	final Lock schedulerLock;
	final Condition wait;
	/**
	 * A map that keeps track of which vehicle entered which tunnel
	 */
	private Map <Vehicle, Tunnel> vehicleTunnel;
	/**
	 * Creates a new instance of the class PreemptivePriorityScheduler with given name by calling the constructor of the super class
	 * @param name the name of the preemptive priority scheduler to create
	 */
	public PreemptivePriorityScheduler(String name, Collection<Tunnel> tunnels, Log log) {
		super(name, log);
		this.basicTunnels = tunnels;
		this.waitQueue = new PriorityQueue<Vehicle>(2, new vehicleComparator());
		this.schedulerLock = new ReentrantLock();
		this.wait = schedulerLock.newCondition();
		this.vehicleTunnel = new HashMap <Vehicle, Tunnel>();

	}

	
	
	@Override
	public boolean tryToEnterInner(Vehicle vehicle) {
		schedulerLock.lock();
		waitQueue.add(vehicle); 
		try {
			while(hasToWait(vehicle)) {
				wait.await();// waiting/ holding at this line until a vehicle exit the tunnel
			}				// if this "wait" is release, go back to the while loop to check again if it can successfully enter a tunnel
			waitQueue.poll();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally {
			schedulerLock.unlock();
		}
		return true;
		
	}
	
	

	@Override
	public void exitTunnelInner(Vehicle vehicle) {
		schedulerLock.lock();
		try {
			Tunnel tunnel = vehicleTunnel.get(vehicle);
			tunnel.exitTunnel(vehicle);
			// remove the vehicle from appropriate tunnel	
			vehicleTunnel.remove(vehicle);
			
			// if an Ambulance left the tunnel, signify other vehicle in the tunnel to continue driving
			if(vehicle instanceof Ambulance) {
				OtherVehicleContinue((BasicTunnel) tunnel);
			}
			//if the vehicle successfully exit, signify and wake up another one
			wait.signalAll();
		}finally {
			schedulerLock.unlock();
		}
	}
	
	
	/**
	 * This method checks if this vehicle can enter a tunnel or it has to wait in the wait queue
	 * @param vehicle
	 * @return false: if it can directly go into the tunnel; return true when it need to wait
	 */
	public boolean hasToWait(Vehicle vehicle) {
		schedulerLock.lock();
		try {
			if(vehicle instanceof Ambulance ) {
				for (Tunnel tunnel: basicTunnels) {
					if(tunnel.tryToEnter(vehicle))	{
						vehicleTunnel.put(vehicle, tunnel);
						//notify other vehicles in the tunnel to pull over
						otherVehiclePullOver((BasicTunnel)tunnel);
						return false;
					}
				}
			}else if(vehicle.getPriority()<waitQueue.peek().getPriority()){
				return true;
			}else {
				for (Tunnel tunnel: basicTunnels) {
					if (tunnel.tryToEnter(vehicle)) {
						vehicleTunnel.put(vehicle, tunnel);	// add to the map to keep track of which vehicle goes to which tunnel	
						return false;
					}
				}
			}
		}finally {
			schedulerLock.unlock();
		}
		return true;
	}
	/**
	 * This method signal other vehicles in the tunnel to pull over of the ambulance
	 * @param tunnel
	 */
	public void otherVehiclePullOver(BasicTunnel tunnel){
		for(Vehicle vehicle: tunnel.getVehicleList()) {
			if(!(vehicle instanceof Ambulance)) {
				vehicle.waitForAmbulance();
			}
		}
	}
	/**
	 * This method signal other vehicle in the tunnel to continue driving immediately after ambulance left
	 * @param tunnel
	 */
	private void OtherVehicleContinue(BasicTunnel tunnel) {
		for (Vehicle vehicle: tunnel.getVehicleList()) {
			if(!(vehicle instanceof Ambulance)) {
				vehicle.goBackAfterAmbLeft();
			}
		}
		
	}
}

