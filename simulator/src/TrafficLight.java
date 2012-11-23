
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
	
	public int remainingWaitingTime(int tick) {
		return isGreen(tick) ? 0 : b - ((b + tick - a) % b);
	}
}
