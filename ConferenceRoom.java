public class ConferenceRoom {

	private boolean occupied = false;
	
	public synchronized void enter() {
		while( occupied ) {
			try {
				wait();
			} catch( InterruptedException e ) {
				e.printStackTrace();
			}
		}
		occupied = true;
	}
	
	public synchronized void leave() {
		occupied = false;
		notifyAll();
	}
}