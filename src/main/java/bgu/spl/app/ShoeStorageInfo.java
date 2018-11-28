package bgu.spl.app;
/**
 * 
An object which represents information about a single type of shoe in the store
 * @author nizan & shiran
 *
 */
public class ShoeStorageInfo {
	private String shoeType;
	private int amountOnStorage;
	private int discountedAmount;

	/**
	 * 
	 * @param shoeType - the shoe name
	 * @param amountOnStorage - how many of this shoe is in storage
	 * @param discountedAmount - how many of this shoe is on discount
	 */
public ShoeStorageInfo(String shoeType, int amountOnStorage,int discountedAmount ){
	this.shoeType = shoeType;
	this.amountOnStorage = amountOnStorage;
	this.discountedAmount = discountedAmount;
}
public String getShoeType(){
	return shoeType;
	
}
public int getAmountOnStorage(){
	return amountOnStorage;
}
public int getDiscountedAmount(){
	return discountedAmount;
}
public void setAmountOnStorage(int x){
	amountOnStorage = amountOnStorage+x;
}
public void setDiscountedAmount(int x){
	discountedAmount = discountedAmount+x;
}

}
