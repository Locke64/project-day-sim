import java.util.concurrent.CountDownLatch;

public class Clock extends Thread {

	public static final int DAY_START_HOUR = 8;
	public static final int DAY_START_MINUTE = 0;
	public static final int DAY_END_HOUR = 5;
	public static final int DAY_END_MINUTE = 0;
	public static final Time DAY_START = new Time(DAY_START_HOUR, DAY_START_MINUTE);
	public static final Time DAY_END = new Time(DAY_END_HOUR, DAY_END_MINUTE);

	private Time time;
	private CountDownLatch startLatch;
	
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
				try {
					wait(10);
					time.minute += 1;
					notifyAll();
				} catch( InterruptedException e ) {
					e.printStackTrace();
				}
			}
		} catch( InterruptedException e ) {
			e.printStackTrace();
		}
	}
	
	public Time getTime() {
		return time.copy();
	}
	
	public static class Time {
		public int hour;
		public int minute;
		public Time( int hr, int mn ) {
			hour = hr;
			minute = mn;
		}
		public Time copy() {
			return new Time( hour, minute );
		}
		public int compareTo( Time other ) {
			return this.hour == other.hour ? this.minute - other.minute : this.hour - other.hour;
		}
		public String toString() {
			return hour + ":" + minute;
		}
	}
}