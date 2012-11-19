import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Stroke;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Polygon;
import java.awt.Image;
import java.awt.Point;
import java.awt.TexturePaint;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Color;

import java.awt.image.BufferedImage;

import java.awt.geom.PathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
import java.awt.geom.Area;

import java.io.InputStream;
import java.io.IOException;

import java.util.Vector;
import java.util.Iterator;

import javax.swing.ImageIcon;

import time.*;
import XML.*;
import Utils.*;

public class RoadNetwork implements XMLSerializable {

	public final static int MAPEDGE = 0;

	protected LaneModel[] lanes;
	protected JunctionModel[] junctions;
	protected int currentIndex = 0;
	protected int totalLaneNumber;
	protected int totalJunctionNumber;
	protected CarGenerator carGenerator;
	protected Image backdrop;
	Graphics2D g;
	protected int timeDelay,width,height,carSpeed;
	RoadDesigner roadDesigner;
    
    
	public RoadNetwork(InputStream xmlIN) {

		roadDesigner = new RoadDesigner();

		System.out.println("Loading");
		try {
	    XMLLoader loader=new XMLLoader(xmlIN);
	    loader.loadObject(this);
	    loader.close();
		} catch (IOException e) {
	    System.out.println("IOException while loading");
			System.out.println(e.toString());
		} 
	
		//create the backdrop image. Need a frame
		Frame frame = new Frame(); 
		frame.addNotify(); 
		backdrop = frame.createImage(width,height);
		g = (Graphics2D)backdrop.getGraphics();
		g.setColor(Color.white);
		g.fillRect(0,0,width,height);

		roadDesigner.drawAllOldRoads(g);
		roadDesigner.drawAllOldJunctions(g);
	
		createAllJunctions(roadDesigner.getAllJunctions());
	
		Vector allRoads = roadDesigner.roads;
		totalLaneNumber = getTotalNumberOfLanes(allRoads);
		lanes = new LaneModel[totalLaneNumber];
	
		final Iterator it = allRoads.iterator();
		while (it.hasNext()) {
	    final Road tRoad = (Road)it.next();    
	    final GeneralPath[] tlanes = tRoad.getLanes();
	    
	    if (tRoad.isSingleLane()) {
				generateRoadLane(tlanes[0],null,tRoad.busyness);
				//System.out.println("singlelane generate road");
	    } else {
				for (int i=1;i < Road.maxLaneNum; i++) {
					if (tlanes[i] != null) {
						//System.out.println("multilane generate road");
						generateRoadLane(tlanes[i],tlanes[0],tRoad.busyness);
					}
				}
	    }
		}
		System.out.println("totalLaneNumber= "+ totalLaneNumber);
		System.out.println("totalJunctionNumber= "+ totalJunctionNumber);
	
		generateJunctionPaths();
		Car.carsEntered = 0;
		Car.carsExited = 0;
		carGenerator = new CarGenerator(this,carSpeed);
	}

	public LaneModel getLane(int i) { return lanes[i]; }

	public JunctionModel getJunction(int iD){
		for (int i=0;i<totalJunctionNumber;i++) {
	    if (junctions[i].getID() == iD) return junctions[i];
		}
		System.out.println("Unknowm junction iD");
		return null;
	}

	public int getNumberInputs() {
		int count = 0;
		for (int i=0; i<totalLaneNumber; i++) {
	    if (lanes[i].startJunctionID == MAPEDGE) count++;
		}
		return count;
	}

	public void drawAllCars(Graphics2D g2d) {
		for (int i=0;i < totalLaneNumber; i++) {
	    lanes[i].drawAllCars(g2d);
		}
		for (int i=0;i < totalJunctionNumber; i++) {
	    junctions[i].drawAllCars(g2d);
	    junctions[i].drawJunctionFeatures(g2d);
		}
	}

	public void kill() {
		carGenerator.kill();
		for (int i=0;i < totalLaneNumber; i++) {
	    lanes[i].kill();
		}
		lanes = null;

		for (int i=0;i < totalJunctionNumber; i++) {
	    junctions[i].kill();
		}
		junctions = null;
		totalLaneNumber = 0;
		totalJunctionNumber = 0;
	}

	public void addTimerToElements(TimeManager theTicker){
		carGenerator.addToTimer(theTicker);
		for (int i=0;i < totalJunctionNumber; i++) {
	    junctions[i].addToTimer(theTicker);
		}
		System.out.println("Finished addTimerToElements");
	}

	private void generateRoadLane(GeneralPath path,GeneralPath centerPath,
																int busyness) {

		//First set up the connection between the lane and the junciton.
		Point2D laneStart,laneEnd;

		//work out point of contact with junction.
		if (centerPath == null) {
	    laneStart = GPathUtils.getStartOfPath(path);
	    laneEnd = GPathUtils.getEndOfPath(path);
		} else {
	    if (GPathUtils.isSameDirection(path,centerPath,
																		 Math.toRadians(20))) {
				laneStart = GPathUtils.getStartOfPath(centerPath);
				laneEnd = GPathUtils.getEndOfPath(centerPath);
				//System.out.print("Lane "+currentIndex+ " is same dir!");
	    } else {
				laneStart = GPathUtils.getEndOfPath(centerPath);
				laneEnd = GPathUtils.getStartOfPath(centerPath);
				//System.out.print("Lane "+currentIndex+ " is not same dir!");
	    }
		}

		//Get junction ID at point of contact.
		int jID1 = roadDesigner.getjunctionID(laneStart);
		int jID2 = roadDesigner.getjunctionID(laneEnd);
		//System.out.println("jside1= "+jside1+" jside2= "+jside2);
	
		if (jID1 != MAPEDGE && jID1 != -1) {
	    final int jside1 = roadDesigner.getjunctionSide(laneStart);
	    getJunction(jID1).addStartLane(currentIndex,jside1);
		}
		if (jID2 != MAPEDGE && jID2 != -1) {
	    final int jside2 = roadDesigner.getjunctionSide(laneEnd);
	    getJunction(jID2).addEndLane(currentIndex,jside2);
		}

		//Now create a lane from the GeneralPath.
		final LaneModel newLane = new LaneModel(currentIndex,this,
																						GPathUtils.getNumPathSections(path),
																						busyness,jID1,jID2);
		double seg[] = new double[6];
		double x1 = 0;double y1 = 0;
		double x2 = 0;double y2 = 0;
		int index = 0;

		for (PathIterator i=path.getPathIterator(null);!i.isDone();i.next()){
	    int segType = i.currentSegment(seg);
	    if (index==0) {
				x1 = seg[0];y1 = seg[1];
	    } else if (index == 1) {
				x2 = seg[0];y2 = seg[1];
	    } else if (index > 1) {
				x1=x2;y1=y2;
				x2 = seg[0];y2 = seg[1];
	    }
	    if (index > 0) { 
				newLane.addLaneSection((int)x1,(int)y1,(int)x2,(int)y2);
	    }
	    index++;
		}
		lanes[currentIndex] = newLane;
		currentIndex++;
	}

	private int getTotalNumberOfLanes(Vector allRoads) {
		int count = 0;
		final Iterator i = allRoads.iterator();
		while (i.hasNext()) count+=((Road)i.next()).getNumLanes();
		return count;
	}

	private void createAllJunctions(Vector allJunctions){
		totalJunctionNumber = allJunctions.size();
		System.out.println("totalJunctionNumber= "+totalJunctionNumber);

		junctions = new JunctionModel[totalJunctionNumber];

		int index = 0;

		final Iterator it = allJunctions.iterator();
		while (it.hasNext()) {
	    Junction tempJunc = (Junction)it.next();
	    int p1 = tempJunc.getPriority(0);
	    int p2 = tempJunc.getPriority(1);
	    int totaltime = tempJunc.totalTime;  
	    boolean actuated =  tempJunc.vehicle_Actuated;
	    boolean adaptive =  tempJunc.adaptive;
	    
			System.out.println("tempJunc.getType()= "+tempJunc.getType());
			
			switch(tempJunc.getType()) {
			case Junction.GIVE_WAY : 
				junctions[index++]= new GiveWayJunctionModel(tempJunc.getID(),p1,p2,
																										 tempJunc.getShape(),this);
				break;
			case Junction.SIGNALLED :
				if (adaptive) {
					junctions[index++] = 
						new AdaptiveSig1JunModel(tempJunc.getID(),tempJunc.getShape(),
																		 totaltime,actuated,this);
				} else {
					junctions[index++] = 
						new Sig1JunctionModel(tempJunc.getID(),tempJunc.getShape(),
																	totaltime,actuated,this);
				} 
				break;
			case Junction.BRIDGE_UNDER :
				junctions[index++] = new BridgeJunctionModel(tempJunc.getID(),p1,p2,
																										 tempJunc.getShape(),this);
				break;
			case Junction.BRIDGE_OVER :
				junctions[index++] = new BridgeJunctionModel(tempJunc.getID(),p1,p2,
																										 tempJunc.getShape(),this);
				break;
			case Junction.ALTSIGNALLED :
				if (adaptive) {
					junctions[index++] = 
						new AdaptiveSig2JunModel(tempJunc.getID(),tempJunc.getShape(),
																		 totaltime,actuated,this);
				} else {
					junctions[index++] = 
						new Sig2JunctionModel(tempJunc.getID(),tempJunc.getShape(),
																	totaltime,actuated,this);
				}
				break;
			case Junction.TURNOFF :
				junctions[index++]= new TurnOffJunctionModel(tempJunc.getID(),p1,p2,
																										 tempJunc.getShape(),this);
				break;
			default : System.out.println("Junction type unidentified");
			}
		}
	}
	
	private void generateJunctionPaths() {
		for (int i=0;i<totalJunctionNumber;i++) junctions[i].generatePaths();
	}

	//**********************************************************************
	//******************** XML LOADING *************************************
	//**********************************************************************

	public String getXMLName() {return "Road_InfraStructure";}
	public XMLElement saveSelf() {return null;}
	public void saveChilds(XMLSaver saver) {}

	public void loadSelf(XMLElement element) { 
		System.out.println("In RoadNetwork loadSelf()");
       
		width = element.getAttribute("Width").getIntValue();
		height = element.getAttribute("Height").getIntValue();
		carSpeed = element.getAttribute("savedSpeed").getIntValue();
		timeDelay = element.getAttribute("savedTiming").getIntValue();
		String imgName = element.getAttribute("BackDropFile").getStringValue();
		System.out.println("backdrop image is: " + imgName);
	}

	public void loadChilds(XMLLoader loader) { 
		System.out.println("In RoadNetwork loadChilds()");
	
		loader.loadObject(roadDesigner);
	}
}















