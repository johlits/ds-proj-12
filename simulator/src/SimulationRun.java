import java.util.Locale;

public class SimulationRun {
	
	public static void main(String[] args) {
		Locale.setDefault(Locale.US);
		
		final GraphicalUserInterface gui = new GraphicalUserInterface();
		final boolean preprocessed = true;
		
		String str = "a { 1 1 0 1 1 } * [ 1 1 1 1 1 ] b * c * d\n" +
				"{ 1 1 5 5 5 } *  { 1 1 5 5 5 } ^   v   ^\n" +
				"e * f * g * [ 2 1 ] h\n" +
				"*   *   * [ 1 1 ] * [ 1 1 ]\n" +
				"i * j * [ 1 1 2 5 5 ] k * [ 1 1 30 5 5 ] l";
		String cars = "a b l\nc d l";

		ManhattenLayout l = null;
		try {
			l = new ManhattenLayout(str, cars, 
			new LocalShortestPathRoutingWithTrafficLightsAndReservationZHLS());
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		final ManhattenLayout ml = l;
		final Simulation sim = ml.getSimulation();
		
		int upperlimit = 10000;
		
		if (preprocessed)
			for (int i = 0; !sim.isFinished() && i < upperlimit; i++)
				sim.progress(ml);
		String s = ml.toSVG(0, true, preprocessed);
		gui.setText(s, "simulator");
		//sim.save(s);
		
		if (!preprocessed)
			new Thread(new Runnable() {
				@Override
				public void run() {
					while (!sim.isFinished() && gui.isAlive()) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						sim.progress();
						gui.update(ml.toSVG(sim.getTick(), false, false),
								String.format("Tick %d", sim.getTick()));
					}
				}} ).start();
		else
			new Thread(new Runnable() {
				@Override
				public void run() {
					for (int i = 1; gui.isAlive(); i++) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						gui.setTitle(String.format("Tick %d", i));
					}
				}} ).start();

		gui.mainloop();
	}
}
