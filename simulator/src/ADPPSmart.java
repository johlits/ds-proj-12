import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Collections;

public class ADPPSmart extends ADPP implements RoutingAlgorithm {
	
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
						(edge.getTrafficLight() == null || edge.getTrafficLight().isGreen(t))) {
					tmp = new Reservation(newE, t, newM);
					reservationTable.put(tmp, congestion+1);
					reservations.add(new CarReservation(newE, false));
				} else {
					tmp = new Reservation(edge, t, m);
					reservationTable.put(tmp, getCongestion(edge, t, m) + 1);
					reservations.add(new CarReservation(newE, true));
				}
				edge = newE;
				m = newM;
			}
			
			Message msg = new Message(Message.createRouteMessage(priority.get(v), 0, reservations), v, 0);
			for (Vehicle vv : vehicles) {
				if (v != vv && priority.get(v) == priority.get(vv)+1)  
					sendMessage(v, vv, msg);
			}
		}
		trafficMonitor.printReport(TrafficMonitor.PrettyPrint.YES);
	}
}
