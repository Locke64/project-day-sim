public class Clock extends Thread {

	public static final int DAY_START_HOUR = 8;
	public static final int DAY_START_MINUTE = 0;
	public static final int DAY_END_HOUR = 5;
	public static final int DAY_END_MINUTE = 0;
	public static final LocalTime DAY_START = LocalTime.of(DAY_START_HOUR, DAY_START_MINUTE);
	public static final LocalTime DAY_END = LocalTime.of(DAY_END_HOUR, DAY_END_MINUTE);

	private LocalTime time;

	public void run() {
		time = LocalTime.of(DAY_START_HOUR, DAY_START_MINUTE);
		while(DAY_END.compareTo(time) > 0) {
			time.plusMinutes(1);
			//notifyAll();
		}
	}
	
	public LocalTime getTime() {
		return time;
	}
}