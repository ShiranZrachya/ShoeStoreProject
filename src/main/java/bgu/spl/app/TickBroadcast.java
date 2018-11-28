package bgu.spl.app;

import bgu.spl.mics.Broadcast;

/**
 * a broadcast messages that is sent at every passed clock tick.
 * @author nizan & shiran
 *
 */
public class TickBroadcast implements Broadcast {
	private int currentTick; 
	
	public TickBroadcast(int currentTick){
		this.currentTick=currentTick;
	}
	public int getCurrentTick() {
		return currentTick;
	}
	
	

}
