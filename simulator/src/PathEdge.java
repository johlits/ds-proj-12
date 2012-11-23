
public class PathEdge implements Comparable<PathEdge> {
	private PathEdge prev;
	private int distance;
	private Edge edge;
	
	public PathEdge (PathEdge prev, int distance, Edge edge) {
		this.prev = prev;
		this.distance = distance;
		this.edge = edge;
	}
	
	public PathEdge getPrev() {
		return prev;
	}
	
	public int getDistance() {
		return distance;
	}
	
	public Edge getEdge () {
		return edge;
	}
	
	public int compareTo(PathEdge e) {
		return (distance > e.getDistance()) ? 1 : -1;
	}
}
