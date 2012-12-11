import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Collections;

public class ADPP extends LocalShortestPathRoutingWithTrafficLights implements RoutingAlgorithm {

	protected HashMap<Reservation, Integer> reservationTable = new HashMap<Reservation, Integer>();
	protected HashMap<Vehicle, List<CarReservation>> vehicleRoutes = new HashMap<Vehicle, List<CarReservation>>();
	
	protected HashMap<Vehicle, List<Message>> inbox = new HashMap<Vehicle, List<Message>>();
	protected HashMap<Vehicle, Integer> priority = new HashMap<Vehicle, Integer>();
	public static HashMap<Edge, Integer> edgeID = new HashMap<Edge, Integer>();
	public TrafficMonitor trafficMonitor = new TrafficMonitor();
	
	protected int getCongestion(Edge e, int t, int m) {
		int congestion = 0;
		Reservation tmp = new Reservation(e, t, m);
		if (reservationTable.containsKey(tmp))
			congestion = reservationTable.get(tmp);
		return congestion;
	}
	
	@Override
	public void init(Vehicle[] vehicles, Edge[] edges) {

		for (int i = 0; i < edges.length; i++) 
			edgeID.put(edges[i], i);

		// init inboxes
		for (Vehicle v : vehicles) 
			inbox.put(v, new LinkedList<Message>());
			
		// set priorities
		LinkedList<Integer> tmpll = new LinkedList<Integer>();
		for (int i = 0; i < vehicles.length; i++) 
			tmpll.add(i);
		//Collections.shuffle(tmpll);
		for (Vehicle v : vehicles) 
			priority.put(v, tmpll.removeLast());
	
		for (Vehicle v : vehicles) {
		
			List<CarReservation> reservations = new ArrayList<CarReservation>();
			vehicleRoutes.put(v, reservations);
			
			Edge edge = v.getPosition();
			int m = v.getMilage();
			int congestion = 0;
			
			for (int t = 0;m < edge.getDistance() - 1 || edge.getOutgoingNode() != v.getTarget() ||
					(edge.getTrafficLight() != null && !edge.getTrafficLight().isGreen(t)); t++) {
				int newM;
				Edge newE;
				if (m < edge.getDistance() - 1) {
					newM = m + 1;
					newE = edge;
				} else if (edge.getTrafficLight() != null && !edge.getTrafficLight().isGreen(t)) {
					newM = m;
					newE = edge;
				} else {
					newE = super.nextEdge(v, edge, t);
					newM = 0;
				}
			
				congestion = getCongestion(newE, t, newM);
				Reservation tmp;
				
				if (congestion < newE.getCapacity() &&
						((m < edge.getDistance() - 1) ||
								edge.getTrafficLight() == null || edge.getTrafficLight().isGreen(t))) {
					tmp = new Reservation(newE, t, newM);
					reservationTable.put(tmp, congestion+1);
					reservations.add(new CarReservation(newE, false));
					edge = newE;
					m = newM;
				} else {
					tmp = new Reservation(edge, t, m);
					reservationTable.put(tmp, getCongestion(edge, t, m) + 1);
					reservations.add(new CarReservation(newE, true));
				}
			}
			
			Message msg = new Message(Message.createRouteMessage(priority.get(v), 0, reservations), v, 0);
			for (Vehicle vv : vehicles) {
				if (v != vv && priority.get(v) > priority.get(vv))  
					sendMessage(v, vv, msg);
			}
		}
		trafficMonitor.printReport(TrafficMonitor.PrettyPrint.YES);
	}
	
	protected void sendMessage(Vehicle from, Vehicle to, Message msg) {
		inbox.get(to).add(msg);
		trafficMonitor.addMessage(msg);
	}
	
	@Override
	public Edge nextEdge(Vehicle vehicle, int tick) {
		if (vehicleRoutes.get(vehicle).size() > tick)
			return vehicleRoutes.get(vehicle).get(tick).e;
		return super.nextEdge(vehicle, tick);
	}
	
	public MovementRequest.CollisionStrategy getStrategy(Vehicle v, int tick) {
		List<CarReservation> cr = vehicleRoutes.get(v);
		if (cr.size() > tick) {
			CarReservation r = cr.get(tick);
			return r.defensive ?
				MovementRequest.CollisionStrategy.Defensive :
				MovementRequest.CollisionStrategy.Aggressive;
		} else
			return MovementRequest.CollisionStrategy.Defensive;
			
	}
	class Reservation {
		public Edge e;
		public int t;
		public int m;
		public Reservation (Edge e, int t, int m ) {
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
