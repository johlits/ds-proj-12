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
	public MapGenerator(int width, int height, int vehicles) {
		Kattio io = new Kattio(System.in, System.out);
		String[][] nodeNames = new String[height*2][width];
		int app = 0;
		for (int i = 0; i < height*2; i+=2)
			for (int j = 0; j < width; j++) 
				nodeNames[i][j] = "v"+(app++);
		io.println(height*2-1);
		for (int i = 0; i < height*2-1; i++) {
			if (i%2 == 0) {
				String aNode = "v"+(app++);
				String bNode = "v"+(app++);
				io.print(String.format("%s",nodeNames[i][0]));
				for (int j = 0; j < width-1; j++) {
					int distance = rand.nextInt(10);
					int capacity = rand.nextInt(10);
					int trafficLightOffset = rand.nextInt(10);
					int trafficLightGreenCycle = 1+rand.nextInt(9);
					int trafficLightRedCycle = 1+rand.nextInt(9);
					for (int k = 0; k < distance; k++) 
						for (int l = 0; l < capacity; l++) 
							startingLocations.add(new StartingLocation(nodeNames[i][j],nodeNames[i][j+1],k));
					int rdistance = rand.nextInt(10);
					int rcapacity = rand.nextInt(10);
					int rtrafficLightOffset = rand.nextInt(10);
					int rtrafficLightGreenCycle = 1+rand.nextInt(9);
					int rtrafficLightRedCycle = 1+rand.nextInt(9);
					for (int k = 0; k < rdistance; k++) 
						for (int l = 0; l < rcapacity; l++) 
							startingLocations.add(new StartingLocation(nodeNames[i][j+1],nodeNames[i][j],k));
					io.print(String.format(" { %d %d %d %d %d } * [ %d %d %d %d %d ] %s", 
						distance, capacity, trafficLightOffset, trafficLightGreenCycle, trafficLightRedCycle, 
						rdistance, rcapacity, rtrafficLightOffset, rtrafficLightGreenCycle, rtrafficLightRedCycle, 
						nodeNames[i][j+1]));
				}
				io.println();
			}
			else {
				for (int j = 0; j < width; j++) {
					int distance = rand.nextInt(10);
					int capacity = rand.nextInt(10);
					int trafficLightOffset = rand.nextInt(10);
					int trafficLightGreenCycle = 1+rand.nextInt(9);
					int trafficLightRedCycle = 1+rand.nextInt(9);
					for (int k = 0; k < distance; k++) 
						for (int l = 0; l < capacity; l++) 
							startingLocations.add(new StartingLocation(nodeNames[i+1][j],nodeNames[i-1][j],k));
					int rdistance = rand.nextInt(10);
					int rcapacity = rand.nextInt(10);
					int rtrafficLightOffset = rand.nextInt(10);
					int rtrafficLightGreenCycle = 1+rand.nextInt(9);
					int rtrafficLightRedCycle = 1+rand.nextInt(9);
					for (int k = 0; k < distance; k++) 
						for (int l = 0; l < capacity; l++) 
							startingLocations.add(new StartingLocation(nodeNames[i-1][j],nodeNames[i+1][j],k));
					io.print(String.format(" { %d %d %d %d %d } * [ %d %d %d %d %d ] ", 
						distance, capacity, trafficLightOffset, trafficLightGreenCycle, trafficLightRedCycle, 
						rdistance, rcapacity, rtrafficLightOffset, rtrafficLightGreenCycle, rtrafficLightRedCycle));
				}
				io.println();
			}
		}
		io.println(vehicles);
		Collections.shuffle(startingLocations);
		for (int i = 0; i < vehicles; i++) {
			StartingLocation tmp = startingLocations.removeFirst();
			int g = rand.nextInt(width*height);
			io.println(tmp.aNode + " " + tmp.bNode + " v" + g + " " + tmp.m);
		}
		
		io.close();
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
