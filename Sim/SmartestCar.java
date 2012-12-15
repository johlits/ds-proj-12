import java.awt.Rectangle;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.Shape;

import java.awt.Polygon;

import java.lang.Math;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;

import time.*;
import Utils.*;
import java.util.*;

public class SmartestCar extends Car implements Timed {
 
 	ArrayList<Move> endMoves;
 	static LinkedList<Reservation> reservations = new LinkedList<Reservation>();
 
	public SmartestCar(LaneSection startLane,CarGenerator parent,
						 TimeManager theTick, int topspeed) {
     	
     	
		super(startLane, parent, theTick, topspeed);
		
		init(startLane);
		

	}
	
	class Move implements Comparable<Move> {
		Edge edge;
		int enter;
		int leave;
		Move prev;
		public Move(Edge e, Move prev, int enter, int leave) {
			this.edge = e;
			this.prev = prev;
			this.enter = enter;
			this.leave = leave;
		}
		public int compareTo(Move m) {
			return (edge.d > m.edge.d) ? 1 : -1;
		}
	}
	
	class Reservation  {
		Edge edge;
		int enter;
		int leave;
		public Reservation(Edge edge, int enter, int leave) {
			this.edge = edge;
			this.enter = enter;
			this.leave = leave;
		}
	}
	
	public int findReservations(int time) {
		int cnt = 0;
		for (Reservation r : reservations) 
			if (time >= r.enter && time <= r.leave) 
				cnt++;
		return cnt;
	}
	
	public void init(CarContainer startLane) {
	
		if (!reservations.isEmpty()) {
			int j = 0;
			for (Reservation r : reservations) {
				if (r.leave < ticker.ticks) {
					j++;
				}
			}
			
			for (int i = 0; i < j; i++) {
				reservations.removeFirst();
			}
		}
		//System.out.println("# of reservations " + reservations.size());
		//System.out.println("current tick " + ticker.ticks);
		//for (Reservation r : reservations) {
		//	System.out.println(""+r.enter+" "+r.leave);
		//}
	
		// find shortest path (dijkstra, no A* yet)
		reachableCarContainers = new ArrayList<CarContainer>();
		endMoves = new ArrayList<Move>();
		PriorityQueue<Move> pq = new PriorityQueue<Move>();
		
		reachableCarContainers.add(startLane);
		pq.add(new Move(new Edge(startLane, startLane.length, null),null,ticker.ticks,-1));
		
		
		int i = 0;
		while (!pq.isEmpty()) {
			Move temp = pq.poll();
			
			//System.out.println("start " + temp.cc.lightStart);
			int time = ticker.ticks;
			int isNow = 1;
			
			if (temp.edge.cc.lightCycle > 0) {
				time = ((int)((double)temp.edge.d/5+ticker.ticks));//estimation
				//System.out.print("est time " + time + " ");
				if ((time/temp.edge.cc.lightCycle)%2==0)
					isNow = temp.edge.cc.lightStart;
				else
					isNow = 1-temp.edge.cc.lightStart;
					
				if (isNow != 1)
					time = time%temp.edge.cc.lightCycle;
				else
					time = 0;
					
				//System.out.print("est wait " + time +" ");
				
				
				//System.out.println("est light " + isNow);
				
				//System.out.println(ticker.ticks + " / " + temp.cc.lightCycle);
				//System.out.println(isNow);
			}
			else
				time = ((int)((double)temp.edge.d/5+ticker.ticks));
			temp.leave = time;

			if (temp.edge.cc.isLastOne()) {
				// store the endpoints
				endMoves.add(temp);
			}
			else {
				// check every adjacent car container
				for (CarContainer c : temp.edge.cc.getAdjacent()) {  
					// if it has not been visited (O(n), can be made O(1))
					if (!reachableCarContainers.contains(c)) {
						reachableCarContainers.add(c);
						//System.out.println("est wait time " + time);
						int congestion = findReservations(time);
						pq.offer(new Move(new Edge(c, temp.edge.d + c.length + time + congestion*1000, temp.edge),temp,time,-1));

					}
				}
			}
			
		}
		
		// choose an end point and create the path of car containers
		Move temp = endMoves.get(rand.nextInt(endMoves.size()));
		while (temp.edge.cc != startLane) {
			plannedPath.addFirst(temp.edge.cc);
			//System.out.println("est enter " + temp.enter + " leave " + temp.leave);
			reservations.addLast(new Reservation(temp.edge,temp.enter,temp.leave));
			temp = temp.prev;
		}
		plannedPath.addFirst(plannedPath.removeLast());
		
		// print the path
		// System.out.println("--- PATH ---");
		// for (CarContainer c : plannedPath)
		//	System.out.println(c);
		
		addCar();
	}
}





