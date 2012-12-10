import java.util.Locale;

public class SilentSimulationRun {
	
	public static void main(String[] args) {
		Locale.setDefault(Locale.US);
		
		String str = "a { 1 1 0 1 1 } * [ 1 1 0 1 1 ] b * c * [ 7 7 ] d\n" +
				"{ 1 1 5 5 5 } *  { 1 1 5 5 5 } ^   v   ^\n" +
				"e * f * g * [ 2 1 ] h\n" +
				"*   *   * [ 1 1 ] * [ 5 5 ]\n" +
				"i * j * [ 1 1 2 5 5 ] k * [ 1 1 30 5 5 ] l";
		String cars = "a b l\nc d l\ne f l";
		
		RoutingAlgorithm[] algos = new RoutingAlgorithm[] { 
				new RandomRouting(),
				new SimpleRouting(),
				new ADPP(),
				new ADPPSmart(),
				new LocalShortestPathRouting(),
				new LocalShortestPathRoutingWithTrafficLights(),
				new LocalShortestPathRoutingWithTrafficLightsAndReservation()
		};
		
		/* measure algorithms */
		
		for (RoutingAlgorithm r : algos) {
			ManhattenLayout ml = null;
			try {
				ml = new ManhattenLayout(str.toString(), cars.toString(), r);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			Simulation sim = ml.getSimulation();
			int upperlimit = 100000;
			int i = 0;
			for (i = 0; !sim.isFinished() && i < upperlimit; i++)
				sim.progress();
			System.out.printf("algorithm: %s took %d ticks to finish\n", r.getClass().toString(), i);
		}
	}
}
