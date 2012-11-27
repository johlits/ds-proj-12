
public class Vehicle {
	/**
	 * The position of the road fragment (0 =< milage =< distance)
	 */
	private int milage;
	private Edge position;
	private Node target;
	
	public Vehicle (Edge position, Node target) {
		this.position = position;
		this.target = target;
		this.milage = position.getDistance();
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
	
	public MovementRequest move (int tick, RoutingAlgorithm routing) {
		if (position == null)
			return null;
		if (milage < position.getDistance()) {
			return new MovementRequest(this, milage + 1);
		} else {
			TrafficLight light = position.getTrafficLight();
			if (light == null || light.isGreen(tick)) {
				if (position.getOutgoingNode() == target)
					return new MovementRequest(this, MovementRequest.MovementType.FINISH);
				return new MovementRequest(this, routing.nextEdge(this, tick));
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
			//position.removeVehicle(this);
			position = null;
			break;
		}
	}
}
