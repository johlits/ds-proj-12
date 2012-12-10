public class PathEdge implements Comparable<PathEdge> {
	private PathEdge prev;
	private int distance;
	private Edge edge;
	/**
	 * (sum of) normalized congestion[s]
	 */
	private double congestion;
	
	public PathEdge (PathEdge prev, int distance, Edge edge, double congestion) {
		this.prev = prev;
		this.distance = distance;
		this.edge = edge;
		this.congestion = congestion;
	}
	
	public PathEdge (PathEdge prev, int distance, Edge edge) {
		this.prev = prev;
		this.distance = distance;
		this.edge = edge;
		this.congestion = -1.0;
	}
	
	public PathEdge getPrev() {
		return prev;
	}
	
	public int getDistance() {
		return distance;
	}
	
	/**
	 * super secret magic(tm) to avoid traffic jams and save the world
	 * @return
	 */
	public double getWeight() {
		/* only do magic if congestion if actually set */
		if (congestion < 0)
			return this.distance;

		/* treshold when we consider us very far away */
		final int magic_treshold = 100;
		int d = distance;
		/* apply magic treshold */
		if (d > magic_treshold)
			d = magic_treshold;
		double f = d / magic_treshold;
		return f * congestion + (1.0 - f) * d;
	}
	
	public Edge getEdge () {
		return edge;
	}
	
	public void setCongestion (int congestion, double previous) {
		this.congestion = (congestion / (double)(edge.getCapacity() * edge.getDistance())) + previous;
	}

	public double getCongestion () {
		return this.congestion;
	}

	public int compareTo(PathEdge e) {
		return (getWeight() > e.getWeight()) ? 1 : -1;
	}
}
