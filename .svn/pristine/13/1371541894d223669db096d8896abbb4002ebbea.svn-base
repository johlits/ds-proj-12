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

import java.awt.Frame;

import java.awt.image.BufferedImage;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.awt.geom.Point2D;

import java.util.Iterator;
import java.util.Vector;

import time.*;

public class SimPanel extends JPanel implements Timed {

	Image backdrop,offscreen;
	RoadNetwork roadNetwork;
	static TimeManager theTicker;
   
	int maxX,maxY,frameNum = 0;
	private Dimension dc,offscreensize;
	Graphics2D offgraphics;

	public SimPanel(RoadNetwork roadNetwork) {

		maxX = roadNetwork.width;
		maxY = roadNetwork.height;

		this.backdrop = roadNetwork.backdrop;
		this.roadNetwork = roadNetwork;

		setSize(maxX,maxY);
	
		theTicker = new TimeManager(roadNetwork.timeDelay);
	}

	public void startSim() {
		System.out.println("In startSim");

		theTicker.start(); 
		theTicker.addTimed(this);
		roadNetwork.addTimerToElements(theTicker);
	}

	public void stopSim() {
		theTicker.removeTimed(this);
		roadNetwork.kill();
		//theTicker = null;
	}

	public TimeManager getTimer() { return theTicker; }

	public void pretick() {}

	public void tick() {
		paintBackbuffer();
		repaint();
	}

	/** Paints the scene onto the backbuffer. */
	private void paintBackbuffer() {
		dc = getSize();
		if (dc == null) System.out.println("dc is null");
		if ((offscreen == null) || (dc.width != offscreensize.width)
				|| (dc.height != offscreensize.height)) {
	    offscreen = this.createImage(dc.width, dc.height);
	    offscreensize = dc;
	    offgraphics = (Graphics2D)offscreen.getGraphics();
	    offgraphics.setFont(new Font("Serif",Font.BOLD,10));
	    offgraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
																	 RenderingHints.VALUE_ANTIALIAS_ON);
		}
		offgraphics.drawImage(backdrop,0,0,this);
		roadNetwork.drawAllCars(offgraphics);

		//Supplies the user with information
		offgraphics.setColor(Color.red);
		offgraphics.drawString("Frame "+ frameNum++,15,20);
		offgraphics.drawString("CarsEntered "+Car.carsEntered,15,30);
		offgraphics.drawString("CarsExited "+Car.carsExited,15,40);
		int carsOnScreen = Car.carsEntered-Car.carsExited;
		offgraphics.drawString("Cars on Screen "+carsOnScreen,15,50);
	}

	/** Overridden <em>not</em> to clear the front buffer. */
	public synchronized void update( Graphics g ) {
		paint(g);
	}
    
	/*  Flip the back-buffer to the front buffer. */
	public synchronized void paint( Graphics g ) {
		if( offscreen == null ) paintBackbuffer();
		g.drawImage( offscreen, 0, 0, null );
	}

	//**********************************************************************
	//******************** drawing the roads *******************************
	//**********************************************************************

	public void loadJunction() {

	}

}














