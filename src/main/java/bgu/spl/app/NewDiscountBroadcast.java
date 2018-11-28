package bgu.spl.app;

import bgu.spl.mics.Broadcast;

/**
 * a broadcast message that is sent when the manager of the store
decides to have a sale on a specific shoe.
 * @author nizan & shiran
 *
 */
public class NewDiscountBroadcast implements Broadcast {
	String shoeOnDiscount;
	/**
	 * 
	 * @param shoe - the discounted shoe
	 */
	public NewDiscountBroadcast(String shoe){
		this.shoeOnDiscount=shoe;
	}
	
	public String getShoeOnDiscount(){
		return shoeOnDiscount;
	}

}
