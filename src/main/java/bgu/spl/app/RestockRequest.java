package bgu.spl.app;

import bgu.spl.mics.Request;

/**
 * a request that is sent by the selling service to the store manager so that he
will know that he need to order new shoes from a factory.
 * @author nizan & shiran
 *
 */
public class RestockRequest implements Request<Boolean>{
	private String shoeType;

	/**
	 * 
	 * @param shoeType - the shoe to order
	 */
	public RestockRequest(String shoeType){
		this.shoeType = shoeType;

	}

	public String getShoeType() {
		return shoeType;
	}



}
