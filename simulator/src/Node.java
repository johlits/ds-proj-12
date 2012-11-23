
public class Node {
	private Edge[] incoming;
	private Edge[] outgoing;
	
	public Node () {
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
