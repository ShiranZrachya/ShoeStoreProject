package bgu.spl.json;

import java.util.concurrent.atomic.AtomicInteger;
/**
 * this class helps us read the json files - it describes a purchase schedule- 
 * the shoe type a costumer wants and the tick he wants to get them.
 * @author sparzada
 *
 */
public class purchaseSchedule {
	private String shoeType; // the shoe type that the client want to buy
	private AtomicInteger tick; // the tick the client want to buy a specific shoe
	
	public String getShoeType() {
		return shoeType;
	}
	
	public AtomicInteger getTick() {
		return tick;
	}
	
}
