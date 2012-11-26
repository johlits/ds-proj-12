
public class MovementRequest {
	private Vehicle vehicle;
	private Edge target;
	private int to;
	private MovementType type;
	
	enum MovementType {
		MOVE,
		FINISH,
		STAY
	}
	
	public MovementRequest (Vehicle vehicle, Edge target) {
		this.type = MovementType.MOVE;
		this.vehicle = vehicle;
		this.target = target;
		this.to = 0;
	}
	
	public MovementRequest (Vehicle vehicle, int to) {
		this.type = MovementType.MOVE;
		this.vehicle = vehicle;
		this.target = vehicle.getPosition();
		this.to = to;
	}
	
	public MovementRequest (Vehicle vehicle, MovementType type) {
		this.vehicle = vehicle;
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
}
