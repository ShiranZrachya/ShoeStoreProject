package bgu.spl.json;
/**
 * this class helps us read the json files - it describes a specific shoe
 * its name and the amount on storage
 * @author sparzada
 *
 */
public class shoeType {
	private String shoeType; // shoe type
	private int amount; // the amount of shoes from this kinf in the store

	
	public shoeType(String shoeType, int amount){
		this.shoeType = shoeType;
		this.amount = amount;
	}

	public String getShoeType() {
		return shoeType;
	}

	public int getAmount() {
		return amount;
	}

}