import java.util.ArrayList;
public class RandomRoutingZHLS implements RoutingAlgorithm {

	private static ArrayList<Zone> zones;
	private static int yzones = 0, xzones = 0;

	@Override
	public Edge nextEdge(Vehicle vehicle, int tick) {
		vehicle.newEdgeUpdate(vehicle.getPosition().getOutgoingNode().getOutgoingEdges()[0]);
		return vehicle.getPosition().getOutgoingNode().getOutgoingEdges()[0];
	}
	
	public MovementRequest.CollisionStrategy getStrategy(Vehicle v, int tick) {
		return MovementRequest.CollisionStrategy.Defensive;
	}

	@Override
	public void init(Vehicle[] vehicles, Edge[] edges) {
		int zid = 0, xzones = 10, yzones = 10;
		float zoneSize = 100;
		zones = new ArrayList<Zone>();
		
		for (int i = 0; i < yzones; i++) 
			for (int j = 0; j < xzones; j++) 
				zones.add(new Zone(i,j,zoneSize,zoneSize,""+(++zid)));
				
		// add 3 edges in each zone
		for (int i = 0,j = 0; i < edges.length; i++) {
			edges[i].setZone(zones.get(i));
			if (i%3==0)
				j++;
		}
	}
}
