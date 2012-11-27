import java.util.Locale;


public class SimulationRun {
	
	public static void main(String[] args) {
		Locale.setDefault(Locale.US);
		
		final GraphicalUserInterface gui = new GraphicalUserInterface();
		
		String str = "a { 5 1 0 5 5 } * [ 5 1 0 5 5 ] b * c * d\n" + 
				"{ 5 1 5 5 5 } *  { 1 1 5 5 5 } ^   v   ^\n" + 
				"e * f * g * h\n" + 
				"*   *   *  { 5 1 0 5 5 } *  [ 5 1 1 5 5 ]\n" + 
				"i * j * [ 5 1 40 5 5 ] k * [ 5 1 1 5 5 ] l";
		String cars = "a b l\na e l";
		
		ManhattenLayout l = null;
		try {
			l = new ManhattenLayout(str, cars, new SimpleRouting());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		final ManhattenLayout ml = l;
		final Simulation sim = ml.getSimulation();
		
		
		while (!sim.isFinished())
			sim.progress(ml);
		//sim.save(ml.toSVG(0, true, true));
		
		
		gui.setText(ml.toSVG(0, true, true), "simulator");
		/*
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
					gui.setText(ml.toSVG(sim.getTick(), true, false),
							String.format("Tick %d", sim.getTick()));
				}
			}} ).start();
		*/
		gui.mainloop();		
	}
}
