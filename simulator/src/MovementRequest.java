
public class MovementRequest {
	private Vehicle vehicle;
	private Edge target;
	private int to = 0;
	private MovementType type = MovementType.MOVE;
	private CollisionStrategy strategy = CollisionStrategy.Defensive;
	
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
	
	public void stay () {
		type = MovementType.STAY;
	}

	public boolean isDefensive () {
		return this.strategy == CollisionStrategy.Defensive;
	}
}
