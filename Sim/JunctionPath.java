import java.util.*;
import java.awt.Graphics2D;

public class JunctionPath extends CarContainer {

	private JunctionModel parent;
	private int endLaneID;

	//iD = ID of lane that ends to become this
	//endLaneID = ID of lane that starts when this ends.

	public JunctionPath(int startiD,int endID,JunctionModel parent,
											int startX,int startY,int endX,int endY) {

		super(startiD,startX,startY,endX,endY);
		this.parent = parent;
		endLaneID = endID;
	}

	public boolean isLastOne() {
		return false;
	} 
	
	public ArrayList<CarContainer> getAdjacent() {
		ArrayList<CarContainer> temp = new ArrayList<CarContainer>();
		temp.add(parent.getnextLane(endLaneID));
		return temp;
	}

	public void drawAllGhostCars(Graphics2D g2d) {
		final Iterator iterator = cars.iterator();
		while (iterator.hasNext()) {
	    final Car tempcar =(Car)iterator.next();
	    tempcar.drawGhostCar(g2d);
		}
	}

	public CarContainer onToNext() {
		//System.out.println("onToNext() in JunctionPath "+iD);
		return parent.getnextLane(endLaneID);
	}

	public int getEndLaneID() { return endLaneID; }

	public int getParentID() { return parent.getID(); }
	
	public String toString() { return "junction path " + getParentID() + "." + iD + "." + getEndLaneID(); }
}













