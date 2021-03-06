public class LocalShortestPathRoutingWithTrafficLights extends LocalShortestPathRouting implements RoutingAlgorithm {
	protected PathEdge calculateDistance (PathEdge a, Edge b, int tick) {
		return new PathEdge(a, a.getDistance() + b.getDistance() +
			(b.getTrafficLight() == null ? 0 :b.getTrafficLight()
					.remainingWaitingTime(tick + a.getDistance() + b.getDistance())), b);
	}
}
