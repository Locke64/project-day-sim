import java.util.concurrent.CountDownLatch;
import java.util.Random;

public class Employee extends Thread {
	
	private static final String ARRIVE = "%s\tEmployee %s arrives.";
	private static final String LUNCH_START = "%s\tEmployee %s goes to lunch.";
	private static final String LUNCH_END = "%s\tEmployee %s finishes lunch.";
	private static final String DEPART = "%s\tEmployee %s departs after a day of %d hours and %d minutes work.";
	private static final String HAS_QUESTION = "%s\tEmployee %s has a question.";
	private static final String LEAD_QUESTION = "%s\tTeam lead %s has a question and goes to ask the manager.";
	private static final String LEAD_ANSWER = "%s\tTeam lead %s answered employee %s question.";
	private static final String ASK_MANAGER = "%s\tTeam lead %s couldn't answer employee %s question so both go ask the manager.";
	
	// random generator
	Random gen;
	
	// lock for asking questions
	private boolean busy = false;
	
	// collaborators
	private Clock clock;
	private Manager manager;
	private Employee teamLead;
	
	// stats
	private int arriveMinute;
	private int lunchDuration;
	private int timeWorked;
	
	public Employee(String name, Clock clock, Manager manager, Employee teamLead) {
		super( name );
		this.clock = clock;
		this.manager = manager;
		this.teamLead = teamLead;
		this.gen = new Random();
	}

	public void run() {
		// arrive between 8:00 and 8:30
		arriveMinute = gen.nextInt( 30 ); //TODO 31
		Clock.Time arriveTime = Clock.timeOf( 8, arriveMinute );
		clock.waitUntil( arriveTime );
		System.out.println( String.format( ARRIVE, arriveTime, getName() ) );
		
		//TODO go to meetings
		
		// when's lunch?
		int lunchStartMinute = gen.nextInt( 30 );
		Clock.Time lunchStartTime = Clock.timeOf( 12, lunchStartMinute );

		// ask questions until lunch
		while( clock.getTime().compareTo( lunchStartTime ) < 0 ) {
			clock.waitFor( 1 );
			askQuestions();
		}
		
		// go to lunch
		System.out.println( String.format( LUNCH_START, lunchStartTime, getName() ) );
		int lunchEndMinute = lunchStartMinute + 30;
		lunchEndMinute += gen.nextInt( 30 - arriveMinute );
		lunchEndMinute = Math.min( lunchEndMinute, 59 );
		lunchDuration = lunchEndMinute - lunchStartMinute;
		Clock.Time lunchEndTime = Clock.timeOf( 12, lunchEndMinute );
		clock.waitUntil( lunchEndTime );
		System.out.println( String.format( LUNCH_END, lunchEndTime, getName() ) );
		
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
		System.out.println( String.format( DEPART, departTime.toString(), getName(), 8, timeWorked ) );
	}
	
	private void askQuestions() {
		int askQuestion = gen.nextInt(200);
		if (askQuestion == 1) {
			if (teamLead == null) { //team lead has the question and goes to ask the manager
				System.out.println( String.format( LEAD_QUESTION, clock.getTime(), this.getName() ) );
				manager.askQuestion( this );
			} else { //developer has question
				System.out.println( String.format( HAS_QUESTION, clock.getTime(), this.getName() ) );
				teamLead.askQuestion( this );
			}
		}
	}
	
	// Employee emp asks its team lead a question.
	private synchronized void askQuestion( Employee emp ) {
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
	
	public int getLunchDuration() {
		return this.lunchDuration;
	}
	
	public int getTimeWorked() {
		return this.timeWorked;
	}
	
}