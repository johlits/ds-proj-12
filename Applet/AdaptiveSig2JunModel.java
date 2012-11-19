import java.util.Vector;
import java.util.Iterator;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Color;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;

import time.*;

public class AdaptiveSig2JunModel extends Sig2JunctionModel implements Timed {

    int[] lightTimeA = new int[maxNumRoads];
    int[] carCount = new int[maxNumRoads];
    
    final static int EASTWEST = 0;
    final static int NORTHSOUTH = 1;
    
    public AdaptiveSig2JunModel(int iD, Shape s, int lighttime,
		   boolean actuated, RoadNetwork parent) {
	super(iD,s,lighttime,actuated,parent);
	for (int i=0;i<2;i++) {
	    lightTimeA[i] = lighttime;
	    carCount[i] = -1;
	}
    }

    public void pretick() {
	if (actuated) pretickactuated();
	else preticknonactuated();

	if (lightTimer == 0) {
	    if (eastLightsGreen()) {
		carCount[EASTWEST] = numCarsWaitingAtLight();
		lightTime = lightTimeA[EASTWEST];
	    } else {
		carCount[NORTHSOUTH] = numCarsWaitingAtLight();
		lightTime = lightTimeA[NORTHSOUTH];
	    }

	    if (carCount[EASTWEST] != -1 && carCount[NORTHSOUTH] != -1) {
		if (carCount[NORTHSOUTH] > carCount[EASTWEST]) {
		    lightTimeA[NORTHSOUTH] += 5;
		    lightTimeA[EASTWEST] -= 5;
		} else {
		    lightTimeA[NORTHSOUTH] -= 5;
		    lightTimeA[EASTWEST] += 5;
		}
		for (int i=0;i<2;i++) {
		    if (lightTimeA[i] < ORANGETIME+10) 
			lightTimeA[i] = ORANGETIME+10;
		}
	    }
	}
    }

    private boolean eastLightsGreen() {
	return (lightColor[1][0] == Color.green);
    }

    private int numCarsWaitingAtLight() {
	int count = 0;
	for (int i=0;i<maxNumRoads;i++) {
	    for (int j=0;j<ei[i];j++) { 
		if (lightColor[i][j] == Color.green) {
		    count += parent.getLane(endLanesID[i][j]).numStationaryCars();
		}
	    }
	}
	return count;
    }

    public void tick() {}

    /**************** Due to mouse Events *******************************/

    public String giveInfo() { 
	return "Adaptive Signalled junction. EW time= "+lightTimeA[EASTWEST]+
	    ",NS time= "+lightTimeA[NORTHSOUTH];
    }
}


