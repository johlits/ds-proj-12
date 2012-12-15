import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

/* TODO cache, incoming/outings here and just set array */
public class ManhattenParser {
	private HashMap<String, Node> nodes = new HashMap<String, Node>();
	private List<List<Node>> matrix = new ArrayList<List<Node>>();
	private HashMap<Edge, ManhattenPosition> edgePosition = new HashMap<Edge, ManhattenPosition>();
	private List<Edge> ea = new LinkedList<Edge>();
	
	private enum ParseExpect {
		SymbolOrAttribute, Symbol, Connector, ConnectorOrAttribute, AttributeDistance, AttributeCapacity, TrafficLightOffsetOrAttributeEnd, TrafficLightGreenCycle, TrafficLightRedCycle, AttributeEnd
	};
	
	public ManhattenParser (String input) {
		parse(input);
	}
	
	private class VerticalConnection {
		String connector;
		AttributeSet up = null;
		AttributeSet down = null;
	}
	
	/**
	 * This method converts a token sequence provided by <em>st</em> into an <tt>AttributeSet</tt>.<br />
	 * EBNF for attribute set:<br />
	 * <pre>
	 *  traffic-light     := offset greencycle redcycle
	 *  edge-attr         := distance capacity (traffic-light)?
	 * </pre>
	 * @param st tokenizer to request tokens during parsing
	 * @param end quit detection on occurrence of this token
	 * @return object holding the parsed information
	 */
	private AttributeSet parseAttributeSet (StringTokenizer st, String end) {
		AttributeSet set = new AttributeSet();
		ParseExpect expect = ParseExpect.AttributeDistance;
		boolean undone = true;
		
		while (undone && st.hasMoreTokens()) {
			String token = st.nextToken();
			switch (expect) {
			case AttributeDistance:
				set.distance = Integer.parseInt(token);
				expect = ParseExpect.AttributeCapacity;
				break;
			case AttributeCapacity:
				set.capacity = Integer.parseInt(token);
				expect = ParseExpect.TrafficLightOffsetOrAttributeEnd;
				break;
			case TrafficLightOffsetOrAttributeEnd:
				if (!token.equals(end)) {
					set.trafficLightOffset = Integer.parseInt(token);
					expect = ParseExpect.TrafficLightGreenCycle;
					break;
				}
			case AttributeEnd:
				if (token.equals(end))
					undone = false;
				else
					return null;
				break;
			case TrafficLightGreenCycle:
				set.trafficLightGreenCycle = Integer.parseInt(token);
				expect = ParseExpect.TrafficLightRedCycle;
				break;
			case TrafficLightRedCycle:
				set.trafficLightRedCycle = Integer.parseInt(token);
				expect = ParseExpect.AttributeEnd;
				break;
			}
		}
		return set;
	}
	
	private void parseHorizontalRow (StringTokenizer st, LinkedList<VerticalConnection> verticalConnectors, List<Edge> ea) {
		AttributeSet left = null;
		AttributeSet right = null;
		
		/* for odd lines */
		Stack<Node> row = new Stack<Node>();
		matrix.add(row);

		ParseExpect expect = ParseExpect.Symbol;
		String connector = null;

		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			switch (expect) {
			case SymbolOrAttribute:
				if (token.equals("[")) {
					right = parseAttributeSet(st, "]");
					expect = ParseExpect.Symbol;
					break;
				}
			case Symbol:
				Node node = new Node();
				nodes.put(token, node);
				int start = 2, end = 2;
				final String[][] conn = new String[][]{
						new String[] { "*", ">" }, new String[] { "*", "<" },
						new String[] { "*", "v" }, new String[] { "*", "^" } };
				final Node[] rel = new Node[4];
				final AttributeSet[] attrs = new AttributeSet[] { left, right, null, null };
				if (connector != null) {
					rel[0] = row.peek();
					rel[1] = node;
					start = 0;
				}
				String scan[] = new String[] { connector, null };
				if (verticalConnectors != null) {
					rel[2] = matrix.get(matrix.size() - 2).get(row.size());
					rel[3] = node;
					end = 4;
					VerticalConnection c = verticalConnectors.removeFirst();
					attrs[2] = c.down;
					attrs[3] = c.up;
					scan[1] = c.connector;
				}
				for (int z = start; z < end; z++)
					if (scan[z>>1].equals(conn[z][0]) || scan[z>>1].equals(conn[z][1])) {
						Edge e = (attrs[z] != null) ?
							new Edge(rel[z], rel[z^1], attrs[z]) : // TODO assign real zones
							new Edge(rel[z], rel[z^1], 5, 1, null);
						ea.add(e);
						rel[z].addOutgoingEdge(e);
						rel[z^1].addIncomingEdge(e);
						ManhattenPosition mp = new ManhattenPosition(row.size(), matrix.size(), z);
						edgePosition.put(e, mp);
					}
				left = right = null;
				connector = null;
				
				row.push(node);
				expect = ParseExpect.ConnectorOrAttribute;
				break;
			case ConnectorOrAttribute:
				if (token.equals("{")) {
					left = parseAttributeSet(st, "}");
					expect = ParseExpect.Connector;
					break;
				}
			case Connector:
				connector = token;
				expect = ParseExpect.SymbolOrAttribute;
				break;
			}
		}
	}
	
	private LinkedList<VerticalConnection> parseVerticalRow (StringTokenizer st) {
		LinkedList<VerticalConnection> verticalConnectors = new LinkedList<VerticalConnection>();
		ParseExpect expect = ParseExpect.ConnectorOrAttribute;
		VerticalConnection vc = null;
		boolean connectorLast = false;
		
		while (st.hasMoreElements()) {
			String token = st.nextToken();
			switch (expect) {
			case ConnectorOrAttribute:
				if (token.equals("[")) {
					expect = ParseExpect.ConnectorOrAttribute;
					vc.up = parseAttributeSet(st, "]");
					connectorLast = false;
					break;
				} else if (token.equals("{")) {
					expect = ParseExpect.Connector;
					connectorLast = false;
					vc = new VerticalConnection();
					vc.down = parseAttributeSet(st, "}");
					break;
				}
			case Connector:
				if (connectorLast || vc == null) {
					vc = new VerticalConnection();
				}
				vc.connector = token;
				verticalConnectors.add(vc);
				connectorLast = true;
				expect = ParseExpect.ConnectorOrAttribute;
				break;
			}
		}
		return verticalConnectors;
	}
	
	private void parse (String input) {
		StringTokenizer lines = new StringTokenizer(input, "\n");
		LinkedList<VerticalConnection> verticalConnectors = null;
		
		for (boolean isHorizontal = true; lines.hasMoreTokens();) {
			String line = lines.nextToken();
			if (line.startsWith("#"))
				continue;
			StringTokenizer st = new StringTokenizer(line);

			if (isHorizontal)
				parseHorizontalRow(st, verticalConnectors, ea);
			else
				verticalConnectors = parseVerticalRow(st);
			
			isHorizontal = !isHorizontal;
		}
	}
	
	public Node getNode (String name) {
		return nodes.get(name);
	}
	
	public Node[] getNodes () {
		return nodes.values().toArray(new Node[]{});
	}
	
	public Edge[] getEdges () {
		return ea.toArray(new Edge[]{});
	}
	
	public ManhattenPosition getPosition (Edge e) {
		return edgePosition.get(e);
	}
	
	public Node[][] getMatrix () {
		ArrayList<Node[]> temp = new ArrayList<Node[]>();
		for (List<Node> a : matrix)
			temp.add(a.toArray(new Node[] {}));
		return temp.toArray(new Node[][] {});
	}	
}
