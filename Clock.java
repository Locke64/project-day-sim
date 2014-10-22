public class Clock extends Thread {

	public static final int DAY_START_HOUR = 8;
	public static final int DAY_START_MINUTE = 0;
	public static final int DAY_END_HOUR = 5;
	public static final int DAY_END_MINUTE = 0;
	public static final LocalTime DAY_START = LocalTime.of(DAY_START_HOUR, DAY_START_MINUTE);
	public static final LocalTime DAY_END = LocalTime.of(DAY_END_HOUR, DAY_END_MINUTE);

	private LocalTime time;
	private CountDownLatch startLatch;
	
	public Clock( CountDownLatch startLatch ) {
		this.startLatch = startLatch;
		time = LocalTime.of(DAY_START_HOUR, DAY_START_MINUTE);
	}

	public void run() {
		// wait until all actors are ready
		startLatch.await();
		
		// increment the minute every 10 ms
		while(DAY_END.compareTo(time) > 0) {
			wait(10);
			time = time.plusMinutes(1);
			notifyAll();
		}
	}
	
	public LocalTime getTime() {
		return time;
	}
}