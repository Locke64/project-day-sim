public class Main {

	public static void main(String[] args) {
		Manager manager = new Manager();
		Employee[] employees = new Employee[12];
		int counter = 0;
		for (int team = 1; team <= 3; team++) {
			for (int employee = 1; employee <= 4; employee++) {
				employees[counter] = new Employee(team + "" + employee);
				counter++;
			}
		}
		for (int a = 0; a < 12; a++) {
			employees[a].start();
		}
	}
}