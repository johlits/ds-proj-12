import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.print.AttributeException;

public class ManhattenLayout implements MovementRequestApplyHandler {
	private Node[][] matrix;
	private Simulation simulation;
	
	/* record movements */
	private int tick = 0;
	private Stack<Stack<MovementRequest>> events;

	private final String svgheader = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
			+ "<svg xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns=\"http://www.w3.org/2000/svg\" height=\"900\" width=\"1440\" version=\"1.1\" xmlns:cc=\"http://creativecommons.org/ns#\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">\n"
			+ " <defs>\n"
			+ "	<style type=\"text/css\"><![CDATA[\n"
			+ "		rect { stroke-dasharray: none; stroke-miterlimit: 4px; stroke-linejoin: mitter; }\n"
			+ "\n"
			+ "		#green stop { stop-color: #76e565 }\n"
			+ "		#red stop { stop-color: #e86c6e }\n"
			+ "\n"
			+ "		rect.edge { fill: #dcdee0 }\n"
			+ "		rect.node { fill: none; stroke-width: 1 }\n"
			+ "		rect.car { fill: #FFF; stroke-width: 0.38 }\n"
			+ "		/* shared attributes */\n"
			+ "		rect.node, rect.car { stroke: #676464 }\n"
			+ "\n"
			+ "		.red path, .green path { stroke-width: 0.1 }\n"
			+ "\n"
			+ "		.red path { fill: #e93134; stroke: #ee8d8e }\n"
			+ "		.red path:first-child { fill: url(#red); stroke: none }\n"
			+ "\n"
			+ "		.green path { fill: #76e565; stroke: #b9efb0 }\n"
			+ "		.green path:first-child { fill: url(#green); stroke: none }\n"
			+ "	]]></style>\n"
			+ "\n"
			+ "  <radialGradient id=\"green\" gradientUnits=\"userSpaceOnUse\" cy=\"1026.9\" cx=\"41.75\" gradientTransform=\"matrix(1,0,0,2,-14.9375,-2043.7021)\" r=\"1.75\">\n"
			+ "   <stop  offset=\"0\"/>\n"
			+ "   <stop  stop-opacity=\"0\" offset=\"1\"/>\n"
			+ "  </radialGradient>\n"
			+ "  <radialGradient id=\"red\" gradientUnits=\"userSpaceOnUse\" cy=\"1026.9\" cx=\"41.75\" gradientTransform=\"matrix(1,0,0,2,-14.9375,-2043.7021)\" r=\"1.75\">\n"
			+ "   <stop offset=\"0\"/>\n"
			+ "   <stop stop-opacity=\"0\" offset=\"1\"/>\n"
			+ "  </radialGradient>\n"
			+ " <g id=\"greenlight\" transform=\"translate(-25.062,-6.5223)\" class=\"green\">\n"
			+ "  <path d=\"m25.062,6.5223c1.933,0,3.5,1.5669,3.5,3.5003,0,1.9328-1.567,3.4997-3.5,3.4997\" />\n"
			+ "  <path d=\"m25.062,8.5223c0.82843,0,1.5,0.6715,1.5,1.5001,0,0.8284-0.67157,1.4999-1.5,1.4999\" />\n"
			+ "  </g>\n"
			+ " <g id=\"redlight\" transform=\"translate(-25.062,-6.5223)\" class=\"red\">\n"
			+ "  <path d=\"m25.062,6.5223c1.933,0,3.5,1.5669,3.5,3.5003,0,1.9328-1.567,3.4997-3.5,3.4997\" />\n"
			+ "  <path d=\"m25.062,8.5223c0.82843,0,1.5,0.6715,1.5,1.5001,0,0.8284-0.67157,1.4999-1.5,1.4999\" />\n"
			+ " </g>\n"
			+ "\n"
			+ " </defs>\n"
			+ "\n"
			+ "<g transform=\"scale(5,5)\">\n"
			+ " <g id=\"main\" transform=\"translate(5, 5)\">";
	final float carWidth = 5;
	final float carLength = 7;
	final float edgeWidth = 7;
	final float edgeLength = 50;
	final float nodeSideLength = 20;
	final float nodeBorderSize = 1;
	final float edgeMargin = (nodeSideLength - (edgeWidth * 2)) / 3;
	final float carMargin = (edgeWidth - carWidth) / 2;

	enum ParseExpect {
		SymbolOrAttribute, Symbol, Connector, ConnectorOrAttribute, AttributeDistance, AttributeCapacity, TrafficLightOffsetOrAttributeEnd, TrafficLightGreenCycle, TrafficLightRedCycle, AttributeEnd
	};

	public ManhattenLayout(Node[][] matrix) {
		this.matrix = matrix;
		this.events = new Stack<Stack<MovementRequest>>();
		events.add(new Stack<MovementRequest>());
	}

	class VerticalConnection {
		String connector;
		AttributeSet up = null;
		AttributeSet down = null;
	}

	public ManhattenLayout(String layout, String vehicles, RoutingAlgorithm algo) throws Exception {
		HashMap<String, Node> nodes = new HashMap<String, Node>();
		List<List<Node>> matrix = new ArrayList<List<Node>>();

		/* TODO cache, incoming/outings here and just set array */

		// parse
		StringTokenizer lines = new StringTokenizer(layout, "\n");
		boolean mixedRow = true;
		LinkedList<VerticalConnection> verticalConnectors = null;

		while (lines.hasMoreTokens()) {
			String line = lines.nextToken();
			if (line.startsWith("#"))
				continue;
			StringTokenizer st = new StringTokenizer(line);

			AttributeSet left = null;
			AttributeSet right = null;
			AttributeSet current = null;
			if (mixedRow) {
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
							expect = ParseExpect.AttributeDistance;
							current = right = new AttributeSet();
							break;
						}
					case Symbol:
						Node node = new Node();
						nodes.put(token, node);
						/* horizontal connection */
						int start = 2, end = 2;
						final String[][] conn = new String[][]{
								new String[] { "*", ">" }, new String[] { "*", "<" },
								new String[] { "*", "v" }, new String[] { "*", "^" } };
						final Node[] rel = new Node[4];
						final AttributeSet[] attrs = new AttributeSet[] { right, left, null, null };
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
									new Edge(rel[z], rel[z^1], attrs[z]) :
									new Edge(rel[z], rel[z^1], 5, 5, null);
								rel[z].addOutgoingEdge(e);
								rel[z^1].addIncomingEdge(e);
							}
						current = left = right = null;
						connector = null;
						
						row.push(node);
						expect = ParseExpect.ConnectorOrAttribute;
						break;
					case ConnectorOrAttribute:
						if (token.equals("{")) {
							current = left = new AttributeSet();
							expect = ParseExpect.AttributeDistance;
							break;
						}
					case Connector:
						connector = token;
						expect = ParseExpect.SymbolOrAttribute;
						break;
					case AttributeDistance:
						current.distance = Integer.parseInt(token);
						expect = ParseExpect.AttributeCapacity;
						break;
					case AttributeCapacity:
						current.capacity = Integer.parseInt(token);
						expect = ParseExpect.TrafficLightOffsetOrAttributeEnd;
						break;
					case TrafficLightOffsetOrAttributeEnd:
						if (!token.equals("]") || !token.equals("}")) {
							current.trafficLightOffset = Integer
									.parseInt(token);
							expect = ParseExpect.TrafficLightGreenCycle;
							break;
						}
					case AttributeEnd:
						if (token.equals("}"))
							expect = ParseExpect.Connector;
						else if (token.equals("]"))
							expect = ParseExpect.Symbol;
						else
							throw new Exception("unexpected symbol");
						break;
					case TrafficLightGreenCycle:
						current.trafficLightGreenCycle = Integer
								.parseInt(token);
						expect = ParseExpect.TrafficLightRedCycle;
						break;
					case TrafficLightRedCycle:
						current.trafficLightRedCycle = Integer.parseInt(token);
						expect = ParseExpect.AttributeEnd;
						break;
					}
				}
				mixedRow = false;
			} else {
				verticalConnectors = new LinkedList<VerticalConnection>();
				ParseExpect expect = ParseExpect.ConnectorOrAttribute;
				VerticalConnection vc = null;
				boolean connectorLast = false;
				while (st.hasMoreElements()) {
					String token = st.nextToken();
					switch (expect) {
					case ConnectorOrAttribute:
						if (token.equals("[")) {
							expect = ParseExpect.AttributeDistance;
							vc.down = current = right = new AttributeSet();
							connectorLast = false;
							break;
						} else if (token.equals("{")) {
							expect = ParseExpect.AttributeDistance;
							connectorLast = false;
							right = null;
							vc = new VerticalConnection();
							vc.up = current = left = new AttributeSet();
							break;
						}
					case Connector:
						if (connectorLast || vc == null) {
							vc = new VerticalConnection();
							left = right = null;
						}
						vc.connector = token;
						verticalConnectors.add(vc);
						connectorLast = true;
						expect = ParseExpect.ConnectorOrAttribute;
						break;
					case AttributeDistance:
						current.distance = Integer.parseInt(token);
						expect = ParseExpect.AttributeCapacity;
						break;
					case AttributeCapacity:
						current.capacity = Integer.parseInt(token);
						expect = ParseExpect.TrafficLightOffsetOrAttributeEnd;
						break;
					case TrafficLightOffsetOrAttributeEnd:
						if (!token.equals("]") || !token.equals("}")) {
							current.trafficLightOffset = Integer
									.parseInt(token);
							expect = ParseExpect.TrafficLightGreenCycle;
							break;
						}
					case AttributeEnd:
						if (token.equals("}")) {
							expect = ParseExpect.Connector;
						} else if (token.equals("]"))
							expect = ParseExpect.ConnectorOrAttribute;
						else
							throw new Exception("unexpected symbol");
						break;
					case TrafficLightGreenCycle:
						current.trafficLightGreenCycle = Integer
								.parseInt(token);
						expect = ParseExpect.TrafficLightRedCycle;
						break;
					case TrafficLightRedCycle:
						current.trafficLightRedCycle = Integer.parseInt(token);
						expect = ParseExpect.AttributeEnd;
						break;
					}
				}
				mixedRow = true;
			}
		}
		/* set vehicles */
		StringTokenizer parser = new StringTokenizer(vehicles, "\n");
		ArrayList<Vehicle> va = new ArrayList<Vehicle>();
		while (parser.hasMoreElements()) {
			StringTokenizer st = new StringTokenizer(parser.nextToken());
			Node from = nodes.get(st.nextElement());
			Node to = nodes.get(st.nextElement());
			Node target = nodes.get(st.nextElement());
			for (Edge e : from.getOutgoingEdges())
				if (e.getOutgoingNode() == to) {
					Vehicle v = new Vehicle(e, target);
					e.addVehicle(v);
					va.add(v);
				}
		}
		/* generate simulation */
		simulation = new Simulation(
				new Graph(nodes.values().toArray(new Node[]{})), 
				va.toArray(new Vehicle[]{}), algo);

		ArrayList<Node[]> temp = new ArrayList<Node[]>();
		for (List<Node> a : matrix)
			temp.add(a.toArray(new Node[] {}));
		this.matrix = temp.toArray(new Node[][] {});
	}

	public Simulation getSimulation () {
		return this.simulation;
	}
	
	private Edge hasConnectionTo(Node a, Node b) {
		for (Edge e : a.getOutgoingEdges())
			if (e.getOutgoingNode() == b)
				return e;
		return null;
	}

	private String putEdges(float x, float y, Node a, Node b, int tick) {
		String s = "";
		for (int j = 0; j < 2; j++) {
			Edge edge = hasConnectionTo(j == 0 ? a : b, j == 0 ? b : a);
			if (edge != null) {
				s += putObject("edge", edgeLength, edgeWidth, x, y + edgeMargin
						+ j * (edgeMargin + edgeWidth), 0);
				/* vehicles */
				for (Vehicle v : edge.getVehicles()) {
					s += putObject(
							"car",
							carLength,
							carWidth,
							x
									+ (((float) v.getMilage()) / (float) edge
											.getDistance())
									* (edgeLength - carLength), y + edgeMargin
									+ carMargin + j * (edgeMargin + edgeWidth),
							0);
				}
				/* traffic lights */
				/* TODO compute cycles up to tick */
				TrafficLight t = edge.getTrafficLight();
				if (t != null)
					s += putLights(t.isGreen(tick) ? "green" : "red", x + j
							* edgeLength, y + edgeMargin + j
							* (edgeMargin + edgeWidth), j != 0);
			}
		}
		return s;
	}

	public String toSVG(int tick, boolean wholeDoc) {
		float x = 0;
		float y = 0;
		String s = "";

		for (int r = 0; r < matrix.length; r++, y += nodeSideLength
				+ edgeLength + nodeBorderSize) {
			Node[] row = matrix[r];
			x = 0;
			for (int c = 0; c < row.length; c++, x += edgeLength) {
				Node n = matrix[r][c];

				s += putObject("node", nodeSideLength, nodeSideLength, x, y,
						nodeBorderSize);
				x += nodeSideLength;

				if (c + 1 < row.length)
					s += putEdges(x, y, matrix[r][c + 1], n, tick);
				if (r + 1 < matrix.length)
					s += translate(x, y + nodeSideLength + nodeBorderSize / 2,
							rotate(putEdges(0, 0, matrix[r + 1][c], n, tick)));
			}
		}
		return wholeDoc ? svgheader + s + "</g></g></svg>" : s;
	}

	private String putObject(String type, float width, float height, float x,
			float y, float border) {
		return String
				.format("<rect class=\"%s\" width=\"%f\" height=\"%f\" x=\"%f\" y=\"%f\" />\n",
						type, width - border, height, x + (border / 2), y);
	}

	private String putLights(String color, float x, float y, boolean flipped) {
		return translate(x, y, String.format(
				"<use xlink:href=\"#%slight\" transform=\"scale(%d,1)\"/>",
				color, flipped ? -1 : 1));
	}

	private static String translate(float x, float y, String s) {
		return String.format("<g transform=\"translate(%f,%f)\">%s</g>\n", x,
				y, s);
	}

	private static String rotate(String s) {
		return String.format("<g transform=\"rotate(90)\">%s</g>", s);
	}

	@Override
	public void apply(MovementRequest request) {
		events.peek().add(request);
	}

	@Override
	public void nextTick() {
		++tick;
		events.add(new Stack<MovementRequest>());
	}
}
