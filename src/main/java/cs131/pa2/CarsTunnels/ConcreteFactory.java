package cs131.pa2.CarsTunnels;
/**
 * Shumin Chen
 */
import java.util.Collection;

import cs131.pa2.Abstract.Direction;
import cs131.pa2.Abstract.Factory;
import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;
import cs131.pa2.Abstract.Log.Log;

/**
 * The class implementing the Factory interface for creating instances of classes
 * @author cs131a
 *
 */
public class ConcreteFactory implements Factory {

    @Override
    public Tunnel createNewBasicTunnel(String name){
    		BasicTunnel tunnel = new BasicTunnel(name);
    		return tunnel;   
    }

    @Override
    public Vehicle createNewCar(String name, Direction direction){
    		Car car = new Car(name, direction);
    		return car; 
    }

    @Override
    public Vehicle createNewSled(String name, Direction direction){
    		Sled sled = new Sled(name, direction);
    		return sled;   
    }

    @Override
    public Tunnel createNewPriorityScheduler(String name, Collection<Tunnel> tunnels, Log log){
    	PriorityScheduler priorityScheduler = new PriorityScheduler(name, tunnels, log);
    	return priorityScheduler;
    }

	@Override
	public Vehicle createNewAmbulance(String name, Direction direction) {
		Ambulance ambulance = new Ambulance (name, direction);
		return ambulance;
	}

	@Override
	public Tunnel createNewPreemptivePriorityScheduler(String name, Collection<Tunnel> tunnels, Log log) {
		PreemptivePriorityScheduler preemptivePriorityScheduler = new PreemptivePriorityScheduler(name, tunnels, log);
		return preemptivePriorityScheduler;
	}
}
