
public class SimulationRun {
	
	private static Simulation loadSimulation () {
		/* TODO instead of defining the map and input parameters, read from files here */
		TrafficLight trafficlight = new TrafficLight(0, 5, 5);
		Node A = new Node();
		Node B = new Node();
		Node C = new Node();
		Node D = new Node();
		Node E = new Node();
		Node F = new Node();
		Node G = new Node();
		Node H = new Node();
		Node I = new Node();
		Node J = new Node();
		Node K = new Node();
		Node L = new Node();
		
		
		Edge a = new Edge(A, B, 5, 10, trafficlight);
		Edge b = new Edge(B, C, 5, 10, null);
		Edge c = new Edge(C, D, 5, 10, null);
		Edge d = new Edge(F, A, 5, 10, null);
		Edge e = new Edge(B, E, 5, 10, null);
		Edge f = new Edge(D, E, 5, 10, null);
		Edge g = new Edge(G, F, 5, 10, null);
		Edge h = new Edge(E, G, 5, 10, null);
		Edge i = new Edge(D, I, 5, 10, null);
		Edge j = new Edge(I, E, 5, 10, null);
		Edge k = new Edge(H, G, 5, 10, null);
		Edge l = new Edge(J, F, 5, 10, null);
		Edge m = new Edge(K, J, 5, 10, null);
		Edge n = new Edge(G, J, 5, 10, null);
		Edge o = new Edge(H, K, 5, 10, null);
		Edge p = new Edge(L, K, 5, 10, null);
		Edge q = new Edge(H, L, 5, 10, null);
		Edge r = new Edge(I, L, 5, 10, null);
		Edge s = new Edge(I, H, 5, 10, null);
		Edge t = new Edge(F, B, 5, 10, null);
		
		A.setIncomingEdges(new Edge[] { d });
		A.setOutgoingEdges(new Edge[] { a });
		
		B.setIncomingEdges(new Edge[] { a, t });
		B.setOutgoingEdges(new Edge[] { b, e });
		
		C.setIncomingEdges(new Edge[] { b });
		C.setOutgoingEdges(new Edge[] { c });
		
		D.setIncomingEdges(new Edge[] { c });
		D.setOutgoingEdges(new Edge[] { f, i });
		
		E.setIncomingEdges(new Edge[] { e, f, j });
		E.setOutgoingEdges(new Edge[] { h });
		
		F.setIncomingEdges(new Edge[] { g, l });
		F.setOutgoingEdges(new Edge[] { d, t });
		
		G.setIncomingEdges(new Edge[] { h, k });
		G.setOutgoingEdges(new Edge[] { g, n });
		
		H.setIncomingEdges(new Edge[] { s });
		H.setOutgoingEdges(new Edge[] { k, o, q });
		
		I.setIncomingEdges(new Edge[] { i });
		I.setOutgoingEdges(new Edge[] { s, j, r });
		
		J.setIncomingEdges(new Edge[] { n, m });
		J.setOutgoingEdges(new Edge[] { l });
		
		K.setIncomingEdges(new Edge[] { o, p });
		K.setOutgoingEdges(new Edge[] { m });
		
		L.setIncomingEdges(new Edge[] { r, q });
		L.setOutgoingEdges(new Edge[] { p });		
		
		Vehicle va = new Vehicle (a, L);
		a.addVehicle(va);
		Vehicle vb = new Vehicle (c, F);
		c.addVehicle(vb);
		
		Graph graph = new Graph(new Node[] { A, B, C, D, E, F, G, H, I, J, K, L });
		Simulation sim = new Simulation(graph, new Vehicle[] { va, vb }, new LocalShortestPathRoutingWithTrafficLightsAndReservation());
		return sim;
	}
	
	public static void main(String[] args) {
		Simulation sim = loadSimulation();
		sim.visualize();
		sim.progress();
		sim.visualize();
		//sim.finish();
	}
}
