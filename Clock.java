import java.util.concurrent.CountDownLatch;
import java.util.List;
import java.util.ArrayList;

public class Clock extends Thread {

	public static final int DAY_START_HOUR = 8;
	public static final int DAY_START_MINUTE = 0;
	public static final int DAY_END_HOUR = 5;
	public static final int DAY_END_MINUTE = 0;
	public static final Time DAY_START = new Time(DAY_START_HOUR, DAY_START_MINUTE);
	public static final Time DAY_END = new Time(DAY_END_HOUR, DAY_END_MINUTE);

	private Time time;
	private CountDownLatch startLatch;
	private List<CountDownLatch> timers = new ArrayList<CountDownLatch>();
	
	public Clock( CountDownLatch startLatch ) {
		this.startLatch = startLatch;
		time = DAY_START.copy();
	}

	public void run() {
		try {
			// wait until all actors are ready
			startLatch.await();
		
			// increment the minute every 10 ms
			while(DAY_END.compareTo(time) > 0) {
				incrementTime();
			}
		} catch( InterruptedException e ) {
			e.printStackTrace();
		}
	}
	
	private synchronized void incrementTime() {
		try {
			wait(10);
			time.increment();
			for( CountDownLatch timer : timers ) {
				timer.countDown();
			}
			notifyAll();
		} catch( InterruptedException e ) {
			e.printStackTrace();
		}
	}
	
	public synchronized Time getTime() {
		return time.copy();
	}
	
	public void waitFor( Time time ) {
		CountDownLatch timer = new CountDownLatch( time.compareTo( getTime() ) );
		timers.add( timer );
		try {
			timer.await();
		} catch( InterruptedException e ) {
			e.printStackTrace();
		}
	}
	
	public static class Time {
		public int hour;
		public int minute;
		public Time( int hr, int mn ) {
			hour = hr;
			minute = mn;
		}
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
		public synchronized Time copy() {
			return new Time( hour, minute );
		}
		public synchronized int compareTo( Time other ) {
			int mymins = (this.hour >= 8 ? this.hour - 8 : this.hour + 4) * 60 + this.minute;
			int othermins = (other.hour >= 8 ? other.hour - 8 : other.hour + 4) * 60 + other.minute;
			return mymins - othermins;
		}
		public synchronized String toString() {
			return hour + ":" + minute;
		}
	}
}