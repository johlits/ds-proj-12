
public class ManhattenPosition {
	private int x;
	private int y;
	private int direction;
	
	public ManhattenPosition (int x, int y, int direction) {
		this.x = x;
		this.y = y;
		this.direction = direction;
	}
	
	public int getX () {
		return this.x;
	}
	
	public int getY () {
		return this.y;
	}
	
	public int getDirection () {
		return this.direction;
	}
}
