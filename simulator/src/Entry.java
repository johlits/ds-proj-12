class Entry {
	private String destination;
	private String nextZone;
	private String nextHopNode;
	public Entry(String destination, String nextZone, String nextHopNode) {
		this.destination = destination;
		this.nextZone = nextZone;
		this.nextHopNode = nextHopNode;
	}
	public String getDestination() {
		return destination;
	}
	public String nextZone() {
		return nextZone;
	}
	public String nextHopNode() {
		return nextHopNode;
	}
}
