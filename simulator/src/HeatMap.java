import java.util.HashMap;
import java.util.Map.Entry;

public class HeatMap {
	private HashMap<Edge,Integer> map = new HashMap<Edge,Integer>();
	
	public void increaseHeat (Edge e) {
		if (!map.containsKey(e))
			map.put(e, 0);
		map.put(e, map.get(e) + 1);
	}
	
	public String toString () {
		String s = "";
		for (Entry<Edge, Integer> e : map.entrySet()) {
			s += " " + e.getKey().toString() + ": " + e.getValue().toString() + "\n";
		}
		return s;
	}
}
