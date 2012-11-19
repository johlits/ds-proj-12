import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Component;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.Box;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JTextField;

public class Popup extends JFrame implements ActionListener {

	JPanel mainPanel;
	GridBagLayout gridBag;
	GridBagConstraints con;
    
	public Popup(String title, int x, int y, boolean alwaysOnTop) {
		super(title);

		if (alwaysOnTop) {
			addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						dispose();
					}
					public void windowDeactivated(WindowEvent e) {
						Window w = e.getWindow();
						w.toFront();
						validate();
					}
				});
		} else {
			//Allow exiting
			addWindowListener(new WindowAdapter()
				{public void windowClosing(WindowEvent e)
					{dispose();}
				});
		}
	}

	//To be overwridden
	public void actionPerformed(ActionEvent ae) {}

	public void addLabel(String label,int x,int y) {
		JLabel l = new JLabel(label);
	
		con.gridx = x;
		con.gridy = y;
		gridBag.setConstraints(l,con);

		mainPanel.add(l);
	}

	public void addSeperator(int x,int y) { 
		Component c = Box.createRigidArea(new Dimension(30,20));
	
		con.gridx = x;
		con.gridy = y;
		gridBag.setConstraints(c,con);

		mainPanel.add(c);
	}

	public JComboBox addComboBox(String[] options, int index,int x,int y) {
		JComboBox list = new JComboBox(options);
		list.setSelectedIndex(index);

		con.gridx = x;
		con.gridy = y;
		gridBag.setConstraints(list,con);

		list.addActionListener(this);
		mainPanel.add(list);
		return list;
	}

	public JRadioButton addRadioButton(String name, boolean selected,int x,
																		 int y,ButtonGroup group) {
		JRadioButton radBut = new JRadioButton(name);
		radBut.setSelected(selected);

		con.gridx = x;
		con.gridy = y;
		gridBag.setConstraints(radBut,con);

		radBut.addActionListener(this);
		group.add(radBut);
		mainPanel.add(radBut);
		return radBut;
	}

	public JTextField addTextField(int length,int x,int y) {
		JTextField textField = new JTextField(length);

		con.gridx = x;
		con.gridy = y;
		gridBag.setConstraints(textField,con);

		textField.addActionListener(this);
		mainPanel.add(textField);
		return textField;
	}

	public JButton addButton(String name,int x,int y,boolean enabled) {
		JButton button = new JButton(name);
		con.gridx = x;
		con.gridy = y;
		gridBag.setConstraints(button,con);

		button.setEnabled(enabled);
		button.addActionListener(this);   
		mainPanel.add(button);
		return button;
	}

	public JCheckBox addCheckBox(String name, int x,int y, boolean selected) {
		JCheckBox box= new JCheckBox(name,selected);
		con.gridx = x;
		con.gridy = y;
		gridBag.setConstraints(box,con);

		box.addActionListener(this);   
		mainPanel.add(box);
		return box;
	}
}









