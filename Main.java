import java.util.concurrent.CountDownLatch;

public class Main {

	public static void main(String[] args) {
		// create latches and barriers
		CountDownLatch startLatch = new CountDownLatch(1);
	
		// create actors
		Clock clock = new Clock(startLatch);
		Manager manager = new Manager(clock, startLatch);
		Employee[] employees = new Employee[12];
		Employee[] teamLeads = new Employee[3];
		int counter = 0;
		for (int team = 1; team <= 3; team++) {
			for (int employeeNum = 1; employeeNum <= 4; employeeNum++) {
				Employee employee;
				if (employeeNum == 1) {
					employee = new Employee(team + "" + employeeNum, clock, startLatch, manager, null);
					teamLeads[team-1] = employee;
				} else {
					employee = new Employee(team + "" + employeeNum, clock, startLatch, manager, teamLeads[team-1]);
				}
				employees[counter] = employee;
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