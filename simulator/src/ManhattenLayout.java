import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

public class ManhattenLayout implements MovementRequestApplyHandler {
	private HashMap<String, Node> nodes;
	private Node[][] matrix;
	private HashMap<Edge, ManhattenPosition> edgePosition;
	private Simulation simulation;
	
	private Zone[][] zones;
	private int yzones = 0, xzones = 0;
	
	class MoveEvent {
		MovementRequest r;
		int time;
		
		public MoveEvent(int time, MovementRequest r) {
			this.time = time;
			this.r = r;
		}
		
		public MovementRequest getRequest () {
			return r;
		}
		
		public int getTick () {
			return time;
		}
	}
	
	/* record movements */
	private int tick = 0;
	private HashMap<Vehicle, LinkedList<MoveEvent>> records = new HashMap<Vehicle, LinkedList<MoveEvent>>();

	private final String svgheader = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
			+ "<svg xmlns=\"http://www.w3.org/2000/svg\" height=\"900\" width=\"1440\" version=\"1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n"
			+ " <defs>\n"
			+ "	<style type=\"text/css\"><![CDATA[\n"
			+ "   rect { stroke-dasharray: none; stroke-miterlimit: 4px; stroke-linejoin: mitter; }\n" + 
			"\n" + 
			"    #green stop { stop-color: #76e565 }\n" + 
			"    #red stop { stop-color: #e86c6e }\n" + 
			"\n" + 
			"    rect.edge { fill: #dcdee0 }\n" + 
			"    rect.node { fill: none; stroke-width: 1 }\n" + 
			"    rect.zone { fill: #1B63E0; fill-opacity: 0.5; stroke-width: 1 }\n" + 
			"    rect.car { fill: #FFF; fill-opacity: 0.5; stroke-width: 0.38 }\n" +
			"    /* shared attributes */\n" + 
			"    rect.node, rect.car { stroke: #676464 }\n" + 
			"\n" + 
			"    path.red, path.green { stroke-width: 0.1 }\n" + 
			"\n" + 
			"    path.red { fill: #e93134; stroke: #ee8d8e }\n" + 
			"    path.red:first-child { fill: url(#red); stroke: none }\n" + 
			"\n" + 
			"    path.green { fill: #76e565; stroke: #b9efb0 }\n" + 
			"    path.green:first-child { fill: url(#green); stroke: none }\n" + 
			""
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
			+ "<g transform=\"scale(5,5)\">\n";
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

	public ManhattenLayout(Node[][] matrix, Zone[][] zones) {
		this.matrix = matrix;
		this.zones = zones;
	}

	class VerticalConnection {
		String connector;
		AttributeSet up = null;
		AttributeSet down = null;
	}

	/* TODO refactor parser with callbacks */
	public ManhattenLayout(String layout, String vehicles, float zoneSize, RoutingAlgorithm algo) throws Exception {
		nodes = new HashMap<String, Node>();
		List<List<Node>> matrix = new ArrayList<List<Node>>();
		
		// TODO a better solution
		xzones = (int)(1440.0/zoneSize);
		yzones = (int)(900.0/zoneSize);
		zones = new Zone[yzones][xzones];
		for (int i = 0; i < yzones; i++) 
			for (int j = 0; j < xzones; j++) 
				zones[i][j] = new Zone(i,j,zoneSize,zoneSize);
		
		edgePosition = new HashMap<Edge, ManhattenPosition>();

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
									new Edge(rel[z], rel[z^1], 5, 1, null);
								rel[z].addOutgoingEdge(e);
								rel[z^1].addIncomingEdge(e);
								ManhattenPosition mp = new ManhattenPosition(row.size(), matrix.size(), z);
								edgePosition.put(e, mp);
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
						if (!token.equals("]") && !token.equals("}")) {
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
						if (!token.equals("]") && !token.equals("}")) {
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
		List<Vehicle> va = parseVehicleList(vehicles);
		/* generate simulation */
		simulation = new Simulation(
				new Graph(nodes.values().toArray(new Node[]{})),
				va.toArray(new Vehicle[]{}), algo);

		ArrayList<Node[]> temp = new ArrayList<Node[]>();
		for (List<Node> a : matrix)
			temp.add(a.toArray(new Node[] {}));
		this.matrix = temp.toArray(new Node[][] {});
	}

	private List<Vehicle> parseVehicleList (String str) {
		StringTokenizer parser = new StringTokenizer(str, "\n");
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
		return va;
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

	private String putEdges(float x, float y, Node a, Node b, int tick, boolean anim) {
		String s = "";
		for (int j = 0; j < 2; j++) {
			Edge edge = hasConnectionTo(j == 0 ? a : b, j == 0 ? b : a);
			if (edge != null) {
				s += putObject("edge", edgeLength, edgeWidth, x, y + edgeMargin
						+ j * (edgeMargin + edgeWidth), 0);
				/* vehicles */
				if (!anim)
					for (Vehicle v : edge.getVehicles()) {
						s += putObject(
								"car",
								carLength,
								carWidth,
								x + 	((1-j) * (edgeLength - carLength)) + (j == 0 ? -1 : 1) *
										(((float) v.getMilage()) / (float) edge.getDistance())* (edgeLength - carLength),
										y + edgeMargin
										+ carMargin + j * (edgeMargin + edgeWidth),
								0, anim ? records.get(v) : null);
					}
				/* traffic lights */
				TrafficLight t = edge.getTrafficLight();
				if (t != null)
					s += putLights(t.isGreen(tick) ? "green" : "red", x + j
							* edgeLength, y + edgeMargin + j
							* (edgeMargin + edgeWidth), j != 0, 0, anim ? t : null);
			}
		}
		return s;
	}
	
	private float[] computePosition (Edge e, int milage) {
		ManhattenPosition mp = edgePosition.get(e);
		int z = mp.getDirection();
		float x = (float)(mp.getX() + (z >> 1)) * (edgeLength + nodeSideLength) - edgeLength;
		float y = (float)((mp.getY() - 1) - (z >> 1)) * (nodeSideLength + edgeLength + nodeBorderSize);
		float[] results = new float[] {
				(z & 1) * (edgeLength - carLength) + ((z & 1) == 1 ? -1 : 1) *
				(((float) milage) / (float) e.getDistance()) * (edgeLength - carLength),
				edgeMargin + carMargin + ((z == 0 || z == 3) ? (edgeMargin + edgeWidth) : 0f), 
				0 };
		if ((z >> 1) == 1)
			results = new float[] {
				results[1] - nodeSideLength,
				results[0] + nodeSideLength,
				1f };
		results[0] += x;
		results[1] += y;
		return results;
	}

	public String toSVG(int tick, boolean wholeDoc, boolean anim) {
		float x = 0;
		float y = 0;
		String s = "<g id=\"main\" transform=\"translate(5, 5)\">";

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
					s += putEdges(x, y, matrix[r][c + 1], n, tick, anim);
				if (r + 1 < matrix.length)
					s += translate(x, y + nodeSideLength + nodeBorderSize / 2,
							rotate(putEdges(0, 0, matrix[r + 1][c], n, tick, anim)));
			}
		}
		for (int i = 0; i < yzones; i++) 
			for (int j = 0; j < xzones; j++) 
				s += putObject("zone", zones[i][j].getWidth(), 
					zones[i][j].getHeight(), zones[i][j].getX()*zones[i][j].getWidth(), 
					zones[i][j].getY()*zones[i][j].getHeight(), 1);
					
		if (anim)
			for (Vehicle v : records.keySet())
				s += putObject(
						"car",
						carLength,
						carWidth,
						0, 0,
						0, anim ? records.get(v) : null);
		s += "</g>";
		return wholeDoc ? svgheader + s + "</g></svg>" :
			"<svg xmlns=\"http://www.w3.org/2000/svg\">" + s + "</svg>";
	}

	private String putObject(String type, float width, float height, float x,
			float y, float border) {
		return putObject(type, width, height, x, y, border, null);
	}
	
	private String putObject(String type, float width, float height, float x,
			float y, float border, LinkedList<MoveEvent> events) {
		String anims = "";
		if (events != null) {
			float px = 0, py = 0, pr = 0;
			MoveEvent first = events.removeFirst();
			float[] r = computePosition(first.getRequest().getTarget(), first.getRequest().getTo());
			px = r[0] - x;
			py = r[1] - y;
			pr = r[2];
			while (!events.isEmpty()) {
				MoveEvent me = events.removeFirst();
				if (me.getRequest().getType() == MovementRequest.MovementType.MOVE) {
					r = computePosition(me.getRequest().getTarget(), me.getRequest().getTo());
					anims += String.format(
						"  <animateMotion begin=\"%fs\" from=\"%f,%f\" to=\"%f,%f\" dur=\"1s\" fill=\"freeze\"/>\n",
						(float)me.getTick(), px, py, r[0] - x + (r[2] * 5), r[1] - y
						);
					px = r[0] - x + (r[2] * 5);
					py = r[1] - y;
					if (pr != r[2])
						anims += String.format(
							"<animateTransform begin=\"%f\" type=\"rotate\" attributeName=\"transform\" attributeType=\"XML\" from=\"%d\" to=\"%d\" dur=\"1s\" fill=\"freeze\" />",
								(float)me.getTick(), (int)pr * 90, (int)r[2] * 90);
					pr = r[2];
				} else if (me.getRequest().getType() == MovementRequest.MovementType.FINISH) {
					anims += anim ("", (float)me.getTick(), 1f, "CSS", "opacity", "1", "0");
				}
			}
		}
		return String
				.format("<g transform=\"translate(%f,%f)\"><g><rect class=\"%s\" width=\"%f\" height=\"%f\" />" + anims + "</g></g>\n",
						x + (border / 2), y, type, width - border, height);
	}

	private static String anim (String id, float start, float duration, String type, String attrname, String from, String to) {
		return anim(id, String.format("%f", start), duration, type, attrname, from, to);
	}
	
	private static String anim (String id, String start, float duration, String type, String attrname, String from, String to) {
		return String.format(" <animate id=\"%s\" begin=\"%s\" dur=\"%fs\" fill=\"freeze\" attributeType=\"%s\" attributeName=\"%s\" from=\"%s\" to=\"%s\" />\n",
				id, start, duration, type, attrname, from, to);
	}
	
	private String[] animateLights (String prefix, String start, boolean toRed) {
		final String[] colors = { "#76e565", "#c0c0c0", "#e93134" };
		return new String[] {
				anim (prefix + "anim1", start, 0.5f, "CSS", "opacity", "1", "0") +
				anim (prefix + "anim2", prefix + "anim1.end", 0.5f, "CSS", "opacity", "0", "1") +
				anim (prefix + "anim3", start, 1f, "XML", "class", toRed ? "green" : "red", toRed ? "red" : "green"),
				anim (prefix + "anim4", start, 0.5f, "CSS", "fill", colors[toRed ? 0 : 2], colors[1]) +
				anim (prefix + "anim5", prefix + "anim4.end", 0.5f, "CSS", "fill", colors[1], colors[toRed ? 2 : 0]) +
				anim (prefix + "anim6", start, 1f, "XML", "class", toRed ? "green" : "red", toRed ? "red" : "green"),
			};
	}
	
	private String putLights (String color, float x, float y, boolean flipped, int fromTick, TrafficLight t) {
		String animations[] = { "", "" };
		if (t != null) {
			String[] names = new String[] { "green" , "red" };
			color = names[t.isGreen(fromTick) ? 0 : 1];
			int[] cycles = new int[] { t.getRedCycle(), t.getGreenCycle() };
			int[] offset = new int[] { t.remainingTimeToNextGreen(fromTick), t.remainingTimeToNextRed(fromTick) };
			int i = 0;
			for (String[] r; i < 2; i++, animations[0] += r[0], animations[1] += r[1])
				r = animateLights(String.format("trafficLight%d_%s_", t.hashCode(), names[i]),
							String.format("%d; trafficLight%d_%s_anim3.end + %d",
								offset[i], t.hashCode(), names[i^1],	cycles[i^1] - 1), i == 1);
		}
		return translate(x, y, scale(flipped ? -1 : 1, 1, translate(-25.062f,-6.5223f,
				"<path d=\"m25.062,6.5223c1.933,0,3.5,1.5669,3.5,3.5003,0,1.9328-1.567,3.4997-3.5,3.4997\" class=\"" + color + "\">\n" + 
				animations[0] + "</path>\n" +
				"<path d=\"m25.062,8.5223c0.82843,0,1.5,0.6715,1.5,1.5001,0,0.8284-0.67157,1.4999-1.5,1.4999\" class=\"" + color + "\">\n" + 
				animations[1] +	"</path>")));
	}

	private static String translate(float x, float y, String s) {
		return String.format("<g transform=\"translate(%f,%f)\">%s</g>\n", x,
				y, s);
	}

	private static String rotate(String s) {
		return String.format("<g transform=\"rotate(90)\">%s</g>", s);
	}
	
	private static String scale(float x, float y, String s) {
		return String.format("<g transform=\"scale(%f,%f)\">%s</g>\n", x,
				y, s);		
	}

	@Override
	public void apply(MovementRequest request) {
		if (records.get(request.getVehicle()) == null) {
			records.put(request.getVehicle(), new LinkedList<MoveEvent>());
			records.get(request.getVehicle()).add(
				new MoveEvent(0,
					new MovementRequest(
							request.getVehicle(),
							request.getVehicle().getPosition(),
							request.getVehicle().getMilage()
					)
				)
			);
		}
		records.get(request.getVehicle()).add(new MoveEvent(tick, request));
	}

	@Override
	public void nextTick() {
		++tick;
	}
}
