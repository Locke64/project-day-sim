import java.util.concurrent.CountDownLatch;

public class Manager extends Thread {

	private static final String ARRIVE = "%s\tManager arrives.";
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
	
	public Manager( Clock clock ) {
		this.clock = clock;
	}
	//When he arrives at 8:00 each day, the manager engages in daily planning 
	//activites and then waits (doing administrivia) until all the team leads 
	//arrive at his office. When all the leads have arrived, they knock
	//on the manager's door and enter for their daily 15 minute standup meeting.
	public void morningStandUp(){
		System.out.println( String.format( ARRIVE, clock.getTime().toString() ) );
				
//		int timeBefore = Integer.valueOf(clock.getTime().toString());

		System.out.println( String.format( START_LEAD_MEETING, clock.getTime().toString() ) );
		

//		int timeAfter = Integer.valueOf(clock.getTime().toString());
		

//		int timeWorked = (timeAfter - timeBefore);

		//----elapse 15 min after executive meeting is initiated----
//		clock.nextTime(new Clock.Time(clock.getTime().hour,clock.getTime().minute + 15));
		clock.waitFor( 15 );
		System.out.println( String.format( END_LEAD_MEETING, clock.getTime().toString() ) );
		
		
		
	}
	
	public void run() {
		morningStandUp();
		
		clock.waitUntil( new Clock.Time( 10, 0 ) );
		System.out.println( String.format( ARRIVE_EXE_MEETING, clock.getTime().toString(), "morning" ) );
		clock.waitUntil( new Clock.Time( 11, 0 ) );
		System.out.println( String.format( LEAVE_EXE_MEETING, clock.getTime().toString(), "morning" ) );
		clock.waitUntil( new Clock.Time( 12, 0 ) );
		System.out.println( String.format( LUNCH_START, clock.getTime().toString() ) );
		clock.waitUntil( new Clock.Time( 1, 0 ) );
		System.out.println( String.format( LUNCH_END, clock.getTime().toString() ) );
		clock.waitUntil( new Clock.Time( 2, 0 ) );
		System.out.println( String.format( ARRIVE_EXE_MEETING, clock.getTime().toString(), "afternoon" ) );
		clock.waitUntil( new Clock.Time( 3, 0 ) );
		System.out.println( String.format( LEAVE_EXE_MEETING, clock.getTime().toString(), "afternoon" ) );
	}
	
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
		clock.waitFor( 15 );
		System.out.println( String.format( ANSWER_QUESTION, clock.getTime(), emp.getName() ) );
		busy = false;
		notifyAll();
	}
}