
public class AttributeSet {
	int distance;
	int capacity;
	int trafficLightOffset = -1;
	int trafficLightGreenCycle;
	int trafficLightRedCycle;

	public String toString () {
		String s = Integer.toString(distance) + " " + Integer.toString(capacity);
		if (trafficLightOffset != -1)
			s +=	" " + Integer.toString(trafficLightOffset)
				+	" " + Integer.toString(trafficLightGreenCycle)
				+	" " + Integer.toString(trafficLightRedCycle);
		return s;
	}
}
