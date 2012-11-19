public class Edge implements Comparable<Edge> {
	public CarContainer cc;
	public Edge prev;
	public double d;
	public Edge(CarContainer cc, double d, Edge p) {
		this.cc = cc;
		this.d = d;
		this.prev = p;
	}
	public int compareTo(Edge e) {
		return (d > e.d) ? 1 : -1;
	}
}
