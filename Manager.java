import java.util.concurrent.CountDownLatch;

public class Manager extends Thread {

	private static final String ARRIVE = "%s Manager arrives.";
	private static final String STANDUP = "%s Project standup begins.";
	
	private Clock clock;
	private CountDownLatch startLatch;
	private int workTime = 0;
	
	public Manager( Clock clock, CountDownLatch startLatch ) {
		this.clock = clock;
		this.startLatch = startLatch;
	}

	public void run() {
		try {
			// wait until all actors are ready
			startLatch.await();
			
			// arrive at 8:00
			System.out.println( String.format( ARRIVE, clock.getTime().toString() ) );
		} catch( InterruptedException e ) {
			e.printStackTrace();
		}
	}
}