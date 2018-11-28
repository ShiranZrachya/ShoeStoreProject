package bgu.spl.app;

import bgu.spl.mics.Request;

/**
 * a request that is sent when the the store manager want that a
shoe factory will manufacture a shoe for the store. Its response type expected to be a Receipt.
On the case the manufacture was not completed successfully null should be returned as the
request result.
 * @author nizan & shiran
 *
 */
public class ManufacturingOrderRequest implements Request<Receipt> {
	private String shoeType;
	private int amount;
	private int requestTick;
	
	
	/**
	 * 
	 * @param shoeType - the shoe to manufacture
	 * @param amount - the amount to make 
	 * @param requestTick - the tick when the request was send
	 */
	public ManufacturingOrderRequest(String shoeType, int amount, int requestTick){
		this.shoeType =shoeType;
		this.amount = amount;
		this.requestTick = requestTick;
	}
	public String getShoeType() {
		return shoeType;
	}
	
	public int getAmount() {
		return amount;
	}
	public int getRequestTick() {
		return requestTick;
	}

	
}
