import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

			System.out.printf("[%s]\n", r.getClass().getName());
			if (i == upperlimit)
				System.out.println(" never finished");
			else
				System.out.printf(" time to finish: %d\n", i);
			/* car stats */
			ArrayList<Integer> carStats = sim.getVehicleFinishTimes();
			Collections.sort(carStats);
			if(carStats.size() > 0)
			System.out.printf(" car stats:\n  min time: %d\n  max time: %d\n  avg time: %f\n  median: %f\n",
				carStats.get(0), carStats.get(carStats.size() - 1), calculateAverage(carStats), calculateMedian(carStats));
		}
	}

	public static double calculateAverage (List<Integer> values) {
		int sum = 0;
		for (int v : values)
			sum += v;
		return (double)sum / (double)values.size();
	}

	public static double calculateMedian (ArrayList<Integer> values)
	{
		if (values.size() % 2 == 1)
			return values.get((values.size()+1)/2-1);
		else
			return (values.get(values.size()/2-1) + values.get(values.size()/2)) / 2.0;
	}
}
