
public interface RoutingAlgorithm {
	public Edge nextEdge (Vehicle vehicle, int tick);
	public void init(Vehicle[] vehicles);
}
