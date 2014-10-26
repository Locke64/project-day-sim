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
	
	private Clock clock;
	private CountDownLatch startLatch;
	private int workTime = 0;
	
	public Manager( Clock clock, CountDownLatch startLatch ) {
		this.clock = clock;
		this.startLatch = startLatch;
	}
	//When he arrives at 8:00 each day, the manager engages in daily planning 
	//activites and then waits (doing administrivia) until all the team leads 
	//arrive at his office. When all the leads have arrived, they knock
	//on the manager's door and enter for their daily 15 minute standup meeting.
	public void morningStandUp(){
		startLatch.countDown();
		System.out.println( String.format( ARRIVE, clock.getTime().toString() ) );
				
//		int timeBefore = Integer.valueOf(clock.getTime().toString());
		try{
			startLatch.await();
		}
		catch(InterruptedException e){
			e.printStackTrace();
		}
		System.out.println( String.format( START_LEAD_MEETING, clock.getTime().toString() ) );
		

//		int timeAfter = Integer.valueOf(clock.getTime().toString());
		

//		int timeWorked = (timeAfter - timeBefore);

		//----elapse 15 min after executive meeting is initiated----
		clock.nextTime(new Clock.Time(clock.getTime().hour,clock.getTime().minute + 15));
		System.out.println( String.format( END_LEAD_MEETING, clock.getTime().toString() ) );
		
		
		
	}
	
	public void run() {
		morningStandUp();
		
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
	}
	
}