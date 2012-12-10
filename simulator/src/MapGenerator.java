import java.util.Random;
class MapGenerator {
	Random rand = new Random();
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
		// TODO fix this!
		height*=2;
		Kattio io = new Kattio(System.in, System.out);
		int app = 0;
		io.println(height-1);
		for (int i = 0; i < height-1; i++) {
			if (i%2 == 0) {
				io.print("v" + app++);
				for (int j = 0; j < width-1; j++) 
					io.print(" { " + rand.nextInt(10) + " " + rand.nextInt(10) + " " + rand.nextInt(10) + " " + (rand.nextInt(10)+1) + " " + (rand.nextInt(10)+1) + " } * [ " + rand.nextInt(10) + " " + rand.nextInt(10) + " " + rand.nextInt(10) + " " + (rand.nextInt(10)+1) + " " + (rand.nextInt(10)+1) + " ] " + "v" + (app++));
				io.println();
			}
			else {

				for (int j = 0; j < width; j++) 
					io.print("{ " + rand.nextInt(10) + " " + rand.nextInt(10) + " " + rand.nextInt(10) + " " + (rand.nextInt(10)+1) + " " + (rand.nextInt(10)+1) + " } * [ " + rand.nextInt(10) + " " + rand.nextInt(10) + " " + rand.nextInt(10) + " " + (rand.nextInt(10)+1)+ " " + (rand.nextInt(10)+1) + " ] ");
				io.println();
			}
		}
		io.println(vehicles);
		for (int i = 0; i < vehicles; i++) {
			int n = rand.nextInt(width*(height/2));
			int g = rand.nextInt(width*(height/2));
			int d = (((n+1)%width)==0) ? n-1 : n+1;
			String s = "v"+n + " v"+d + " v" + g;
			io.println(s);
		}
		
		io.close();
	}
	public static void main(String[] args) {
		if (args.length == 0)
			new MapGenerator();
		else
			try {
				new MapGenerator(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
			} catch (Exception e) {
				System.err.println("Parse error");
			}
	}
}
