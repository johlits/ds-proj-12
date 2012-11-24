import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.HashMap;

public class LocalShortestPathRoutingWithTrafficLightsAndReservation implements RoutingAlgorithm {

	public static HashMap<Reservation, Integer> reservationTable = new HashMap<Reservation, Integer>();
	
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
					dist += e.getTrafficLight().remainingWaitingTime(dist);
				
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

		return ptr.getEdge();
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
}
