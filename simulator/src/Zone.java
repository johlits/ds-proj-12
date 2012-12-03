import java.util.ArrayList;
public class Zone {
	private float x, y, w, h;
	private ArrayList<Vehicle> vehiclesInZone;
	public Zone(float x, float y, float w, float h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}
	public float getX() {
		return x;
	}
	public float getY() {
		return y;
	}
	public float getWidth() {
		return w;
	}
	public float getHeight() {
		return h;
	}
	public void enterZone(Vehicle v) {
		vehiclesInZone.add(v);
	}
	public void leaveZone(Vehicle v) {
		vehiclesInZone.remove(v);
	}
	public boolean isWithinZone(float x, float y) {
		return x >= this.x*w && y >= this.y*h && 
			x < this.x*w+w && y < this.y*h+h;
	}
	public String toString() {
		return "zone " + x + "," + y + " - " + vehiclesInZone + " vehicles";
	}
}
