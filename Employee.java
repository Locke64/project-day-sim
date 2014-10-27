import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.CountDownLatch;
import java.util.Random;

// An employee working on the project. May or may not be a team lead.
public class Employee extends Thread {
	
	// logging strings
	private static final String ARRIVE = "%s\tEmployee %s arrives.";
	private static final String LUNCH_START = "%s\tEmployee %s goes to lunch.";
	private static final String LUNCH_END = "%s\tEmployee %s finishes lunch.";
	private static final String DEPART = "%s\tEmployee %s departs after a day of %d hours and %d minutes work.";
	private static final String HAS_QUESTION = "%s\tEmployee %s has a question.";
	private static final String LEAD_QUESTION = "%s\tTeam lead %s has a question and goes to ask the manager.";
	private static final String LEAD_ANSWER = "%s\tTeam lead %s answers employee %s's question.";
	private static final String ASK_MANAGER = "%s\tTeam lead %s couldn't answer employee %s's question so both go ask the manager.";
	private static final String STANDUP_START = "%s\tTeam lead %s begins the team standup meeting.";
	private static final String STANDUP_END = "%s\tTeam lead %s ends the team standup meeting.";
	
	// question frequency configuration - dividing 1 by this number is the chance that a developer or team lead will have a question each minute
	private static final int CHANCE_QUESTIONS = 200;
	
	// random generator
	private Random gen;
	
	// lock for asking questions
	private boolean busy = true;
	
	// another lock for asking questions, for when the employee needs to go to a meeting or to lunch
	private boolean canAnswerQuestions = true;
	
	// barrier for morning standup
	private CyclicBarrier standupBarrier;
	
	// latch for ending the morning standup
	private CountDownLatch standupLatch;
	
	// barrier for project status meeting
	private CyclicBarrier statusBarrier;
	
	// latch for ending the status meeting
	private CountDownLatch statusLatch;
	
	// collaborators
	private Clock clock;
	private ConferenceRoom confRoom;
	private Manager manager;
	private Employee teamLead;
	
	// stats
	private int arriveMinute;
	private int lunchDuration;
	private int timeWorked;
	private int timeMeeting;
	private int timeWaiting;
	
	public Employee(String name, Clock clock, ConferenceRoom confRoom, Manager manager, Employee teamLead) {
		super( name );
		this.clock = clock;
		this.confRoom = confRoom;
		this.manager = manager;
		this.teamLead = teamLead;
		this.gen = new Random();
		this.standupBarrier = new CyclicBarrier( 4 );
		this.standupLatch = new CountDownLatch( 1 );
		this.statusBarrier = new CyclicBarrier( 4 );
		this.statusLatch = new CountDownLatch( 1 );
		this.timeMeeting = 0;
		this.timeWaiting = 0;
	}

	public void run() {
		clock.clockIn();
		
		// arrive between 8:00 and 8:30
		arriveMinute = gen.nextInt( 30 );
		Clock.Time arriveTime = Clock.timeOf( 8, arriveMinute );
		clock.waitUntil( arriveTime );
		System.out.println( String.format( ARRIVE, arriveTime, getName() ) );
		busy = false;
		
		// go to daily standups
		doStandups();
		
		// pick a desired lunchtime
		int lunchHour = gen.nextInt( 3 ) + 9; // generate random hour from 9am and 12pm
		if (lunchHour > 12)
			lunchHour -= 12;
		int lunchMinute = gen.nextInt( 60 );
		Clock.Time lunchStartTime = Clock.timeOf( lunchHour, lunchMinute );

		// ask questions until lunch
		while( clock.getTime().compareTo( lunchStartTime ) < 0 ) {
			clock.waitFor( 1 );
			askQuestions();
		}
		
		// go to lunch
		lunch( lunchStartTime );
		
		// ask questions until the project status meeting
		Clock.Time statusMeetingTime = Clock.timeOf( 4, 0 );
		while( clock.getTime().compareTo( statusMeetingTime ) < 0 ) {
			clock.waitFor( 1 );
			askQuestions();
		}
		
		// go to project status meeting
		doStatusMeeting();

		// when can I leave? -between 4:30 and 5:00, after at least 8 hours of work
		int earliestDeparture = arriveMinute + lunchDuration;
		int departMinute = gen.nextInt( 60 - earliestDeparture ) + earliestDeparture;
		Clock.Time departTime = Clock.timeOf( 4, departMinute );
		clock.waitUntil( departTime );
		
		// assuming I won't ask questions after the project status meeting
		
		// leave
		timeWorked = departMinute - arriveMinute - lunchDuration;
		System.out.println( String.format( DEPART, departTime, getName(), 8, timeWorked ) );
		clock.clockOut();
	}

	/*
	 * --- run helpers ---
	 */
	
	// Go to standup meeting(s)
	private synchronized void doStandups() {
		getAttention();
		if( teamLead == null ) {
			manager.reportForStandup(); // report to manager for daily project standup
			morningStandup(); // hold team-based standup
			timeMeeting += 30; // has 30 minutes of meetings in the morning
		} else {
			teamLead.reportForStandup(); // report to team lead for team-based standup
			timeMeeting += 15; // has 15 minutes of meetings in the morning
		}
		releaseAttention();
	}
	
	// team-based standup meeting
	private synchronized void morningStandup() {
		try {
			standupBarrier.await(); // wait for team members to check in
		} catch( InterruptedException e ) {
			e.printStackTrace();
		} catch( BrokenBarrierException e ) {
			e.printStackTrace();
		}
		confRoom.enter(); // get the conference room
		System.out.println( String.format( STANDUP_START, clock.getTime(), getName() ) );
		clock.waitFor( 15 );
		System.out.println( String.format( STANDUP_END, clock.getTime(), getName() ) );
		confRoom.leave(); // release the conference room
		standupLatch.countDown(); // let the team go
	}
	
	// ask questions to manager if team lead, otherwise to team lead
	private synchronized void askQuestions() {
		if( !busy ) { // can't block here or we might ask a question during a meeting or something, so just skip the whole thing if busy
			int askQuestion = gen.nextInt(CHANCE_QUESTIONS);
			if (askQuestion == 0) {
				int managerWait;
				if (teamLead == null) { //team lead has the question and goes to ask the manager
					busy = true;
					System.out.println( String.format( LEAD_QUESTION, clock.getTime(), this.getName() ) );
					managerWait = manager.askQuestion( this );
					timeWaiting += managerWait;
					timeMeeting += 10; // team lead meets with manager for 10 minutes
					busy = false;
				} else { //developer has question
					busy = true;
					System.out.println( String.format( HAS_QUESTION, clock.getTime(), this.getName() ) );
					timeWaiting += teamLead.askQuestion( this );
					busy = false;
				}
			}
		}
	}
	
	// go to lunch. try for the given lunchStartTime, but finish getting a question answered first if there's one in progress.
	private synchronized void lunch( Clock.Time lunchStartTime ) {
		clock.waitUntil( lunchStartTime ); // planned lunch start time
		canAnswerQuestions = false;
		getAttention( true ); // finish answering a question (but no more than one)
		lunchStartTime = clock.getTime(); // actual lunch start time
		System.out.println( String.format( LUNCH_START, lunchStartTime, getName() ) );
		
		// compute an end time
		int lunchEndHour = lunchStartTime.hour;
		int lunchEndMinute = lunchStartTime.minute + 30;
		lunchEndMinute += gen.nextInt( 30 - arriveMinute );
		if (lunchEndMinute > 59) {
			lunchEndMinute -= 60;
			lunchEndHour += 1;
		}
		if (lunchEndHour > 12)
			lunchEndHour -= 12;
		lunchDuration = lunchEndMinute - lunchStartTime.minute;
		if (lunchDuration < 1)
			lunchDuration += 60;
		Clock.Time lunchEndTime = Clock.timeOf( lunchEndHour, lunchEndMinute );
		clock.waitUntil( lunchEndTime );
		System.out.println( String.format( LUNCH_END, lunchEndTime, getName() ) );
		canAnswerQuestions = true;
		releaseAttention();
	}

	// project status meeting
	private void doStatusMeeting() {
		if( teamLead == null ) { // I'm a team lead
			try {
				statusBarrier.await(); // wait for team to check in
				manager.reportForStatus(); // report readiness to manager
				statusLatch.countDown(); // let the team go
			} catch( InterruptedException e ) {
				e.printStackTrace();
			} catch( BrokenBarrierException e ) {
				e.printStackTrace();
			}
		} else { // I'm not a team lead
			teamLead.reportForStatus(); // report readiness to team lead
		}
		timeMeeting += 15; // has 15 minutes of meetings in the morning
	}

	/*
	 * --- team management ---
	 */

	// report readiness to team lead for morning team-based standup
	public void reportForStandup() {
		try {
			standupBarrier.await(); // report ready to begin
			standupLatch.await(); // wait for meeting to end
		} catch( InterruptedException e ) {
			e.printStackTrace();
		} catch( BrokenBarrierException e ) {
			e.printStackTrace();
		}
	}
	
	// Employee emp asks its team lead a question.
	public int askQuestion( Employee emp ) {
		int managerWait = 0;
		getAttention();
		if (gen.nextBoolean()) { //team lead can answer the question
			System.out.println( String.format( LEAD_ANSWER, clock.getTime(), this.getName(), emp.getName() ) );
		} else { //team lead couldn't answer the question and both go ask the manager
			System.out.println( String.format( ASK_MANAGER, clock.getTime(), this.getName(), emp.getName() ) );
			managerWait = manager.askQuestion( this );
			timeMeeting += 10; // team lead and developer both meet with manager for 10 minutes
			emp.increaseTimeMeeting(); // team lead and developer both meet with manager for 10 minutes
			timeWaiting += managerWait;
		}
		releaseAttention();
		return managerWait;
	}

	// report readiness to team lead for afternoon project status meeting
	public void reportForStatus() {
		try {
			statusBarrier.await(); // report readiness to team lead
			statusLatch.await(); // wait until the meeting is over
		} catch( InterruptedException e ) {
			e.printStackTrace();
		} catch( BrokenBarrierException e ) {
			e.printStackTrace();
		}
	}
	
	// Get the employee's attention (lock).
	public synchronized void getAttention() {
		getAttention( false );
	}
		
	// Get the employee's attention (lock). If override is false, the employee's attention may be taken elsewhere first.
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
	
	// Release the employee's attention (lock).
	public synchronized void releaseAttention() {
		busy = false;
		notifyAll();
	}
	
	/*
	 * --- stats ---
	 */
	
	public int getLunchDuration() {
		return this.lunchDuration;
	}
	
	public int getTimeWorked() {
		return this.timeWorked;
	}
	
	public void increaseTimeMeeting() {
		this.timeMeeting += 10;
	}
	
	public int getTimeMeeting() {
		return this.timeMeeting;
	}
	
	public int getTimeWaiting() {
		return this.timeWaiting;
	}
	
}
