import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.*;

public class LaneModel {

	private int laneSecIndex = 0;
	private int numOfSections;
	private LaneSection[] laneSections;
	private int iD;
	private RoadNetwork parent;

	public int startJunctionID = -1;   //ie: JUNCTION or MAPEDGE; 
	public int endJunctionID = -1;     //ID of Junction of 0 for MAPEDGE;
	protected int startJunctionSide;   //The side of the junction come from.
	protected int endJunctionSide;     //The side of the junction go to.
	protected int startJunctionIndex;  //The index of the junction come from.
	protected int endJunctionIndex;    //The index of the junction that go to.
	protected int busyness;
	protected int carQueue;
    
	public LaneModel(int iD, RoadNetwork parent,int numOfSections,
									 int busyness,int startJunctionID, int endJunctionID) {
		this.numOfSections = numOfSections;
		this.startJunctionID = startJunctionID;
		this.endJunctionID = endJunctionID;
		this.iD = iD;
		this.parent = parent;
		this.busyness = busyness;
		carQueue = 0;
		laneSections = new LaneSection[numOfSections];
		laneSecIndex = 0;
	}

	public void addLaneSection(int startX,int startY,int endX,int endY) {
		laneSections[laneSecIndex] = new LaneSection(laneSecIndex,this,
																								 startX,startY,endX,endY);
		laneSecIndex++;
	}

	public int getID() { return iD; }

	public int getStartingXCoord() { return laneSections[0].getstartX(); }
	public int getStartingYCoord() { return laneSections[0].getstartY(); }

	public int getEndingXCoord() { 
		return laneSections[numOfSections-1].getendX(); 
	}
	public int getEndingYCoord() { 
		return laneSections[numOfSections-1].getendY(); 
	}

	public LaneSection getStartingLaneSection() {
		return laneSections[0];
	}

	public LaneSection getEndLaneSection() {
		return laneSections[numOfSections-1];
	}

	public LaneSection getLaneSection(int index) {
		return laneSections[index];
	}

	public CarContainer passToJunction() {
		//System.out.println("passing to junction, endJunctionID= "+
		//endJunctionID+ " iD= "+iD);
		return (endJunctionID==0) ? null : parent.getJunction(endJunctionID).receiveCar(iD);
	}
	
	public LinkedList<CarContainer> getChoices() {
		LinkedList<CarContainer> temp = new LinkedList<CarContainer>();
		if (endJunctionID==0)
			return temp;
		for (CarContainer c : parent.getJunction(endJunctionID).getChoices(iD)) {
			temp.add(c);
		}
		return temp;
	}

	public int getNumberOfLaneSections() { return numOfSections; }

	public void drawAllCars(Graphics2D g2d) {
		//System.out.print("numOfSections= "+numOfSections);
		//System.out.println("  laneSecIndex= "+laneSecIndex);
		for (int i=0;i < numOfSections; i++) {
	    laneSections[i].drawAllCars(g2d);
	    laneSections[i].drawIDs(g2d, iD);
		}
		//if (MAIN.DEBUG) {
	  //for (int i=0;i < numOfSections; i++) {
		//laneSections[i].drawIDs(g2d,iD);
	  //}
		//}
	}

	public int numStationaryCars() {
		int sum = 0;
		for (int i=0;i < numOfSections; i++) {
	    sum += laneSections[i].numStationaryCars();
		}
		return sum;
	}

	public void kill() {
		for (int i=0;i < numOfSections; i++) {
	    laneSections[i].kill();
		}
		laneSections = null;
	}

	public boolean isOkToGo(Car currentcar,double cdist){
		if (endJunctionID == RoadNetwork.MAPEDGE) return true;
		return parent.getJunction(endJunctionID).isOKToGo(currentcar,cdist);
	}

	public double getTotalLength() {
		double total = 0;
		for (int i=0;i<numOfSections;i++) total += laneSections[i].length;
		return total;
	}

	/**************** Due to mouse Events *******************************/
	public Car getCar(Point2D p) {
		Car car = null;
		for (int i=0;i < numOfSections; i++) {
	    car = laneSections[i].getCar(p);
	    if (car != null) return car;
		}
		return null;
	}

	public int getSpeeds() {
		int speedSum = 0;
		for (int i=0;i < numOfSections; i++) {
	    speedSum += laneSections[i].getSpeeds();
		}
		return speedSum;
	}

}

















