import java.util.HashMap;
import java.util.Map.Entry;

public class HeatMap<T> {
	private HashMap<T,Integer> map = new HashMap<T,Integer>();
	
	public void increaseHeat (T e) {
		if (!map.containsKey(e))
			map.put(e, 0);
		map.put(e, map.get(e) + 1);
	}
	
	public int getHeat (T e) {
		return map.containsKey(e) ? map.get(e) : 0;
	}

	public String toString () {
		String s = "";
		for (Entry<T, Integer> e : map.entrySet()) {
			s += " " + e.getKey().toString() + ": " + e.getValue().toString() + "\n";
		}
		return s;
	}
}
