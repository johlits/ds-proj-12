
import java.util.Vector;
import java.util.Iterator;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Color;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;

import time.*;

public class AdaptiveSig1JunModel extends Sig1JunctionModel implements Timed {

    int[] lightTime = new int[maxNumRoads];
    int[] carCount = new int[maxNumRoads];
        
    public AdaptiveSig1JunModel(int iD, Shape s, int lighttime,
		    boolean actuated, RoadNetwork parent) {
	super(iD,s,lighttime,actuated,parent);
	for (int i=0;i<maxNumRoads;i++) {
	    lightTime[i] = lighttime;
	    carCount[i] = 0;
	}
	    
    }

    public void pretick() {
	
	if (lightTimer == lightTime[currentLightSet]-ORANGETIME) {

	    //calculate the next light set.
	    if (actuated) {
		nextLightSet = getNextValidLightSet();
	    } else {
		nextLightSet = getNextLightSet(currentLightSet);
	    }

	    //Turn red lights orange if they are going to change.
	    if (nextLightSet != currentLightSet) { 
		for (int j=0;j<ei[currentLightSet];j++) {
		    lightColor[currentLightSet][j] = Color.orange;
		}
	    }
	}
    
	if (lightTimer >= lightTime[currentLightSet]) {

	    lightTimer = 0;

	    if (nextLightSet != currentLightSet) { 
		
		for (int j=0;j<ei[currentLightSet];j++) {
		    lightColor[currentLightSet][j] = Color.red;
		}

		currentLightSet = nextLightSet;

		for (int j=0;j<ei[currentLightSet];j++) {
		    lightColor[currentLightSet][j] = Color.green;
		}

		//Count cars that are waiting at each traffic light set.
		for (int i=0;i<maxNumRoads;i++) {
		    int count = 0;
		    for (int j=0;j<ei[i];j++) { 
			count += parent.getLane(endLanesID[i][j]).numStationaryCars();
		    }
		    carCount[i] = count;
		}
		
		//Modify green light time according to cars waiting.
		lightTime[getMinCarCountSide()] -= 5;
		lightTime[getMaxCarCountSide()] += 5;

		//Limit the minimum time that a light set can get.
		for (int i=0;i<maxNumRoads;i++) {
		    if (lightTime[i] < ORANGETIME+10) 
			lightTime[i] = ORANGETIME+10;
		}
	    }
	} else lightTimer++;
    }
    
    public void tick() {}

    private int getMaxCarCountSide() {
	int max = 0, returnval = 0;
	for (int i=0;i<maxNumRoads;i++) {
	    if (carCount[i] > max) {
		max = carCount[i];
		returnval = i;
	    }
	}
	return returnval;
    }

    private int getMinCarCountSide() {
	int min = 100000, returnval = 0;
	for (int i=0;i<maxNumRoads;i++) {
	    if (carCount[i] < min) {
		min = carCount[i];
		returnval = i;
	    }
	}
	return returnval;
    }

    /**************** Due to mouse Events *******************************/

    public String giveInfo() { 
	return "Adaptive Signalled junction. N= "+ lightTime[_NORTH] + 
	    ",E= " + lightTime[_EAST] +
	    ",S= " + lightTime[_SOUTH] +
	    ",W= " + lightTime[_WEST];
    }
}











