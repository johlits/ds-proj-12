import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


public class Simulation {
	private Graph graph;
	private int time;
	private List<Vehicle> vehicles;
	private RoutingAlgorithm routing;
	
	public Simulation (Graph graph, Vehicle[] vehicles, RoutingAlgorithm routing) {
		this.graph = graph;
		this.vehicles = new ArrayList<Vehicle>();
		for (int i = 0; i < vehicles.length; i++)
			this.vehicles.add(vehicles[i]);
		this.time = 0;
		this.routing = routing;
		routing.init(vehicles);
	}
	
	public String visualize () {
		String dot = graph.toDot(this.time);
		String url = null;
		try {
			Process p = Runtime.getRuntime().exec("dot -Tsvg");
			OutputStream o = p.getOutputStream();
			BufferedReader i = new BufferedReader( new InputStreamReader (p.getInputStream() ) );
			o.write(dot.getBytes());
			o.close();
			File temp = File.createTempFile("temp",".svg");
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
	
	public String save (String txt) {
		File temp;
		String url = null;
		try {
			temp = File.createTempFile("temp",".svg");
			FileWriter fileoutput = new FileWriter(temp);
			BufferedWriter buffout = new BufferedWriter(fileoutput);
			buffout.write(txt);
			buffout.close();
			url = temp.getPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return url;
	}
	
	public void progress () {
		ArrayList<Vehicle> delete = new ArrayList<Vehicle>();
		for (Vehicle v : vehicles) {
			v.move (time, routing);
			if (v.getPosition() == null) delete.add(v);
		}
		vehicles.removeAll(delete);
		++this.time;
	}
	
	public void finish () {
		while (vehicles.size() > 0)
			progress ();
		System.out.printf("steps: %d\n", this.time);
	}
	
	public boolean isFinished () {
		return vehicles.size() == 0;
	}
	
	public int getTick () {
		return this.time;
	}
	
	public void printStatus () {
		Node[] nodes = graph.getNodes();
		for (Node node : nodes) {
			System.out.println("*** Node ***");
			Edge[] incoming = node.getIncomingEdges();
			System.out.printf("incoming edges: %d\n", incoming.length);
			for (Edge e : incoming) {
				System.out.printf(" edge, vehicles: %d, traffic light? %s\n",
						e.getVehicleCount(),
						e.getTrafficLight() == null ? "false" : "true");
			}
			Edge[] outgoing = node.getOutgoingEdges();
			System.out.printf("outgoing edges: %d\n", incoming.length);
			for (Edge e : outgoing) {
				System.out.printf(" edge, vehicles: %d, traffic light? %s\n",
						e.getVehicleCount(),
						e.getTrafficLight() == null ? "false" : "true");
			}
		}
	}
}
