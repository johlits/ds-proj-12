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
	private HashMap<Edge, ManhattenPosition> edgePosition;
	private Simulation simulation;
	
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
			+ "<svg xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns=\"http://www.w3.org/2000/svg\" height=\"900\" width=\"1440\" version=\"1.1\" xmlns:cc=\"http://creativecommons.org/ns#\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">\n"
			+ " <defs>\n"
			+ "	<style type=\"text/css\"><![CDATA[\n"
			+ "   rect { stroke-dasharray: none; stroke-miterlimit: 4px; stroke-linejoin: mitter; }\n" + 
			"\n" + 
			"    #green stop { stop-color: #76e565 }\n" + 
			"    #red stop { stop-color: #e86c6e }\n" + 
			"\n" + 
			"    rect.edge { fill: #dcdee0 }\n" + 
			"    rect.node { fill: none; stroke-width: 1 }\n" + 
			"    rect.car { fill: #FFF; stroke-width: 0.38 }\n" + 
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
	}

	class VerticalConnection {
		String connector;
		AttributeSet up = null;
		AttributeSet down = null;
	}

	public ManhattenLayout(String layout, String vehicles, RoutingAlgorithm algo) throws Exception {
		HashMap<String, Node> nodes = new HashMap<String, Node>();
		List<List<Node>> matrix = new ArrayList<List<Node>>();
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
									new Edge(rel[z], rel[z^1], 5, 5, null);
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

	private String putEdges(float x, float y, Node a, Node b, int tick, boolean anim) {
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
							0, anim ? records.get(v) : null);
				}
				/* traffic lights */
				/* TODO traffic lights via repeat attribute */
				TrafficLight t = edge.getTrafficLight();
				if (t != null)
					s += putLights(t.isGreen(tick) ? "green" : "red", x + j
							* edgeLength, y + edgeMargin + j
							* (edgeMargin + edgeWidth), j != 0, 0, 50, anim ? t : null);
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
					s += putEdges(x, y, matrix[r][c + 1], n, tick, anim);
				if (r + 1 < matrix.length)
					s += translate(x, y + nodeSideLength + nodeBorderSize / 2,
							rotate(putEdges(0, 0, matrix[r + 1][c], n, tick, anim)));
			}
		}
		return wholeDoc ? svgheader + s + "</g></g></svg>" : s;
	}

	private String putObject(String type, float width, float height, float x,
			float y, float border) {
		return putObject(type, width, height, x, y, border, null);
	}
	
	private String putObject(String type, float width, float height, float x,
			float y, float border, LinkedList<MoveEvent> events) {
		String anims = "";
		float px = 0, py = 0;
		float oldh = height, oldw = width;
		while (events != null && !events.isEmpty()) {
			MoveEvent me = events.removeFirst();
			float[] r = computePosition(me.getRequest().getTarget(), me.getRequest().getTo());
			anims += String.format(
				"  <animateMotion begin=\"%fs\" from=\"%f,%f\" to=\"%f,%f\" dur=\"1s\" fill=\"freeze\"/>\n",
				(float)me.getTick(), px, py, r[0] - x, r[1] - y
				);
			px = r[0] - x;
			py = r[1] - y;
			float newh = (r[2] == 1f) ? width - border : height;
			if (newh != oldh) {
				anims += String.format(
					"  <animate begin=\"%fs\" attributeType=\"XML\" attributeName=\"height\" from=\"%f\" to=\"%f\" dur=\"1s\" fill=\"freeze\" />\n",
					(float)me.getTick(), oldh, newh
				);
				oldh = newh;
			}
			float neww = (r[2] == 0f) ? width - border : height;
			if (neww != oldw) {
				anims += String.format(
					"  <animate begin=\"%fs\" attributeType=\"XML\" attributeName=\"width\" from=\"%f\" to=\"%f\" dur=\"1s\" fill=\"freeze\" />\n",
					(float)me.getTick(), oldw, neww
				);
				oldw = neww;
			}
		}
		return String
				.format("<rect class=\"%s\" width=\"%f\" height=\"%f\" x=\"%f\" y=\"%f\">" + anims + "</rect>\n",
						type, width - border, height, x + (border / 2), y);
	}
	
	private String anim (float start, float duration, String type, String attrname, String from, String to) {
		return String.format("<animate begin=\"%fs\" dur=\"%fs\" fill=\"freeze\" attributeType=\"%s\" attributeName=\"%s\" from=\"%s\" to=\"%s\" />",
				start, duration, type, attrname, from, to);
	}
	
	private String[] animateLights (float start, boolean toRed) {
		final String[] colors = { "#76e565", "#c0c0c0", "#e93134" };
		return new String[] {
				anim (start, 0.5f, "CSS", "opacity", "1", "0") +
				anim (start + 0.5f, 0.5f, "CSS", "opacity", "0", "1") +
				anim (start, 1f, "XML", "class", toRed ? "green" : "red", toRed ? "red" : "green"),
				anim (start, 0.5f, "CSS", "fill", colors[toRed ? 0 : 2], colors[1]) +
				anim (start + 0.5f, 0.5f, "CSS", "fill", colors[1], colors[toRed ? 2 : 0]) +
				anim (start, 1f, "XML", "class", toRed ? "green" : "red", toRed ? "red" : "green"),
			};
	}

	private String putLights (String color, float x, float y, boolean flipped) {
		return putLights(color, x, y, flipped, 0, 0, null);
	}
	
	private String putLights (String color, float x, float y, boolean flipped, int fromTick, int toTick, TrafficLight t) {
		String animations[] = { "", "" };
		if (t != null) {
			int myTick = fromTick;
			color = t.isGreen(myTick) ? "green" : "red";
			if (!t.isGreen(myTick)) {
				myTick += t.remainingWaitingTime(myTick);
				String[] r = animateLights(myTick, false);
				animations[0] += r[0];
				animations[1] += r[1];
				myTick += t.getGreenCycle();
			} else
				myTick += t.remainingTimeToNextCycle(myTick); // currently green -> skip to red
			while (myTick <= toTick) {
				String[] r = animateLights(myTick, true);
				animations[0] += r[0];
				animations[1] += r[1];
				myTick += t.getRedCycle();
				if (myTick > toTick)
					break;
				r = animateLights(myTick, false);
				animations[0] += r[0];
				animations[1] += r[1];
				myTick += t.getGreenCycle();				
			}
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
		if (request.getType() != MovementRequest.MovementType.MOVE)
			return;
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
