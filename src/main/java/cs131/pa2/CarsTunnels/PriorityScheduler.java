package cs131.pa2.CarsTunnels;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;
import cs131.pa2.Abstract.Log.EventType;
import cs131.pa2.Abstract.Log.Log;

/**
 * The priority scheduler assigns vehicles to tunnels based on their priority
 * It extends the Tunnel class.
 * @author cs131a
 *
 */
public class PriorityScheduler extends Tunnel{
	private Collection <Tunnel> basicTunnels ;
	private Log log;
	/**
	 * A queue that stores the vehicles waiting to enter a tunnel
	 */
	private PriorityQueue <Vehicle> waitQueue;
	/**
	 * A lock to lock the method of this class
	 */
	final Lock lock;
	final Condition condition;
	/**
	 * A map that keeps track of which vehicle entered which tunnel
	 */
	private Map <Vehicle, Tunnel> vehicleTunnel;
	
	
	/**
	 * Creates a new instance of the class PriorityScheduler with given name by calling the constructor of the super class
	 * @param name the name of the priority scheduler to create
	 */
	public PriorityScheduler(String name) {
		super(name);
		this.lock = new ReentrantLock();
		this.condition = lock.newCondition();
		
	}
	
	public PriorityScheduler (String name, Collection<Tunnel> tunnels, Log log) {
		super(name,log);
		this.basicTunnels = tunnels;
		this.waitQueue = new PriorityQueue<Vehicle>(2, new vehicleComparator());
		this.lock = new ReentrantLock();
		this.condition = lock.newCondition();
		this.vehicleTunnel = new HashMap <Vehicle, Tunnel>();
	}

	@Override
	public boolean tryToEnterInner(Vehicle vehicle) {
		lock.lock();
		waitQueue.add(vehicle);
		try {
			while(hasToWait(vehicle)) {
				condition.await();// waiting/ holding at this line until a vehicle exit the tunnel
			}						// if this "wait" is release, go back to the while loop 
									//to check again if it can successfully enter a tunnel
			waitQueue.poll();					       
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally {
			lock.unlock();
		}
		return true;
	}

	@Override
	public void exitTunnelInner(Vehicle vehicle) {
		lock.lock();
		try {
			Tunnel tunnel = vehicleTunnel.get(vehicle);
			tunnel.exitTunnel(vehicle);// remove the vehicle from the tunnel	
			vehicleTunnel.remove(vehicle);
			//	if the vehicle successfully exit, signify and wake up another one
			condition.signalAll();
		}finally {
			lock.unlock();
		}	
	}
	/**
	 * This method checks if this vehicle can enter a tunnel or it has to wait in the wait queue
	 * @param vehicle
	 * @return false: if it can directly go into the tunnel; return true when it need to wait
	 */
	public boolean hasToWait(Vehicle vehicle) {
		lock.lock();
		try {
			if(vehicle.getPriority()<waitQueue.peek().getPriority()){
				return true;
			}else {
				for (Tunnel tunnel: basicTunnels) {
					if (tunnel.tryToEnter(vehicle)) {
						vehicleTunnel.put(vehicle, tunnel);	// add to the map			
						return false;
					}
				}
				return true;
			}
		}finally {
			lock.unlock();
		}
	}
	
}
