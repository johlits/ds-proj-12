import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Stack;

public class HeatMapRouting extends LocalShortestPathRoutingWithTrafficLights implements RoutingAlgorithm {
	private ArrayList<HeatMap<Edge>> maps = new ArrayList<HeatMap<Edge>>();
	private Stack<PathEdge> path;

	/* TODO heatmap should be rebuild regularly */
	@Override
	public void init(Vehicle[] vehicles, Edge[] edges) {
		generateHeatmap(vehicles, 0);
		printHeatMaps();
	}
	
	@Override
	public Edge nextEdge(Vehicle vehicle, int tick) {
		if (maps.size() < tick)
			return null;
		return nextEdgeHeat(vehicle, vehicle.getPosition(), tick);
	}

	private int getHeat (int tick, Edge e) {
		return maps.size() > tick ? maps.get(tick).getHeat(e) : 0;
	}

	/* TODO could maybe refactored into calculateDistance */
	private Edge nextEdgeHeat(Vehicle vehicle, Edge edge, int tick) {
		ArrayList<Edge> visited = new ArrayList<Edge>();
		PriorityQueue<PathEdge> distances = new PriorityQueue<PathEdge>();
		PathEdge spawn = new PathEdge(null, 0, edge);
		spawn.setCongestion(0, 0.0);
		distances.add(spawn);
		PathEdge target = null;
		/* calculate path distances */
		while (!distances.isEmpty()) {
			PathEdge current = distances.poll();
			if (current.getEdge().getOutgoingNode() == vehicle.getTarget()) {
				target = current;
				break;
			}
			for (Edge e : getPossibilities(current.getEdge(), tick + current.getDistance())) {
				if (visited.contains(e)) continue;
				PathEdge d = calculateDistance(current, e, tick);
				/* TODO just snapshot for this tick, or average over all ticks i would need to traval it? */
				d.setCongestion(getHeat(tick + current.getDistance(), d.getEdge()),
					current.getCongestion());
				distances.offer(d);
				visited.add(e);
			}
		}
		/* build path to target node */
		return getEdgeAfterSpawn(spawn, target);
	}

	private void printHeatMaps () {
		int i = 0;
		for (HeatMap m : maps)
			System.out.printf("%d:\n%s", i++, m);
	}

	protected Edge getEdgeAfterSpawn (PathEdge spawn, PathEdge ptr) {
		path = new Stack<PathEdge>();
		for (; ptr.getPrev() != spawn; ptr = ptr.getPrev()) path.push(ptr);
		path.push(ptr);
		return ptr.getEdge();
	}
	
	private void generateHeatmap(Vehicle[] vehicles, int tick) {
		for (Vehicle v : vehicles) {
			int m = v.getMilage();
			int t = tick;
			/* the way to the next edge */
			Edge pos = v.getPosition();
			System.out.printf("Vehicle: %s, start: %s\n", v, v.getPosition());
			int diff = pos.getDistance() - m;
			TrafficLight tl = pos.getTrafficLight();
			if (tl != null)
				diff += tl.remainingWaitingTime(t + diff - 1);
			System.out.printf("initial diff = %d\n", diff);
			heat (v.getPosition(), t, t + diff);
			t += diff;
			/* compute route to target */
			super.nextEdge(v, t);
			/* heat it up */
			int off = 0;
			while (!path.isEmpty()) {
				PathEdge p = path.pop();
				heat (p.getEdge(), t + off, t + p.getDistance());
				System.out.printf("[%s]: %d -> %d\n", v, t + off, t + p.getDistance());
				off = p.getDistance();
			}
		}
	}
	
	private void heat (Edge e, int fromTick, int toTick) {
		if (toTick < fromTick)
			return;
		while (this.maps.size() < toTick)
			this.maps.add(new HeatMap());
		for (int t = fromTick; t < toTick; t++)
			this.maps.get(t).increaseHeat(e);
	}

}
