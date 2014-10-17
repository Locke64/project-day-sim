public class Manager extends Thread {

	private static final String ARRIVE = "%s Manager arrives.";
	private static final String STANDUP = "%s Project standup begins.";
	
	private Clock clock;
	private int workTime = 0;
	
	public Manager(Clock clock) {
		this.clock = clock;
	}

	public void run() {
		//TODO
	}
}