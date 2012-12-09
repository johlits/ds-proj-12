import java.math.*;
import java.util.*;

class TrafficMonitor {

	private LinkedList<Message> allMessages;
	public static enum PrettyPrint {
		YES, NO
	}
	
	public TrafficMonitor() {
		allMessages = new LinkedList<Message>();
	}
	public String getTotalTraffic(LinkedList<Message> messages) {
		BigDecimal totalTraffic = BigDecimal.ZERO;
		for (Message m : messages) 
			totalTraffic = totalTraffic.add(BigDecimal.valueOf(m.getLength()));
		return totalTraffic.toString();
	}
	public double getAverageMessageLength(LinkedList<Message> messages) {
		if (messages.size() == 0)
			return 0;
		BigDecimal totalTraffic = BigDecimal.ZERO;
		for (Message m : messages) 
			totalTraffic = totalTraffic.add(BigDecimal.valueOf(m.getLength()));
		return totalTraffic.divide(BigDecimal.valueOf(messages.size())).doubleValue();
	}
	public double getMedianMessageLength(LinkedList<Message> messages) {
		if (messages.size() == 0)
			return 0;
		Collections.sort(messages, 
		new Comparator<Message>() {
			@Override public int compare(Message a, Message b) {
    			return (a.getLength() > b.getLength()) ? 1 : -1;
			}
		});
		return messages.get(messages.size()/2).getLength();
	}
	public double getMinMessageLength(LinkedList<Message> messages) {
		if (messages.size() == 0)
			return 0;
		return Collections.min(messages, 
		new Comparator<Message>() {
			@Override public int compare(Message a, Message b) {
    			return (a.getLength() > b.getLength()) ? 1 : -1;
			}
		}).getLength();
	}
	public double getMaxMessageLength(LinkedList<Message> messages) {
		if (messages.size() == 0)
			return 0;
		return Collections.max(messages, 
		new Comparator<Message>() {
			@Override public int compare(Message a, Message b) {
    			return (a.getLength() > b.getLength()) ? 1 : -1;
			}
		}).getLength();
	}
	public void addMessage(Message m) {
		allMessages.add(m);
	}
	public void printReport(PrettyPrint pp) {
		switch (pp) {
			case YES: System.out.println(getPrettyReport()); break;
			case NO: System.out.println(getRawReport()); break;
			default: break;
		}
	}
	private String getPrettyReport() {
		StringBuilder sb = new StringBuilder();
		sb.append("----- TOTAL -----\n");
		sb.append("Total traffic sent: " + getTotalTraffic(allMessages) + "\n");
		sb.append("Longest message: " + getMinMessageLength(allMessages) + "\n");
		sb.append("Shortest message: " + getMaxMessageLength(allMessages) + "\n");
		sb.append("Average message length: " + getAverageMessageLength(allMessages) + "\n");
		sb.append("Median message length: " + getMedianMessageLength(allMessages) + "\n");
		sb.append("----- PER VEHICLE -----\n");
		HashMap<Vehicle, LinkedList<Message>> hm = new HashMap<Vehicle, LinkedList<Message>>();
		for (Message m : allMessages) {
			if (hm.containsKey(m.getSender())) {
				LinkedList tmp = hm.get(m.getSender());
				tmp.add(m);
				hm.put(m.getSender(), tmp);
			}
			else {
				LinkedList tmp = new LinkedList<Message>();
				tmp.add(m);
				hm.put(m.getSender(), tmp);
			}
		}
		Iterator it = hm.entrySet().iterator();
		while (it.hasNext()) {
		    Map.Entry pairs = (Map.Entry)it.next();
		    sb.append(pairs.getKey() + "\n");
		    LinkedList<Message> ll = (LinkedList<Message>)pairs.getValue();
	    	sb.append("Total traffic sent: " + getTotalTraffic(ll) + "\n");
			sb.append("Longest message: " + getMinMessageLength(ll) + "\n");
			sb.append("Shortest message: " + getMaxMessageLength(ll) + "\n");
			sb.append("Average message length: " + getAverageMessageLength(ll) + "\n");
			sb.append("Median message length: " + getMedianMessageLength(ll) + "\n");
		    it.remove();
		}
		return sb.toString();
	}
	private String getRawReport() {
		StringBuilder sb = new StringBuilder("");
		sb.append(getTotalTraffic(allMessages) + " ");
		sb.append(getMinMessageLength(allMessages) + " ");
		sb.append(getMaxMessageLength(allMessages) + " ");
		sb.append(getAverageMessageLength(allMessages) + " ");
		sb.append(getMedianMessageLength(allMessages) + "\n");
		HashMap<Vehicle, LinkedList<Message>> hm = new HashMap<Vehicle, LinkedList<Message>>();
		for (Message m : allMessages) {
			if (hm.containsKey(m.getSender())) {
				LinkedList tmp = hm.get(m.getSender());
				tmp.add(m);
				hm.put(m.getSender(), tmp);
			}
			else {
				LinkedList tmp = new LinkedList<Message>();
				tmp.add(m);
				hm.put(m.getSender(), tmp);
			}
		}
		Iterator it = hm.entrySet().iterator();
		while (it.hasNext()) {
		    Map.Entry pairs = (Map.Entry)it.next();
		    LinkedList<Message> ll = (LinkedList<Message>)pairs.getValue();
	    	sb.append(getTotalTraffic(ll) + " ");
			sb.append(getMinMessageLength(ll) + " ");
			sb.append(getMaxMessageLength(ll) + " ");
			sb.append(getAverageMessageLength(ll) + " ");
			sb.append(getMedianMessageLength(ll) + "\n");
		    it.remove();
		}
		return sb.toString();
	}
}


