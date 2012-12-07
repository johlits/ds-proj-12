import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.HashMap;

public class LocalShortestPathRoutingWithTrafficLightsAndReservation extends LocalShortestPathRoutingWithTrafficLights implements RoutingAlgorithm {

	private HashMap<Reservation, Integer> reservationTable = new HashMap<Reservation, Integer>();
	private HashMap<Vehicle, List<Reservation>> vehicleRoutes = new HashMap<Vehicle, List<Reservation>>();
	
	private int getCongestion(Edge e, int t, int m) {
		int congestion = 0;
		Reservation tmp = new Reservation(e, t, m);
		if (reservationTable.containsKey(tmp))
			congestion = reservationTable.get(tmp);
		return congestion;
	}
	
	private List<Edge> getPossibilities(Edge e, int t) {
		ArrayList<Edge> possibilities = new ArrayList<Edge>();
		for (Edge oe : e.getOutgoingNode().getOutgoingEdges()) {
			int c = getCongestion(oe, t, 0);
			if (c < e.getCapacity())
				possibilities.add(oe);
		}
		return possibilities;
	}
	
	@Override
	public void init(Vehicle[] vehicles, Edge[] edges) {
		
		for (Vehicle v : vehicles) {
			List<Reservation> reservations = new ArrayList<Reservation>();
			vehicleRoutes.put(v, reservations);
			
			Edge edge = v.getPosition();
			int m = v.getMilage();
			int congestion = 0;
			
			for (int t = 0; m < edge.getDistance() || edge.getOutgoingNode() != v.getTarget(); t++) {
				int newM;
				Edge newE;
				if (m < edge.getDistance()) {
					newM = m + 1;
					newE = edge;
				} else {
					newE = super.nextEdge(v, t);
					newM = 0;
				}
			
				congestion = getCongestion(newE, t, newM);
				Reservation tmp;
				
				if (congestion < edge.getCapacity()) {
					tmp = new Reservation(newE, t, newM);
					reservationTable.put(tmp, congestion+1);
					reservations.add(tmp);
				} else {
					tmp = new Reservation(edge, t, m);
					reservationTable.put(tmp, getCongestion(edge, t, m) + 1);
					reservations.add(tmp);
				}
				edge = newE;
				m = newM;
			}
		}
	}
	
	@Override
	public Edge nextEdge(Vehicle vehicle, int tick) {
		return vehicleRoutes.get(vehicle).get(tick).e;
	}
	
	public MovementRequest.CollisionStrategy getStrategy(Vehicle v, int tick) {
		Reservation r = vehicleRoutes.get(v).get(tick);
		return r.e == v.getPosition() && r.m == v.getMilage() ?
			MovementRequest.CollisionStrategy.Defensive : MovementRequest.CollisionStrategy.Aggressive;
			
	}
	
	class Reservation {
		public Edge e;
		public int t;
		public int m;
		public Reservation( Edge e, int t, int m ) {
			this.e = e;
			this.t = t;
			this.m = m;
		} 
		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Reservation that = (Reservation) o;

			if (m != that.m) return false;
			if (e != that.e) return false;
			if (t != that.t) return false;

			return true;
		}
		@Override
		public int hashCode()
		{
		    int result = (int) (e.hashCode() ^ (e.hashCode() >>> 32));
		    result = 31 * result + (int) (t ^ (t >>> 32));
		    result = 31 * result + (int) (m ^ (m >>> 32));
		    return result;
		}
	}
	
}
