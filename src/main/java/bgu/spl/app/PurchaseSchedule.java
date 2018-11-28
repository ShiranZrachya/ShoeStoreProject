package bgu.spl.app;
/**
 * An object which describes a schedule of a single client-purchase at a specific tick.
 * @author nizan & shiran
 *
 */
public class PurchaseSchedule {
	private String shoeType;
	private int tick;
	/**
	 * 
	 * @param shoeType - the type of shoe to purchase.
	 * @param tick - the tick number to send the PurchaseOrderRequest at.
	 */
public PurchaseSchedule(String shoeType, int tick){
	this.shoeType = shoeType;
	this.tick = tick;
}

public String getShoeType() {
	return shoeType;
}

public int getTick() {
	return tick;
}

}
