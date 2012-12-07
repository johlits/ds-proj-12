import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.HashMap;

public class LocalShortestPathRoutingWithTrafficLightsAndReservationZHLS implements RoutingAlgorithm {

	public static HashMap<Reservation, Integer> reservationTable = new HashMap<Reservation, Integer>();
	private static ArrayList<Zone> zones;
	private static int yzones = 0, xzones = 0;
	
	@Override
	public Edge nextEdge(Vehicle vehicle, int tick) {
		Edge edge = vehicle.getPosition();
		
		ArrayList<Edge> visited = new ArrayList<Edge>();
		PriorityQueue<PathEdge> distances = new PriorityQueue<PathEdge>();
		
		// get congestion
		int congestion = 0;
		Reservation key = new Reservation(edge, 0);
		if (reservationTable.containsKey(key))
			congestion = reservationTable.get(key); 
			
		PathEdge spawn = new PathEdge(null, 0, edge, congestion);
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
					dist += e.getTrafficLight().remainingWaitingTime(dist + tick);
				
				// get congestion
				congestion = 0;
				key = new Reservation(e, dist);
				if (reservationTable.containsKey(key))
					congestion = reservationTable.get(key); 

				PathEdge d = new PathEdge(current, dist, e, congestion);
				distances.offer(d);
				visited.add(e);
			}
		}
		
		/* build path to target node */
		PathEdge ptr;
		for (ptr = target; ptr.getPrev() != spawn; ptr = ptr.getPrev()) {
		
			congestion = 0;
			key = new Reservation(ptr.getEdge(), ptr.getDistance());
			if (reservationTable.containsKey(key)) 
				congestion = reservationTable.get(key); 
			
			// use ptr.getDistance() as time-unit for the reservation, probably wrong?
			reservationTable.put(new Reservation(ptr.getEdge(), ptr.getDistance()), congestion+1); 
		}
			
		// TODO: after some number of ticks, clean hashmap of old stuff
		vehicle.newEdgeUpdate(ptr.getEdge());
		return ptr.getEdge();
	}
	
	public MovementRequest.CollisionStrategy getStrategy(Vehicle v, int tick) {
		return MovementRequest.CollisionStrategy.Defensive;
	}
	
	class Reservation {
		public Edge e;
		public int t;
		public Reservation( Edge e, int t ) {
			this.e = e;
			this.t = t;
		} 
		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Reservation that = (Reservation) o;

			if (e != that.e) return false;
			if (t != that.t) return false;

			return true;
		}
		@Override
		public int hashCode()
		{
		    int result = (int) (e.hashCode() ^ (e.hashCode() >>> 32));
		    result = 31 * result + (int) (t ^ (t >>> 32));
		    return result;
		}
	}
	public void init(Vehicle[] vehicles, Edge[] edges) {
		int zid = 0, xzones = 10, yzones = 10;
		float zoneSize = 100;
		zones = new ArrayList<Zone>();
		
		for (int i = 0; i < yzones; i++) 
			for (int j = 0; j < xzones; j++) 
				zones.add(new Zone(i,j,zoneSize,zoneSize,""+(++zid)));
				
		// add 3 edges in each zone
		for (int i = 0,j = 0; i < edges.length; i++) {
			edges[i].setZone(zones.get(j));
			//if (i%3==0)
			//	j++;
		}
	}
}
