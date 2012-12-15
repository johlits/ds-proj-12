import java.util.*;
class MapGenerator {
	Random rand = new Random();
	LinkedList<StartingLocation> startingLocations = new LinkedList<StartingLocation>();
	public MapGenerator() {
		Kattio io = new Kattio(System.in, System.out);
		String str = "5\na { 1 1 0 1 1 } * [ 1 1 0 1 1 ] b * c * [ 7 7 ] d\n" +
			"{ 1 1 5 5 5 } *  { 1 1 5 5 5 } ^   v   ^\n" +
			"e * f * g * [ 2 1 ] h\n" +
			"*   *   * [ 1 1 ] * [ 5 5 ]\n" +
			"i * j * [ 1 1 2 5 5 ] k * [ 1 1 30 5 5 ] l";
		String cars = "3\na b l\nc d l\ne f l";
		io.println(str);
		io.println(cars);
		io.close();
	}

	private String generateCellName (int row, int col) {
		return String.format("r%dc%d", row, col);
	}

	private AttributeSet generateRandomAttributes () {
		AttributeSet attrset = new AttributeSet();
		attrset.distance = rand.nextInt(10) + 1;
		attrset.capacity = rand.nextInt(10) + 1;
		if (rand.nextBoolean()) { // traffic light?
			attrset.trafficLightOffset = rand.nextInt(10);
			attrset.trafficLightGreenCycle = rand.nextInt(10) + 1;
			attrset.trafficLightRedCycle = rand.nextInt(10) + 1;
		}
		return attrset;
	}

	public MapGenerator (int cols, int rows, int vehicles) {
		String t = Integer.toString(2 * rows - 1) + "\n";
		for (int r = 0; r < 2 * rows - 1; r++) {
			/* horizontal connector row + nodes */
			for (int c = 0; c < cols - (~r & 1); c++) {
				AttributeSet[] sets = new AttributeSet[] {
					generateRandomAttributes(),
					generateRandomAttributes()
				};
				t += ((r & 1) == 0 ? generateCellName (r/2, c) + " " : "")
						+ "{ " + sets[0] + " } * [ " + sets[1] + " ] ";
				/* add start point */
				for (int s = 0; s < 2; s++)
					for (int d = 0; d < sets[s].distance; d++)
						for (int z = 0; z < sets[s].capacity; z++)
							startingLocations.add (
								(r & 1) == 0 ?
								new StartingLocation (
									generateCellName (r/2, c + s),
									generateCellName (r/2, c + (1 - s)), d):
								new StartingLocation (
									generateCellName ((r-1+(2*s))/2, c),
									generateCellName ((r+1-(2*s))/2, c), d)
							);
			}
			if ((r & 1) == 0)
				t += generateCellName (r/2, cols - 1);
			t += "\n";
		}
		System.out.println(t);
		Collections.shuffle(startingLocations);
		int i;
		t = "";
		for (i = 0; i < vehicles && !startingLocations.isEmpty(); i++) {
			StartingLocation tmp = startingLocations.removeFirst();
			t += String.format("%s %s %s %d\n", tmp.aNode, tmp.bNode,
				generateCellName(rand.nextInt(rows), rand.nextInt(cols)), tmp.m);
		}
		System.out.println(Integer.toString(i));
		System.out.println(t);
	}

	class StartingLocation {
		private String aNode, bNode;
		private int m;
		public StartingLocation(String aNode, String bNode, int m) {
			this.aNode = aNode;
			this.bNode = bNode;
			this.m = m;
		}
		public String getANode() {
			return aNode;
		}
		public String getBNode() {
			return bNode;
		}
		public int getM() {
			return m;
		}
	}
	public static void main(String[] args) {
		if (args.length == 0)
			new MapGenerator();
		else
			new MapGenerator(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));

	}
}

