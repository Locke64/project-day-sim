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
	private static final String ASK_MANAGER = "%s\tTeam lead %s couldn't answer employee %s question so both go ask the manager";
	
	private String name;
	private Clock clock;
	private CountDownLatch startLatch;
	private Manager manager;
	private Employee teamLead;
	
	private int arriveMinute;
	private int lunchDuration;
	
	public Employee(String name, Clock clock, CountDownLatch startLatch, Manager manager, Employee teamLead) {
		this.name = name;
		this.clock = clock;
		this.startLatch = startLatch;
		this.manager = manager;
		this.teamLead = teamLead;
	}

	public void run() {
		Random gen = new Random();

		try {
			// wait until all actors are ready
			startLatch.await();
			simulate();
			/*
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
			*/
		} catch( InterruptedException e ) {
			e.printStackTrace();
		}
	}
	
	private synchronized void simulate() {
		Random gen = new Random();		
		// arrive between 8:00 and 8:30
		arriveMinute = gen.nextInt( 30 ); //TODO 31
		Clock.Time arriveTime = new Clock.Time( 8, arriveMinute );
		clock.nextTime( arriveTime );
		System.out.println( String.format( ARRIVE, arriveTime, name ) );
		
		//TODO go to meetings
		
		// when's lunch?
		int lunchStartMinute = gen.nextInt( 30 );
		Clock.Time lunchStartTime = new Clock.Time( 12, lunchStartMinute );

		// ask questions until lunch
		while( clock.nextTime().compareTo( lunchStartTime ) < 0 ) {
			//TODO ask questions
			askQuestions(gen);
		}
		
		// go to lunch
		System.out.println( String.format( LUNCH_START, lunchStartTime, name ) );
		int lunchEndMinute = lunchStartMinute + 30;
		lunchEndMinute += gen.nextInt( 30 - arriveMinute );
		lunchEndMinute = Math.min( lunchEndMinute, 59 );
		lunchDuration = lunchEndMinute - lunchStartMinute;
		Clock.Time lunchEndTime = new Clock.Time( 12, lunchEndMinute );
		clock.nextTime( lunchEndTime );
		System.out.println( String.format( LUNCH_END, lunchEndTime, name ) );

		// when can I leave? -between 4:30 and 5:00, after at least 8 hours of work
		int earliestDeparture = arriveMinute + lunchDuration;
		int departMinute = gen.nextInt( 60 - earliestDeparture ) + earliestDeparture;
		Clock.Time departTime = new Clock.Time( 4, departMinute );
		while( clock.nextTime().compareTo( departTime ) < 0 ) {
			//TODO ask questions
			askQuestions(gen);
		}
		
		// leave
		System.out.println( String.format( DEPART, departTime.toString(), name, 8, departMinute - arriveMinute - lunchDuration ) );
		clock.nextTime( new Clock.Time( 5, 0 ) ); // let everyone else go too
	}
	
	private void askQuestions(Random gen) {
		int askQuestion = gen.nextInt(100);
		if (askQuestion == 1) {
			if (teamLead == null) { //team lead has the question and goes to ask the manager
				System.out.println( String.format( LEAD_QUESTION, clock.getTime(), this.getEmployeeName() ) );
			} else { //developer has question
				System.out.println( String.format( HAS_QUESTION, clock.getTime(), this.getEmployeeName() ) );
				if (gen.nextBoolean()) { //team lead can answer the question
					System.out.println( String.format( LEAD_ANSWER, clock.getTime(), teamLead.getEmployeeName(), this.getEmployeeName() ) );
				} else { //team lead couldn't answer the question and both go ask the manager
					System.out.println( String.format( ASK_MANAGER, clock.getTime(), teamLead.getEmployeeName(), this.getEmployeeName() ) );
				}
			}
		}
	}
	
	public String getEmployeeName() {
		return this.name;
	}
	
}