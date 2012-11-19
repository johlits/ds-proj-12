/* a specialised Applet button to launch VISSIM */

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.applet.Applet;
import javax.swing.UIManager;

public class AppletButton extends Applet implements Runnable {
	Button button;
	Thread windowThread;
	boolean pleaseCreate = false;
	Main window = null;

	public void init() {
		setLayout(new BorderLayout());
		button = new Button("Launch VISSIM");
		button.setFont(new Font("Helvetica", Font.BOLD, 18));
		button.addActionListener(new ButtonAction());
		add("Center",button);
	}

	public void start() {
		if (windowThread == null) {
			windowThread = new Thread(this);
			windowThread.start();
		}
	}

	public void stop() {
		windowThread=null;
		if (window!=null) window.dispose();
	}
    
	public void ended() {
		if (window!=null) window=null;
	}

	public synchronized void run() {
		while (windowThread != null) {
			while (pleaseCreate == false) {
				try { wait();} catch (InterruptedException e) {}
			}
			pleaseCreate=false;
			try {
				String lf = UIManager.getSystemLookAndFeelClassName();
				UIManager.setLookAndFeel(lf);
			} catch(Exception e) {}
			if (window==null) {
				showStatus("Please wait while the window comes up...");
				window = new Main(this);
				window.setTitle("Visual Traffic Simulation");
				window.pack();
				Main.centre(window);
				window.setVisible(true);
				showStatus("The program is now ready to use");
			}
		}
	}
    
	synchronized void triggerWindow() {
		pleaseCreate = true;
		notify();
	}
    
	class ButtonAction implements ActionListener {
		public void actionPerformed(ActionEvent e) { triggerWindow();}
	}

}
