import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Queue;

public class ADPPKLocal extends ADPPSmart implements RoutingAlgorithm {
	
	protected int broadcast_depth = 0;
	protected int k_hops = 0;
	
	public ADPPKLocal(int bd, int kh) {
		broadcast_depth = bd;
		k_hops = kh;
	}
	
	public LinkedList<Vehicle> inRadius(Vehicle sender, Vehicle[] vehicles, int n) {
		Edge tempEdge = sender.getPosition();
		int tempHops = 0;
		Queue<Edge> qe = new LinkedList<Edge>();
		Queue<Integer> qi = new LinkedList<Integer>();
		ArrayList<Edge> visited = new ArrayList<Edge>();
		qe.offer(tempEdge);
		visited.add(tempEdge);
		qi.offer(0);
		while (!qe.isEmpty()) {
			tempEdge = qe.poll();
			tempHops = qi.poll();
			if (tempHops >= n)
				continue;
			for (Edge e : tempEdge.getOutgoingNode().getOutgoingEdges()) {
				if (!visited.contains(e)) {
					visited.add(e);
					qe.offer(e);
					qi.offer(tempHops+1);
				}
			}
		}
		LinkedList<Vehicle> tmp = new LinkedList<Vehicle>();
		for (Edge e : visited) {
			for (Vehicle v : vehicles) {
				if (v.getPosition() == e && v != sender) {
					tmp.add(v);
				}
			}
		}
		return tmp;
	}
	
	public void broadcast(Vehicle sender, Vehicle[] vehicles, Message msg, int k, int n) {
	
		if (k < 1)
			return;
			
		LinkedList<Vehicle> reachable = inRadius(sender, vehicles, n);
		reachable.add(sender);
		Queue<Vehicle> qv = new LinkedList<Vehicle>();
		Queue<Integer> qi = new LinkedList<Integer>();
		for (Vehicle v : reachable) {
			qv.offer(v);
			qi.offer(1);
		}
		while (!qv.isEmpty()) {
			Vehicle tmpVehicle = qv.poll();
			int tmpK = qi.poll();
			if (tmpK >= k)
				break;
			
			LinkedList<Vehicle> ir = inRadius(tmpVehicle, vehicles, n);
			for (Vehicle v : ir) {
				if (reachable.contains(v))
					continue;
				qv.add(v);
				qi.add(tmpK+1);
				reachable.add(v);
			}
		}
		
		// ASSUMES vehicles IS ORDERED BY PRIORITY!!
		for (Vehicle v : vehicles) {
			if (reachable.contains(v) && v != sender && priority.get(v) < priority.get(sender)) {
				sendMessage(sender, v, msg);
			}
		}
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
			
			for (int t = 0;m < edge.getDistance() || edge.getOutgoingNode() != v.getTarget() ||
					(edge.getTrafficLight() != null && !edge.getTrafficLight().isGreen(t)); t++) {
				int newM;
				Edge newE;
				if (m < edge.getDistance()) {
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
			broadcast(v, vehicles, msg, k_hops, broadcast_depth); // HOPS, BROADCAST DEPTH
		}
		trafficMonitor.printReport(TrafficMonitor.PrettyPrint.YES);
		
	}
}
