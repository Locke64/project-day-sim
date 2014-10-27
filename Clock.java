import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

// Clock simulates the passage of time.
public class Clock extends Thread {

	// constants
	public static final int DAY_START_HOUR = 8;
	public static final int DAY_START_MINUTE = 0;
	public static final int DAY_END_HOUR = 5;
	public static final int DAY_END_MINUTE = 0;
	public static final Time DAY_START = new Time(DAY_START_HOUR, DAY_START_MINUTE);
	public static final Time DAY_END = new Time(DAY_END_HOUR, DAY_END_MINUTE);

	// The current time
	private Time time;
	
	// Latch for beginning the day
	private CountDownLatch clockinLatch;
	
	// Barrier for ending the day
	private CyclicBarrier clockoutBarrier;
	
	// List of timers
	private List<CountDownLatch> timers = Collections.synchronizedList( new ArrayList<CountDownLatch>() );
	
	// Factory method for convenient creation of Time objects
	public static Time timeOf( int hr, int mn ) {
		return new Time( hr, mn );
	}
	
	// Creates the Clock
	public Clock( CountDownLatch clockin, CyclicBarrier clockout ) {
		clockinLatch = clockin;
		clockoutBarrier = clockout;
		time = DAY_START.copy();
	}
	
	// Runs the clock - one simulated minute every 10 real milliseconds
	public void run() {
		clockIn();
		
		while( getTime().compareTo( DAY_END ) < 0 ) {
			try {
				// increment the time
				synchronized( this ) {
					wait(10);
					time.increment();
				}
				
				// decrement the counters
				synchronized( timers ) {
					for( CountDownLatch timer : timers )
						timer.countDown();
				}
			} catch( InterruptedException e ) {
				e.printStackTrace();
			}
		}
		
		clockOut();
	}
	
	// Gets the current simulated time
	public synchronized Time getTime() {
		return time.copy();
	}
	
	// wait for given number of minutes
	public void waitFor( int minutes ) {
		CountDownLatch timer = new CountDownLatch( minutes );
		timers.add( timer );
		try {
			timer.await();
		} catch( InterruptedException e ) {
			e.printStackTrace();
		}
	}
	
	// wait until the given time
	public void waitUntil( Time until ) {
		if( until.compareTo( getTime() ) > 0 )
			waitFor( until.compareTo( getTime() ) );
	}
	
	// wait until the day starts
	public void clockIn() {
		try {
			clockinLatch.await();
		} catch( InterruptedException e ) {
			e.printStackTrace();
		}
	}
	
	// ready for day to end
	public void clockOut() {
		try {
			clockoutBarrier.await();
		} catch( InterruptedException e ) {
			e.printStackTrace();
		} catch( BrokenBarrierException e ) {
			e.printStackTrace();
		}
	}
	
	// Simple data structure for Time, composed of hours and minutes.
	//TODO this does not include am/pm - that is assumed based on whether hour is >=8 (am) or <=5 (pm). Should am/pm be added?
	public static class Time {
	
		// The data. It's public for lightweight access.
		public int hour;
		public int minute;
		
		// Creates a new Time object with the given hours and minutes
		public Time( int hr, int mn ) {
			hour = hr;
			minute = mn;
		}
		
		// Adds one minute, performing appropriate wraparounds
		public synchronized void increment() {
			minute += 1;
			if( minute >= 60 ) {
				minute = 0;
				hour += 1;
				if( hour >= 13 ) {
					hour = 1;
				}
			}
		}
		
		// Creates a new copy of the Time object with the same hours and minutes
		public synchronized Time copy() {
			return new Time( hour, minute );
		}
		
		// Compares this Time to another, returning the difference in minutes
		// Negative if this time is less than (before) the other time,
		// Positive if this time is greater than (after) the other time,
		// 0 if the times are equal
		public synchronized int compareTo( Time other ) {
			int mymins = (this.hour >= 8 ? this.hour - 8 : this.hour + 4) * 60 + this.minute;
			int othermins = (other.hour >= 8 ? other.hour - 8 : other.hour + 4) * 60 + other.minute;
			return mymins - othermins;
		}
		
		// Prints the time in the form h:mm
		public synchronized String toString() {
			if (minute < 10) {
				return hour + ":0" + minute;
			} else {
				return hour + ":" + minute;
			}
		}
	}
}