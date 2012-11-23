public class Graph {
	private Node[] nodes;
	
	public Graph (Node[] nodes) {
		this.nodes = nodes;
	}
	
	public Node[] getNodes () {
		return nodes;
	}
	
	/**
	 * @return dot code for the graph
	 */
	public String toDot (int tick) {
		final String[] colors = { "#e51f1f", "#1fe549" };
		
		String s = "digraph tree {";
		for (Node n : nodes) {
			for (Edge e : n.getOutgoingEdges()) {
				String v = "";
				for (Vehicle vehicle : e.getVehicles()) {
					v += String.format("[vehicle:milage=%d]", vehicle.getMilage());
				}
				s += String.format("%d -> %d[label=\"%s\",color=\"%s\"];",
						e.getIncomingNode().hashCode(),
						e.getOutgoingNode().hashCode(),
						v,
						e.getTrafficLight() == null ? "#000" : (
							e.getTrafficLight().isGreen(tick) ? colors[1] : colors[0]
						));
			}
		}
		s += "}";
		return s;
	}
}
