import java.util.ArrayList;
import java.util.List;


public class Node {
	private Edge[] incoming;
	private Edge[] outgoing;
	
	public Node () {
		incoming = new Edge[] {};
		outgoing = new Edge[] {};
	}
	
	private static <T> T[] addElementToArray(T[] old, T element) {
		List<T> array = new ArrayList<T>();
		for (T e : old)
			array.add(e);
		array.add(element);
		return array.toArray(old);
	}
	
	public void addIncomingEdge (Edge edge) {
		incoming = (incoming == null) ? new Edge[] { edge } : addElementToArray(incoming, edge);
	}
	
	public void addOutgoingEdge (Edge edge) {
		outgoing = (outgoing == null) ? new Edge[] { edge } : addElementToArray(outgoing, edge);
	}
	
	public void setIncomingEdges (Edge[] incoming) {
		this.incoming = incoming;
	}
	
	public void setOutgoingEdges (Edge[] outgoing) {
		this.outgoing = outgoing;
	}

	public Edge[] getIncomingEdges() {
		return incoming;
	}
	
	public Edge[] getOutgoingEdges() {
		return outgoing;
	}

}
