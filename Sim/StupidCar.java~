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

public class StupidCar extends Car implements Timed {
 
	public StupidCar(LaneSection startLane,CarGenerator parent,
						 TimeManager theTick, int topspeed) {
     
		super(startLane, parent, theTick, topspeed);
		init(startLane);

	}
	
	public void init(CarContainer startLane) {
		// find a path (stack based)
		reachableCarContainers = new ArrayList<CarContainer>();
		endPoints = new ArrayList<Edge>();
		Stack<Edge> s = new Stack<Edge>();
		
		reachableCarContainers.add(startLane);
		s.push(new Edge(startLane, startLane.length, null));
		
		while (!s.isEmpty()) {
			Edge temp = s.pop();
			if (temp.cc.isLastOne()) {
				// store the endpoints
				endPoints.add(temp);
			}
			else {
				// check every adjacent car container
				for (CarContainer c : temp.cc.getAdjacent()) {  
					// if it has not been visited (O(n), can be made O(1))
					if (!reachableCarContainers.contains(c)) {
						reachableCarContainers.add(c);
						s.push(new Edge(c, temp.d + c.length, temp));
					}
				}
			}
		}
		
		// choose an end point and create the path of car containers
		Edge temp = endPoints.get(rand.nextInt(endPoints.size()));
		while (temp.cc != startLane) {
			plannedPath.addFirst(temp.cc);
			temp = temp.prev;
		}
		plannedPath.addFirst(plannedPath.removeLast());
		
		// print the path
		System.out.println("--- PATH ---");
		for (CarContainer c : plannedPath)
			System.out.println(c);
		
		addCar();
	}
}





