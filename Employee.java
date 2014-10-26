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
	
	private String name;
	private Clock clock;
	private Manager manager;
	private Employee teamLead;
	
	private int arriveMinute;
	private int lunchDuration;
	private int timeWorked;
	
	public Employee(String name, Clock clock, Manager manager, Employee teamLead) {
		this.name = name;
		this.clock = clock;
		this.manager = manager;
		this.teamLead = teamLead;
	}

	public void run() {
		Random gen = new Random();		
		// arrive between 8:00 and 8:30
		arriveMinute = gen.nextInt( 30 ); //TODO 31
		Clock.Time arriveTime = Clock.timeOf( 8, arriveMinute );
		clock.nextTime( arriveTime );
		System.out.println( String.format( ARRIVE, arriveTime, name ) );
		
		//TODO go to meetings
		
		// when's lunch?
		int lunchStartMinute = gen.nextInt( 30 );
		Clock.Time lunchStartTime = Clock.timeOf( 12, lunchStartMinute );

		// ask questions until lunch
		while( clock.nextTime().compareTo( lunchStartTime ) < 0 ) {
			askQuestions(gen);
		}
		
		// go to lunch
		System.out.println( String.format( LUNCH_START, lunchStartTime, name ) );
		int lunchEndMinute = lunchStartMinute + 30;
		lunchEndMinute += gen.nextInt( 30 - arriveMinute );
		lunchEndMinute = Math.min( lunchEndMinute, 59 );
		lunchDuration = lunchEndMinute - lunchStartMinute;
		Clock.Time lunchEndTime = Clock.timeOf( 12, lunchEndMinute );
		clock.nextTime( lunchEndTime );
		System.out.println( String.format( LUNCH_END, lunchEndTime, name ) );
		
		Clock.Time statusMeetingTime = Clock.timeOf( 4, 0 );
		while( clock.nextTime().compareTo( statusMeetingTime ) < 0 ) {
			askQuestions(gen);
		}
		
		//TODO go to project status meeting

		// when can I leave? -between 4:30 and 5:00, after at least 8 hours of work
		int earliestDeparture = arriveMinute + lunchDuration;
		int departMinute = gen.nextInt( 60 - earliestDeparture ) + earliestDeparture;
		Clock.Time departTime = Clock.timeOf( 4, departMinute );
		clock.nextTime( departTime );
		
		// assuming I won't ask questions after the project status meeting?
		
		// leave
		timeWorked = departMinute - arriveMinute - lunchDuration;
		System.out.println( String.format( DEPART, departTime.toString(), name, 8, timeWorked ) );
		clock.nextTime( Clock.timeOf( 5, 0 ) ); // let everyone else go too
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
	
	public int getLunchDuration() {
		return this.lunchDuration;
	}
	
	public int getTimeWorked() {
		return this.timeWorked;
	}
	
}