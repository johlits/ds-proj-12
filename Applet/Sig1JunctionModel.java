import java.util.Vector;
import java.util.Iterator;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Color;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;

import time.*;

public class Sig1JunctionModel extends JunctionModel implements Timed {

    int lightTime;
    int lightTimer;
    int currentLightSet = 0, nextLightSet = 0;
    Rectangle2D[][] trafficLightArea;
    Color[][] lightColor;
    boolean actuated;
    LaneModel lane;
    final static int ORANGETIME = 8;

        
    public Sig1JunctionModel(int iD, Shape s, int lighttime,
		    boolean actuated, RoadNetwork parent) {
	super(iD,s,parent);
	this.lightTime = lighttime;
	this.actuated = actuated;
	       	
	lightTimer = lightTime-1;
    }

    public void generatePaths() {

	//Get the maximum possible number of paths
	maxNum = Math.max(ei[0]*(si[1]+si[2]+si[3]),ei[1]*(si[0]+si[2]+si[3]));
	maxNum = Math.max(maxNum,ei[2]*(si[0]+si[1]+si[3]));
	maxNum = Math.max(maxNum,ei[3]*(si[0]+si[1]+si[2]));
	
	paths = new JunctionPath[maxNumRoads][maxNum];

	//Generate a sensible default junction path set.
	for (int i=0;i<maxNumRoads;i++) {
	 for (int i2=0;i2<ei[i];i2++) { //all the lane ends of all sides.

	     if (createPathToOppositeSide(i,i2)) {
		 if (i2+1 == ei[i]) createLeftTurns(i,i2);
		 if (i2 == 0) createRightTurns(i,i2);
	     } else if (isOnLeftSide(i,i2)) {
		 if (createcorrespondingLeft(i,i2)) {
		 } else if (createcorrespondingRight(i,i2)) {
		 } else if (createAnyLeft(i,i2)) {
		 } else if (createAnyRight(i,i2)) {
		 } else if (createAnyOpposite(i,i2)) {
		 } else System.out.println("Path option unaccounted for");
	     } else if (isOnRightSide(i,i2)) {
		 if (createcorrespondingRight(i,i2)) {
		 } else if (createcorrespondingLeft(i,i2)) {
		 } else if (createAnyRight(i,i2)) {
		 } else if (createAnyLeft(i,i2)) {
		 } else if (createAnyOpposite(i,i2)) {
		 } else System.out.println("Path option unaccounted for");
	     }
	 }
	}

	System.out.println("generated junction paths, number= "+totalNumPaths);
	trafficLightArea = new Rectangle2D[maxNumRoads][maxNum];
	lightColor = new Color[maxNumRoads][maxNum];

	//Place Traffic lights at junction inputs
	for (int i=0;i<maxNumRoads;i++) {
	    for (int j=0;j<ei[i];j++) { //all the lane ends of all sides.
		trafficLightArea[i][j] = new Rectangle2D.Double(
		        parent.getLane(endLanesID[i][j]).getEndingXCoord()-3,
		        parent.getLane(endLanesID[i][j]).getEndingYCoord()-3,
			6,6);

		lightColor[i][j] = Color.red;
		parent.getLane(endLanesID[i][j]).endJunctionSide = i;
		parent.getLane(endLanesID[i][j]).endJunctionIndex = j;
	    }
	}

	//Test whether some inputs may not have outputs?
	testForLiveness();
    }

    /**
     * Draws the junction traffic lights with the correct colour
     **/
    public void drawJunctionFeatures(Graphics2D g2d) {
	super.drawJunctionFeatures(g2d);

	for (int i=0;i<maxNumRoads;i++) {
	    for (int j=0;j<ei[i];j++) { //all the lane ends of all sides.
		g2d.setColor(lightColor[i][j]);
		g2d.fill(trafficLightArea[i][j]);
	    }
	}
    }

    public void pretick() {
	
	if (lightTimer == lightTime-ORANGETIME) {

	    if (actuated) {
		nextLightSet = getNextValidLightSet();
	    } else {
		nextLightSet = getNextLightSet(currentLightSet);
	    }
	    if (nextLightSet != currentLightSet) { 
		for (int j=0;j<ei[currentLightSet];j++) {
		    lightColor[currentLightSet][j] = Color.orange;
		}
	    }
	}
    
	if (lightTimer >= lightTime) {

	    lightTimer = 0;

	    if (nextLightSet != currentLightSet) { 
		
		for (int j=0;j<ei[currentLightSet];j++) {
		    lightColor[currentLightSet][j] = Color.red;
		}

		currentLightSet = nextLightSet;

		for (int j=0;j<ei[currentLightSet];j++) {
		    lightColor[currentLightSet][j] = Color.green;
		}
	    }
	} else lightTimer++;
    }
 
    public void tick() {}

    public int getNextLightSet(int currentLightSet) {
	//Need to check empty sides don't have a green light.
	if (currentLightSet==_NORTH) {
	    if (ei[_EAST] != 0) return _EAST;
	    if (ei[_SOUTH] != 0) return _SOUTH;
	    if (ei[_WEST] != 0) return _WEST;
	} else if (currentLightSet == _EAST) {
	    if (ei[_SOUTH] != 0) return _SOUTH;
	    if (ei[_WEST] != 0) return _WEST;
	    if (ei[_NORTH] != 0) return _NORTH;
	} else if (currentLightSet == _SOUTH) {
	    if (ei[_WEST] != 0) return _WEST;
	    if (ei[_NORTH] != 0) return _NORTH;
	    if (ei[_EAST] != 0) return _EAST;
	} else if (currentLightSet == _WEST) {
	    if (ei[_NORTH] != 0) return _NORTH;
	    if (ei[_EAST] != 0) return _EAST;
	    if (ei[_SOUTH] != 0) return _SOUTH;
	}
	System.out.println("invalid traffic light set!");
	return -1;
    }

    public int getNextValidLightSet() {
	//Need to check empty sides don't have a green light.
	int nextSet = currentLightSet;
	boolean stop = false;
	do {
	    nextSet = getNextLightSet(nextSet);

	    if (currentLightSet != nextSet) {
		//test all lanes of nextSet to check a car is waiting.
		for (int i=0; i < ei[nextSet];i++) {
		    if (parent.getLane(endLanesID[nextSet][i]).getEndLaneSection().hasCars()) stop = true;
		}
	    } else {
		//Exit when checked all the sides
		stop = true;
	    } 
	} while (!stop);
	return nextSet;
    }
	
    /** 
     * Checks whether this signalled junction should allow a car to go or not.
     * Called by LaneSection when car coming up to junction.
     * @param currentcar Car that is testing whether it can go.
     * @param currentdist Distance to the end of the current lanesection.
     */   
    public boolean isOKToGo(Car currentcar,double currentdist) {

	// We need to stop cars entering a junction if it's not realistic.
	// Obviously, don't enter if there is a red light.
	// But also don't enter if there's other cars blocking the way.
	// e.g. if cars havn't driven off the junction in time.
	//      or if there's no space on the other side of the junction.

	lane = parent.getLane(currentcar.carPath[0].getParentID());
	CarContainer path = currentcar.carPath[1];
	
	//Not OK to go if red light.
	if (lightColor[lane.endJunctionSide][lane.endJunctionIndex] == 
	    Color.red) return false;

	//Not OK to go if Amber light and still approaching it.
	if (currentdist > 4 &&
	    lightColor[lane.endJunctionSide][lane.endJunctionIndex] == 
	    Color.orange) return false;

	//Not ok to go unless there is a space on the other side.
	if (path.cars.size() == 0 &&
	    (currentcar.inFrontInfo[0]-
	     (currentdist+currentcar.halfwidth+
	      currentcar.carPath[1].length)) < currentcar.width) return false;

	//Not ok to go if other cars still on the juction on other paths.
	//Also not ok to go unless cars on this path are moving.
	for (int i=0;i<maxNumRoads;i++) {
	    for (int j=0;j<pi[i];j++) {
		if (paths[i][j] != null && paths[i][j].cars.size() > 0) {

		    if (paths[i][j] == path) {
			//Same path as currentCar so check speed
			final Iterator it = path.cars.iterator();
			while (it.hasNext()) {
			    Car tempCar = (Car)it.next();
			    if (tempCar.speed == 0) return false;
			}
		    } else {
			//Only check intersecting paths (possible collision)
			if (Line2D.linesIntersect(path.startX,path.startY,
					 	  path.endX,path.endY,
						  paths[i][j].startX,
						  paths[i][j].startY,
						  paths[i][j].endX,
						  paths[i][j].endY)) 
			    return false;
		    }
		}
	    }
	}
		
	return true;
    }

    /**************** Due to mouse Events *******************************/

    public String giveInfo() { 
	return "Signalled junction.";
    }
}
















