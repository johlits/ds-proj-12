import java.util.Vector;
import java.util.Iterator;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Line2D;

import time.*;

public class TurnOffJunctionModel extends JunctionModel implements Timed {

	public Car permittedCar = null;
	protected int priority1, priority2;

	public TurnOffJunctionModel(int iD, int p1, int p2,
															Shape s, RoadNetwork parent) {
		super(iD,s,parent);
		priority1 = convertToArrayConvienient(p1);
		priority2 = convertToArrayConvienient(p2);
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
				
				//i is road i.e. N,E,S,W
				//i2 is input lanes.

				if (createPathToOppositeSide(i,i2)) {
					if (i2+1 == ei[i]) createLeftTurns(i,i2);
				} else {
					if (createcorrespondingLeft(i,i2)) {
					} else if (createAnyLeft(i,i2)) {
					} else if (createAnyOpposite(i,i2)) {
					} else if (createcorrespondingRight(i,i2)) {
					} else if (createAnyRight(i,i2)) {
					} else System.out.println("Path option unaccounted for");
				}
				parent.getLane(endLanesID[i][i2]).endJunctionSide = i;
				parent.getLane(endLanesID[i][i2]).endJunctionIndex = i2;
			}
		}
	}
	
	public boolean carsOnPriorityLane() {
		for (int i=0;i<maxNumRoads;i++) {
			if (i == priority1 || i == priority2) {
				for (int j=0;j<ei[i];j++) {
					if (parent.getLane(endLanesID[i][j]).getEndLaneSection().cars.size() > 0) return true; 
					}
				}
			}
		return false;
	}

	public boolean acceptGap() {
		for (int i=0;i<maxNumRoads;i++) {
			if (i == priority1 || i == priority2) {
				for (int j=0;j<ei[i];j++) {
					
					
					final Iterator iterator = parent.getLane(endLanesID[i][j]).getEndLaneSection().cars.iterator();
					while (iterator.hasNext()) {
						final Car tempcar =(Car)iterator.next();
						if (tempcar.distanceToEnd < 40) return false;
					} 
				}
			}
		}
		return true;
	}

	public boolean carsOnConflictingPath(CarContainer path) {
		for (int i=0;i<maxNumRoads;i++) {
	    for (int j=0;j<pi[i];j++) {
				if (paths[i][j] != null && paths[i][j].cars.size() > 0 &&
						Line2D.linesIntersect(path.startX,path.startY,
																	path.endX,path.endY,
																	paths[i][j].startX,
																	paths[i][j].startY,
																	paths[i][j].endX,
																	paths[i][j].endY)) {
					return true;
				}
			}
		}
		return false;
	}

	public void pretick() {}
	public void tick() {}
 
	public boolean isOKToGo(Car car,double currentdist) {
		
		LaneModel lane = parent.getLane(car.plannedPath.get(0).getParentID());
		CarContainer path = car.plannedPath.get(1);

		if (lane.endJunctionSide == priority1 ||
				lane.endJunctionSide == priority2) {
			return !carsOnConflictingPath(path);
		} else {
			//A v v v v simple  "Gap exceptance" model. :-)
			if (carsOnConflictingPath(path)) return false;
			if (!acceptGap()) return false;
			//if (carsOnPriorityLane()) return false;
			return true;
		}
	}

	/**************** Due to mouse Events *******************************/

	public String giveInfo() { 
		return "Turn on/off give-way junction";
	}
}

