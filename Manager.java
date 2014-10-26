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
	
	public Manager( Clock clock ) {
		this.clock = clock;
		this.standupBarrier = new CyclicBarrier( 4 );
		this.standupLatch = new CountDownLatch( 1 );
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
		
		//TODO 4:00 meeting
		clock.waitUntil(Clock.timeOf(4,0));
		afternoonMeeting();
		
		Clock.Time departTime = Clock.timeOf( 5, 0 );
		clock.waitUntil( departTime );
		System.out.println( String.format( DEPART, departTime, 8, 0 ) ); //TODO calculate working time
		clock.clockOut();
	}
	
	private synchronized void morningStandup() {
		getAttention();
		try {
			standupBarrier.await();
		} catch( InterruptedException e ) {
			e.printStackTrace();
		} catch( BrokenBarrierException e ) {
			e.printStackTrace();
		}
		timeMeeting += 15;
		System.out.println( String.format( START_LEAD_MEETING, clock.getTime().toString() ) );
		clock.waitFor( 15 );
		System.out.println( String.format( END_LEAD_MEETING, clock.getTime().toString() ) );
		standupLatch.countDown();
		releaseAttention();
	}
	
	// morning executive meeting
	private synchronized void morningExecMeeting() {
		canAnswerQuestions = false;
		getAttention( true ); // finish answering a question
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
	
	private synchronized void afternoonMeeting(){

		while(clock.getTime().minute < 15 ){
		busy = true;
		System.out.println(clock.getTime().toString()+ "    Team starts 4 o'clock meeting");

		clock.waitFor( 15 );
		System.out.println(clock.getTime().toString() +"    Team ends 4 o'clock meeting");
		busy = false;
		notifyAll();
		}
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
	
	public int getTimeMeeting() {
		return this.timeMeeting;
	}
	
	// Employee emp asks manager a question
	public int askQuestion( Employee emp ) {
		Clock.Time startWaiting = clock.getTime();
		getAttention();
		Clock.Time endWaiting = clock.getTime();
		System.out.println( String.format( HEAR_QUESTION, clock.getTime(), emp.getName() ) );
		clock.waitFor( 10 );
		timeMeeting += 10;
		System.out.println( String.format( ANSWER_QUESTION, clock.getTime(), emp.getName() ) );
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
	
	public synchronized void releaseAttention() {
		busy = false;
		notifyAll();
	}
}