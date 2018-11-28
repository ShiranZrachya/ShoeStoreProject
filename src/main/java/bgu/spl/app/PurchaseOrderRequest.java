package bgu.spl.app;
import bgu.spl.mics.Request;
/**
 * a request that is sent when the a store client wish to buy a shoe.
Its response type expected to be a Receipt.  On the case the purchase was not completed
successfully null should be returned as the request result
 * @author nizan & shiran
 *
 */

public class PurchaseOrderRequest implements Request<Receipt> {
	private String client;
	private boolean onlyDiscount;
	private String shoe;
	int requestTick;
	/**
	 * 
	 * @param client - the requesting client
	 * @param onlyDiscount - if the client wants the shoe only if its on discount
	 * @param shoe - the discounted shoe
	 * @param requestTick - the tick of the request
	 */
	public PurchaseOrderRequest(String client,boolean onlyDiscount,String shoe, int requestTick){
		this.client= client;
		this.onlyDiscount=onlyDiscount;
		this.shoe=shoe;
		this.requestTick = requestTick;
		
	}
	public String getClient(){
		return client;
	}
	public boolean isOnlyDiscount() {
		return onlyDiscount;
	}
	public String getShoe() {
		return shoe;
	}
	public int getRequestTick(){
		return requestTick;
	}
	
	
}
