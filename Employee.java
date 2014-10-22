public class Employee extends Thread {
	
	private static final String ARRIVE = "%s Employee %s arrives.";
	
	private String name;
	private Clock clock;
	private CountDownLatch startLatch;
	
	public Employee(String name, Clock clock, CountDownLatch startLatch) {
		this.name = name;
		this.clock = clock;
		this.startLatch = startLatch;
	}

	public void run() {
		Random gen = new Random();
		LocalTime startTime = LocalTime.of(8, gen.nextInt(30));
		
		// wait until all actors are ready
		startLatch.await();
		
		// arrive between 8:00 and 8:30
		while( startTime.compareTo( clock.getTime() ) > 0 )
			wait();
		System.out.println( String.format( ARRIVE, clock.getTime().toString(), name ) );
	}
	
}