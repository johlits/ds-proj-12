import java.util.List;
class Message {
	private String msg = "";
	private int sendTick = 0;
	private int messageLength = 0;
	private Vehicle sender;
	public Message(String str, Vehicle sender, int sendTick) {
		this.msg = str;
		this.sendTick = sendTick;
		this.messageLength = str.length();
		this.sender = sender;
	}
	public static String createRouteMessage(int prio, int sendTick, List<CarReservation> reservations) {
		StringBuilder sb = new StringBuilder("route$");
		sb.append(prio+"$");
		sb.append(sendTick+"$");
		for (CarReservation cr : reservations) 
			sb.append(cr.toString()+"$");
		sb.append("!");
		return sb.toString();
	}
	public Vehicle getSender() {
		return sender;
	}
	public int getSendTick() {
		return sendTick;
	}
	public int getLength() {
		return msg.length();
	}
	public String getMessage() {
		return msg;
	}
}
