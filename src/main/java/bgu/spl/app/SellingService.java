package bgu.spl.app;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;

/**
 * This micro-service handles PurchaseOrderRequest.
 * When the SellingService receives a Purchase- OrderRequest, it handles it by trying to take the required
 * shoe from the storage. If it succedded it creates a recipt, file it in the store and pass it to the client 
 * If there were no shoes on the requested type on stock, the selling service will send RestockRequest,
 * if the request completed with the value “false”  the SellingService will complete the PurchaseOrderRequest with the value of “null”
 * @author nizan & shiran
 *
 */
public class SellingService extends MicroService {
	int currentTick;
	CountDownLatch cdlStart;
	CountDownLatch cdlEnd;
	private static final Logger logger = Logger.getLogger( SellingService.class.getName() );
	
	public SellingService(String name, CountDownLatch cdlStart,CountDownLatch cdlEnd) {
		super(name);
		this.cdlStart=cdlStart;
		this.cdlEnd=cdlEnd;
		
	}


	 /**
     * starts the event loop of the seller
     */
	protected void initialize() {
		
		this.subscribeBroadcast(TickBroadcast.class, callback->{//the seller gets all the ticks so he will know to terminate at the final one		
			this.currentTick=callback.getCurrentTick();
			if (callback.getCurrentTick()== -1){
				terminate();
				cdlEnd.countDown();
			}
		});
	
		subscribeRequest(PurchaseOrderRequest.class, callback->{
			BuyResult x = Store.getInstance().take(callback.getShoe(), callback.isOnlyDiscount());//takes a shoe and saves the result
			if(x.equals(BuyResult.REGULAR_PRICE)){//if the result is regular price completes the request with a new receipt and file it at the store
				Receipt receipt = new Receipt(getName(), callback.getClient(), callback.getShoe() ,callback.isOnlyDiscount(), currentTick, callback.requestTick, 1);
				Store.getInstance().file(receipt);
				this.complete(callback, receipt);
				logger.info(this.getName()+" sell a "+callback.getShoe()+" to"+ callback.getClient());
			}
			
			if(x.equals(BuyResult.DISCOUNTED_PRICE)){//if the result is discounted price completes the request with a new receipt and file it at the store
				Receipt receipt = new Receipt(getName(), callback.getClient(), callback.getShoe(),true, currentTick, callback.requestTick, 1);
				Store.getInstance().file(receipt);
				this.complete(callback, receipt);
				logger.info(this.getName()+" sell a "+callback.getShoe()+" to"+ callback.getClient());
			}
			
			if(x.equals(BuyResult.NOT_ON_DISCOUNT)){//if the result is not on discount completes the request with null
				this.complete(callback, null);
				logger.info(callback.getShoe()+" is not on discount");
			}
			
			if(x.equals(BuyResult.NOT_IN_STOCK)){//if the result is not in stock and the client wanted the shoe on discount send a restock request 
				logger.info("the shoe "+callback.getShoe()+" is not in stock" );
				if(!callback.isOnlyDiscount()){
					logger.info(this.getName()+" is sending a RestockRequest for "+callback.getShoe() );
					boolean ans = sendRequest(new RestockRequest(callback.getShoe()), (Boolean c)->{
			
						if (c){//if the result of the request was true complete the request with a new receipt and file it at the store
			
							Receipt receipt = new Receipt(getName(), callback.getClient(), callback.getShoe(),callback.isOnlyDiscount(), currentTick, callback.requestTick, 1);
							Store.getInstance().file(receipt);
							complete(callback, receipt);
							logger.info(this.getName()+" sell a "+callback.getShoe()+" to"+callback.getClient() );
							
						}
						else {//if the result of the request was false complete the request with null
							complete(callback, null);	
							logger.info(callback.getShoe()+ " could not be sold");
						}
				});
					if(!ans){//if the request was'nt received complete the request with null
						complete(callback, null);
					}
						
				}
					
				}
		
			
			
		});
		
		cdlStart.countDown();
		
	
		
	}


	
	
}
