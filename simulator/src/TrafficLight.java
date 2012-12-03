
public class TrafficLight {
	private int a;
	private int b;
	private int c;
	
	/**
	 * @param start point of time for start of first green period
	 * @param greencycle duration of a green period
	 * @param redcycle duration of a red period
	 */
	public TrafficLight (int start, int greencycle, int redcycle) {
		this.a = start;
		this.b = redcycle + greencycle;
		this.c = greencycle;
	}
	
	public boolean isGreen(int tick) {
		return tick >= a && ((b + tick - a) % b) < c;
	}
	
	public int remainingTimeToNextGreen(int tick) {
		return tick >= a ? b - ((b + tick - a) % b) : a - tick;
	}

	public int remainingTimeToNextRed(int tick) {
		return remainingTimeToNextGreen(tick) + c;
	}
	
	public int remainingWaitingTime(int tick) {
		return isGreen(tick) ? 0 : remainingTimeToNextGreen(tick);
	}
	
	public int getStart () {
		return a;
	}
	
	public int getRedCycle () {
		return b - c;
	}
	
	public int getGreenCycle () {
		return c;
	}
}
