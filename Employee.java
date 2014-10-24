import java.util.concurrent.CountDownLatch;
import java.util.Random;

public class Employee extends Thread {
	
	private static final String ARRIVE = "%s\tEmployee %s arrives.";
	private static final String LUNCH_START = "%s\tEmployee %s goes to lunch.";
	private static final String LUNCH_END = "%s\tEmployee %s finishes lunch.";
	
	private String name;
	private Clock clock;
	private CountDownLatch startLatch;
	
	public Employee(String name, Clock clock, CountDownLatch startLatch) {
		this.name = name;
		this.clock = clock;
		this.startLatch = startLatch;
	}

	public void run() {
		Random gen = new Random();

		try {
			// wait until all actors are ready
			startLatch.await();
			
			// arrive between 8:00 and 8:30
			clock.waitFor( new Clock.Time( 8, gen.nextInt( 30 ) ) );
			System.out.println( String.format( ARRIVE, clock.getTime().toString(), name ) );
			
			// go to lunch between 12:00 and 12:30
			//TODO handle when they don't get here until after 12:00 (i.e. they're asking a question)
			int lunchStartMinute = gen.nextInt( 30 );
			clock.waitFor( new Clock.Time( 12, lunchStartMinute ) );
			System.out.println( String.format( LUNCH_START, clock.getTime().toString(), name ) );
			int lunchEndMinute = lunchStartMinute + 30;
			lunchEndMinute += gen.nextInt( 60 - lunchEndMinute );
			clock.waitFor( new Clock.Time( 12, lunchEndMinute ) );
			System.out.println( String.format( LUNCH_END, clock.getTime().toString(), name ) );
			
			// leave between 4:30 and 5:00, after at least 8 hours of work
			//TODO
			
		} catch( InterruptedException e ) {
			e.printStackTrace();
		}
	}
	
}