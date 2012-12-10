import java.util.Random;


public class RandomRouting implements RoutingAlgorithm {
	@Override
	public Edge nextEdge(Vehicle vehicle, int tick) {
		Edge[] edges = vehicle.getPosition().getOutgoingNode().getOutgoingEdges();
		return edges[new Random().nextInt(edges.length)];
	}

	@Override
	public void init(Vehicle[] vehicles, Edge[] edges) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public MovementRequest.CollisionStrategy getStrategy(Vehicle v, int tick) {
		return MovementRequest.CollisionStrategy.Defensive;
	}
}
