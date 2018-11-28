package bgu.spl.app;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.impl.MessageBusImpl;
/**
 * 
This micro-service is our global system timer (handles the clock ticks in the system). 
It is responsible for counting how much clock ticks passed since the beggining of its execution and notifying every other microservice  about it using the TickBroadcast.
The TimeService receives the number of milliseconds each clock tick takes (speed:int) toogether with the number of ticks before termination (duration:int) as a constructor arguments.
 * @author nizan & shiran
 *
 */
public class TimeService extends  MicroService{
	private Timer timer;
	private int speed;
	private int duration;
	private int counter;
	private CountDownLatch cdlStart;
	private CountDownLatch cdlEnd;
	private static final Logger logger = Logger.getLogger( MicroService.class.getName() );
	
	public TimeService(String name, int speed, int duration,CountDownLatch cdlStart,CountDownLatch cdlEnd) {
		super("timer");
		timer = new Timer();
		this.speed = speed;
		this.duration = duration;
		counter = 1;
		this.cdlStart=cdlStart;
		this.cdlEnd=cdlEnd;
	}

	 /**
     * starts the event loop of the timer
     */
	protected void initialize() {
	try {
		cdlStart.await();
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
	
	
//starts a new TimerTask and schedule it with the given speed and 0 delay
	TimerTask task = new TimerTask(){

		public void run() {		//the timer task will send information about the current tick to the subscribed services	
			
			if (counter>duration){//if this is the final tick send the broadcast with -1 so they will know to terminate
				int i=-1;
				sendBroadcast(new TickBroadcast(new Integer(i)));	
			}
			else{	//send information about the current tick to the subscribed services			
				TickBroadcast tick=  new TickBroadcast(counter);
				logger.info("the current tick is "+ tick.getCurrentTick());
				sendBroadcast(tick);
				counter = counter+1;
			}
		}
			
			
	};
	timer.schedule(task,0, speed);
	this.subscribeBroadcast(TickBroadcast.class, callback->{//subscribe to itself to know when its time to cancel the timer task and to terminate
		if (callback.getCurrentTick()==-1){
			timer.cancel();
			this.terminate();
			logger.info("--------tick end---------");
			cdlEnd.countDown();
			task.cancel();
		}
	});
	
	}
	
	public int getCurrentTick(){
		return counter;
	}
	public int getDuration(){
		return duration;
	}

}

