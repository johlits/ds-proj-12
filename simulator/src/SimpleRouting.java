import java.util.ArrayList;
import java.util.Stack;


public class SimpleRouting implements RoutingAlgorithm {
	@Override
	public Edge nextEdge(Vehicle vehicle, int tick) {
		Edge edge = vehicle.getPosition();
		ArrayList<Edge> visited = new ArrayList<Edge>();
		Stack<PathEdge> distances = new Stack<PathEdge>();
		PathEdge spawn = new PathEdge(null, 0, edge);
		distances.add(spawn);
		PathEdge target = null;
		/* calculate path distances */
		while (!distances.isEmpty()) {
			PathEdge current = distances.pop();
			if (current.getEdge().getOutgoingNode() == vehicle.getTarget()) {
				target = current;
				break;
			}
			for (Edge e : current.getEdge().getOutgoingNode().getOutgoingEdges()) {
				if (visited.contains(e)) continue;
				PathEdge d = new PathEdge(current, current.getEdge().getDistance() + e.getDistance(), e);
				distances.push(d);
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
