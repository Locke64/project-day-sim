import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.CountDownLatch;

public class Main {

	private static Manager manager;
	private static Employee[] employees;

	public static void main(String[] args) {
		// create latch & barrier
		CountDownLatch startLatch = new CountDownLatch( 1 ); // Main thread counts down once others are started
		CyclicBarrier endBarrier = new CyclicBarrier( 14, new Runnable() { // 12 employees, 1 manager, 1 clock
				public void run() {
					printStats();
				}
			});
		
		// create actors
		Clock clock = new Clock( startLatch, endBarrier );
		ConferenceRoom confRoom = new ConferenceRoom();
		manager = new Manager(clock);
		employees = new Employee[12];
		Employee[] teamLeads = new Employee[3];
		int counter = 0;
		for (int team = 1; team <= 3; team++) {
			for (int employeeNum = 1; employeeNum <= 4; employeeNum++) {
				Employee employee;
				if (employeeNum == 1) {
					employee = new Employee(team + "" + employeeNum, clock, confRoom, manager, null);
					teamLeads[team-1] = employee;
				} else {
					employee = new Employee(team + "" + employeeNum, clock, confRoom, manager, teamLeads[team-1]);
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
	
	//Statistics
	public static void printStats() {
		System.out.println("\n**********STATISTICS**********");
		int totalWorking = 0;
		int totalLunch = 0;
		int totalMeeting = 0;
		for (int a = 0; a < 12; a++) {
			Employee emp = employees[a];
			totalWorking += emp.getTimeWorked();
			totalLunch += emp.getLunchDuration();
			totalMeeting += emp.getTimeMeeting();
		}
		totalMeeting += manager.getTimeMeeting();
		int hoursWorked = totalWorking / 60 + 104; //add 8 hours for manager and each developer
		int minutesWorked = totalWorking % 60;
		int hoursLunch = totalLunch / 60 + 1; //add one hour for manager's lunch
		int minutesLunch = totalLunch % 60;
		int hoursMeeting = totalMeeting / 60;
		int minutesMeeting = totalMeeting % 60;
		System.out.println("Total time of manager and developers working: " + hoursWorked + " hours " + minutesWorked + " minutes");
		System.out.println("Total time of manager and developers at lunch: " + hoursLunch + " hours " + minutesLunch + " minutes");
		System.out.println("Total time of manager and developers meeting: " + hoursMeeting + " hours " + minutesMeeting + " minutes");
	}
}