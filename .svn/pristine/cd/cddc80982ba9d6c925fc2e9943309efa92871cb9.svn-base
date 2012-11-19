import javax.swing.JPanel;
import javax.swing.Timer;

import java.awt.Image;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.MediaTracker;
import java.awt.Font;
import java.awt.Color;
import java.awt.Shape;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.awt.geom.Point2D;

import java.util.Iterator;
import java.util.Vector;

import time.*;

/**
 * The SimPanel class extends JPanel to display the visual simulation.
 * @author <a href="mailto:tf98@doc.ic.ac.uk">Thomas Fotherby</a>
 */
public class SimPanel extends JPanel implements Timed, MouseListener {

	public final static int savedDataPoints = 400;

	RoadNetwork roadNetwork;
	static TimeManager theTicker;
   
	//screen graphics variables
	Image backdrop,offscreen;
	private Dimension dc,offscreensize;
	Graphics2D offgraphics;

	//Vars to record simulation data.
	int frameNum;
	boolean recordData;
	int roadSurfaceLength;
	int[] throughFlowData,trafficSpeedData,congestionData;
	int dataIndex, dataCounter,carsOnScreen, averageCarSpeed,congestionMeasure;
       
	//Variables for mouse-events
	String clickMessage = "";
	Car trackingCar = null;
	Shape junctionComm = null;
	boolean showData = true;

	/**
	 * Creates a new <code>SimPanel</code> instance.
	 * @param backdrop A backdrop <code>Image</code> to use. 
	 * @param roadNetwork a <code>RoadNetwork</code>
	 * @param timeDelay an <code>int</code> value representing the clock tick delay.
	 */
	public SimPanel(Image backdrop, RoadNetwork roadNetwork, int timeDelay) {

		//Initilisation of all the variables.
		this.backdrop = backdrop;
		this.roadNetwork = roadNetwork;
		roadSurfaceLength = roadNetwork.getTotalRoadLength();
		recordData = true;
		frameNum = 0;
		throughFlowData  = new int[savedDataPoints];
		trafficSpeedData = new int[savedDataPoints];
		congestionData   = new int[savedDataPoints];
		dataIndex = 0;
		dataCounter = 0;
		carsOnScreen=-1;
		averageCarSpeed=-1; 
		congestionMeasure=-1;
		
		theTicker = new TimeManager(timeDelay);
		setSize(backdrop.getWidth(null),backdrop.getHeight(null));
		addMouseListener(this);
	}

	/** Starts the simulation */
	public void startSim() {
		theTicker.start(); 
		theTicker.addTimed(this);
		roadNetwork.addTimerToElements(theTicker);
	}

	/** Stops the simulation */
	public void stopSim() {
		theTicker.removeTimed(this);
		roadNetwork.kill();
		throughFlowData  = null;
		trafficSpeedData = null;
		congestionData   = null;
	}

	public boolean isStopped() {
		return (throughFlowData == null);
	}
	
	public void setPauseState(boolean pauseState) {
	 theTicker.setPaused(pauseState);
	}

	public void changeDelay(int newDelay) {
		theTicker.changeDelay(newDelay);
	}
	
	/** empty */
	public void pretick() {}

	/** Updates the screen */
	public void tick() {
		paintBackbuffer();
		repaint();
	}

	/** Paints the simulation scene onto the backbuffer. */
	private void paintBackbuffer() {
		//Initial Screen image setup.
		dc = getSize();
		if ((offscreen == null) || 
				(dc.width != offscreensize.width) ||
				(dc.height != offscreensize.height)) {
	    offscreen = this.createImage(dc.width, dc.height);
	    offscreensize = dc;
	    offgraphics = (Graphics2D)offscreen.getGraphics();
	    offgraphics.setFont(new Font("Serif",Font.BOLD,10));
	    offgraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
																	 RenderingHints.VALUE_ANTIALIAS_ON);
		}
		//Draws the back image
		offgraphics.drawImage(backdrop,0,0,this);

		//Draw the Network Components
		roadNetwork.drawNetworkComponents(offgraphics);

		//Records simulation data
		carsOnScreen = Car.carsEntered-Car.carsExited;
		if (recordData) recordData(carsOnScreen);
		
		//Supplies the user with information
		if (showData) {
			offgraphics.setColor(Color.red);
			offgraphics.drawString("Frame "+ frameNum++,15,20);
			offgraphics.drawString("CarsEntered "+Car.carsEntered,15,30);
			offgraphics.drawString("CarsExited "+Car.carsExited,15,40);
			offgraphics.drawString("Cars on Screen "+carsOnScreen,15,50);
			if (recordData && carsOnScreen != 0) {
				offgraphics.drawString("Average Speed: "+averageCarSpeed+" mph",15,60);
				offgraphics.drawString("Average Congestion: "+
															 congestionMeasure+"%",15,70);
			}
			offgraphics.setColor(Color.blue);
							
			if (trackingCar != null) {
				offgraphics.setStroke(Road.centerLine);
				offgraphics.draw(trackingCar.getFuturePath());
				clickMessage = "Car ID "+trackingCar.iD+
					", speed: "+trackingCar.getSpeedString()+
					", Object in front: "+trackingCar.getDistanceToNext()+
					"m, "+trackingCar.getSpeedOfNext()+"mph";
				offgraphics.setStroke(Road.normal);
			}
			
			if (junctionComm != null) {
				offgraphics.setStroke(Road.centerLine);
				offgraphics.draw(junctionComm);
				offgraphics.setStroke(Road.normal);
			}
			offgraphics.drawString(clickMessage,15,80);
		}
	}

	/**
	 * Overridden <em>not</em> to clear the front buffer.
	 * @param g a <code>Graphics</code> value
	 */
	public synchronized void update( Graphics g ) {
		paint(g);
	}
    
	/** Flip the back-buffer to the front buffer. */
	public synchronized void paint( Graphics g ) {
		if( offscreen == null ) paintBackbuffer();
		g.drawImage( offscreen, 0, 0, null );
	}

	/**
	 * Keep an array of the last n traffic data items.
	 */
	private void recordData(int carCount) {
		dataCounter++;
		if (dataCounter >= 10) {
	    dataCounter = 0;
	    if (dataIndex >= savedDataPoints) dataIndex = 0;
	    else {
				if (carCount == 0) {
					averageCarSpeed = -1;
					congestionMeasure = -1;
				} else {
					averageCarSpeed = roadNetwork.getAverageSpeed()/carCount;
					congestionMeasure = 
						(int)(((carCount*28)/(double)roadSurfaceLength)*100);
				}

				throughFlowData[dataIndex] = carCount;
				trafficSpeedData[dataIndex] = averageCarSpeed;
				congestionData[dataIndex] = congestionMeasure;
				dataIndex++;
			}
		}
	}

	/** Sets the recording of car-count numbers variables. */
	public void setRecordData(boolean value) {
		if (value) {
	    recordData = true;
			dataIndex = 0;
	    dataCounter = 0;
		} else {
			recordData = false;
		}
	}

	/** Sets the calculation of car-speeds. */
	public void setShowData(boolean value) {
		showData = value;
	}

	public int getavCarsOnScreen() {
		int sum = 0;
		for (int i=0;i<dataIndex;i++) {
			sum += throughFlowData[i];
		}
		return sum/dataIndex;
	}

	public int getavSpeed() {
		int sum = 0;
		for (int i=0;i<dataIndex;i++) {
			sum += trafficSpeedData[i];
		}
		return sum/dataIndex;
	}

	public int getavCongestion() {
		int sum = 0;
		for (int i=0;i<dataIndex;i++) {
			sum += congestionData[i];
		}
		return sum/dataIndex;
	}

	/** Sets whether junctions communicate or not */
	public void setJunctionComm(boolean value) {
		if (value) junctionComm = roadNetwork.getAdaptivePath();
		else {
			roadNetwork.randomizeAdaptivePath();
			junctionComm = null;
		}
	}
  
	//********************************* Mouse events *******************
  
	public void mousePressed(MouseEvent e) {

		trackingCar = null;

		//Check car queue at map inputs.
		int carsQing = roadNetwork.getInputQ(e.getX(),e.getY());
		if (carsQing >= 0) {
	    clickMessage = "Cars queueing: "+carsQing; 
		} else {

	    Point2D p = new Point2D.Double(e.getX(),e.getY());

	    //Check car information.
	    trackingCar = roadNetwork.getCar(p);
	    
			if (trackingCar == null) {
				//Check junction information.
				clickMessage = roadNetwork.getJunctionVars(p);
	    }
		}

		if (theTicker.isPaused()) {
			paintBackbuffer();
			repaint();
		}
	}

	public void mouseReleased(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
}














