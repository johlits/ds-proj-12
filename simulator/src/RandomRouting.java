
public class RandomRouting implements RoutingAlgorithm {
	@Override
	public Edge nextEdge(Vehicle vehicle, int tick) {
		return vehicle.getPosition().getOutgoingNode().getOutgoingEdges()[0];
	}
}
