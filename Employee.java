import java.util.concurrent.CountDownLatch;
import java.util.Random;

public class Employee extends Thread {
	
	private static final String ARRIVE = "%s\tEmployee %s arrives.";
	private static final String LUNCH_START = "%s\tEmployee %s goes to lunch.";
	private static final String LUNCH_END = "%s\tEmployee %s finishes lunch.";
	private static final String DEPART = "%s\tEmployee %s departs after a day of %d hours and %d minutes work.";
	
	private String name;
	private Clock clock;
	private CountDownLatch startLatch;
	
	private int arriveMinute;
	private int lunchDuration;
	
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
			arriveMinute = gen.nextInt( 30 );
			Clock.Time arriveTime = new Clock.Time( 8, arriveMinute );
			clock.waitFor( arriveTime );
			System.out.println( String.format( ARRIVE, arriveTime.toString(), name ) );
			
			// go to lunch between 12:00 and 12:30
			//TODO handle when they don't get here until after 12:00 (i.e. they're asking a question)
			int lunchStartMinute = gen.nextInt( 30 );
			Clock.Time lunchStartTime = new Clock.Time( 12, lunchStartMinute );
			clock.waitFor( lunchStartTime );
			System.out.println( String.format( LUNCH_START, lunchStartTime.toString(), name ) );
			
			int lunchEndMinute = lunchStartMinute + 30;
			lunchEndMinute += gen.nextInt( 30 - arriveMinute );
			lunchEndMinute = Math.min( lunchEndMinute, 59 );
			lunchDuration = lunchEndMinute - lunchStartMinute;
			Clock.Time lunchEndTime = new Clock.Time( 12, lunchEndMinute );
			clock.waitFor( lunchEndTime );
			System.out.println( String.format( LUNCH_END, lunchEndTime.toString(), name ) );
			
			// leave between 4:30 and 5:00, after at least 8 hours of work
			int earliestDeparture = arriveMinute + lunchDuration;
			int departMinute = gen.nextInt( 60 - earliestDeparture ) + earliestDeparture;
			Clock.Time departTime = new Clock.Time( 4, departMinute );
			clock.waitFor( departTime );
			System.out.println( String.format( DEPART, departTime.toString(), name, 8, departMinute - arriveMinute - lunchDuration ) );
			
		} catch( InterruptedException e ) {
			e.printStackTrace();
		}
	}
	
}