package bgu.spl.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.impl.MessageBusImpl;
/**
 * 
This micro-service describes one client connected to the web-site. 
The WebsiteClientService expects to get two lists as arguments to its constructor:
purchaseSchedule: - contains purchases that the client needs to make .
The list does not guaranteed to be sorted. 
The WebsiteClientService will make the purchase on the tick specied on the schedule irrelevant of the discount on that item.
wishList:- The client wish list contains name of shoe types that the client will buy only when there is a discount 
on them. Once the client bought a shoe from its wishlist - he removes it from the list.
In order to get notified when new discount is available, the client should subscribe to the NewDiscountBroadcast message. 
If the client finish receiving all its purchases and have nothing in its wishList it must immidiatly terminate.
 * @author nizan & shiran
 *
 */
public class WebClientService extends MicroService {
;
	private ArrayList<String> wishList;
	private int currentTick;
	private ArrayList<PurchaseSchedule> purchaseSchedules;
	int i=0;
	CountDownLatch cdlStart;
	CountDownLatch cdlEnd;
	int howManyPurcahseLeft=0;
	private static final Logger logger = Logger.getLogger( WebClientService.class.getName() );
	
	public WebClientService(String name,ArrayList<PurchaseSchedule> purchaseSchedules, ArrayList<String> wishList,CountDownLatch cdlStart, CountDownLatch cdlEnd) {
		super(name);
		this.cdlStart=cdlStart;
		this.cdlEnd=cdlEnd;
		this.wishList=wishList;
		this.purchaseSchedules=purchaseSchedules;
		howManyPurcahseLeft=purchaseSchedules.size();
	}

    /**
     * starts the event loop of the client
     */
	@Override
	protected void initialize() {
		this.subscribeBroadcast(TickBroadcast.class, callback->{	//the seller gets all the ticks so he will know when to send PurchaseOrderRequest from his purchaseSchedules and when to terminate
			this.currentTick=callback.getCurrentTick();
			if (callback.getCurrentTick()== -1){				
				terminate();
				cdlEnd.countDown();
			}		
			//finds a purchaseSchedule with the same tick as the current and sends a PurchaseOrderRequest for it
			for(i=0;i<purchaseSchedules.size();i++){
				if(callback.getCurrentTick()==purchaseSchedules.get(i).getTick()){
					logger.info(this.getName()+" is sending a PurchaseOrderRequest for "+purchaseSchedules.get(i).getShoeType());
					boolean ans = sendRequest(new PurchaseOrderRequest(this.getName(),false,purchaseSchedules.get(i).getShoeType(), currentTick), (Receipt c)->{
					
						if (c != null){//if the result was receipt forget the purchaseSchedule - this tick passed
							logger.info(this.getName()+" bought a "+ c.getShoeType() );
							howManyPurcahseLeft--;
							
						}
					
						else{//if the result was null forget the purchaseSchedule - this tick passed
							logger.info(this.getName()+" fail to buy "+purchaseSchedules.get(i).getShoeType() );
							howManyPurcahseLeft--;
						}
						
					});
					logger.info(this.getName()+" send a PurchaseOrderRequest for "+purchaseSchedules.get(i).getShoeType() );
				}
				}
			//terminates if only both the wishlist and the purchaseSchedules is empty
				if(wishList.isEmpty()&& howManyPurcahseLeft==0){
					this.terminate();
					cdlEnd.countDown();
				}
		});
		
		
		this.subscribeBroadcast(NewDiscountBroadcast.class, callback->{//subscribes to new discount so he will know if something on his wishlist is on sale
			if(wishList.contains(callback.getShoeOnDiscount())){//for every NewDiscountBroadcast checks if it exists in his wishlist 
				logger.info(this.getName()+" is sending a PurchaseOrderRequest for "+callback.getShoeOnDiscount() );
						sendRequest(new PurchaseOrderRequest(this.getName(),true,callback.getShoeOnDiscount(), currentTick), (Receipt c)->{//if the shoe is in the wishlist send a new PurchaseOrderRequest
								if (c != null){//if the result was receipt remove the shoe from the wishlist;
									wishList.remove(wishList.indexOf(callback.getShoeOnDiscount()));
									logger.info(this.getName()+" bought a "+callback.getShoeOnDiscount() );
								}
						 });
							logger.info(this.getName()+" send a PurchaseOrderRequest for "+callback.getShoeOnDiscount() );
				}
			//terminates if only both the wishlist and the purchaseSchedules is empty
				if(wishList.isEmpty()&&howManyPurcahseLeft==0){
					this.terminate();
					cdlEnd.countDown();
				}	
		});
		
		cdlStart.countDown();
	}

		}

