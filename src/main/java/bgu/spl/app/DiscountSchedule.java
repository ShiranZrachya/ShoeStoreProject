package bgu.spl.app;
/**
 * An object which describes a schedule of a single discount that the manager will add to a specific shoe at a specific tick.	
 * @author nizan & shiran
 *
 */
public class DiscountSchedule {
	private String shoeType;
	private int tick;
	private int amount;
	
/**
 * 
 * @param shoeType - the type of shoe to add discount to.
 * @param tick - the tick number to send the add the discount at.
 * @param amount -  - the amount of items to put on discount
 */
	public DiscountSchedule(String shoeType, int tick, int amount){
		this.shoeType = shoeType;
		this.tick = tick;
		this.amount = amount;
	}

/**
 * shoe type getter
 * @return
 */

	public String getShoeType() {
		return shoeType;
	}
/**
 * tick getter
 * @return
 */
	public int getTick() {
		return tick;
	}
/**
 * amount getter
 * @return
 */
	public int getAmount() {
		return amount;
	}

}

