package bgu.spl.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import bgu.spl.mics.MicroService;
/**
 * 
 * 
The store object holds a collection of ShoeStorageInfo: One for each shoe type the store offers.
 In addition, it contains a list of receipts issued to and by the store.
 the store is implemented as a thread safe singleton

 * @author nizan & shiran
 *
 */
public class Store {
	private ConcurrentHashMap<String ,ShoeStorageInfo> finfo;
	private ArrayList<Receipt> receipts;
	private BuyResult BuyResult;
	private static final Logger logger = Logger.getLogger( Store.class.getName() );

	
	private static class StoreHolder {
        private static Store instance = new Store();
    }
    private Store() {
    	finfo = new ConcurrentHashMap<String ,ShoeStorageInfo> ();
    	receipts = new ArrayList<Receipt>();
    }
    public static Store getInstance() {
        return StoreHolder.instance;
    }
    /**
     * This method should be called in order to initialize the store storage before starting an execution.
     *  The method will add the items in the given array to the store.
     * @param storage
     */
    public void load(ShoeStorageInfo[] storage){
    	for(int i = 0; i<storage.length; i++){
    		finfo.put(storage[i].getShoeType(), storage[i]);
    	}
    }
    /**
     * This method will attempt to take a single showType from the store. 
     * It receives the shoeType to take and a boolean - onlyDiscount which indicates that the caller wish to take the item only if it is in discount. 
     * Its result is an enum which have the following values:
 		NOT_IN_STOCK: which indicates that there were no shoe of this type in stock (the store storage should not be changed in this case)
		NOT_ON_DISCOUNT: which indicates that the "onlyDiscount" was true and there are no discounted shoes with the requested type.
		REGULAR_PRICE: which means that the item was successfully taken (the amount of items of this type was reduced by one)
		DISCOUNTED_PRICE: which means that was successfully taken in a discounted price (the amount of items of this type was reduced by one and the amount of discounted items reduced by one)
     * @param shoeType
     * @param onlyDiscount
     * @return BuyResult
     */
    public BuyResult take(String shoeType, boolean onlyDiscount){
    	
    		if(finfo.containsKey(shoeType)){ //if the store knows this shoe type
    			if (finfo.get(shoeType).getAmountOnStorage()==0){return BuyResult.NOT_IN_STOCK;}//returns NOT_IN_STOCK if the amount in storage is 0
    			else{ 
    				if (finfo.get(shoeType).getDiscountedAmount()==0 && onlyDiscount){return BuyResult.NOT_ON_DISCOUNT;}//returns NOT_ON_DISCOUNT if the discounted amount is 0 and the client wanted the shoe only on discount
    			    else{ //returns REGULAR_PRICE if the discounted amount is 0 and remove one shoe from the storage
    			    	if (finfo.get(shoeType).getDiscountedAmount()==0){
    			    		finfo.get(shoeType).setAmountOnStorage(-1);
    			    		return BuyResult.REGULAR_PRICE;
    			    	}	
    			    	else{//returns DISCOUNTED_PRICE if the shoe exists on discount
    			    		finfo.get(shoeType).setAmountOnStorage(-1);
    			    		finfo.get(shoeType).setDiscountedAmount(-1);
    			    		return BuyResult.DISCOUNTED_PRICE;
    			    	}
    			    }		
    			}
    		}	
    	return BuyResult.NOT_IN_STOCK;
    }
    /**
     * This method adds the given amount to the ShoeStorageInfo of the given shoeType.
     * @param shoeType
     * @param amount
     */
    public void add(String shoeType, int amount){
    	ShoeStorageInfo newShoe;
   
    	if(!finfo.containsKey(shoeType)){//if the shoe doesn't exists create a new one 
    		newShoe= new ShoeStorageInfo(shoeType, amount, 0 );
    		finfo.put(newShoe.getShoeType(), newShoe);
    	}
    	else{
    		finfo.get(shoeType).setAmountOnStorage(amount);//if the shoe exists set the amount on storage
    	}
    		
    }
    	
    
    
   /**
    * Adds the given amount to the corresponding ShoeStorageInfo’s discountedAmount field.
    * @param shoeType
    * @param amount
    */
    public void addDiscount(String shoeType, int amount){
    	ShoeStorageInfo newShoe;
    	if(!finfo.containsKey(shoeType)){//if the shoe doesn't exists create a new one 
    		newShoe= new ShoeStorageInfo(shoeType, 0, 0);
    		finfo.put(newShoe.getShoeType(), newShoe);
    	}
    	else{
    		if (finfo.get(shoeType).getAmountOnStorage()!=0)
    			finfo.get(shoeType).setDiscountedAmount(amount);//if the shoe exists set the amount on discount
    	}
    		
    	
    }
    /**
     * Save the given receipt in the store.
     * @param receipt
     */
    public void file (Receipt receipt){
    	receipts.add(receipt);
    }
    /**
     * This method prints to the standard output the following information:
		For each item on stock - its name, amount and discountedAmount
		For each receipt filed in the store - all its fields
     */
    public void print (){
    	System.out.println("storage information: ");
    	for (String shoe : finfo.keySet()){
    		System.out.println("shoe: "+shoe+", "+"amount: "+finfo.get(shoe).getAmountOnStorage()+", "+"amount on discount: "+finfo.get(shoe).getDiscountedAmount());
    	
    	}
    	System.out.println("receipts information: ");
    	for(Receipt receipt : receipts){
    		System.out.println("seller: "+receipt.getSeller()+", "+ " amount sold: "+ receipt.getAmountSold()+", "+" type of shoe: "+receipt.getShoeType()+", "+" client: " + receipt.getCustomer() +", "+" Tick: " +  receipt.getIssuedTick() +", "+" discount: "+receipt.isDiscount());
    	
    	}
    }
}
