package bgu.spl.json;

import java.util.concurrent.atomic.AtomicInteger;
/** 
 * this class helps us read the json files - it describes a discount schedule- the name of the shoe that is on discount, the amount of this shoe that are on discount 
 * and the tick the shoe is going on discount
 * @author sparzada
 *
 */
public class discountSchedule {
private String shoeType; // the shoe that is on discount
private AtomicInteger amount; // the amount of the shoes that are on discount from this type
private AtomicInteger tick; // the tick that the manager sends the discount message about this shoe


public discountSchedule(String shoeType, AtomicInteger amount, AtomicInteger tick ){
	this.amount = amount;
	this.shoeType = shoeType;
	this.tick = tick;
}


public String getShoeType() {
	return shoeType;
}



public AtomicInteger getAmount() {
	return amount;
}



public AtomicInteger getTick() {
	return tick;
}



}