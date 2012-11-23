
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
	
	public Edge getPosition () {
		return position;
	}
	
	public Node getTarget () {
		return target;
	}
	
	public void move (int tick, RoutingAlgorithm routing) {
		/* if we have not reached the end of our current road, advance on the road fragment */
		if (milage < position.getDistance()) {
			++milage;
		} else {
			/* we have reached the end of our road fragment and require a decision for the next fragment.
			 * probably we need to wait at the traffic light
			 */
			TrafficLight light = position.getTrafficLight();
			if (light == null || light.isGreen(tick)) {
				if (position.getOutgoingNode() == target) {
					// vehicle reached goal, make it disappear
					// TODO maybe a call to the routing, that we finished (if needed)
					position.removeVehicle(this);
					position = null;
					return;
				}
				Edge next = routing.nextEdge(this, tick);	
				if (next.getVehicleCount() < next.getCapacity()) {
					/* move to next road fragment (edge) */
					position.removeVehicle(this);
					next.addVehicle(this);
					position = next;
					milage = 0;
				}
			}
		}
	}
}
