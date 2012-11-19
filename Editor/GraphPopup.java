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

public class GraphPopup extends Popup implements ActionListener {
    
	GraphPanel graphPanel;
	SimPanel simPanel;
	Timer timer;
	Updater updater;
	JCheckBox throughFlowBut,avSpeedBut,congestionBut;
	JButton resetBut;

	public GraphPopup(SimPanel simPanel) {
		super("Traffic Graph",100,100,false);
		this.simPanel = simPanel;
		graphPanel = new GraphPanel();
	
		mainPanel = new JPanel();
		gridBag = new GridBagLayout();
		con = new GridBagConstraints();
		con.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.setLayout(gridBag);

		throughFlowBut = addCheckBox("Cars on Screen",0,1,true);
		avSpeedBut = addCheckBox("Average Speed",0,2,false);
		congestionBut = addCheckBox("Congestion",0,3,false);
		resetBut = addButton("Reset",0,4,true);
		
		this.getContentPane().add(mainPanel,BorderLayout.WEST);
		this.getContentPane().add(graphPanel,BorderLayout.CENTER);
		this.getContentPane().validate();
	
		//Position and size the toolbar.
		setSize(500, 300);
		setLocation(simPanel.getX(),simPanel.getY());
		validate();
		setVisible(true);
		setLocation(simPanel.getX(),simPanel.getY()); //twice to stop jump.
		validate();
		
		timer = new Timer();
		updater = new Updater();
		timer.schedule(updater,100,1000);
	}

	public void actionPerformed(ActionEvent ae) {
	
		//File Menu ******************************************************

		if (ae.getSource() == throughFlowBut) {
			graphPanel.drawThroughFlow = throughFlowBut.isSelected();
		}
		if (ae.getSource() == avSpeedBut) {
			graphPanel.drawSpeed = avSpeedBut.isSelected();
		}
		if (ae.getSource() == congestionBut) {
			graphPanel.drawCongestion = congestionBut.isSelected();
		}
		if (ae.getSource() == resetBut) {
			simPanel.setRecordData(true);
		}
		graphPanel.repaint();
	}

	class Updater extends TimerTask {
		public void run(){
			if (simPanel.isStopped()) dispose();
			else graphPanel.repaint();
		}
	}

	class GraphPanel extends JPanel {

		boolean drawThroughFlow,drawSpeed,drawCongestion;

		public GraphPanel() {
			drawThroughFlow = true;
			drawSpeed = false;
			drawCongestion = false;
		}		

		public void paint( Graphics g ) {
			
			Dimension dim = getSize();
	    int w = dim.width;
	    int h = dim.height;
	    
	    g.setColor(Color.white);
	    g.fillRect(0,0,w,h);
			int offset = 0;
			if (drawThroughFlow) 
				drawGraphLine(g,w,h,offset++,simPanel.throughFlowData,
											"",Color.black);
			if (drawSpeed) 
				drawGraphLine(g,w,h,offset++,simPanel.trafficSpeedData,
											"mph",Color.blue);
			if (drawCongestion) 
				drawGraphLine(g,w,h,offset++,simPanel.congestionData,
											"%",Color.red);
		}

		public void drawGraphLine(Graphics g,int w, int h,int off,int[] dataArray,
															String xAxisMessage,Color lineColour) {
	    
			g.setColor(lineColour);   

	    double unitsx;
	    if (simPanel.dataIndex == 0) unitsx = 0;
	    else unitsx = (double)w/(double)simPanel.dataIndex;

	    int max = 0;
	    for (int i=0; i < simPanel.dataIndex;i++) {
				if (dataArray[i] > max) 
					max = dataArray[i];
	    }

	    double unitsy;
	    if (max == 0) unitsy = 0;
			else          unitsy = (double)h/(double)max;

			g.drawString("Data Points "+simPanel.dataIndex+" of "+
									 simPanel.savedDataPoints,w/2,10);
	    
			g.drawString(xAxisMessage,10+off*15,h/2+off*15);

			g.drawString("Time "+(simPanel.frameNum-simPanel.dataIndex*10)+
									 " to "+simPanel.frameNum,w/2,h);
				    	    
	    if (unitsy != 0) {
				for (int i=h-10;i>=0;i-=20) {
					g.drawString(""+(int)(i/unitsy),5+off*15,h-i);
				}
	    }

			double xp=0,yp=h,xn=0,yn=(int)(h-dataArray[0]*unitsy);
	    for (int i=0; i < simPanel.dataIndex;i++) {
				g.drawLine((int)xp,(int)yp,(int)xn,(int)yn);
				xp = xn;
				yp = yn;
				xn = i*unitsx;
				yn = h-dataArray[i]*unitsy;
	    }
		}
	} 

}










