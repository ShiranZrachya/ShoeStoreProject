package bgu.spl.json;

import java.util.concurrent.atomic.AtomicInteger;
/** 
 * this class helps us read the json files - it describes the timer
 * its speed and the duration time.
 * @author sparzada
 *
 */
public class time {
private AtomicInteger speed; // the speed of the timer
private AtomicInteger duration; // the duration time of the timer

public AtomicInteger getSpeed() {
	return speed;
}

public AtomicInteger getDuration() {
	return duration;
}

}
