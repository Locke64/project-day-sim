import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.CountDownLatch;
import java.util.Random;

public class Employee extends Thread {
	
	private static final String ARRIVE = "%s\tEmployee %s arrives.";
	private static final String LUNCH_START = "%s\tEmployee %s goes to lunch.";
	private static final String LUNCH_END = "%s\tEmployee %s finishes lunch.";
	private static final String DEPART = "%s\tEmployee %s departs after a day of %d hours and %d minutes work.";
	private static final String HAS_QUESTION = "%s\tEmployee %s has a question.";
	private static final String LEAD_QUESTION = "%s\tTeam lead %s has a question and goes to ask the manager.";
	private static final String LEAD_ANSWER = "%s\tTeam lead %s answered employee %s's question.";
	private static final String ASK_MANAGER = "%s\tTeam lead %s couldn't answer employee %s's question so both go ask the manager.";
	private static final String STANDUP_START = "%s\tTeam lead %s begins the team standup meeting.";
	private static final String STANDUP_END = "%s\tTeam lead %s ends the team standup meeting.";
	
	// random generator
	Random gen;
	
	// lock for asking questions
	private boolean busy = true;
	
	// barrier for morning standup
	private CyclicBarrier standupBarrier;
	
	// latch for ending the morning standup
	private CountDownLatch standupLatch;
	
	// collaborators
	private Clock clock;
	private ConferenceRoom confRoom;
	private Manager manager;
	private Employee teamLead;
	
	// stats
	private int arriveMinute;
	private int lunchDuration;
	private int timeWorked;
	
	public Employee(String name, Clock clock, ConferenceRoom confRoom, Manager manager, Employee teamLead) {
		super( name );
		this.clock = clock;
		this.confRoom = confRoom;
		this.manager = manager;
		this.teamLead = teamLead;
		this.gen = new Random();
		this.standupBarrier = new CyclicBarrier( 4 );
		this.standupLatch = new CountDownLatch( 1 );
	}

	public void run() {
		// arrive between 8:00 and 8:30
		arriveMinute = gen.nextInt( 30 ); //TODO 31
		Clock.Time arriveTime = Clock.timeOf( 8, arriveMinute );
		clock.waitUntil( arriveTime );
		System.out.println( String.format( ARRIVE, arriveTime, getName() ) );
		busy = false;
		
		// go to daily standups
		doStandups();

		// ask questions until manager goes to lunch (to avoid missing lunch while waiting for manager)
		// this also prevents devs from asking team leads questions when they could be at lunch
		Clock.Time noon = Clock.timeOf( 12, 0 );
		while( clock.getTime().compareTo( noon ) < 0 ) {
			askQuestions();
			clock.waitFor( 1 );
		}
		
		// go to lunch
		lunch();
		
		Clock.Time statusMeetingTime = Clock.timeOf( 4, 0 );
		while( clock.getTime().compareTo( statusMeetingTime ) < 0 ) {
			clock.waitFor( 1 );
			askQuestions();
		}
		
		//TODO go to project status meeting

		// when can I leave? -between 4:30 and 5:00, after at least 8 hours of work
		int earliestDeparture = arriveMinute + lunchDuration;
		int departMinute = gen.nextInt( 60 - earliestDeparture ) + earliestDeparture;
		Clock.Time departTime = Clock.timeOf( 4, departMinute );
		clock.waitUntil( departTime );
		
		// assuming I won't ask questions after the project status meeting?
		
		// leave
		timeWorked = departMinute - arriveMinute - lunchDuration;
		System.out.println( String.format( DEPART, departTime, getName(), 8, timeWorked ) );
	}

	private synchronized void doStandups() {
		busy = true;
		if( teamLead == null ) {
			manager.reportForStandup(); // report to manager for daily project standup
			morningStandup(); // hold team-based standup
		} else {
			teamLead.reportForStandup(); // report to team lead for team-based standup
		}
		busy = false;
		notifyAll();
	}
	
	private synchronized void morningStandup() {
		try {
			standupBarrier.await();
		} catch( InterruptedException e ) {
			e.printStackTrace();
		} catch( BrokenBarrierException e ) {
			e.printStackTrace();
		}
		confRoom.enter();
		System.out.println( String.format( STANDUP_START, clock.getTime(), getName() ) );
		clock.waitFor( 15 );
		System.out.println( String.format( STANDUP_END, clock.getTime(), getName() ) );
		confRoom.leave();
		standupLatch.countDown();
	}
	
	private synchronized void askQuestions() {
		if( !busy ) {
			int askQuestion = gen.nextInt(200);
			if (askQuestion == 1) {
				if (teamLead == null) { //team lead has the question and goes to ask the manager
					busy = true;
					System.out.println( String.format( LEAD_QUESTION, clock.getTime(), this.getName() ) );
					manager.askQuestion( this );
					busy = false;
				} else { //developer has question
					busy = true;
					System.out.println( String.format( HAS_QUESTION, clock.getTime(), this.getName() ) );
					teamLead.askQuestion( this );
					busy = false;
				}
			}
		}
	}
	
	private synchronized void lunch() {
		//TODO wait until not busy
		// then, random between NOW and 30, instead of 0 and 30
		int lunchStartMinute = gen.nextInt( 30 );
		Clock.Time lunchStartTime = Clock.timeOf( 12, lunchStartMinute );
		clock.waitUntil( lunchStartTime );
		busy = true;
		System.out.println( String.format( LUNCH_START, lunchStartTime, getName() ) );
		int lunchEndMinute = lunchStartMinute + 30;
		lunchEndMinute += gen.nextInt( 30 - arriveMinute );
		lunchEndMinute = Math.min( lunchEndMinute, 59 );
		lunchDuration = lunchEndMinute - lunchStartMinute;
		Clock.Time lunchEndTime = Clock.timeOf( 12, lunchEndMinute );
		clock.waitUntil( lunchEndTime );
		System.out.println( String.format( LUNCH_END, lunchEndTime, getName() ) );
		busy = false;
		notifyAll();
	}

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
	public synchronized void askQuestion( Employee emp ) {
		while( busy ) {
			try {
				wait();
			} catch( InterruptedException e ) {
				e.printStackTrace();
			}
		}
		if (gen.nextBoolean()) { //team lead can answer the question
			System.out.println( String.format( LEAD_ANSWER, clock.getTime(), this.getName(), emp.getName() ) );
		} else { //team lead couldn't answer the question and both go ask the manager
			busy = true;
			System.out.println( String.format( ASK_MANAGER, clock.getTime(), this.getName(), emp.getName() ) );
			manager.askQuestion( this );
		}
		busy = false;
		notifyAll();
	}
	
	public synchronized void getAttention() {
		while( busy ) {
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
	
	public int getLunchDuration() {
		return this.lunchDuration;
	}
	
	public int getTimeWorked() {
		return this.timeWorked;
	}
	
}