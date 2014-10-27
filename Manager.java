import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class Manager extends Thread {

	// logging strings
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
	private static final String DECLINE_QUESTION = "%s\tManager doesn't have time to answer employee %s's question today.";
	
	private Clock clock;
	private int timeMeeting;
	
	// lock for asking questions
	private boolean busy = false;
	
	// another lock for asking questions, for when the manager needs to go to a meeting or to lunch
	// this ensures the busy lock is obtained by the main Manager thread instead of any other Employee threads asking more questions
	private boolean canAnswerQuestions = true;
	
	// barrier for morning standup
	private CyclicBarrier standupBarrier;
	
	// latch for ending the morning standup
	private CountDownLatch standupLatch;
	
	// barrier for project status meeting
	private CyclicBarrier statusBarrier;
	
	// latch for ending the project status meeting
	private CountDownLatch statusLatch;
	
	public Manager( Clock clock ) {
		this.clock = clock;
		this.standupBarrier = new CyclicBarrier( 4 );
		this.standupLatch = new CountDownLatch( 1 );
		this.statusBarrier = new CyclicBarrier( 4 );
		this.statusLatch = new CountDownLatch( 1 );
		this.timeMeeting = 0;
	}
	
	public void run() {
		clock.clockIn();
		
		// arrive
		System.out.println( String.format( ARRIVE, clock.getTime().toString() ) );
		
		// team lead standup
		morningStandup();
		
		// morning executive meeting
		clock.waitUntil( Clock.timeOf( 10, 0 ) );
		morningExecMeeting();
		
		// lunch
		clock.waitUntil( Clock.timeOf( 12, 0 ) );
		lunch();
		
		// afternoon executive meeting
		clock.waitUntil( Clock.timeOf( 2, 0 ) );
		afternoonExecMeeting();
		
		// project status meeting
		clock.waitUntil(Clock.timeOf(4,0));
		afternoonMeeting();
		
		Clock.Time departTime = Clock.timeOf( 5, 0 );
		clock.waitUntil( departTime );
		System.out.println( String.format( DEPART, departTime, 8, 0 ) );
		clock.clockOut();
	}

	/*
	 * --- run() helpers ---
	*/
	
	// host the morning standup meeting with the team leads
	private synchronized void morningStandup() {
		getAttention();
		try {
			standupBarrier.await(); // wait for team leads to check in
		} catch( InterruptedException e ) {
			e.printStackTrace();
		} catch( BrokenBarrierException e ) {
			e.printStackTrace();
		}
		timeMeeting += 15;
		System.out.println( String.format( START_LEAD_MEETING, clock.getTime().toString() ) );
		clock.waitFor( 15 );
		System.out.println( String.format( END_LEAD_MEETING, clock.getTime().toString() ) );
		standupLatch.countDown(); // let the team leads go
		releaseAttention();
	}
	
	// morning executive meeting
	private synchronized void morningExecMeeting() {
		canAnswerQuestions = false;
		getAttention( true ); // finish answering a question (but no more than one)
		Clock.Time startMeeting = clock.getTime();
		System.out.println( String.format( ARRIVE_EXE_MEETING, clock.getTime().toString(), "morning" ) );
		clock.waitUntil( Clock.timeOf( 11, 0 ) );
		Clock.Time endMeeting = clock.getTime();
		timeMeeting += endMeeting.compareTo(startMeeting);
		System.out.println( String.format( LEAVE_EXE_MEETING, clock.getTime().toString(), "morning" ) );
		canAnswerQuestions = true;
		releaseAttention();
	}
	
	// lunch
	private synchronized void lunch() {
		canAnswerQuestions = false;
		getAttention( true ); // finish answering a question		
		System.out.println( String.format( LUNCH_START, clock.getTime().toString() ) );
		clock.waitFor( 60 );
		System.out.println( String.format( LUNCH_END, clock.getTime().toString() ) );
		canAnswerQuestions = true;
		releaseAttention();
	}
	
	// afternoon executive meeting
	private synchronized void afternoonExecMeeting() {
		canAnswerQuestions = false;
		getAttention( true ); // finish answering a question
		Clock.Time startMeeting = clock.getTime();
		System.out.println( String.format( ARRIVE_EXE_MEETING, clock.getTime().toString(), "afternoon" ) );
		clock.waitUntil( Clock.timeOf( 3, 0 ) );
		Clock.Time endMeeting = clock.getTime();
		timeMeeting += endMeeting.compareTo(startMeeting);
		System.out.println( String.format( LEAVE_EXE_MEETING, clock.getTime().toString(), "afternoon" ) );
		canAnswerQuestions = true;
		releaseAttention();
	}
	
	// project status meeting
	// unsynchronized so the manager can wait here and still answer questions
	private void afternoonMeeting() {
		try {
			statusBarrier.await(); // wait for team leads to check in. now we're sure they're done with questions.
		} catch( InterruptedException e ) {
			e.printStackTrace();
		} catch( BrokenBarrierException e ) {
			e.printStackTrace();
		}
		System.out.println(clock.getTime().toString()+ "\tTeam starts 4 o'clock meeting");
		clock.waitFor( 15 );
		System.out.println(clock.getTime().toString() +"\tTeam ends 4 o'clock meeting");
		timeMeeting += 15;
		statusLatch.countDown();
	}

	/*
	 * --- employee management --
	 */
	
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
	
	// report to the manager for the daily project status meeting
	public void reportForStatus() {
		try {
			statusBarrier.await(); // report ready to begin
			statusLatch.await(); // wait for meeting to end
		} catch( InterruptedException e ) {
			e.printStackTrace();
		} catch( BrokenBarrierException e ) {
			e.printStackTrace();
		}
	}
	
	// Employee emp asks manager a question
	public int askQuestion( Employee emp ) {
		Clock.Time startWaiting = clock.getTime();
		getAttention();
		Clock.Time endWaiting = clock.getTime();
		System.out.println( String.format( HEAR_QUESTION, clock.getTime(), emp.getName() ) );
		if( clock.getTime().compareTo( Clock.timeOf( 4, 0 ) ) < 0 ) {
			clock.waitFor( 10 );
			timeMeeting += 10;
			System.out.println( String.format( ANSWER_QUESTION, clock.getTime(), emp.getName() ) );
		} else {
			System.out.println( String.format( DECLINE_QUESTION, clock.getTime(), emp.getName() ) );
		}
		releaseAttention();
		return endWaiting.compareTo(startWaiting);
	}
	
	// Get the manager's attention (lock).
	public synchronized void getAttention() {
		getAttention( false );
	}
	
	// Get the manager's attention (lock). If override is false, the manager's attention may be taken elsewhere first.
	private synchronized void getAttention( boolean override ) {
		while( busy || !(override || canAnswerQuestions) ) {
			try {
				wait();
			} catch( InterruptedException e ) {
				e.printStackTrace();
			}
		}
		busy = true;
	}
	
	// Release the manager's attention (lock).
	public synchronized void releaseAttention() {
		busy = false;
		notifyAll();
	}

	/*
	 * --- stats ---
	 */
	
	// get the manager's total time spent in meetings
	public int getTimeMeeting() {
		return this.timeMeeting;
	}
}
