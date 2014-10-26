import java.util.concurrent.CountDownLatch;

public class Main {

	public static void main(String[] args) {
	
		// create actors
		Clock clock = new Clock();
		Manager manager = new Manager(clock);
		Employee[] employees = new Employee[12];
		Employee[] teamLeads = new Employee[3];
		int counter = 0;
		for (int team = 1; team <= 3; team++) {
			for (int employeeNum = 1; employeeNum <= 4; employeeNum++) {
				Employee employee;
				if (employeeNum == 1) {
					employee = new Employee(team + "" + employeeNum, clock, manager, null);
					teamLeads[team-1] = employee;
				} else {
					employee = new Employee(team + "" + employeeNum, clock, manager, teamLeads[team-1]);
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
		for (int a = 0; a < 12; a++) {
			try {
				employees[a].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//TODO: REMOVE
		System.out.println("5:00\tManager departs after 8 hours and 0 minutes of work.");
		
		//Statistics
		System.out.println("\n**********STATISTICS**********");
		int totalWorking = 0;
		int totalLunch = 0;
		for (int a = 0; a < 12; a++) {
			totalWorking += employees[a].getTimeWorked();
			totalLunch += employees[a].getLunchDuration();
		}
		int hoursWorked = totalWorking / 60 + 104; //add 8 hours for manager and each developer
		int minutesWorked = totalWorking % 60;
		int hoursLunch = totalLunch / 60 + 1; //add one hour for manager's lunch
		int minutesLunch = totalLunch % 60;
		System.out.println("Total time of manager and developers working: " + hoursWorked + " hours " + minutesWorked + " minutes");
		System.out.println("Total time of manager and developers at lunch: " + hoursLunch + " hours " + minutesLunch + " minutes");
	}
}