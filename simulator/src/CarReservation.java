class CarReservation {
	public Edge e;
	public boolean defensive;
	public CarReservation (Edge e, boolean defensive) {
		this.e = e;
		this.defensive = defensive;
	}
	public String toString() {
		return "e," + ADPP.edgeID.get(e) + "," + (defensive ? 1 : 0);
	}
}
