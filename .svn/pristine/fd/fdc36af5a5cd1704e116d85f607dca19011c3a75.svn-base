import java.util.Vector;
import java.util.Iterator;

import java.awt.Graphics2D;
import java.awt.Shape;

import time.*;

public class GiveWayJunctionModel extends JunctionModel implements Timed {

    public Car permittedCar = null;
    protected int priority1, priority2;
    protected Vector priorityQ, waitQ;
    protected boolean collectPhase = false;

    public GiveWayJunctionModel(int iD, int p1, int p2,
				Shape s, RoadNetwork parent) {
	super(iD,s,parent);
	priority1 = convertToArrayConvienient(p1);
	priority2 = convertToArrayConvienient(p2);
	priorityQ = new Vector();
	waitQ = new Vector();
    }

    public boolean isTheremoreCars(int max) {
	int count = 0;
	for (int i=0;i<maxNumRoads;i++) {
	    for (int j=0;j<maxNum;j++) {
		if (paths[i][j] != null) {
		    count += paths[i][j].cars.size(); 
		    if (count > max) return true;
		}
	    }
	}
	return false;
    }

    public void pretick() { 
	
	collectPhase = !collectPhase;

	if (collectPhase) {
	    priorityQ.removeAllElements();
	    waitQ.removeAllElements();
	} else if (!isTheremoreCars(0)) {
	    if (priorityQ.size() > 0) {
		permittedCar = getOldestCar(priorityQ);
	    } else if (waitQ.size() > 0) {
		permittedCar = getOldestCar(waitQ);
	    } else {
		permittedCar = null;
	    }
	}
    }
    public void tick() {}
 
    public boolean isOKToGo(Car car,double currentdist) {

	if (collectPhase) {
	    LaneModel lane = parent.getLane(car.carPath[0].getParentID());
	    if (lane.endJunctionSide == priority1 ||
		lane.endJunctionSide == priority2) {
		priorityQ.addElement(car);
	    } else {
		waitQ.addElement(car);
	    }
	    if (car == permittedCar) return true;
	    return false;
	}

	if (car == permittedCar) return true;
	return false;
    }

    //Get the car that has been waiting the longest.
    private Car getOldestCar(Vector cars) {
	int smallest;
	double closest;
	Car returnCar;

	final Iterator it = cars.iterator();
	returnCar = (Car)it.next();
	smallest = returnCar.iD;
	closest = returnCar.distanceToEnd;

	while (it.hasNext()) {
	    final Car tempCar = (Car)it.next();
	    if (tempCar.distanceToEnd < closest) {
		returnCar = tempCar;
		smallest = tempCar.iD;
		closest = tempCar.distanceToEnd;
	    } else if (tempCar.distanceToEnd == closest && 
		       tempCar.iD < smallest){
		returnCar = tempCar;
		smallest = tempCar.iD;
		closest = tempCar.distanceToEnd;
	    }
	}
	return returnCar;
    }

    /**************** Due to mouse Events *******************************/

    public String giveInfo() { 
	String returnString = "priorityQ size: "+priorityQ.size()+
	    ", waitQ size: "+waitQ.size();
	if (permittedCar != null) {
	    returnString += ", permittedCar ID: "+permittedCar.iD;
	}
	return returnString;
    }
}



