package bgu.spl.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.impl.MessageBusImpl;
/**
 * This micro-service can add discount to shoes in the store and send
	NewDiscountBroadcast to notify clients about them.
	In addition, the ManagementService handles RestockRequests that is being sent by the
	Sell-ingService. Whenever a RestockRequest of a specific shoe type received the service first check that
	this shoe type is not already on order,  if it doesn't  it will send a ManufacturingOrderRequestfor(current-tick%5) + 1
	shoes of this type, when this order completes - it update the store stock, file the receipt and only then complete the RestockRequest (and not before)
	with the result of true. If there were no one that can handle the ManufacturingOrderRequest it will complete the
	RestockRequest with the result false.
 * @author shiran & nizan
 *
 */
public class ManagementService extends MicroService{
	private int currentTick;
	private ConcurrentHashMap<Integer, LinkedBlockingQueue<DiscountSchedule>> discounts;
	private ConcurrentHashMap<String, ArrayList<RestockRequest>> restockRequests;
	private ConcurrentHashMap<String, AtomicInteger> amountToOrder;
	CountDownLatch cdlStart;
	CountDownLatch cdlEnd;
	private static final Logger logger = Logger.getLogger( ManagementService.class.getName() );
	
public ManagementService(List<DiscountSchedule> toSort, CountDownLatch cdlStart, CountDownLatch cdlEnd){
	super("manager");
	restockRequests = new ConcurrentHashMap<String, ArrayList<RestockRequest>>();
	amountToOrder = new ConcurrentHashMap<String, AtomicInteger>();
	discounts = new ConcurrentHashMap<Integer, LinkedBlockingQueue<DiscountSchedule>>();
	this.cdlStart=cdlStart;
	this.cdlEnd=cdlEnd;
	for (int i = 0; i<toSort.size(); i++){ // this loop sorts the discount list by ticks. from the lower to the higher tick.
		if (discounts.containsKey(toSort.get(i).getTick()))
			try {
				discounts.get(toSort.get(i).getTick()).put(toSort.get(i));
			} catch (InterruptedException e) {
			
				e.printStackTrace();
			}
		else{
			discounts.put(new Integer(toSort.get(i).getTick()), new LinkedBlockingQueue<DiscountSchedule>());
			try {
				discounts.get(toSort.get(i).getTick()).put(toSort.get(i));
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
		}
	}
	
}


@Override
protected void initialize() {	
	this.subscribeBroadcast(TickBroadcast.class, callback->{  //the manager gets all the ticks so he will know when to send a discount broadcast to the web client services.
		this.currentTick = callback.getCurrentTick();
		if (callback.getCurrentTick()== -1){
			terminate();
			cdlEnd.countDown();
		}
		for (Integer tick : discounts.keySet()){ // if the current tick fits this discount broadcast message's tick, the message will be sent to the clients.
			if (tick.intValue() == callback.getCurrentTick()){
				while(!discounts.get(tick.intValue()).isEmpty()){
					DiscountSchedule dis = discounts.get(tick.intValue()).poll();
					Store.getInstance().addDiscount(dis.getShoeType(), dis.getAmount());
					logger.info("manager send a NewDiscountBroadcast for "+ dis.getAmount()+" "+ dis.getShoeType());
					this.sendBroadcast(new NewDiscountBroadcast(dis.getShoeType()));		
				}
			}
		}
	});
	
	
	this.subscribeRequest(RestockRequest.class, callback->{ // the manager gets the restock requests from the sellers.		

		if(amountToOrder.containsKey(callback.getShoeType()) && amountToOrder.get(callback.getShoeType()).intValue() > 0){ // checks if he already orderd enough shoes from this specific kind.			
				amountToOrder.get(callback.getShoeType()).getAndDecrement();
				restockRequests.get(callback.getShoeType()).add(callback);
		}
		else{
			ManufacturingOrderRequest mor = new ManufacturingOrderRequest( callback.getShoeType(), currentTick%5+1 , currentTick); // creates a new  ManufacturingOrderRequest to send the factory
			if(!amountToOrder.containsKey(callback.getShoeType())){
				amountToOrder.put(callback.getShoeType(), new AtomicInteger(currentTick%5));
				restockRequests.put(mor.getShoeType(), new ArrayList<RestockRequest>());
				restockRequests.get(mor.getShoeType()).add(callback);
			}
			else {
				amountToOrder.get(callback.getShoeType()).addAndGet(currentTick%5);
				restockRequests.get(mor.getShoeType()).add(callback);
			}
		
			logger.info("manager is sending a ManufacturingOrderRequest for "+mor.getAmount()+" "+ mor.getShoeType());
			boolean ans = sendRequest(mor, (Receipt c)->{ // sends a new request to the factory to manufacture the wanted shoe		
				
				if(c!=null){ // if the factory manufactured the shoe, the manager sends the sellers a massage that says that they can now sell the shoe to the waiting client.
						for(int i=0; i<c.getAmountSold()&&restockRequests.get(c.getShoeType()).size()>i;i++){
						complete(restockRequests.get(c.getShoeType()).get(i), true);
					}
						for(int i=0; i<c.getAmountSold()&&restockRequests.get(c.getShoeType()).size()>i;i++){// removes the restock request that was take care from the list
							restockRequests.get(c.getShoeType()).remove(0);
						}
					
						Store.getInstance().add(c.getShoeType(),amountToOrder.get(c.getShoeType()).intValue()); // adds the rest of the manufactured shoes to the store.
						amountToOrder.get(c.getShoeType()).set(0);
						Store.getInstance().file(c);
				}
				else{
					logger.info("the ManufacturingOrderRequest for "+mor.getAmount()+" "+ mor.getShoeType()+ " was unsuccesfull");
					for(int i=0; i<mor.getAmount() &&restockRequests.get(mor.getShoeType()).size()>0;i++){ // if the factory didn't manufacture any shoe, the manager sends a complete message with a false argument to the seller
						complete(restockRequests.get(mor.getShoeType()).get(i), false);
					}
					for(int i=0; i<mor.getAmount()&&restockRequests.get(mor.getShoeType()).size()>0;i++){// removes the restock request from the list
						restockRequests.get(mor.getShoeType()).remove(0);
						}
				}			
			});
			if(!ans){
				logger.info("the ManufacturingOrderRequest for "+mor.getAmount()+" "+ mor.getShoeType()+ " was unsuccesfull");
				complete(callback, false);// if there were no factories to receive the request, it sends the seller complete with false 
			}
		}
		
	});

		cdlStart.countDown();

	}

	

}