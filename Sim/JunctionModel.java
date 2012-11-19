import java.util.*;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Color;

import java.awt.geom.Point2D;

import time.*;
import Utils.*;

public abstract class JunctionModel implements Timed {

	final static int NORTH = 2;
	final static int _NORTH = 0;
	final static int EAST = 4;
	final static int _EAST = 1;
	final static int SOUTH = 6;
	final static int _SOUTH = 2;
	final static int WEST = 8;
	final static int _WEST = 3;
	final static int maxNumRoads = 4;
	static boolean DRAWPATHS = false;

	protected TimeManager ticker;
	protected int iD;
	protected int[][] startLanesID,endLanesID;
	protected JunctionPath[][] paths;
	protected int[] si,ei,pi;
	protected int maxNum;
	protected int totalNumPaths = 0;
	protected RoadNetwork parent;
	protected Shape junctionShape;

	// Lanes in a road have an index.
	// Even index's are outputs.
	// Odd index's are inputs.
	// 0 index exists if only a single lane enters the junction on that side.
	//
	//                    _NORTH
	//              8 6 4 2 0 1 3 5 7
	//             *-----------------*
	//        -> 7 |                 | 8  ->
	//        -> 5 | JUNCTION PATHS  | 6  ->
	//        -> 3 |                 | 4  ->
	//        -> 1 |      EXIST      | 2  ->  
	//   _WEST   0 |                 | 0      _EAST
	//        <- 2 |       IN        | 1  <-
	//        <- 4 |                 | 3  <-
	//        <- 6 |       HERE      | 5  <-
	//        <- 8 |                 | 7  <-
	//             *-----------------*
	//              7 5 3 1 0 2 4 6 8              
	//                     _SOUTH
	//
	//si counts the number of paths that start on a particular side
	//ei counts the number of paths that end on a particular side
	//pi counts the number of paths on a particular side (i.e. si+ei)


	public JunctionModel(int iD, Shape s, RoadNetwork parent) {
		this.iD = iD;
		this.parent = parent;
		this.junctionShape = s;
		startLanesID = new int[maxNumRoads][Road.maxLaneNum/2]; //[4],[4]
		endLanesID = new int[maxNumRoads][Road.maxLaneNum/2];
		si = new int[maxNumRoads]; //A count of the start lanes from each side.
		ei = new int[maxNumRoads]; //A count of the end lanes to each side.
		pi = new int[maxNumRoads]; //A count of junction paths from each side.
		for (int i=0;i<maxNumRoads;i++) {
	    si[i] = 0;
	    ei[i] = 0;
	    pi[i] = 0;
		}
	}

	public abstract boolean isOKToGo(Car currentcar,double currentdist);

	public int getID() { return iD; }

	public void addToTimer(TimeManager theTick) {
		ticker = theTick;
		ticker.addTimed(this);
	}

	public void addStartLane(int laneID,int junctionSide) {
		switch (junctionSide) {
		case NORTH: startLanesID[_NORTH][si[_NORTH]++] = laneID; break;
		case EAST: startLanesID[_EAST][si[_EAST]++] = laneID;  break;
		case SOUTH: startLanesID[_SOUTH][si[_SOUTH]++] = laneID; break;
		case WEST: startLanesID[_WEST][si[_WEST]++] = laneID;  break;
		default: System.out.println("No matching junct Side in addStartLane");
		}
	}

	public void addEndLane(int laneID,int junctionSide) {
		switch (junctionSide) {
		case NORTH: endLanesID[_NORTH][ei[_NORTH]++] = laneID; break;
		case EAST: endLanesID[_EAST][ei[_EAST]++] = laneID;  break;
		case SOUTH: endLanesID[_SOUTH][ei[_SOUTH]++] = laneID; break;
		case WEST: endLanesID[_WEST][ei[_WEST]++] = laneID;  break;
		default:  System.out.println("No matching junct Side in addEndLane");
		}
	}

	public void generatePaths() {
		maxNum = Math.max(ei[0]*(si[1]+si[2]+si[3]),ei[1]*(si[0]+si[2]+si[3]));
		maxNum = Math.max(maxNum,ei[2]*(si[0]+si[1]+si[3]));
		maxNum = Math.max(maxNum,ei[3]*(si[0]+si[1]+si[2]));

		paths = new JunctionPath[maxNumRoads][maxNum];
	
		for (int i=0;i<maxNumRoads;i++) {
			for (int i2=0;i2<ei[i];i2++) { //all the lane ends of all sides.

				for (int j=0;j<maxNumRoads;j++) {
					for (int j2=0;j2<si[j];j2++) { //all the lane starts of all sides.

						if (i!=j) {
							//System.out.println("creating JunctionPath["+i+"]["+pi[i]+
							//"] from "+ endLanesID[i][i2] + " to " + startLanesID[j][j2]);

							paths[i][pi[i]++] = new JunctionPath(
											endLanesID[i][i2],startLanesID[j][j2],this,
											parent.getLane(endLanesID[i][i2]).getEndingXCoord(),
											parent.getLane(endLanesID[i][i2]).getEndingYCoord(),
											parent.getLane(startLanesID[j][j2]).getStartingXCoord(),
											parent.getLane(startLanesID[j][j2]).getStartingYCoord());

							totalNumPaths++;
						}
					}
				}
				parent.getLane(endLanesID[i][i2]).endJunctionSide = i;
				parent.getLane(endLanesID[i][i2]).endJunctionIndex = i2;
			}
		}

		System.out.println("generated junction paths, number= "+totalNumPaths);

		testForLiveness();
	}

	public LaneSection getnextLane(int nextLaneID) {
		return parent.getLane(nextLaneID).getStartingLaneSection();
	}

	public CarContainer receiveCar (int laneID) {
		//System.out.println("Receiving car, laneID= "+laneID);
		if (si[0]==0 && si[1]==0 && si[2]==0 && si[3]==0) {
	    System.out.println("In receiveCar, No lanes from junction!");
	    return null;
		}
		return getRandomPath(laneID);
	}
	
	public LinkedList<CarContainer> getChoices(int laneID) {
		LinkedList<CarContainer> temp = new LinkedList<CarContainer>();
		if (si[0]==0 && si[1]==0 && si[2]==0 && si[3]==0) 
			return temp;
		for (int i=0;i<maxNumRoads;i++) {
			for (int j=0;j<pi[i];j++) {
				if (paths[i][j].getID() == laneID) temp.add(paths[i][j]);
			}
		}
		Collections.shuffle(temp);	
		return temp;
	}

	private JunctionPath getRandomPath(int laneID) {
		//need to know all paths starting from "laneID"
		JunctionPath[] tempPaths = new JunctionPath[maxNum];
		int index = 0;

		for (int i=0;i<maxNumRoads;i++) {
			for (int j=0;j<pi[i];j++) {
				if (paths[i][j].getID() == laneID) tempPaths[index++] = paths[i][j];
			}
		}	
		if (index == 0) System.out.println("JunctionPath Options= "+index);
		return tempPaths[(int)(Math.random()*index)];
	}	

	public void drawAllCars(Graphics2D g2d) {
		for (int i=0;i<maxNumRoads;i++) {
	    for (int j=0;j<pi[i];j++) {
				paths[i][j].drawAllCars(g2d);
				//paths[i][j].drawIDs(g2d);
	    }
		}
	}

	public void drawJunctionFeatures(Graphics2D g2d) {
		if (DRAWPATHS) {
			for (int i=0;i<maxNumRoads;i++) {
				for (int j=0;j<pi[i];j++) {
					switch(i) {
					case _NORTH : g2d.setColor(Color.cyan); break;
					case _EAST :  g2d.setColor(Color.blue); break;
					case _SOUTH : g2d.setColor(Color.magenta); break;
					case _WEST :  g2d.setColor(Color.black); break;
					}
					g2d.drawLine(paths[i][j].startX,paths[i][j].startY,
											 paths[i][j].endX,paths[i][j].endY);
				}
			}
		}
	}

	public void kill() {
		ticker.removeTimed(this);
		for (int i=0;i<maxNumRoads;i++) {
	    for (int j=0;j<pi[i];j++) {
				paths[i][j].kill();
	    }
		}
		paths = null;
	}

	public void testForLiveness() {

		for (int i=0;i<maxNumRoads;i++) {
			for (int j=0;j<ei[i];j++) { //all the lane ends of all sides.
				if (!isPathFrom(endLanesID[i][j],i)) 
					System.out.println("No Path from "+i+","+j);
			}
		}
	
		for (int i=0;i<maxNumRoads;i++) {
			for (int j=0;j<si[i];j++) { //all the lane ends of all sides.
				if (!isPathTo(startLanesID[i][j])) 
					System.out.println("No Path to "+i+","+j);
			}
		}
	}

	private boolean isPathFrom(int pathID, int dir) {
		for (int i=0;i<pi[dir];i++) { //all the lane ends of all sides.
	    if (paths[dir][i].getID() == pathID) return true;
		}
	
		return false;
	}

	private boolean isPathTo(int pathID) {
		for (int i=0;i<maxNumRoads;i++) {
			for (int j=0;j<pi[i];j++) { //all the lane ends of all sides.
				if (paths[i][j].getEndLaneID() == pathID) return true;
			}
		}
	
		return false;
	}

	public boolean isOnMapEdge() {
		for (int i=0;i<maxNumRoads;i++) {
	    for (int i2=0;i2<ei[i];i2++) {
				if (parent.getLane(endLanesID[i][i2]).startJunctionID == 
						parent.MAPEDGE) {
					return true;
				}
	    }
		}
		return false;
	}

	public int getAnOpositeLaneFromEdge(int side) {
	
		for (int i=0;i<ei[side];i++) {
	    if (parent.getLane(endLanesID[side][i]).startJunctionID == 
					parent.MAPEDGE) {
				int opside = getOpDir(side);
				if (si[opside] != 0) {
					return startLanesID[opside][si[opside]-1];
				}
	    }
		}
		return -1;
	}

	public int getAnyOutputLane(int side) {
		if (si[side] != 0) return startLanesID[side][si[side]-1];
		return -1;
	}
    
	public int getAnOpositeLaneFromJunction(JunctionModel jun) {
		for (int i=0;i<maxNumRoads;i++) {
	    for (int i2=0;i2<ei[i];i2++) {
				for (int j=0;j<maxNumRoads;j++) {
					for (int j2=0;j2<jun.si[j];j2++) {
						if (endLanesID[i][i2] == jun.startLanesID[j][j2]) {

							int opdir = getOpDir(i);
							if (si[opdir] != 0)
								return startLanesID[opdir][si[opdir]-1];

						}
					}
				}
	    }
		}
		return -1;
	}

	public int getsharedPath(JunctionModel jun) {
		for (int i=0;i<maxNumRoads;i++) {
	    for (int i2=0;i2<si[i];i2++) {
				for (int j=0;j<maxNumRoads;j++) {
					for (int j2=0;j2<jun.ei[j];j2++) {
						if (startLanesID[i][i2] == jun.endLanesID[j][j2]) {
								return startLanesID[i][i2];
						}
					}
				}
	    }
		}
		System.out.println("Unknown Shared Path");
		return -1;
	}

	public int getLightsTo(int laneID) {
		for (int i=0;i<maxNumRoads;i++) {
	    for (int i2=0;i2<si[i];i2++) {
				if (startLanesID[i][i2] == laneID) return getOpDir(i);
			}
		}
		System.out.println("Unknown lights to");
		return -1;
	}

	public int getLightsFrom(int laneID) {
		for (int i=0;i<maxNumRoads;i++) {
	    for (int i2=0;i2<ei[i];i2++) {
				if (endLanesID[i][i2] == laneID) return i;
			}
		}
		System.out.println("Unknown lights From");
		return -1;
	}

	public boolean hasInputOf(int inputJID) {
		for (int i=0;i<maxNumRoads;i++) {
	    for (int i2=0;i2<ei[i];i2++) {
				if (endLanesID[i][i2] == inputJID) return true;
	    }
		}
		return false;
	}

	public Point2D getCenter() {
		return GeoUtils.getRectangleCenter(junctionShape.getBounds());
	}

	public void pretick() {}
	public void tick() {}

	//**********************************************************************
	//************************** Usefull ***********************************
	//**********************************************************************

	protected int getOpDir(int dir) {
		switch(dir) {
		case _NORTH :     return _SOUTH;
		case _EAST :      return _WEST;
		case _SOUTH :     return _NORTH;
		case _WEST :      return _EAST;
		}
		System.out.println("Unexpected value for dir in getOpDir");
		return -1;
	}
    
	protected int getLeftDir(int dir) {
		switch(dir) {
		case _NORTH :     return _EAST;
		case _EAST :      return _SOUTH;
		case _SOUTH :     return _WEST;
		case _WEST :      return _NORTH;
		}
		System.out.println("Unexpected value for dir in getLeftDir");
		return -1;
	}

	protected int getRightDir(int dir) {
		switch(dir) {
		case _NORTH :     return _WEST;
		case _EAST :      return _NORTH;
		case _SOUTH :     return _EAST;
		case _WEST :      return _SOUTH;
		}
		System.out.println("Unexpected value for dir in getRightDir");
		return -1;
	}

	protected int convertToArrayConvienient(int dir) {
		switch(dir) {
		case NORTH :     return _NORTH;
		case EAST :      return _EAST;
		case SOUTH :     return _SOUTH;
		case WEST :      return _WEST;
		}
		System.out.println("Unexpected value in convertToArrayConvienient");
		return -1;
	}


	public boolean createPathToOppositeSide(int dir, int num) {
		return createPathBetween(dir, getOpDir(dir), num); 
	}

	public boolean createPathBetween(int dir1, int no1,int dir2, int no2) {
	
		paths[dir1][pi[dir1]++] = 
			new JunctionPath(endLanesID[dir1][no1],startLanesID[dir2][no2],this,
											 parent.getLane(endLanesID[dir1][no1]).getEndingXCoord(),
											 parent.getLane(endLanesID[dir1][no1]).getEndingYCoord(),
											 parent.getLane(startLanesID[dir2][no2]).getStartingXCoord(),
											 parent.getLane(startLanesID[dir2][no2]).getStartingYCoord());
		
		totalNumPaths++;
		
		return true;
	}

	public boolean createPathBetween(int dir1, int dir2, int num) {
		if (num+1 <= si[dir2]) {
	    return createPathBetween(dir1,num,dir2,num);
		}
		return false;
	}

	public boolean isOnLeftSide(int dir, int num) {
		if (num < ei[dir]/2) return false;
		return true;
	}

	public boolean isOnRightSide(int dir, int num) {
		if (num >= ei[dir]/2) return false;
		return true;
	}

	public boolean createcorrespondingLeft(int dir, int num) {
		return createPathBetween(dir, getLeftDir(dir), num); 
	}

	public boolean createcorrespondingRight(int dir, int num) {
		return createPathBetween(dir, getRightDir(dir), num);
	}

	public boolean createAnyLeft(int dir, int num) {
		int opDir = getLeftDir(dir);
		if (si[opDir] > 0) {
	    createPathBetween(dir,num,opDir,(int)Math.random()*si[opDir]);
	    return true;
		}
		return false;
	}

	public boolean createAnyRight(int dir, int num) {
		int opDir = getRightDir(dir);
		if (si[opDir] > 0) {
	    createPathBetween(dir,num,opDir,(int)Math.random()*si[opDir]);
	    return true;
		}
		return false;
	}

	public boolean createAnyOpposite(int dir, int num) {
		int opDir = getOpDir(dir);
		if (si[opDir] > 0) {

	    createPathBetween(dir,num,opDir,(int)Math.random()*si[opDir]);
	    System.out.println(".");
	    return true;
		}
		return false;
	}

	public void createLeftTurns(int dir, int num) {
		int leftDir = getLeftDir(dir);
		for (int i=0; i<si[leftDir];i++) {
	    createPathBetween(dir,num,leftDir,i);
		}
	}

	public void createRightTurns(int dir, int num) {
		int rightDir = getRightDir(dir);
		for (int i=0; i<si[rightDir];i++) {
	    createPathBetween(dir,num,rightDir,i);
		}
	}

	public boolean isSignalled() {
		return false;
	}
    
	/**************** Due to mouse Events *******************************/

	public abstract String giveInfo();

	public Car getCar(Point2D p) {
		Car car = null;

		for (int i=0;i<maxNumRoads;i++) {
	    for (int j=0;j<pi[i];j++) {
				car = paths[i][j].getCar(p);
				if (car != null) return car;
	    }
		}
		return null;
	}

	public int getSpeeds() {
		int speedSum = 0;
		for (int i=0;i<maxNumRoads;i++) {
	    for (int j=0;j<pi[i];j++) {
				speedSum += paths[i][j].getSpeeds();
	    }
		}
		return speedSum;
	}
}













