
public class RandomRouting implements RoutingAlgorithm {
	@Override
	public Edge nextEdge(Vehicle vehicle, int tick) {
		return vehicle.getPosition().getOutgoingNode().getOutgoingEdges()[0];
	}

	@Override
	public void init(Vehicle[] vehicles) {
		// TODO Auto-generated method stub
		
	}
}
