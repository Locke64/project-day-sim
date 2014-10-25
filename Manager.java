import java.util.concurrent.CountDownLatch;

public class Manager extends Thread {

	private static final String ARRIVE = "%s\tManager arrives.";
	private static final String STANDUP = "%s\tProject standup begins.";
	private static final String ARRIVE_EXE_MEETING = "%s\tManager goes to %s executive meeting.";
	private static final String LEAVE_EXE_MEETING = "%s\tManager finishes %s executive meeting";
	private static final String LUNCH_START = "%s\tManager goes to lunch.";
	private static final String LUNCH_END = "%s\tManager finishes lunch.";
	
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
			clock.nextTime( new Clock.Time( 10, 0 ) );
			System.out.println( String.format( ARRIVE_EXE_MEETING, clock.getTime().toString(), "morning" ) );
			clock.nextTime( new Clock.Time( 11, 0 ) );
			System.out.println( String.format( LEAVE_EXE_MEETING, clock.getTime().toString(), "morning" ) );
			clock.nextTime( new Clock.Time( 12, 0 ) );
			System.out.println( String.format( LUNCH_START, clock.getTime().toString() ) );
			clock.nextTime( new Clock.Time( 1, 0 ) );
			System.out.println( String.format( LUNCH_END, clock.getTime().toString() ) );
			clock.nextTime( new Clock.Time( 2, 0 ) );
			System.out.println( String.format( ARRIVE_EXE_MEETING, clock.getTime().toString(), "afternoon" ) );
			clock.nextTime( new Clock.Time( 3, 0 ) );
			System.out.println( String.format( LEAVE_EXE_MEETING, clock.getTime().toString(), "afternoon" ) );
		} catch( InterruptedException e ) {
			e.printStackTrace();
		}
	}
}