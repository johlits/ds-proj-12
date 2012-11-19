import time.*;

public class CarGenerator implements Timed {
    
    private int topSpeed = 30;
    private boolean speedLimited = false;
    protected RoadNetwork roadNetwork;
    protected TimeManager ticker;
    private LaneModel[] startLanes;
    private int[] lanesCounter;
    private int laneNum;
        
    public CarGenerator(RoadNetwork roadNetwork,int speed) {
	this.roadNetwork = roadNetwork;
	setCarSpeeds(speed);

	laneNum = roadNetwork.getNumberInputs();

	lanesCounter = new int[laneNum];
	startLanes = new LaneModel[laneNum];

	System.out.println("totalLaneNumber "+roadNetwork.totalLaneNumber);
	System.out.println("laneNum "+laneNum);

	int j=0;
	for (int i=0;i < roadNetwork.totalLaneNumber;i++) {
	    if (roadNetwork.lanes[i].startJunctionID == roadNetwork.MAPEDGE) {
		startLanes[j] = roadNetwork.lanes[i];
		lanesCounter[j++] = (int)(Math.random()*roadNetwork.lanes[i].busyness);
		System.out.println("lanesCounter[j-1] "+lanesCounter[j-1]);
	    }
	}
    }

    public void addToTimer(TimeManager theTick) {
	ticker = theTick;
	ticker.addTimed(this);
    }

    public void kill() {
	ticker.removeTimed(this);
    }

    public void pretick() {
	//car generator
	for (int i=0; i < laneNum; i++) {
	    lanesCounter[i]++;
 
	    if (lanesCounter[i] >= startLanes[i].busyness) {
		startLanes[i].carQueue++;
		lanesCounter[i] = 0;

		System.out.println("Adding to car Q");
	    }

	    if (startLanes[i].carQueue > 0) {
		Car c;
		if (speedLimited) {
		    c = new Car(startLanes[i].getStartingLaneSection(),this,ticker,topSpeed);
		} else {
		    c = new Car(startLanes[i].getStartingLaneSection(),this,ticker, 
				topSpeed+(int)(Math.random()*10.0));
		}
		if (c.checkSetUp()) startLanes[i].carQueue--;
	    }
	}
    }

    public void tick() {}

    public void setCarSpeeds(int index) {
	switch (index) {
	case 0 : speedLimited = false; topSpeed = 25; break;     
	case 1 : speedLimited = true;  topSpeed = 30; break; 
	case 2 : speedLimited = false; topSpeed = 35; break; 
	case 3 : speedLimited = true;  topSpeed = 40; break; 
	default : System.out.println("Unexpected value in setCarSpeeds");
	}
    }
}











