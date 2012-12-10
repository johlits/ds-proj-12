class MapGenerator {
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
		Kattio io = new Kattio(System.in, System.out);
		int app = 0;
		io.println(height);
		for (int i = 0; i < height; i++) {
			if (i%2 == 0) {
				io.print("v" + app++);
				for (int j = 0; j < width; j++) 
					io.print(" * " + "v" + (app++));
				io.println();
			}
			else {
				io.print("*");
				for (int j = 0; j < width; j++) 
					io.print("   *");
				io.println();
			}
		}
		io.println("0");
		io.close();
	}
	public static void main(String[] args) {
		if (args.length == 0)
			new MapGenerator();
		else
			try {
				new MapGenerator(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[1]));
			} catch (Exception e) {
				System.err.println("Parse error");
			}
	}
}
