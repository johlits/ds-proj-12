import java.util.ArrayList;


public class MovementRequest {
	private Vehicle vehicle;
	private Edge target;
	private int to = 0;
	private MovementType type = MovementType.MOVE;
	private CollisionStrategy strategy = CollisionStrategy.Defensive;
	/* in case an option is blocked, keep track of remaining available options */
	private ArrayList<Edge> options = null;
	
	enum MovementType {
		MOVE,
		FINISH,
		STAY
	}
	
	enum CollisionStrategy {
		Aggressive,
		Defensive
	}

	public MovementRequest (Vehicle vehicle, Edge target, int to) {
		this.vehicle = vehicle;
		this.target = target;
		this.to = to;		
	}
	
	public MovementRequest (Vehicle vehicle, Edge target, CollisionStrategy strategy) {
		this.vehicle = vehicle;
		this.target = target;
		this.strategy = strategy;
	}
	
	public MovementRequest (Vehicle vehicle, int to, CollisionStrategy strategy) {
		this.vehicle = vehicle;
		this.target = vehicle.getPosition();
		this.to = to;
		this.strategy = strategy;
	}
	
	public MovementRequest (Vehicle vehicle, MovementType type) {
		this.vehicle = vehicle;
		this.target = vehicle.getPosition();
		this.to = vehicle.getMilage();
		this.type = type;
	}
	
	public int getTo () {
		return to;
	}
	
	public MovementType getType () {
		return type;
	}
	
	public Edge getTarget () {
		return target;
	}
	
	public Vehicle getVehicle () {
		return vehicle;
	}
	
	public void reject () {
		if (target != vehicle.getPosition()) {
			if (options == null) {
				options = new ArrayList<Edge>();
				Node current = vehicle.getPosition().getOutgoingNode();
				for (Edge e : current.getOutgoingEdges())
					if  (e != this.target)
						options.add(e);
			}
			if (options.size() > 0) {
				this.target = options.remove(0);
				return;
			}
		}
		this.type = MovementType.STAY;
		this.to = vehicle.getMilage();
		this.target = vehicle.getPosition();
	}

	public boolean isDefensive () {
		return this.strategy == CollisionStrategy.Defensive;
	}
}
