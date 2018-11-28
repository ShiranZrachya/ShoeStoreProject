package bgu.spl.json;
/**
 * this class helps us read the json files -it describes a costumer- his name, his wish list and his purchase schedule.
 * @author sparzada
 *
 */

public class customers {
	private String name; // the customer name
	private String[] wishList; // the customer wishlist
	private purchaseSchedule[] purchaseSchedule; // the costumer purchase schedule
	
	
	public String getName() {
		return name;
	}
	
	public String[] getWishList() {
		return wishList;
	}

	public purchaseSchedule[] getPurchaseSchedule() {
		return purchaseSchedule;
	}
	
}

