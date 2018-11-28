package bgu.spl.app;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import bgu.spl.mics.MicroService;
/**
 * This micro-service describes a shoe factory that manufacture shoes for the store. This micro-service handles the
	ManufacturingOrderRequest it takes it exactly 1 tick to manufacture a single shoe
	(starting from tick following the request). When done manufacturing, this micro-service completes
	the request with a receipt (which has the value “store” in the customer field and “discount” = false).
	The micro-service cannot manufacture more than one shoe per tick
 * @author shiran & nizan
 *
 */
public class ShoeFactoryService extends MicroService{
	private LinkedBlockingQueue<ManufacturingOrderRequest> ManufacturOrder;
	private int counter = 0;
	ManufacturingOrderRequest curr;
	CountDownLatch cdlStart;
	CountDownLatch cdlEnd;
	private static final Logger logger = Logger.getLogger( ManagementService.class.getName() );
	
	public ShoeFactoryService(String name,CountDownLatch cdlStart, CountDownLatch cdlEnd) {
		super(name);
		ManufacturOrder=new LinkedBlockingQueue<ManufacturingOrderRequest>();
		this.cdlStart=cdlStart;
		this.cdlEnd=cdlEnd;
	}

	@Override
	protected void initialize() {
		
		this.subscribeRequest(ManufacturingOrderRequest.class, callback2->{// //subscribes to new manufacture order so it can start make the shoe
			try {
				ManufacturOrder.put(callback2);
			} catch (Exception e) {
				e.printStackTrace();
			}	
		});
		
		this.subscribeBroadcast(TickBroadcast.class, callback1->{  //the factory gets all the ticks so it will know when to start and end manufacture a shoe 
			if (callback1.getCurrentTick()== -1){
				terminate();
				cdlEnd.countDown();
			}	
			
			
			 if (!ManufacturOrder.isEmpty() && counter==0){
				curr = ManufacturOrder.poll(); 
			}
			 
			
			  if (curr!=null && counter<curr.getAmount()+1){// checks if it needs to manufacture more shoes from this kind
				counter++;
				if(counter<curr.getAmount()+1)
					logger.info("one "+curr.getShoeType()+" was made by "+this.getName());
			 }
			   if ( curr!=null && counter==curr.getAmount()+1){
				this.complete(curr, new Receipt(this.getName(), "store", curr.getShoeType(), false,callback1.getCurrentTick() , curr.getRequestTick(),  curr.getAmount())); // sends a complete message to the manager
				logger.info("order for "+curr.getAmount()+" "+curr.getShoeType()+" completed by "+this.getName());
				counter = 0;
				if (!ManufacturOrder.isEmpty()){ // checks if there are more manufacture request, and if there are start handling them. 
					curr = ManufacturOrder.poll();
			}
				else curr=null;
				if (curr!=null && counter<curr.getAmount()+1){
					counter++;
					logger.info("one "+curr.getShoeType()+" was made by "+this.getName());
				}
			}
			
		});
		
		cdlStart.countDown();	
		}
}