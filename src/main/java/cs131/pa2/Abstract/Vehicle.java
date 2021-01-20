package cs131.pa2.Abstract;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cs131.pa2.Abstract.Log.EventType;
import cs131.pa2.Abstract.Log.Log;
import cs131.pa2.CarsTunnels.Ambulance;
import cs131.pa2.CarsTunnels.PreemptivePriorityScheduler;
import cs131.pa2.CarsTunnels.PriorityScheduler;

/**
 * A Vehicle is a Runnable which enters tunnels. You must subclass
 * Vehicle to customize its behavior (e.g., Car and Sled).
 *
 * When you start a thread which runs a Vehicle, the Vehicle will
 * immediately begin trying to enter the tunnel or tunnels passed into
 * its constructor by calling tryToEnter on each Tunnel instance. As
 * long as tryToEnter returns false (indicating that the Vehicle did
 * not enter that tunnel), the Vehicle will keep trying. This is
 * called busy-waiting.
 *
 * In addition to recreating the constructors, the only method that
 * you must override in Vehicle subclasses is getDefaultSpeed. This
 * instance method is called from the private init method, and the
 * integer that it returns is used as the speed for the vehicle.
 *
 * @author cs131a
 */
public abstract class Vehicle implements Runnable {
	/**
	 * The name of this vehicle
	 */
    private String            	name;
    /**
     * The direction of this vehicle
     */
    private Direction          	direction;
    /**
     * The collection of tunnels available to this vehicle (The vehicle should attempt to enter any of these tunnels)
     */
    private Collection<Tunnel> 	tunnels;
    /**
     * The priority of this vehicle
     */
    private int                	priority;
    /**
     * The speed of this vehicle
     */
    private int                	speed;
    /**
     * The log used to log operations
     */
    private Log 				log;
    /**
     * The lock of this car
     */
    private Lock lock;
    /**
     * A Condition. When it is await, it represents this car is running. 
     * When it is awake, it just allow the program to execute next line of code
     */
    private Condition driving;
    

    /**
     * Initialize a Vehicle; called from Vehicle constructors.
     * 
     * @param name the name of the vehicle
     * @param direction the direction of the vehicle
     * @param priority the priority of the vehicle
     * @param log the log to be use for logging
     */
    private void init(String name, Direction direction,
                      int priority, Log log) {
        this.name      = name;
        this.direction = direction;
        this.priority  = 0;
        this.speed     = getDefaultSpeed();
        this.log       = log;
        this.tunnels   = new ArrayList<Tunnel>();
        this.lock= new ReentrantLock();
        this.driving = lock.newCondition();
        if(this.speed < 0 || this.speed > 9) {
            throw new RuntimeException("Vehicle has invalid speed");
        }
    }

    /**
     * Override in a subclass to determine the speed of the
     * vehicle.
     *
     * Must return a number between 0 and 9 (inclusive). Higher
     * numbers indicate greater speed. The faster a vehicle, the less
     * time it will spend in a tunnel.
     * 
     * @return the speed of this vehicle
     */
    protected abstract int getDefaultSpeed();

    /**
     * Create a Vehicle with default priority that can cross one of
     * a collection of tunnels.
     * 
     * @param name      The name of this vehicle to be displayed in the
     *                  output.
     * @param direction The side of the tunnel being entered.
     * @param log the log to be used for logging
     */
    public Vehicle(String name, Direction direction, Log log) {
        init(name, direction, 0, log);
    }
    
    /**
     * Create a Vehicle with default priority that can cross one of
     * a collection of tunnels and use the default log.
     * 
     * @param name      The name of this vehicle to be displayed in the
     *                  output.
     * @param direction The side of the tunnel being entered.
     */
    public Vehicle(String name, Direction direction) {
        this(name, direction, Tunnel.DEFAULT_LOG);
    }
    
    
    
    /**
     * Sets this vehicle's speed - used for preemptive priority scheduler test
     * @param speed the new speed to be set (0 to 9)
     */
    public void setSpeed(int speed) {
    	if(this.speed < 0 || this.speed > 9) {
            throw new RuntimeException("Invalid speed: "+ speed);
        }
    	this.speed = speed;
    }
    
    /** 
     * Sets this vehicle's priority - used for priority scheduling
     *
     * @param priority The new priority (between 0 and 4 inclusive)
     */
    public final void setPriority(int priority) {
        if(priority < 0 || priority > 4) {
            throw new RuntimeException("Invalid priority: " + priority);
        }
        this.priority = priority;
    }
    
    /**
     * Returns the priority of this vehicle
     *
     * @return This vehicle's priority.
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Returns the name of this vehicle
     *
     * @return The name of this vehicle
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns a string representation of this vehicle
     * 
     * @return the string representation of this vehicle
     */
    public String toString() {
    		return String.format("%s VEHICLE %s", this.direction, this.name);
    }
    
    /**
     * Adds a new tunnel to the collection of tunnels for this vehicle
     * @param newTunnel the new tunnel to be added
     */
    public final void addTunnel(Tunnel newTunnel) {
        this.tunnels.add(newTunnel);
    }

    /**
     * Adds a new collection of tunnels to the current collection
     * @param newTunnels the new collection of tunnels to be added
     */
    public final void addTunnel(Collection<Tunnel> newTunnels) {
        this.tunnels.addAll(newTunnels);
    }
    
    /**
     * Find and cross through one of the tunnels.
     * 
     * When a thread is run, it keeps looping through its collection
     * of available tunnels until it succeeds in entering one of
     * them. Then, it will call doWhileInTunnel (to simulate doing
     * some work inside the tunnel, i.e., that it takes time to cross
     * the tunnel), then exit that tunnel.
     */
    public final void run() {
        // Loop over all tunnels repeated until we can enter one, then
        // think inside the tunnel, exit the tunnel, and leave this
        // entire method.
        //
        while(true) {
            for(Tunnel tunnel : tunnels) {
                if(tunnel.tryToEnter(this)) {
                    if(tunnel instanceof PreemptivePriorityScheduler) {
                    	doWhileInTunnelPreemptive();
                    }else {
                    	doWhileInTunnel();	
                    }
                    tunnel.exitTunnel(this);
                    this.log.addToLog(this, EventType.COMPLETE);
                    return; // done, so leave the whole function
                }
            }
        }
    }
    
    
    
   
    
    /**
     * Returns the direction of this vehicle
     *
     * @return the direction of this vehicle
     */
    public final Direction getDirection() {
        return direction;
    }

    /**
     * This is what your vehicle does while inside the tunnel to
     * simulate taking time to "cross" the tunnel. The faster your
     * vehicle is, the less time this will take.
     */
   
    public final void doWhileInTunnel() {
         try {
             Thread.sleep((10 - speed) * 100);
         } catch(InterruptedException e) {
             System.err.println("Interrupted vehicle " + getName());
         }
    }
    
    /**
     * This is what your vehicle does while inside the tunnel to
     * simulate taking time to "cross" the tunnel. The faster your
     * vehicle is, the less time this will take.
     * 
     * It will be used for preemptive scheduler
     */
    public final void doWhileInTunnelPreemptive() {
    	long timeout = (10 - speed) * 100;
    	lock.lock();
    	try {
    		//if the vehicle is an Ambulance, directly run until it pass the tunnel
    		if(this instanceof Ambulance) {
    			driving.await(timeout, TimeUnit.MILLISECONDS );
    		}else {
    			while(timeout>0) {
    				long startTime = System.currentTimeMillis();
    				try {
    					driving.await(timeout, TimeUnit.MILLISECONDS );
    				}catch (InterruptedException e) {
        				System.err.println("Interrupted vehicle " + getName());
        			}
    				long endTime = System.currentTimeMillis();
    				timeout = timeout-(endTime-startTime);
    				
    				// if the vehicle is interrupted (ie. driving.await() didn't hold for the time it is supposed to drive)
    				if(timeout>0) {
        				try {
        					driving.await(); // the vehicle waits (holds at this line) until the ambulance left and notify this vehicle
        									 // if the vehicle gets notified, it move to the next line of code and then go back to continue running 
        									// (by re-entering the while loop)
        				} catch (InterruptedException e) {
        					e.printStackTrace();	
        				}
        			}
    			}	
    		}	
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
    		lock.unlock();
    	}  
   }
    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.name);
        hash = 23 * hash + Objects.hashCode(this.direction);
        hash = 23 * hash + this.speed;
        hash = 23 * hash + this.priority;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Vehicle other = (Vehicle) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (this.direction != other.direction) {
            return false;
        }
        if (this.speed != other.speed) {
            return false;
        }
        if (this.priority != other.priority) {
            return false;
        }
        return true;
    }
    /**
     * This method interrupt the vehicle while it is driving
     */
	public void waitForAmbulance() {
		this.lock.lock();
		try {
			this.driving.signal();
		}finally {
			this.lock.unlock();
		}
	}
	/**
	 * This method notify the vehicle to go back to the driving state in the tunnel 
	 * after an ambulance has left
	 */
	public void goBackAfterAmbLeft() {
		this.lock.lock();
		try {
			this.driving.signal();
		}finally {
			this.lock.unlock();
		}
	}
}
