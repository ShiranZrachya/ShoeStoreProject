package bgu.spl.mics.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.Iterator;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.RequestCompleted;
import sun.misc.resources.Messages;
/**
 * The message-bus is a shared object used for communication between micro-services. It is implemented as a thread-safe singleton
 The message-bus implementation must be thread-safe as it is shared between all the micro-services in the
system.
Subscribing to message types is done by using the required message class. 
The message-bus manages the micro-services queues, it creates a queue for a micro-service using the register method.
 When the micro-service calls the unregister method the message-bus should remove its queue and clean all references related to the micro-service. Once the
queue is created, a micro-service can take the next message in the queue using the await- Message method. This method is blocking - meaning that if no messages are available in the
micro-service queue it should wait until a message became available.
 * @author shiranzr
 *
 */
public class MessageBusImpl implements MessageBus{
	private ConcurrentHashMap<MicroService,LinkedBlockingQueue<Message>> fServices;
	private ConcurrentHashMap<Class<? extends Message>, ArrayList<MicroService>> fMessages;
	private ConcurrentHashMap<Message,MicroService> fRequstedFrom;
	private ConcurrentHashMap<Class<? extends Message>, AtomicInteger> fIndicator;
	private ConcurrentHashMap<MicroService, ArrayList<Class<? extends Message>>> fservicesMessages;
	 private static final Logger logger = Logger.getLogger( MessageBusImpl.class.getName() );
	
	 private static class MessageBusImplHolder {
	        private static MessageBusImpl instance = new MessageBusImpl();
	    }
	    private MessageBusImpl() {
	    	fServices=new ConcurrentHashMap <MicroService,LinkedBlockingQueue<Message>>();
	    	fMessages = new ConcurrentHashMap<Class<? extends Message>, ArrayList<MicroService>>();
	    	fIndicator= new ConcurrentHashMap<Class<? extends Message>, AtomicInteger> ();
	    	fRequstedFrom = new ConcurrentHashMap<Message,MicroService>();
	    	fservicesMessages=new ConcurrentHashMap<MicroService, ArrayList<Class<? extends Message>>>();
	    }
	    public static MessageBusImpl getInstance() {
	        return MessageBusImplHolder.instance;
	    }
	@Override
	public void subscribeRequest(Class<? extends Request> type, MicroService m) {// subscribes a micro service to receive requests of a specific type.
		
		synchronized(type){
		if (fMessages.containsKey(type)){
			fMessages.get(type).add(m);
			fservicesMessages.get(m).add(type);
		}
		else {
			fIndicator.put(type,new AtomicInteger());
			fMessages.put(type, new ArrayList<MicroService>());
			fMessages.get(type).add(m);
			fservicesMessages.get(m).add(type);
		}
		}

	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) { // subscribes a micro service to receive Broadcasts of a specific type.
		synchronized(type){
		if (fMessages.containsKey(type)){
			fMessages.get(type).add(m);
			fservicesMessages.get(m).add(type);
		}
		else {
			fMessages.put(type, new ArrayList<MicroService>());
			fMessages.get(type).add(m);
			fservicesMessages.get(m).add(type);
		}
		}
	}

	@Override
	public <T> void  complete(Request<T> r, T result) { //Notifying the MessageBus that the request  is completed and its result.
		if(  fRequstedFrom.get(r)!=null && fServices.get(fRequstedFrom.get(r))!=null){
			fServices.get(fRequstedFrom.get(r)).add(new RequestCompleted<T>(r, result ));
			fRequstedFrom.remove(r);
		}
	}

	@Override
	public void sendBroadcast(Broadcast b) { //add the broadcast to the message queues of all the micro-services subscribed to this broadcast.
		
		if(fMessages.get(b.getClass())!=null){
		for (int i=0;i<fMessages.get(b.getClass()).size();i++){
			if (fServices.containsKey(fMessages.get(b.getClass()).get(i)))
				fServices.get(fMessages.get(b.getClass()).get(i)).add(b);	
		}
		}
		
	}

	@Override
	public  boolean  sendRequest(Request<?> r, MicroService requester) { //add the Request to the message queue of one of the micro-services subscribed to the request class in a round-robin fashion.
		fRequstedFrom.put(r, requester);
		synchronized(fMessages.get(r.getClass())){
			if ((!(fMessages.get(r.getClass()).isEmpty())) &&  fServices.get(fMessages.get(r.getClass()).get(fIndicator.get(r.getClass()).intValue()))!=null){
				fServices.get(fMessages.get(r.getClass()).get(fIndicator.get(r.getClass()).intValue())).add(r);
				fIndicator.put(r.getClass(),new AtomicInteger((fIndicator.get(r.getClass()).incrementAndGet())%fMessages.get(r.getClass()).size())); // keeps the last micro service's worked place in the list
				return true;
			}
		}
		return false;
		}
		  
	

		 

	@Override
	public void register(MicroService m) { //allocates a message-queue for the micro service.
		fServices.put(m, new LinkedBlockingQueue<Message>());
		fservicesMessages.put(m,new ArrayList());
		
		
	
		
	}

	@Override
	public void unregister(MicroService m) { //remove the message queue allocated to the micro service and clean all references related to the micro in this message-bus. If the micro service was not registered, nothing should happen.
		synchronized(m){
		fServices.remove(m);
		for(Class<? extends Message> message : fservicesMessages.get(m)){
			if(fIndicator.get(message)!=null && fMessages.get(message)!=null){
			if(fIndicator.get(message).intValue()> fMessages.get(message).indexOf(m)){
				fIndicator.put(message,new AtomicInteger((fIndicator.get(message).decrementAndGet())%fMessages.get(message).size()));
			}
			fMessages.get(message).remove(m);
			}
		
			
		}

		}
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		if(!fServices.containsKey(m)){
			throw new IllegalStateException(" is not registerd ");}
		return fServices.get(m).take();
	}
}