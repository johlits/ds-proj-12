import java.util.Locale;

public class SimulationRun {
	
	public static void main(String[] args) {
		Locale.setDefault(Locale.US);
		
		final GraphicalUserInterface gui = new GraphicalUserInterface();
		final boolean preprocessed = false;
		
		StringBuilder str = new StringBuilder("");
		StringBuilder cars = new StringBuilder("");
		
		Kattio io = new Kattio(System.in, System.out);
		int lines = io.getInt();
		for (int i = 0; i < lines; i++) 
			str.append(io.readLine()+"\n");
		lines = io.getInt();
		for (int i = 0; i < lines; i++) 
			cars.append(io.readLine()+"\n");

		ManhattenLayout l = null;
		try {
			l = new ManhattenLayout(str.toString(), cars.toString(), 
			new ADPPSmart());
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
		io.close();
	}
}
