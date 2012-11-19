import java.awt.BorderLayout;
import javax.swing.JApplet;
import java.io.InputStream;

public class Main extends JApplet {
    
	SimPanel simPanel;
	RoadNetwork roadNetwork;
	    
	public void init() {

		System.out.println("in init()");

		String specFile = getParameter("specFile");		
		InputStream xmlIN = this.getClass().getResourceAsStream(specFile);
		
		if (xmlIN == null) {
			System.out.println("Couldn't find spec file. Used default");
			xmlIN = this.getClass().getResourceAsStream("backup_specFile.XML");
		}

		roadNetwork= new RoadNetwork(xmlIN);
		simPanel = new SimPanel(roadNetwork);

		this.getContentPane().add(simPanel,BorderLayout.CENTER);
		this.getContentPane().validate();
	}

	public void start() {
		System.out.println("in start()");

		simPanel.startSim();
	}

	public void stop() {
		System.out.println("in stop()");

		simPanel.stopSim();
		//simPanel = null;
	}
}











