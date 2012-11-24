import java.util.ArrayList;
import java.util.List;

public class GraphVizGraph {
	private int nodectr = 0;
	
	class GraphVizNode {
		int id;
		
		public GraphVizNode() {
			this.id = nodectr++;
		}
		
		public int getId() {
			return id;
		}
	}
	
	class GraphVizEdge {
		GraphVizNode in;
		GraphVizNode out;
		String label;
		String color;
		
		public GraphVizEdge(GraphVizNode in, GraphVizNode out, String label, String color) {
			this.in = in;
			this.out = out;
			this.label = label;
			this.color = color;
		}
		
		public String toString() {
			return String.format("%d -> %d[label=\"%s\",color=\"%s\"];", in.getId(), out.getId(), label, color);
		}
	}
	
	private List<GraphVizEdge> edges;
	
	public GraphVizGraph () {
		this.edges = new ArrayList<GraphVizEdge>();
	}
	
	public void addEdge (GraphVizEdge edge) {
		edges.add(edge);
	}
	
	public String toDot() {
		String s = "";
		for (GraphVizEdge edge : edges)
			s += edge.toString();
		return "digraph tree {" + s + "}";
	}
}
