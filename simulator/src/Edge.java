import java.util.ArrayList;
import java.util.List;


public class Edge {
	private int distance;
	private int capacity;
	private TrafficLight trafficlight;
	private List<Vehicle> vehicles;
	private Node in;
	private Node out;
	
	/**
	 * @param in incoming Node
	 * @param out outgoing Node
	 * @param distance distance between incoming and outgoing node on this edge
	 * @param capacity amount of vehicles this edge can hold
	 * @param trafficlight time based barrier at the end of the edge
	 */
	public Edge (Node in, Node out, int distance, int capacity, TrafficLight trafficlight) {
		this.distance = distance;
		this.capacity = capacity;
		this.trafficlight = trafficlight;
		this.vehicles = new ArrayList<Vehicle>();
		this.in = in;
		this.out = out;
	}
	
	/* simplified constructor for dijkstra usage */
	public Edge (Node in, Node out, int distance) {
		this.in = in;
		this.out = out;
		this.distance = distance;
	}
	
	public Node getIncomingNode () {
		return in;
	}
	
	public Node getOutgoingNode () {
		return out;
	}
	
	public int getDistance () {
		return distance;
	}
	
	public TrafficLight getTrafficLight () {
		return trafficlight;
	}
	
	public void addVehicle (Vehicle v) {
		vehicles.add(v);
	}
	
	public void removeVehicle (Vehicle v) {
		vehicles.remove(v);
	}
	
	public int getVehicleCount () {
		return vehicles.size();
	}
	
	public Vehicle[] getVehicles () {
		return vehicles.toArray(new Vehicle[]{});
	}
	
	public int getCapacity () {
		return capacity;
	}
}
