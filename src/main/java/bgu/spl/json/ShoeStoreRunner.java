package bgu.spl.json;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import bgu.spl.app.DiscountSchedule;
import bgu.spl.app.ManagementService;
import bgu.spl.app.PurchaseSchedule;
import bgu.spl.app.SellingService;
import bgu.spl.app.ShoeFactoryService;
import bgu.spl.app.ShoeStorageInfo;
import bgu.spl.app.Store;
import bgu.spl.app.TimeService;
import bgu.spl.app.WebClientService;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.impl.MessageBusImpl;

public class ShoeStoreRunner {

	/**
	 * When started, this class should accept as argument (command line argument) the name of the json input file to read . The
ShoeStoreRunner should read the input file (using Gson), it then should add the initial storage to
the store and create and start the micro-services. When the current tick number is larger than the
duration given to the TimeService in the input file all the micro-services should gracefully terminate themselves
	 * @param args
	 */
	
	public static void main(String[] args){
		Gson gson =new GsonBuilder().create();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader (args[0]));
		} catch (FileNotFoundException e) {
		
			e.printStackTrace();
		}
		
		Json json = gson.fromJson(reader, Json.class);
		
		
		Store store = Store.getInstance();
		MessageBus messageBus = MessageBusImpl.getInstance();
		
		for(int i = 0; i<json.getInitialStorage().length; i++){
			store.add(json.getInitialStorage()[i].getShoeType(), json.getInitialStorage()[i].getAmount());
			}
		ShoeStorageInfo[] storage = new ShoeStorageInfo[json.getInitialStorage().length];
		for(int i = 0;i<json.getInitialStorage().length; i++){ // make an array of shoeStorageInfo to load the store
			storage[i] = new ShoeStorageInfo(json.getInitialStorage()[i].getShoeType(), json.getInitialStorage()[i].getAmount(),0);
		}
		store.load(storage);// loads the shoes to the store
		
		WebClientService[] webClientService=new  WebClientService[json.getServices().getCustomers().length];
		SellingService[] sellingService=new SellingService[json.getServices().getSellers().intValue()];
		ShoeFactoryService[] shoeFactoryService=new ShoeFactoryService[json.getServices().getFactories().intValue()];
		int numberOfThreads=webClientService.length + shoeFactoryService.length + sellingService.length + 2;
		CountDownLatch  start=new CountDownLatch(numberOfThreads-1);
		CountDownLatch end=new CountDownLatch(numberOfThreads);
		
		
		//ExecutorService
		ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
				
				
		//WebClientService
		
		for(int i=0;i<json.getServices().getCustomers().length;i++){
			ArrayList<PurchaseSchedule> tmp=new ArrayList<PurchaseSchedule> ();
			for(int j=0;j<json.getServices().getCustomers()[i].getPurchaseSchedule().length;j++){ // make a list of the purchase scheule for a specific client
				PurchaseSchedule purchaseSchedule=new PurchaseSchedule(json.getServices().getCustomers()[i].getPurchaseSchedule()[j].getShoeType(),json.getServices().getCustomers()[i].getPurchaseSchedule()[j].getTick().intValue());
				tmp.add(purchaseSchedule);
			}
			ArrayList<String> tmpWish=new ArrayList();
			for(int j=0;j<json.getServices().getCustomers()[i].getWishList().length;j++){// make a list of the wish list for a specific client
				tmpWish.add(json.getServices().getCustomers()[i].getWishList()[j]);
			}
			webClientService[i]= new WebClientService(json.getServices().getCustomers()[i].getName(),tmp,tmpWish, start, end); // create a web client service
			executor.execute(webClientService[i]);
		
	}
		//ManagementService
		List<DiscountSchedule> tmp=new ArrayList();
		if(json.getServices().getManager()!=null)
		if(json.getServices().getManager().getDiscountSchedule()!=null){
		for(int i=0;i<json.getServices().getManager().getDiscountSchedule().length;i++){ 
			DiscountSchedule discountSchedule=new DiscountSchedule(json.getServices().getManager().getDiscountSchedule()[i].getShoeType(),json.getServices().getManager().getDiscountSchedule()[i].getTick().intValue(),json.getServices().getManager().getDiscountSchedule()[i].getAmount().intValue());// make a discount massage
			tmp.add(discountSchedule);// adds the discount message to the discount schedule
		}
		}
		ManagementService managementService=new ManagementService(tmp, start, end); //create the manager
		executor.execute(managementService);
		
		//ShoeFactoryService
		for(int i=0;i<shoeFactoryService.length;i++){ // 
			shoeFactoryService[i]=new ShoeFactoryService("Factory"+i, start, end); // create a factory
			executor.execute(shoeFactoryService[i]);
		}
		
		//SellingService
		for(int i=0;i<sellingService.length;i++){ //create a seller
			sellingService[i]=new SellingService("Seller"+i, start, end);
			executor.execute(sellingService[i]);
		}
		
		//TimeService
		TimeService timeService	=new TimeService("timer",json.getServices().getTime().getSpeed().intValue(),json.getServices().getTime().getDuration().intValue(), start,end); // create the time sevice
		executor.execute(timeService);
		
		
		try {
			end.await();
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		store.print();
		executor.shutdown();
	
		
}
		
}