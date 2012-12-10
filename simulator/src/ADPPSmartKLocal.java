import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Queue;

public class ADPPSmartKLocal extends ADPPKLocal implements RoutingAlgorithm {
	
	public ADPPSmartKLocal(int bd, int kh) {
		super(bd, kh);
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
		fin: 
		for (Vehicle v : vehicles) {
			if (reachable.contains(v) && v != sender && priority.get(v) < priority.get(sender)) {
				sendMessage(sender, v, msg);
				break fin;
			}
		}
	}
}
