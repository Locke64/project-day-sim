public class Main {

	public static void main(String[] args) {
		// create latches and barriers
		CountDownLatch startLatch = new CountDownLatch(1);
	
		// create actors
		Clock clock = new Clock(startLatch);
		Manager manager = new Manager(startLatch);
		Employee[] employees = new Employee[12];
		int counter = 0;
		for (int team = 1; team <= 3; team++) {
			for (int employee = 1; employee <= 4; employee++) {
				employees[counter] = new Employee(team + "" + employee, clock, startLatch);
				counter++;
			}
		}
		
		// start actors
		clock.start();
		manager.start();
		for (int a = 0; a < 12; a++) {
			employees[a].start();
		}
		startLatch.countDown();
	}
}