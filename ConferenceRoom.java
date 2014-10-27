// Shared resource used for team standup meetings
public class ConferenceRoom {

	// the lock
	private boolean occupied = false;
	
	// acquire the lock
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
	
	// release the lock
	public synchronized void leave() {
		occupied = false;
		notifyAll();
	}
}