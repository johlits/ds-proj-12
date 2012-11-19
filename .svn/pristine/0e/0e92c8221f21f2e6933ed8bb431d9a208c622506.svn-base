import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

import java.util.Iterator;
import java.util.Vector;

import java.awt.Graphics2D;
import java.awt.Shape;

import time.*;
import Utils.*;

/**
 * The <code>RoadNetwork</code> class creates and stores all the road network
 * infrastructure classes such as roads and junctions and how they 
 * inter-relate.
 * @author <a href="mailto:tf98@doc.ic.ac.uk">Thomas Fotherby</a>
 */
public class RoadNetwork {

	public final static int MAPEDGE = 0;

	protected LaneModel[] lanes;
	protected JunctionModel[] junctions;
	protected int currentIndex = 0;
	protected int totalLaneNumber;
	protected int totalJunctionNumber;
	protected RoadDesigner roadDesigner;
	protected CarGenerator carGenerator;

	private int[] bestPath;
	private int[] bestIDs;
	private int bestindex;

   
	/**
	 * The RoadNetwork constructor pulls information from the Road-Editor and
	 * converts into a form the simulation can use.
	 * The editor uses vectors because the user can shrink and grow elements.
	 * The sim uses arrays has elements are fixed and arrays are faster.
	 * @param roadDesigner The road editor
	 * @param speed The speed of the cars in the simulation
	 */
	public RoadNetwork(RoadDesigner roadDesigner, int speed) {

		this.roadDesigner = roadDesigner;
	
		createAllJunctions(roadDesigner.getAllJunctions());

		Vector allRoads = roadDesigner.clipAllRoads();
		totalLaneNumber = getTotalNumberOfLanes(allRoads);
		lanes = new LaneModel[totalLaneNumber];

		final Iterator it = allRoads.iterator();
		while (it.hasNext()) {
	    final Road tRoad = (Road)it.next();    
	    final GeneralPath[] tlanes = tRoad.getLanes();
	    
	    if (tRoad.isSingleLane()) {
				generateRoadLane(tlanes[0],null,true,tRoad.busyness);
	    } else {
				for (int i=1;i < Road.maxLaneNum; i++) {
					if (tlanes[i] != null) {
						generateRoadLane(tlanes[i],tlanes[0],i%2!=0,tRoad.busyness);
					}
				}
	    }
		}
		System.out.println("totalLaneNumber= "+ totalLaneNumber);
		System.out.println("totalJunctionNumber= "+ totalJunctionNumber);

		generateJunctionPaths();

		//Calculate a path of junctions to optimise in terms of timings.
		//Used as research for junctions to communicate with each other in the path
		bestPath = new int[totalJunctionNumber];
		bestindex = 0;
		generateAdaptivePath();
		
		//Initiate cars into the road network.
		Car.carsEntered = 0;
		Car.carsExited = 0;
		carGenerator = new CarGenerator(this,speed);
	}

	/**
	 * Generates a lane in the simulation. Used centerPath to calculate the 
	 * direction the lane is going. Assigns the lane to a start/end at a 
	 * junction or MAPEDGE. Also Assigns a busyness value.
	 */
	private void generateRoadLane(GeneralPath path,GeneralPath centerPath,
																boolean sameDir,int busyness) {

		//First set up the connection between the lane and the junciton.
		Point2D laneStart,laneEnd;

		//work out point of contact with junction.
		if (centerPath == null) {
	    laneStart = GPathUtils.getStartOfPath(path);
	    laneEnd = GPathUtils.getEndOfPath(path);
		} else {
	    if (sameDir) {
				laneStart = GPathUtils.getStartOfPath(centerPath);
				laneEnd = GPathUtils.getEndOfPath(centerPath);
			} else {
				laneStart = GPathUtils.getEndOfPath(centerPath);
				laneEnd = GPathUtils.getStartOfPath(centerPath);
			}
		}

		//Get junction ID at point of contact.
		int jID1 = roadDesigner.getjunctionID(laneStart);
		int jID2 = roadDesigner.getjunctionID(laneEnd);
			
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

	/**
	 * @return Number of lane inputs to the road network.
	 */
	public int getNumberInputs() {
		int count = 0;
		for (int i=0; i<totalLaneNumber; i++) {
	    if (lanes[i].startJunctionID == MAPEDGE) count++;
		}
		return count;
	}

	private void createAllJunctions(Vector allJunctions){
		totalJunctionNumber = allJunctions.size();
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

	/**
	 * @param i A lane index
	 * @return A laneModel
	 */
	public LaneModel getLane(int i) { 
		//System.out.println("in getLane(), i= "+i);
		return lanes[i]; 
	}

	/**
	 * @param iD A junction ID
	 * @return a <code>JunctionModel</code>
	 */
	public JunctionModel getJunction(int iD){
		for (int i=0;i<totalJunctionNumber;i++) {
	    if (junctions[i].getID() == iD) return junctions[i];
		}
		System.out.println("Unknowm junction iD");
		return null;
	}

	private void generateJunctionPaths() {
		for (int i=0;i<totalJunctionNumber;i++) junctions[i].generatePaths();
	}

	/**
	 * Draws all the cars known to the road network.
	 * @param g2d a <code>Graphics2D</code> value
	 */
	public void drawNetworkComponents(Graphics2D g2d) {
		for (int i=0;i < totalLaneNumber; i++) {
	    lanes[i].drawAllCars(g2d);
		}
		for (int i=0;i < totalJunctionNumber; i++) {
	    junctions[i].drawAllCars(g2d);
	    junctions[i].drawJunctionFeatures(g2d);
		}
	}

	/**
	 * Removes all the cars from the network.
	 *
	 */
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

	/**
	 * Adds timed elements to the global clock.
	 *
	 * @param theTicker a <code>TimeManager</code>
	 */
	public void addTimerToElements(TimeManager theTicker){
		carGenerator.addToTimer(theTicker);
		for (int i=0;i < totalJunctionNumber; i++) {
	    junctions[i].addToTimer(theTicker);
		}
	}

	public int getTotalRoadLength() {
		int totalLength = 0;
		
		for (int i=0;i < totalLaneNumber; i++) {
	    totalLength += lanes[i].getTotalLength();
		}
		return ((int)totalLength);
	}

	/**
	 * Tries to discover the longest path of junctions back to back.
	 *
	 * 1) For each signalled junction:
	 * 2) try to find a neighbour.
	 * 3) extend to another neightbour
	 * 4) Save the longest path found.
	 */
	public void generateAdaptivePath() {
		for (int i=0;i<totalJunctionNumber;i++) { 
			if (junctions[i].isSignalled()) {  //all signalled junctions
				for (int k=0;k<4;k++) {          //all sides
					
					int[] tempPath = new int[totalJunctionNumber];
					int index = 0;
					tempPath[index] = i;
					int nextJuncID;
					int laneID = junctions[i].getAnyOutputLane(k);
					
					if (laneID != -1) {
						nextJuncID = getOutputJunction(laneID);
				  			
						while (nextJuncID != -1 && 
									 index < totalJunctionNumber-1) {
							index++;
							tempPath[index] = nextJuncID;
							
							nextJuncID = 
								getOpositeNeighbour(tempPath[index],tempPath[index-1]);
						}
						
						if (index > bestindex) {
							bestindex = index;
							for (int j=0;j<=index;j++) {
								bestPath[j] = tempPath[j];
							}
						}
					}
				}
			}
		}
	}

	private int getOutputJunction(int laneID) {
		for (int i=0;i<totalJunctionNumber;i++) {
	    if (junctions[i].hasInputOf(laneID) && junctions[i].isSignalled()) {
				return i;
	    }
		}	
		return -1;
	}

	private int getOpositeNeighbour(int thisJ, int previousJ) {
		//first find side to look at = opposite to where previousJ was.
		int laneID = 
			junctions[thisJ].getAnOpositeLaneFromJunction(junctions[previousJ]);
	
		if (laneID == -1) return -1;
		
		for (int i=0;i<totalJunctionNumber;i++) {
	    if (i!=thisJ && i!=previousJ && 
					junctions[i].isSignalled() && junctions[i].hasInputOf(laneID)) {
				return i;
	    }
		}
		return -1;
	}

	//Given a line of junctions to syncronise, need to find
	//1) distance between them
	//2) speed of cars
	//3) lights to adjust.
	// Remember there are bestindex+1 junctions
	// Therefore we need bestindex distances but bestindex+1 lightsets.
	private void generateAdaptiveInfo() {
		int[] lights = new int[bestindex+1];
		int[] distances = new int[bestindex+1];

		for (int i=0;i<bestindex;i++) {

			Sig1JunctionModel jM1 = (Sig1JunctionModel)junctions[bestPath[i]];
			Sig1JunctionModel jM2 = (Sig1JunctionModel)junctions[bestPath[i+1]];
			int laneID = jM1.getsharedPath(jM2);
			
			distances[i] = (int)lanes[laneID].getTotalLength();
			lights[i] = jM1.getLightsTo(laneID);
		}

		int laneID = junctions[bestindex-1].getsharedPath(junctions[bestindex]);
		lights[bestindex] = junctions[bestindex].getLightsFrom(laneID);

		int timeDelay = 59;

		for (int i=0;i<=bestindex;i++) {
			Sig1JunctionModel jM = ((Sig1JunctionModel)junctions[bestPath[i]]);
			jM.resetLights(lights[i],timeDelay);
			System.out.println("Junction ID " + jM.iD      +
												 " lights: "    + lights[i]  + 
												 " timeDelay: " + timeDelay);
			timeDelay -= ((distances[i]/10)%60);

		}
	}
    
	/**
	 * Tries to syncronize the junctions on a junction path found earlier.
	 * @return a <code>Shape</code> representing the path of junctions trying to 
	 * synronize
	 */
	public Shape getAdaptivePath() {

		System.out.println("In getAdaptivePath(), bestindex= "+bestindex);
		if (bestindex == 0) return null;

		generateAdaptiveInfo();
	
		GeneralPath gp = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
		Point2D p;
	
		for (int i=0;i<bestindex+1;i++) {
	    p = junctions[bestPath[i]].getCenter();
	    if (i==0) gp.moveTo((float)p.getX(),(float)p.getY());
	    else      gp.lineTo((float)p.getX(),(float)p.getY());
	  }
		return gp;      
	}

	public void randomizeAdaptivePath() {
		System.out.println("In randomizeAdaptivePath()");
		for (int i=0;i<totalJunctionNumber;i++) { 
			if (junctions[i].isSignalled()) {  //all signalled junctions
				Sig1JunctionModel jM = ((Sig1JunctionModel)junctions[i]);
				int l = (int)(Math.random()*4);
				int t = (int)(Math.random()*59);
				jM.resetLights(l,t);
			}
		}
	}
                
	// ********************** From mouse clicks ********************************

	/**
	 * @param x an <code>int</code> value
	 * @param y an <code>int</code> value
	 * @return an <code>int</code> representing cars queueing at the input near 
	 * where the user clicked.
	 */
	public int getInputQ(int x, int y) {

		for (int i=0;i < totalLaneNumber;i++) {
	    if (lanes[i].startJunctionID == MAPEDGE) {
				if(GeoUtils.getDistance(lanes[i].getStartingXCoord(),
																lanes[i].getStartingYCoord(),x,y) < 8){
					return lanes[i].carQueue;
				}
	    }
		}
		return -1;
	}

	/**
	 * Gives information to the user about whatever they clicked on.
	 * @param p a <code>Point2D</code> value
	 * @return a <code>String</code> value
	 */
	public String getJunctionVars(Point2D p) {
		for (int i=0;i < totalJunctionNumber;i++) {
	    if (junctions[i].junctionShape.contains(p)) {
				return junctions[i].giveInfo();
	    }
		}
		return "No Object Found";
	}

	/**
	 * Gets the speed of all the cars in the roadnetwork and calculates an 
	 * average value to display on screen.
	 * @return an <code>int</code> value
	 */
	public int getAverageSpeed() {
		int speedSum = 0;
		for (int i=0;i < totalLaneNumber; i++) {
	    speedSum += lanes[i].getSpeeds();
		}
		for (int i=0;i < totalJunctionNumber; i++) {
	    speedSum += junctions[i].getSpeeds();
		}
		return speedSum;
	}

	/**
	 * Gets the car under the mouse click.
	 *
	 * @param p a <code>Point2D</code> value
	 * @return a <code>Car</code> value
	 */
	public Car getCar(Point2D p) {
		Car car = null;
		for (int i=0;i < totalLaneNumber; i++) {
	    car = lanes[i].getCar(p);
	    if (car != null) return car;
		}
		for (int i=0;i < totalJunctionNumber; i++) {
	    car = junctions[i].getCar(p);
	    if (car != null) return car;
		}
		return null;
	}
}















