package time;

import java.util.*;
import java.awt.event.*;

/**
 * Class providing functions to manage this timing thread.
 *
 * @author <a href="mailto:jnm@doc.ic.ac.uk">Jeff Magee</a>
 */
public class TimeManager extends Thread
	implements AdjustmentListener {
	volatile int delay;
	volatile boolean paused;
	public int ticks = 0;
	ImmutableList clocked = null;
    
	/**
	 * Creates a new <code>TimeManager</code> instance.
	 *
	 * @param d a int that specifies the delay in millisecond between each clock tick of this timer.
	 */
	public TimeManager(int d) {delay = d; }
    
	/**
	 * Method to add an object to the timer
	 *
	 * @param el a object implementing the Timed interface.
	 */
	public void addTimed(Timed el) {
		clocked = ImmutableList.add(clocked,el);
	}
    
	/**
	 * Describe <code>removeTimed</code> method here.
	 *
	 * @param el a <code>Timed</code> value
	 */
	public void removeTimed(Timed el) {
		clocked = ImmutableList.remove(clocked,el);
	}
   
	public boolean isPaused() {
		return paused;
	}
 
	/**
	 * A method to change the timers delay. Used by a slider bar for example.
	 *
	 * @param e an <code>AdjustmentEvent</code>
	 */
	public void adjustmentValueChanged(AdjustmentEvent e) {
		delay = e.getValue();
	}
    
	/**
	 * Stops the timer from generating events.
	 *
	 * @param p a <code>boolean</code> value
	 */
	public void setPaused(boolean p) {
		paused = p;
	}

	/**
	 * A method to change the timers delay.
	 *
	 * @param d a int that specifies the delay in millisecond between each clock tick of this timer.
	 */
	public void changeDelay(int d) {
		delay = d;
	}
    
	/**
	 * The run() method for the timer thread. Every d milliseconds, the timer calls pretick() and then tick() on all the objects it has been given.
	 *
	 */
	public void run () {
		try {
	    while(true) {
				if (!paused) {
					try {
						Enumeration e = ImmutableList.elements(clocked);
						while (e.hasMoreElements()) //pretick broadcast
							((Timed)e.nextElement()).pretick();
			
						e = ImmutableList.elements(clocked);
		    
						while (e.hasMoreElements()) //tick broadcast
							((Timed)e.nextElement()).tick();
						ticks++;
					} catch (TimeStop s) {
						System.out.println("*********** TimeStop *************");
						return;
					}
				}
				Thread.sleep(delay);
	    }
		} catch (InterruptedException e){}
	}
}








