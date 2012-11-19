import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Component;
import java.awt.Color;
import java.awt.Rectangle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.Box;
import javax.swing.JCheckBox;

public class StatsPopup extends Popup implements ActionListener {
    
	SimPanel simPanel;
	JButton refresh,done;
	
	public StatsPopup(SimPanel simPanel) {
		super("Statistics SnapShot",250,350,false);
		this.simPanel = simPanel;
		
		setUpPanel();

		this.getContentPane().add(mainPanel,BorderLayout.CENTER);
		this.getContentPane().validate();

		//Position and size the toolbar.
		setSize(250, 350);
		setLocation(simPanel.getX(),simPanel.getY());
		validate();
		setVisible(true);
		setLocation(simPanel.getX(),simPanel.getY()); //twice to stop jump.
		validate();
	} 

	public void setUpPanel() {
		if (simPanel.isStopped()) {
			dispose();
			return;
		}
		mainPanel = new JPanel();
		gridBag = new GridBagLayout();
		con = new GridBagConstraints();
		con.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.setLayout(gridBag);
		int i=0;
		addLabel("At Frame "+simPanel.frameNum,0,i++);
		addSeperator(0,i++);
	  addLabel("CarsEntered: "+Car.carsEntered,0,i++);
		addLabel("CarsExited: "+Car.carsExited,0,i++);
		addLabel("Cars on the screen: "+simPanel.carsOnScreen,0,i++);
		addLabel("Through Put: "+Car.carsExited/(simPanel.frameNum/10)+
						 " (cars per 10 frames)",0,i++);
		addLabel("------------------------------",0,i++);
		addLabel("From frame "+(simPanel.frameNum-simPanel.dataIndex*10)+
						 " to "+simPanel.frameNum,0,i++);
		addLabel("Data values: "+simPanel.dataIndex,0,i++);
		addLabel("Average cars on screen: "+simPanel.getavCarsOnScreen(),0,i++);
 		addLabel("Average car speed: "+simPanel.getavSpeed()+"mph",0,i++);
		addLabel("Average road congestion: "+simPanel.getavCongestion()+"%",0,i++);
		addSeperator(0,i++);
		refresh = addButton("Refresh",0,i++,true);
		done = addButton("Exit",0,i++,true);
	}
	
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == refresh) {
			this.getContentPane().remove(mainPanel);
			setUpPanel();
			this.getContentPane().add(mainPanel,BorderLayout.CENTER);
			this.getContentPane().validate();
		}
		if (ae.getSource() == done) {
			dispose();
		}
	}
}












