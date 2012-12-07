import java.util.ArrayList;
public class Vehicle {
	/**
	 * The position of the road fragment (0 =< milage =< distance)
	 */
	private int milage;
	private Edge position;
	private Node target;
	private ArrayList<Entry> routingTable;
	private String vehicleID;
	
	public Vehicle (Edge position, Node target, String id) {
		this.position = position;
		this.target = target;
		this.milage = position.getDistance();
		routingTable = new ArrayList<Entry>();
		vehicleID = id;
	}
	
	public int getMilage () {
		return milage;
	}
	
	public void setMilage (int milage) {
		this.milage = milage;
	}
	
	public Edge getPosition () {
		return position;
	}

	public Node getTarget () {
		return target;
	}
	private ArrayList<LinkResponse> broadcastLinkRequest(Edge e) {
		return e.getZone().broadcastLinkRequest(this);
	}
	public LinkResponse linkRequest() {
		return new LinkResponse(vehicleID, position.getZone().getZoneID());
	}
	
	public void newEdgeUpdate(Edge e) {
		System.out.println(vehicleID + " broadcasts link request in zone " + e.getZone().getZoneID());
		ArrayList<LinkResponse> al = broadcastLinkRequest(e);
		if (al.isEmpty()) 
			System.out.println("but got no answers");
		else
			System.out.println("got answers");
		for (LinkResponse linkResponse : al) {
			System.out.println(linkResponse);
		}
		System.out.println("---");
	}
	
	public MovementRequest move (int tick, RoutingAlgorithm routing) {
		if (position == null)
			return null;
		if (milage < position.getDistance()) {
			return new MovementRequest(this, milage + 1, routing.getStrategy(this, milage + 1));
		} else {
			TrafficLight light = position.getTrafficLight();
			if (light == null || light.isGreen(tick)) {
				if (position.getOutgoingNode() == target)
					return new MovementRequest(this, MovementRequest.MovementType.FINISH);
				return new MovementRequest(this, routing.nextEdge(this, tick), routing.getStrategy(this, tick));
			}
			return new MovementRequest(this, MovementRequest.MovementType.STAY);
		}
	}
	
	public void apply (MovementRequest request) {
	
		switch (request.getType()) {
		case MOVE:
			if (request.getTarget() == position) {
				milage = request.getTo();
			} else {
				position.removeVehicle(this);
				(position = request.getTarget()).addVehicle(this);
				milage = 0;
			}
			break;
		case FINISH:
			position.removeVehicle(this);
			position = null;
			break;
		}
	}
}
