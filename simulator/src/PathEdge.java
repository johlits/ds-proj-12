
public class PathEdge implements Comparable<PathEdge> {
	private PathEdge prev;
	private int distance;
	private Edge edge;
	private int congestion;
	/**
	 * constant value for A*
	 */
	private static final int K = 10;
	
	public PathEdge (PathEdge prev, int distance, Edge edge, int congestion) {
		this.prev = prev;
		this.distance = distance;
		this.edge = edge;
		this.congestion = congestion;
	}
	
	public PathEdge (PathEdge prev, int distance, Edge edge) {
		this.prev = prev;
		this.distance = distance;
		this.edge = edge;
		this.congestion = 0;
	}
	
	public PathEdge getPrev() {
		return prev;
	}
	
	public int getDistance() {
		return distance;
	}
	
	public int getWeight() {
		return distance + congestion*K; 
	}
	
	public Edge getEdge () {
		return edge;
	}
	
	public int compareTo(PathEdge e) {
		return (getWeight() > e.getWeight()) ? 1 : -1;
	}
}
