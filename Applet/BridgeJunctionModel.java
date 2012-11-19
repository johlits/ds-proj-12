import java.util.Vector;
import java.util.Iterator;

import java.awt.Graphics2D;
import java.awt.Shape;

import time.*;

public class BridgeJunctionModel extends JunctionModel implements Timed {

    protected int priority1, priority2;
    
    public BridgeJunctionModel(int iD, int p1, int p2,
			       Shape s, RoadNetwork parent) {
	super(iD,s,parent);
	priority1 = convertToArrayConvienient(p1);
	priority2 = convertToArrayConvienient(p2);
    }

    public void generatePaths() {
	maxNum = Math.max(ei[_NORTH],ei[_SOUTH]);
	maxNum = Math.max(maxNum,ei[_EAST]);
	maxNum = Math.max(maxNum,ei[_WEST]);
	
	paths = new JunctionPath[maxNumRoads][maxNum];
	
	for (int dir=0;dir<maxNumRoads;dir++) {
	    for (int j=0;j<ei[dir];j++) {

		int opDir = getOpDir(dir);

		paths[dir][pi[dir]++] = new JunctionPath(
		 endLanesID[dir][j],startLanesID[opDir][j],this,
		 parent.getLane(endLanesID[dir][j]).getEndingXCoord(),
		 parent.getLane(endLanesID[dir][j]).getEndingYCoord(),
		 parent.getLane(startLanesID[opDir][j]).getStartingXCoord(),
		 parent.getLane(startLanesID[opDir][j]).getStartingYCoord());
	    
		totalNumPaths++;
	    }
	}
    }

    public void drawAllCars(Graphics2D g2d) {
	
	for (int i=0;i<maxNumRoads;i++) {
	    if (i == priority1 || i == priority2) {
		for (int j=0;j<pi[i];j++) {
		    paths[i][j].drawAllCars(g2d);
		}
	    } else {
		for (int j=0;j<pi[i];j++) {
		    paths[i][j].drawAllGhostCars(g2d);
		}
	    }
	}
    }
    
    public void pretick() {}
    public void tick() {}
    
    public boolean isOKToGo(Car car,double currentdist) {
	return true;
    }

    /**************** Due to mouse Events *******************************/

    public String giveInfo() { 
	return "Bridge";
    }
}


