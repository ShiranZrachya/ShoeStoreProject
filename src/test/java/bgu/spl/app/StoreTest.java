package bgu.spl.app;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import bgu.spl.mics.impl.MessageBusImpl;

public class StoreTest {

	Store store;
	
	@Before
	public void setUp() throws Exception 
	{
		store= Store.getInstance();
	}

	@Test
	public void testGetInstance() {
		assertSame("Not the Same!!!",store, Store.getInstance());
	}

	@Test
	public void testStore() {
		assertSame("Not the Same!!!",store, Store.getInstance());
	}

	@Test
	public void testLoad() {
		ShoeStorageInfo s1= new ShoeStorageInfo("shoe1" , 1 , 0);
		ShoeStorageInfo s2= new ShoeStorageInfo("shoe2" , 1 , 0);
		store.load(new ShoeStorageInfo[]{s1,s2});
		BuyResult r1 = store.take("shoe1", false);
		BuyResult r2 = store.take("shoe1", false);
		BuyResult r3 = store.take("shoe2", true);
		assertEquals(r1,BuyResult.REGULAR_PRICE);
		assertEquals(r2,BuyResult.NOT_IN_STOCK);
		assertEquals(r3,BuyResult.NOT_ON_DISCOUNT);
	}

	@Test
	public void testTake() {
		ShoeStorageInfo s1= new ShoeStorageInfo("shoe1" , 1, 1);
		ShoeStorageInfo s2= new ShoeStorageInfo("shoe2" , 1 , 0);
		store.load(new ShoeStorageInfo[]{s1,s2});
		BuyResult r1 = store.take("shoe1", false);
		BuyResult r2 = store.take("shoe1", false);
		BuyResult r3 = store.take("shoe2", true);
		BuyResult r4 = store.take("shoe2", false);
		assertEquals(r1,BuyResult.DISCOUNTED_PRICE);
		assertEquals(r2,BuyResult.NOT_IN_STOCK);
		assertEquals(r3,BuyResult.NOT_ON_DISCOUNT);
		assertEquals(r4,BuyResult.REGULAR_PRICE);
	}

	@Test
	public void testAdd() {
		store.add("shoe1", 1);
		BuyResult r1 = store.take("shoe1", false);
		BuyResult r2 = store.take("shoe1", false);
		assertEquals(r1,BuyResult.REGULAR_PRICE);
		assertEquals(r2,BuyResult.NOT_IN_STOCK);
	}

	@Test
	public void testAddDiscount() 
	{
		store.addDiscount("shoe1", 1);
		BuyResult r1 = store.take("shoe1", false);
		assertEquals(r1,BuyResult.NOT_IN_STOCK);
		store.add("shoe1", 2);
		BuyResult r2 = store.take("shoe1", true);
		assertEquals(r2,BuyResult.NOT_ON_DISCOUNT);
		store.addDiscount("shoe1", 1);
		BuyResult r3 = store.take("shoe1", true);
		assertEquals(r3,BuyResult.DISCOUNTED_PRICE);
		BuyResult r4 = store.take("shoe1", true);
		assertEquals(r4,BuyResult.NOT_ON_DISCOUNT);
	}

}
