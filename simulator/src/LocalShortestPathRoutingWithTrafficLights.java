import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;


public class LocalShortestPathRoutingWithTrafficLights implements RoutingAlgorithm {
	HashMap<Vehicle, ArrayList<PathEdge>> fixedRoutes = new HashMap<Vehicle, ArrayList<PathEdge>>();
	
	@Override
	public Edge nextEdge(Vehicle vehicle, int tick) {
		Edge current = vehicle.getPosition();
		for (PathEdge e : fixedRoutes.get(vehicle)) {
			if (e.getPrev().getEdge() == current) {
				current = e.getEdge();
				break;
			}
		}
		return current;
	}

	@Override
	public void init(Vehicle[] vehicles) {
		for (Vehicle vehicle : vehicles) {
			ArrayList<PathEdge> route = new ArrayList<PathEdge>();
			
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
				for (Edge e : current.getEdge().getOutgoingNode().getOutgoingEdges()) {
					if (visited.contains(e)) continue;
					int dist = current.getEdge().getDistance() + e.getDistance();
					if (e.getTrafficLight() != null)
						dist += e.getTrafficLight().remainingWaitingTime(dist);
					PathEdge d = new PathEdge(current, dist, e);
					distances.offer(d);
					visited.add(e);
				}
			}
			/* build path to target node */
			PathEdge ptr;
			for (ptr = target; ptr.getPrev() != spawn; ptr = ptr.getPrev()) {
				route.add(0, ptr);
			}
			fixedRoutes.put(vehicle, route);
		}
	}

}
