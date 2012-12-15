import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class Simulation {
	private Graph graph;
	private int time;
	private List<Vehicle> vehicles;
	private RoutingAlgorithm routing;
	private ArrayList<Integer> vehicleFinishTimes;

	public Simulation(Graph graph, Vehicle[] vehicles, Edge[] edges, RoutingAlgorithm routing) {
		this.graph = graph;
		this.vehicles = new ArrayList<Vehicle>();
		for (int i = 0; i < vehicles.length; i++)
			this.vehicles.add(vehicles[i]);
		this.time = 0;
		this.routing = routing;
		this.vehicleFinishTimes = new ArrayList<Integer>();
		routing.init(vehicles, edges);
	}
	
	public List<Vehicle> getVehicles () {
		return vehicles;
	} 

	public String visualize() {
		String dot = graph.toDot(this.time);
		String url = null;
		try {
			Process p = Runtime.getRuntime().exec("dot -Tsvg");
			OutputStream o = p.getOutputStream();
			BufferedReader i = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			o.write(dot.getBytes());
			o.close();
			File temp = File.createTempFile("temp", ".svg");
			FileWriter fileoutput = new FileWriter(temp);
			BufferedWriter buffout = new BufferedWriter(fileoutput);
			String line = "";
			while ((line = i.readLine()) != null) {
				buffout.write(line);
			}
			i.close();
			buffout.close();
			url = temp.getPath();
			p.destroy();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return url;
	}

	public String save(String txt) {
		File temp;
		String url = null;
		try {
			temp = File.createTempFile("temp", ".svg");
			FileWriter fileoutput = new FileWriter(temp);
			BufferedWriter buffout = new BufferedWriter(fileoutput);
			buffout.write(txt);
			buffout.close();
			url = temp.getPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return url;
	}

	public void progress() {
		progress(null);
	}
	
	public void progress(MovementRequestApplyHandler handler) {
		List<MovementRequest> requests = new ArrayList<MovementRequest>();
		for (Vehicle v : vehicles)
			if (v.getPosition() != null)
				requests.add(v.move(time, routing));
		requests = resolveMovements(requests);
		for (MovementRequest r : requests) {
			if (r.getType() == MovementRequest.MovementType.FINISH) {
				vehicles.remove(r.getVehicle());
				vehicleFinishTimes.add(time);
			}
			if (handler != null)
				handler.apply(r);
			/*System.out.printf("tick %d: [%s@%d] -> [%s@%d] (%s)\n", time,
					r.getVehicle().getPosition(), r.getVehicle().getMilage(),
					r.getTarget(), r.getTo(), r.getType().name());*/
			r.getVehicle().apply(r);
		}
		++time;
		if (handler != null)
			handler.nextTick();
	}

	private List<MovementRequest> resolveMovements (List<MovementRequest> requests) {
		boolean done = true;
		do {
			done = true;
			for (MovementRequest r : requests) {
				if (r.getType() == MovementRequest.MovementType.MOVE) {
					Edge edge = r.getTarget();
					int defensiveCount = 0;
					List<MovementRequest> same = new ArrayList<MovementRequest>();
					int delta = 0;
					for (MovementRequest r2 : requests)
						if (edge == r2.getTarget())
							if (r2.getType() == MovementRequest.MovementType.MOVE && r2.getTo() == r.getTo())
								same.add(r2.isDefensive() ? defensiveCount++ : same.size(), r2);
							else if (r2.getType() == MovementRequest.MovementType.STAY &&
								r.getTo() == r2.getVehicle().getMilage())
								++delta;
							else if (r2.getType() == MovementRequest.MovementType.FINISH &&
									r2.getVehicle().getMilage() == r.getTo())
								--delta;
					//System.out.printf("same requests: %d + %d (delta), capacity = %d\n", same.size(), delta, edge.getCapacity());
					if (same.size() + delta <= edge.getCapacity())
						continue;
					//System.out.printf("rejecting out some requests ...");
					while (same.size() > 0 && same.size() + delta > edge.getCapacity())
						same.remove(new Random().nextInt(defensiveCount > 0 ?
							defensiveCount-- : same.size())).stay();
					if (same.size() == 0 && delta > edge.getCapacity()) {
						System.out.println("We got a problem here Houston");
					}
					//System.out.printf("after rejecting: %d + %d (delta), capacity = %d\n", same.size(), delta, edge.getCapacity());
					done = false;
					break;
				}
			}
		} while (!done);
		return requests;
	}

	public void finish() {
		while (vehicles.size() > 0)
			progress();
		System.out.printf("steps: %d\n", this.time);
	}

	public boolean isFinished() {
		return vehicles.size() == 0;
	}

	public int getTick() {
		return this.time;
	}

	public void printStatus() {
		Node[] nodes = graph.getNodes();
		for (Node node : nodes) {
			System.out.println("*** Node ***");
			Edge[] incoming = node.getIncomingEdges();
			System.out.printf("incoming edges: %d\n", incoming.length);
			for (Edge e : incoming) {
				System.out.printf(" edge [%s], distance: %d, cap: %d, vehicles: %d, traffic light? %s\n", e,
						e.getDistance(),
						e.getCapacity(),
						e.getVehicleCount(),
						e.getTrafficLight() == null ? "false" : "true");
			}
			Edge[] outgoing = node.getOutgoingEdges();
			System.out.printf("outgoing edges: %d\n", incoming.length);
			for (Edge e : outgoing) {
				System.out.printf(" edge [%s], distance: %d, cap: %d, vehicles: %d, traffic light? %s\n", e,
						e.getDistance(),
						e.getCapacity(),
						e.getVehicleCount(),
						e.getTrafficLight() == null ? "false" : "true");
			}
		}
	}

	public ArrayList<Integer> getVehicleFinishTimes () {
		return this.vehicleFinishTimes;
	}
}
