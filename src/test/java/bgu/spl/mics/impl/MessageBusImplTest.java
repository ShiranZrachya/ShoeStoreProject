package bgu.spl.mics.impl;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import bgu.spl.app.PurchaseOrderRequest;
import bgu.spl.app.RestockRequest;
import bgu.spl.app.SellingService;
import bgu.spl.app.TickBroadcast;
import bgu.spl.mics.Broadcast;
import bgu.spl.mics.MicroService;

public class MessageBusImplTest {

	private MessageBusImpl mbi;
	
	
	@Before
	public void setUp() throws Exception {
		 mbi = MessageBusImpl.getInstance();
	}

	@Test
	public void testGetInstance() {
		assertSame("Not the Same!",mbi, MessageBusImpl.getInstance());
	}

	@Test
	public void testSubscribeRequest() {
		MicroService m1= new MicroService("m1") {
			@Override
			protected void initialize() {}
		};
		MicroService m2= new MicroService("m2") {
			@Override
			protected void initialize() {}
		};
		mbi.register(m1);
		mbi.subscribeRequest(RestockRequest.class, m1);
		RestockRequest request= new RestockRequest("shoe");
		assertTrue(mbi.sendRequest(request, m2));	
		mbi.unregister(m1);
		
	}

	@Test
	public void testSubscribeBroadcast() {
		MicroService m1= new MicroService("m1") {
			@Override
			protected void initialize() {}
		};
		mbi.subscribeBroadcast(TickBroadcast.class, m1);
	}

	@Test
	public void testComplete() {
		MicroService m1= new MicroService("m1") {
			@Override
			protected void initialize() {}
		};
		MicroService m2= new MicroService("m2") {
			@Override
			protected void initialize() {}
		};
		RestockRequest request= new RestockRequest("shoe");
		mbi.register(m1);
		mbi.register(m2);
		mbi.subscribeRequest(request.getClass(), m1);
		mbi.sendRequest(request, m2);
		mbi.complete(request, false);
		mbi.unregister(m1);
		mbi.unregister(m2);
	
	}

	@Test
	public void testSendBroadcast() {
		MicroService m2= new MicroService("m2") {
			@Override
			protected void initialize() {}
		};
		TickBroadcast tb= new TickBroadcast(2);
		mbi.subscribeBroadcast(tb.getClass(), m2);
		mbi.sendBroadcast(tb);
	}

	@Test
	public void testSendRequest() {
		MicroService m1= new MicroService("m1") {
			@Override
			protected void initialize() {}
		};
		MicroService m2= new MicroService("m2") {
			@Override
			protected void initialize() {}
		};
		mbi.register(m1);
		mbi.subscribeRequest(RestockRequest.class, m1);
		RestockRequest request= new RestockRequest("shoe");
		assertTrue(mbi.sendRequest(request, m2));
		mbi.unregister(m1);
		
	}

	@Test
	public void testRegister() {
		MicroService m1= new MicroService("m1") {
			@Override
			protected void initialize() {}
		};
		MicroService m2= new MicroService("m2") {
			@Override
			protected void initialize() {}
		};
		mbi.register(m1);
		mbi.subscribeRequest(RestockRequest.class, m1);
		RestockRequest request= new RestockRequest("shoe");
		assertTrue(mbi.sendRequest(request, m2));
		mbi.unregister(m1);
	}

	@Test
	public void testUnregister() {
		MicroService m1= new MicroService("m1") {
			
			protected void initialize() {}
		};
		MicroService m2= new MicroService("m2") {
			
			protected void initialize() {}
		};
		
		mbi.register(m1);
		mbi.register(m2);
		PurchaseOrderRequest request= new PurchaseOrderRequest("m1", false, "shoe", 3);
		mbi.subscribeRequest(request.getClass(), m1);
		mbi.unregister(m1);
		assertFalse(mbi.sendRequest(request, m2));
		mbi.unregister(m2);
		
	
	}

	@Test
	public void testAwaitMessage() {
		MicroService m1= new MicroService("m1") {
			@Override
			protected void initialize() {}
		};
		TickBroadcast tb= new TickBroadcast(3);
		mbi.register(m1);
		mbi.subscribeBroadcast(tb.getClass(), m1);
		mbi.sendBroadcast(tb);
		try {
			assertTrue(mbi.awaitMessage(m1).equals(tb));
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		mbi.unregister(m1);
		
	}

}

