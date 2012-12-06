import java.util.ArrayList;
public class Zone {
	private float x, y, w, h;
	private String zoneID;
	private ArrayList<Edge> edgesInZone;
	public Zone(float x, float y, float w, float h, String zoneID) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.zoneID = zoneID;
		edgesInZone = new ArrayList<Edge>();
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
	public String getZoneID() {
		return zoneID;
	}
	public void addEdge(Edge e) {
		edgesInZone.add(e);
	}
	public void removeEdge(Edge e) {
		edgesInZone.remove(e);
	}
	public ArrayList<LinkResponse> broadcastLinkRequest(Vehicle v) {
		ArrayList<LinkResponse> al = new ArrayList<LinkResponse>();
		for (Edge e : edgesInZone) {
			for (LinkResponse lr : e.linkRequest(v)) {
				al.add(lr);
			}
		}
		// TODO add some border nodes of other zones?
		return al;
	}
	public boolean isWithinZone(float x, float y) {
		return x >= this.x*w && y >= this.y*h && 
			x < this.x*w+w && y < this.y*h+h;
	}
	public String toString() {
		return "zone " + x + "," + y;
	}
}
