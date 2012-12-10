import java.util.ArrayList;
import java.util.Stack;

public class HeatMapRouting extends LocalShortestPathRoutingWithTrafficLights implements RoutingAlgorithm {
	/* index = time tick */
	private ArrayList<HeatMap> maps = new ArrayList<HeatMap>();
	private Stack<PathEdge> path;

	@Override
	public void init(Vehicle[] vehicles, Edge[] edges) {
		generateHeatmap(vehicles, 0);
		printHeatMaps();
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
	
	public void generateHeatmap(Vehicle[] vehicles, int tick) {
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
