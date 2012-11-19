import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.Color;
import java.awt.Shape;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.Box;
import javax.swing.WindowConstants;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.JScrollPane;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.event.*;



public class JunctionPopup extends Popup implements ActionListener,ComponentListener {

	Junction junction;
	JComboBox junctionTypeBox;
	String[] junctionOps;
	PathPanel pathPanel;
	JPanel donePanel;
	JButton doneButton;

	MyTableModel myModel;
	JTable table;

	public JunctionPopup(Junction junction, int x, int y) {
		super("Junction Specification",x,y,true);
		this.junction = junction;

		mainPanel = new JPanel();
		pathPanel = new PathPanel();
		donePanel = new JPanel();

		gridBag = new GridBagLayout();
		con = new GridBagConstraints();
		con.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.setLayout(gridBag);
		
		//MainPanel
		addLabel("Junction type Options:",0,0);
		junctionOps = new String[Junction.JUNCTION_TYPES];
		for (int i=0;i<Junction.JUNCTION_TYPES;i++) {
			junctionOps[i] = i+") "+Junction.getName(i);
		}

		junctionTypeBox = addComboBox(junctionOps,junction.junctionType,0,1);
		
		//donePanel
		doneButton = new JButton("Done");
		doneButton.addActionListener(this);
		donePanel.add(doneButton);

		this.getContentPane().add(mainPanel,BorderLayout.WEST);
		this.getContentPane().add(pathPanel,BorderLayout.CENTER);
		this.getContentPane().add(donePanel,BorderLayout.SOUTH);

		if (junction.junctionType == Junction.SIGNALLED || 
				junction.junctionType == Junction.ALTSIGNALLED) {

			//The Table
			myModel = new MyTableModel();
			table = new JTable(myModel);
			table.setPreferredScrollableViewportSize(new Dimension(300, 70));
			
			//Create the scroll pane and add the table to it. 
			JScrollPane scrollPane = new JScrollPane(table);
			this.getContentPane().add(scrollPane, BorderLayout.EAST);
		}

		this.getContentPane().validate();

		//Position and size the toolbar.
		setLocation(x,y);
		pack();
		setVisible(true);
		
		//Allow exiting
		addWindowListener(new WindowAdapter()
	    {public void windowClosing(WindowEvent e)
				{updateInfo();}
	    });
	}

	public void componentHidden(ComponentEvent e) {}
	public void componentMoved(ComponentEvent e) {}
	public void componentResized(ComponentEvent e) {pathPanel.repaint();}
	public void componentShown(ComponentEvent e) {}

	public void updateInfo() {
				
		if (junction.junctionType == Junction.SIGNALLED || 
				junction.junctionType == Junction.ALTSIGNALLED) {
			
			junction.totalTime = ((Integer)table.getValueAt(0,0)).intValue();
			junction.vehicle_Actuated = ((Boolean)table.getValueAt(0,1)).booleanValue();
			junction.adaptive = ((Boolean)table.getValueAt(0,2)).booleanValue();
		}
		junction.junctionType = junctionTypeBox.getSelectedIndex();
		this.dispose();
		junction.updateJunctionLook();
	}

	public void actionPerformed(ActionEvent ae) {

		if (ae.getSource() == doneButton) {
	    System.out.println("Done pressed");
	    updateInfo();
		}
	}

	class PathPanel extends JPanel {
	
		public PathPanel() {
	    setSize(200, 200);
	    repaint();
		}

		public Dimension getPreferredSize() {
			return new Dimension(200, 200);
		}
	
		public void paint( Graphics g ) {
	    g.setFont(new Font("Serif",Font.BOLD,8));
	    Dimension dim = getSize();
	    int w = dim.width;
	    int h = dim.height;

	    g.setColor(Color.white);
	    g.fillRect(0,0,w,h);

	    g.setColor(Color.black);
	    g.drawRect(1,1,w-2,h-2);

	    //Get the values so that the junction will be drawn in the center.
	    float[] x = new float[9];
	    float[] y = new float[9];

	    for (int i=0;i<9;i++) {
				x[i] = junction.x[i];
				y[i] = junction.y[i];
	    }
	    
	    float minx = 10000, miny=10000, maxx = 0, maxy=0;
	    
	    for (int i=0;i<9;i++) {
				if (x[i] < minx) minx = x[i];
				if (y[i] < miny) miny = y[i];
				if (x[i] > maxx) maxx = x[i];
				if (y[i] > maxy) maxy = y[i];
	    }

	    minx -= w/2-((maxx-minx)/2);
	    miny -= h/2-((maxy-miny)/2);

	    for (int i=0;i<9;i++) {
				x[i] -= minx;
				y[i] -= miny;
	    }

	    junction.drawSimpleJunction((Graphics2D)g,x,y,getShape(x,y));

	    //Add a legend
	    g.setColor(Color.green);
	    g.fillOval(7,7,14,14);
	    g.setColor(Color.black);
	    g.drawString("Inputs",25,14);
	    g.fillOval(7,20,14,14);
	    g.drawString("Outputs",25,30);
	    g.drawString("N,S,E,W is North,South,East,West",5,45);

	    //Now draw the inputs and outputs:
	    Point2D tempPoint;
	    int tempX=0,tempY=0;
	    
	    for (int i=2; i<9;i+=2) {
				if (junction.road[i] != -1) {
					Road tempRoad = junction.parent.getRoad(junction.road[i]);
					if (junction.isRoadStart[i]) {
			
						if (tempRoad.isSingleLane()) {
							tempPoint = tempRoad.getLaneStartPoint(0);
							tempX = (int)(tempPoint.getX() - minx);
							tempY = (int)(tempPoint.getY() - miny);
							g.setColor(Color.black);
							g.fillOval(tempX-6,tempY-6,12,12);
						}
			
						for (int j=1; j<Road.maxLaneNum;j++) {
							if (tempRoad.getLane(j) != null) {
								if (j%2 == 0) {
									g.setColor(Color.green);
									tempPoint = tempRoad.getLaneEndPoint(j);
								} else {
									g.setColor(Color.black);
									tempPoint = tempRoad.getLaneStartPoint(j);
								}
								tempX = (int)(tempPoint.getX() - minx);
								tempY = (int)(tempPoint.getY() - miny);
								g.fillOval(tempX-7,tempY-7,14,14);
								if (j%2 == 0) {
									g.setColor(Color.black);
									g.drawString(getDir(i)+j,tempX-6,tempY+4);
								} else {
									g.setColor(Color.white);
									g.drawString(getDir(i)+j,tempX-6,tempY+4);
								}
							}
						}
					} else {
						if (tempRoad.isSingleLane()) {
							tempPoint = tempRoad.getLaneEndPoint(0);
							tempX = (int)(tempPoint.getX() - minx);
							tempY = (int)(tempPoint.getY() - miny);
							g.setColor(Color.green);
							g.fillOval(tempX-6,tempY-6,12,12);
						}
			
						for (int j=1; j<Road.maxLaneNum;j++) {
							if (tempRoad.getLane(j) != null) {
								if (j%2 != 0) {
									g.setColor(Color.green);
									tempPoint = tempRoad.getLaneEndPoint(j);
								} else {
									g.setColor(Color.black);
									tempPoint = tempRoad.getLaneStartPoint(j);
								}
								tempX = (int)(tempPoint.getX() - minx);
								tempY = (int)(tempPoint.getY() - miny);
								g.fillOval(tempX-7,tempY-7,14,14);
								if (j%2 != 0) {
									g.setColor(Color.black);
									g.drawString(getDir(i)+j,tempX-6,tempY+4);
								} else {
									g.setColor(Color.white);
									g.drawString(getDir(i)+j,tempX-6,tempY+4);
								}
							}
						}
			
					}
				}
	    }
		}

		public String getDir(int i) {
	    switch(i) {
	    case 2 : return "N";
	    case 4 : return "E";
	    case 6 : return "S";
	    case 8 : return "W";
	    default : return "ERROR";
	    }
		}

		public Shape getShape(float[] x, float[] y) {
	    GeneralPath jShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
	    jShape.moveTo(x[1],y[1]);
	    jShape.lineTo(x[3],y[3]);
	    jShape.lineTo(x[5],y[5]);
	    jShape.lineTo(x[7],y[7]);
	    jShape.closePath();
	    return jShape;
		}
	} 

	class MyTableModel extends AbstractTableModel {

		final String[] columnNames = {"Total Time",
																	"Vehicle Actuated",
																	"Singly Adaptive"};
		final Object[][] data = {
			{new Integer(junction.totalTime), 
	     new Boolean(junction.vehicle_Actuated), 
	     new Boolean(junction.adaptive)}
		};

		public MyTableModel() {}

		public int getColumnCount() {
			return columnNames.length;
		}
        
		public int getRowCount() {
			return data.length;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		/*
		 * JTable uses this method to determine the default renderer/
		 * editor for each cell.  If we didn't implement this method,
		 * then the last column would contain text ("true"/"false"),
		 * rather than a check box.
		 */
		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		public boolean isCellEditable(int row, int col) {
	    return true;
		}

		public void setValueAt(Object value, int row, int col) {
	    data[row][col] = value;
	    fireTableCellUpdated(row, col);
		}
	}
}





 








