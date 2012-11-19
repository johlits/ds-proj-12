import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class InputPopup extends Popup implements ActionListener {

	public final static int VCALM = 500;
	public final static int CALM = 300;
	public final static int AVERAGE = 100;
	public final static int BUSY = 50;
	public final static int VBUSY = 10;
    
	Road parent;

	ButtonGroup group;
	JRadioButton vCalm,calm,average,busy,vBusy,custom;
	JButton done;
	JTextField customTF;
	JPanel messagePanel;

	int ticks;

	public InputPopup(Road road, int x, int y) {

		super("Traffic Input",x,y,true);
		parent = road;
		mainPanel = new JPanel();
		group = new ButtonGroup();
		gridBag = new GridBagLayout();
		con = new GridBagConstraints();
		con.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.setLayout(gridBag);

		ticks = parent.busyness;
	
		addLabel("Input Traffic:",0,0);
	
		//Set correct radio button.
		boolean[] temp = new boolean[6];
		for (int i=0;i<temp.length;i++) temp[i] =false;
		switch(ticks) {
		case VCALM : temp[0] = true; break;
		case CALM : temp[1] = true; break;
		case AVERAGE : temp[2] = true; break;
		case BUSY : temp[3] = true; break;
		case VBUSY : temp[4] = true; break;
		default : temp[5] = true;
		}
		vCalm = addRadioButton("V Calm",temp[0],0,1,group);
		calm = addRadioButton("Calm",temp[1],0,2,group);
		average = addRadioButton("Average",temp[2],0,3,group);
		busy = addRadioButton("Busy",temp[3],0,4,group);
		vBusy = addRadioButton("V Busy",temp[4],0,5,group);
		custom = addRadioButton("Custom",temp[5],0,6,group);
	
		this.getContentPane().add(mainPanel,BorderLayout.NORTH);

		messagePanel = new JPanel();
		JLabel l = new JLabel("Car enters every " + ticks + " ticks");
		messagePanel.add(l);
		done = new JButton("Done");
		done.addActionListener(this);
		messagePanel.add(done,BorderLayout.SOUTH);

		this.getContentPane().add(messagePanel,BorderLayout.CENTER);
		this.getContentPane().validate();

		//Position and size the toolbar.
		setSize(200, 270);
		setLocation(x,y); //parent.getWidth(),parent.getY());
		setVisible(true);
		setLocation(x,y); //parent.getWidth(),parent.getY()); //twice to stop jump.
		validate();
	}

	public void setMessagePanel(int ticks) {
		this.getContentPane().remove(messagePanel);
	
		messagePanel = new JPanel();
		JLabel l = new JLabel("Car enters every "+ ticks + " ticks");
		messagePanel.add(l);

		done = new JButton("Done");
		done.addActionListener(this);
		messagePanel.add(done,BorderLayout.CENTER);

		this.getContentPane().add(messagePanel,BorderLayout.CENTER);
		this.getContentPane().validate();
	}

	public void setMessagePanel() {
		this.getContentPane().remove(messagePanel);

		messagePanel = new JPanel();
		JLabel l = new JLabel("Car enters every ");
		messagePanel.add(l,BorderLayout.WEST);

		customTF = new JTextField(3);
		customTF.addActionListener(this);
		messagePanel.add(customTF,BorderLayout.CENTER);

		JLabel l2 = new JLabel(" ticks");
		messagePanel.add(l2,BorderLayout.EAST);

		this.getContentPane().add(messagePanel,BorderLayout.CENTER);
		this.getContentPane().validate();
	}

	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == vCalm) {
	    ticks = VCALM;
	    setMessagePanel(ticks);
		}
		if (ae.getSource() == calm) {
	    ticks = CALM;
	    setMessagePanel(ticks);
		}
		if (ae.getSource() == average) {
	    ticks = AVERAGE;
	    setMessagePanel(ticks);
		}
		if (ae.getSource() == busy) {
	    ticks = BUSY;
	    setMessagePanel(ticks);
		}
		if (ae.getSource() == vBusy) {
	    ticks = VBUSY;
	    setMessagePanel(ticks);
		}
		if (ae.getSource() == custom) {
	    setMessagePanel();
		}
		if (ae.getSource() == customTF) {
	    try {
				ticks = Integer.parseInt(customTF.getText().trim());
	    } catch (NumberFormatException nfe) {
				System.out.println("NumberFormatException: "+nfe.getMessage());
	    }
	    parent.busyness = ticks;
	    this.dispose();
		} 
		if (ae.getSource() == done) {
	    parent.busyness = ticks;
	    this.dispose();
		}
	}
}






















