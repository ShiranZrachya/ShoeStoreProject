package bgu.spl.json;

import java.util.concurrent.atomic.AtomicInteger;
/**
 * this class helps us read the json files - it describes all the services
 * the timer, manager, factories, sellers and array of the customers.
 * @author sparzada
 *
 */
public class services {
	private time time; // the timer
	private manager manager; // the manager
	private AtomicInteger factories; //the factories
	private AtomicInteger sellers; // the sellers
	private customers[] customers; // an array of the costumers
	
	public time getTime() {
		return time;
	}

	public manager getManager() {
		return manager;
	}

	public AtomicInteger getFactories() {
		return factories;
	}
	
	public AtomicInteger getSellers() {
		return sellers;
	}

	public customers[] getCustomers() {
		return customers;
	}

}