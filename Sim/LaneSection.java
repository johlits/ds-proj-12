import java.util.*;
import java.awt.Graphics2D;

public class LaneSection extends CarContainer {

	private LaneModel parent;
    
	public LaneSection(int iD, LaneModel parent,
										 int startX,int startY,int endX,int endY) {

		super(iD,startX,startY,endX,endY);
		this.parent = parent;
	}

	public boolean isLastOne() {

		if (parent.endJunctionID == RoadNetwork.MAPEDGE) {
	    //System.out.println("islastOne() in LaneSection not false.");
	    return (iD == parent.getNumberOfLaneSections()-1);
		} else return false; 
	}
	
	public ArrayList<CarContainer> getAdjacent() {
		ArrayList<CarContainer> temp = new ArrayList<CarContainer>();
		if (iD == parent.getNumberOfLaneSections()-1) {
			for (CarContainer c : parent.getChoices()) {
	    		temp.add(c);
	    	}
		} else 
			temp.add(parent.getLaneSection(iD+1));
		return temp;
	}

	public CarContainer onToNext() {
		//System.out.println("onToNext() in LaneSection "+iD);
		if (iD == parent.getNumberOfLaneSections()-1) {
	    return parent.passToJunction();
		} else return parent.getLaneSection(iD+1);
	}

	public double[] isOKToGo(Car car,double[] inFrontInfo,double distToEnd) {
	
		if (iD == parent.getNumberOfLaneSections()-1 &&
				!(parent.isOkToGo(car,distToEnd))) {

	    if (inFrontInfo[0] >= distToEnd) {
				inFrontInfo[0] = distToEnd - car.halflength;
				inFrontInfo[1] = 0; 
	    }
		}
		return inFrontInfo;
	}

	public int getParentID() { return parent.getID(); }

	public String toString() {
		return "lane section " + getParentID() + "." + iD;
	}
}









