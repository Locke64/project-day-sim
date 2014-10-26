import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class Manager extends Thread {

	private static final String ARRIVE = "%s\tManager arrives.";
	private static final String DEPART = "%s\tManager departs after a day of %d hours and %d minutes work.";
	private static final String STANDUP = "%s\tProject standup begins.";
	private static final String START_LEAD_MEETING = "%s\tManager begins standup meeting with team leads.";
	private static final String END_LEAD_MEETING = "%s\tManager ends standup meeting with team leads";
	private static final String ARRIVE_EXE_MEETING = "%s\tManager goes to %s executive meeting.";
	private static final String LEAVE_EXE_MEETING = "%s\tManager finishes %s executive meeting";
	private static final String LUNCH_START = "%s\tManager goes to lunch.";
	private static final String LUNCH_END = "%s\tManager finishes lunch.";
	private static final String HEAR_QUESTION = "%s\tManager hears employee %s's question.";
	private static final String ANSWER_QUESTION = "%s\tManager answers employee %s's question.";
	
	private Clock clock;
	private int workTime = 0;
	
	// lock for asking questions
	private boolean busy = false;
	
	// barrier for morning standup
	private CyclicBarrier standupBarrier;
	
	// latch for ending the morning standup
	private CountDownLatch standupLatch;
	
	public Manager( Clock clock ) {
		this.clock = clock;
		this.standupBarrier = new CyclicBarrier( 4 );
		this.standupLatch = new CountDownLatch( 1 );
	}
	
	public void run() {
		// arrive
		System.out.println( String.format( ARRIVE, clock.getTime().toString() ) );
		
		// team lead standup
		morningStandup();
		
		//TODO finish answering one question if asked; the rest have to wait
		clock.waitUntil( Clock.timeOf( 10, 0 ) );
		morningExecMeeting();
		
		// lunch
		//TODO finish answering one question if asked; the rest have to wait
		clock.waitUntil( Clock.timeOf( 12, 0 ) );
		busy = true;
		System.out.println( String.format( LUNCH_START, clock.getTime().toString() ) );
		clock.waitUntil( Clock.timeOf( 1, 0 ) );
		System.out.println( String.format( LUNCH_END, clock.getTime().toString() ) );
		busy = false;
		
		// afternoon executive meeting
		//TODO finish answering one question if asked; the rest have to wait
		clock.waitUntil( Clock.timeOf( 2, 0 ) );
		afternoonExecMeeting();
		
		//TODO 4:00 meeting
		
		Clock.Time departTime = Clock.timeOf( 5, 0 );
		clock.waitUntil( departTime );
		System.out.println( String.format( DEPART, departTime, 8, 0 ) ); //TODO calculate working time
	}
	
	private synchronized void morningStandup() {
		busy = true;
		try {
			standupBarrier.await();
		} catch( InterruptedException e ) {
			e.printStackTrace();
		} catch( BrokenBarrierException e ) {
			e.printStackTrace();
		}
		System.out.println( String.format( START_LEAD_MEETING, clock.getTime().toString() ) );
		clock.waitFor( 15 );
		System.out.println( String.format( END_LEAD_MEETING, clock.getTime().toString() ) );
		standupLatch.countDown();
		busy = false;
		notifyAll();
	}
	
	// morning executive meeting
	private synchronized void morningExecMeeting() {
		busy = true;
		System.out.println( String.format( ARRIVE_EXE_MEETING, clock.getTime().toString(), "morning" ) );
		clock.waitUntil( Clock.timeOf( 11, 0 ) );
		System.out.println( String.format( LEAVE_EXE_MEETING, clock.getTime().toString(), "morning" ) );
		busy = false;
		notifyAll();
	}
	
	// afternoon executive meeting
	private synchronized void afternoonExecMeeting() {
		busy = true;
		System.out.println( String.format( ARRIVE_EXE_MEETING, clock.getTime().toString(), "afternoon" ) );
		clock.waitUntil( Clock.timeOf( 3, 0 ) );
		System.out.println( String.format( LEAVE_EXE_MEETING, clock.getTime().toString(), "afternoon" ) );
		busy = false;
		notifyAll();
	}
	
	// report to the manager for the daily project standup meeting
	public void reportForStandup(){
		try {
			standupBarrier.await(); // report ready to begin
			standupLatch.await(); // wait for meeting to end
		} catch( InterruptedException e ) {
			e.printStackTrace();
		} catch( BrokenBarrierException e ) {
			e.printStackTrace();
		}
	}
	
	// Employee emp asks manager a question
	public synchronized void askQuestion( Employee emp ) {
		while( busy ) {
			try {
				wait();
			} catch( InterruptedException e ) {
				e.printStackTrace();
			}
		}
		busy = true;
		System.out.println( String.format( HEAR_QUESTION, clock.getTime(), emp.getName() ) );
		clock.waitFor( 10 );
		System.out.println( String.format( ANSWER_QUESTION, clock.getTime(), emp.getName() ) );
		busy = false;
		notifyAll();
	}
}