class LinkResponse {
	private String nID, zID;
	public LinkResponse(String nID, String zID) {
		this.nID = nID;
		this.zID = zID;
	}
	public String getNID() {
		return nID;
	}
	public String getZID() {
		return zID;
	}
	public String toString() {
		return "link response nID: " + nID + " zID: " + zID;
	}
}
