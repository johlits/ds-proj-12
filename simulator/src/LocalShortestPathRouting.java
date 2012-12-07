import java.util.ArrayList;
import java.util.PriorityQueue;

public class LocalShortestPathRouting implements RoutingAlgorithm {

	protected PathEdge calculateDistance (PathEdge a, Edge b, int tick) {
		return new PathEdge(a, a.getEdge().getDistance() + b.getDistance(), b);
	}

	private List<Edge> getPossibilities(Edge e, int t) {
		return e.getOutgoingNode().getOutgoingEdges();
	}

	@Override
	public Edge nextEdge(Vehicle vehicle, int tick) {
		Edge edge = vehicle.getPosition();
		ArrayList<Edge> visited = new ArrayList<Edge>();
		PriorityQueue<PathEdge> distances = new PriorityQueue<PathEdge>();
		PathEdge spawn = new PathEdge(null, 0, edge);
		distances.add(spawn);
		PathEdge target = null;
		/* calculate path distances */
		while (!distances.isEmpty()) {
			PathEdge current = distances.poll();
			if (current.getEdge().getOutgoingNode() == vehicle.getTarget()) {
				target = current;
				break;
			}
			for (Edge e : getPossibilities(current, tick + current.getEdge().getDistance())) {
				if (visited.contains(e)) continue;
				PathEdge d = calculateDistance(current, e, tick);
				distances.offer(d);
				visited.add(e);
			}
		}
		/* build path to target node */
		PathEdge ptr;
		for (ptr = target; ptr.getPrev() != spawn; ptr = ptr.getPrev());
		return ptr.getEdge();
	}

	@Override
	public void init(Vehicle[] vehicles, Edge[] edges) {
		// TODO Auto-generated method stub
		
	}

}
