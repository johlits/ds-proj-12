package Utils;

import java.awt.geom.Point2D;
import java.awt.geom.Line2D;

import java.awt.Rectangle;

public abstract class GeoUtils {

	public final static int NORTHWEST = 1;
	public final static int NORTH = 2;
	public final static int NORTHEAST = 3;
	public final static int EAST = 4;
	public final static int SOUTHEAST = 5;
	public final static int SOUTH = 6;
	public final static int SOUTHWEST = 7;
	public final static int WEST = 8;

	public static boolean isSamePoint(double x,double y,double x2,double y2) {
		return (x==x2 && y==y2);
	}
	public static boolean isSamePoint(double x, double y, Point2D p) {
		return isSamePoint(x,y,p.getX(),p.getY());
	}
	public static boolean isSamePoint(Point2D p, Point2D p2) {
		return isSamePoint(p.getX(),p.getY(),p2.getX(),p2.getY());
	}

	private static double getDistance(int x1, int y1, int x2, int y2) {
		return Math.sqrt(Math.pow((double)(x1-x2),2)+
										 Math.pow((double)(y1-y2),2));
	}

	public static double getDistance(double x1, double y1, 
																	 double x2, double y2) {
		return Math.sqrt(Math.pow(x1-x2,2)+
										 Math.pow(y1-y2,2));
	}
	public static double getDistance(double x, double y, Point2D p) {
		return getDistance(x,y,p.getX(),p.getY());
	}
	public static double getDistance(Point2D p, Point2D p2) {
		return getDistance(p.getX(),p.getY(),p2.getX(),p2.getY());
	}
	public static Point2D getMidPoint(Point2D p1, Point2D p2) {
		return (new Point2D.Double((p1.getX()+p2.getX())/2,
															 (p1.getY()+p2.getY())/2));
	}
    
	/** 
	 * Returns the angle between line (x1,y1)->(x2,y2) and the positive x-axis
	 * in Radians
	 */
	public static double getAngle(double x1,double y1,double x2,double y2) {
		//Get gradient of line.
	
		double m = (y2-y1)/(x2-x1); //Get gradient of line.
	
		if (x2==x1) {//Protect against divide by 0
	    if (y2 == y1) return -1;
	    else if (y2 < y1) return Math.PI/2;
	    else return 3*(Math.PI/2);
		} 
  
		//Remember gradient is the opposite sign to what you would expect due
		//to computer screen geometry differing from maths X-Y geometry.
		//Value to return depends on what quadrand the line is in.

		if (x2>=x1 && y2<=y1) {
	    return  (-Math.atan(m)); //1st quadrant. 
		} else if (x2<x1 && y2<=y1) {
	    return  (Math.PI-Math.atan(m)); //2nd quadrant. 
		} else if (x2<x1 && y2>y1) {
	    return  (Math.PI-Math.atan(m)); //3rd quadrant. 
		} else {
	    return  (2*Math.PI-Math.atan(m)); //4th quadrant. 
		}
	}

	public static double getAngle(Line2D l) {
		return getAngle(l.getX1(),l.getY1(),l.getX2(),l.getY2());
	}

	/**
	 * Uses the SOHCAHTOA triangle to return the horizontal x point "length"
	 * away from "x" at "angle" degrees.
	 */
	public static double  getXPointAtAngle(double x,double angle,
																				 double length) {
		//System.out.print(".");
		return(x+length*Math.cos(angle));
	}

	/**
	 * Uses the SOHCAHTOA triangle to return the vertical y point "length"
	 * away from "y" at "angle" degrees.
	 */
	public static double  getYPointAtAngle(double y,double  angle, 
																				 double length) {
		return (y-length*Math.sin(angle));
	}

	/** 
	 * draw a line at "x","y" at angle "angle" and length "length".
	 */
	public static Line2D getAngledLine(double x, double y,
																		 double  angle, int length) {
		double x2 = x;
		double y2 = y;
       
		if (angle == 0) {
			x2 += length;
		} else if (angle < 90) {
			x2 += length*Math.cos(angle);
			y2 -= length*Math.sin(angle);
		} else if (angle == 90) {
			y2 -= length;
		} else if (angle < 180) {
			x2 -= length*Math.cos(angle-Math.PI);
			y2 -= length*Math.sin(angle-Math.PI);
		} else if (angle == 180) {
			x2 -= length;
		} else if (angle < 270) {
			x2 -= length*Math.cos(Math.PI-angle);
			y2 += length*Math.sin(Math.PI-angle);
		} else if (angle == 270) {
			y2 +=length;
		} else {
			x2 += length*Math.cos(2*Math.PI-angle);
			y2 += length*Math.sin(2*Math.PI-angle);
		}
		return (new Line2D.Double(x,y,x2,y2));
	}

	public static Line2D reverseLine(Line2D l) {
		return new Line2D.Double(l.getX2(),l.getY2(),l.getX1(),l.getY1());
	}

	public static Line2D getLastHalf(Line2D l) {
		return new Line2D.Double((l.getX1()+l.getX2())/2,
														 (l.getY1()+l.getY2())/2,
														 l.getX2(),l.getY2());
	}

	public static Point2D getIntersectPoint(double x1, double y1, 
																					double x2, double y2, Line2D l2,
																					boolean infinite) {
		return getIntersectPoint(x1,y1,x2,y2,
														 (int)l2.getX1(),(int)l2.getY1(),
														 (int)l2.getX2(),(int)l2.getY2(),
														 infinite);
	}

	public static Point2D getIntersectPoint(Line2D l1, Line2D l2,
																					boolean infinite) {
		return getIntersectPoint((int)l1.getX1(),(int)l1.getY1(),
														 (int)l1.getX2(),(int)l1.getY2(),
														 (int)l2.getX1(),(int)l2.getY1(),
														 (int)l2.getX2(),(int)l2.getY2(),
														 infinite);
	}

	public static Point2D getIntersectPoint(double x1, double y1, 
																					double x2, double y2,
																					double x3, double y3, 
																					double x4, double y4,
																					boolean infinite) {

		if (infinite || Line2D.linesIntersect(x1,y1,x2,y2,x3,y3,x4,y4)) {
	    double l1m = (y2-y1)/(x2+0.0001-x1); //0.001 to stop / by 0.
	    double l2m = (y4-y3)/(x4+0.0001-x3); //0.001 to stop / by 0.
	    
	    double x = ((-l1m)*x1) + y1 + (l2m*x3) -y3;
	    x = x/(l2m-l1m);
	    double y= l1m*(x-x1) + y1;
	    
	    return (new Point2D.Double(x,y));
		}
    
		return null;
	}

	public static int calculateGeneralDirection(Line2D l1) {
		return calculateGeneralDirection((int)l1.getX1(),(int)l1.getY1(),
																		 (int)l1.getX2(),(int)l1.getY2());
	}
    
	public static int calculateGeneralDirection(int startX,int startY, 
																							int endX, int endY) {
		if (startX==endX) {
	    if (startY < endY) {
				return SOUTH;
	    } else {
				return NORTH;
	    }
		} else if (startY==endY){
	    if (startX < endX) {
				return EAST;
	    } else {
				return WEST;
	    }
		} else if (startX < endX) {
	    if (startY < endY) {
				return SOUTHEAST;
	    } else {
				return NORTHEAST;
	    }
		} else if (startX >= endX) {
	    if (startY < endY) {
				return SOUTHWEST;
	    } else {
				return NORTHWEST;
	    }
		} else {
	    System.out.println("Problem in calculateGeneralDirection");
	    return -1;
		}
	}

	public static String getDirectionString(int dir) {
		switch(dir) {
		case NORTH :     return "SOUTH";
		case NORTHEAST : return "SOUTHWEST";
		case EAST :      return "WEST";
		case SOUTHEAST : return "NORTHWEST";
		case SOUTH :     return "NORTH";
		case SOUTHWEST : return "NORTHEAST";
		case WEST :      return "EAST";
		case NORTHWEST : return "SOUTHEAST";
		}
		System.out.println("Unexpected value for dir in getDirectionString");
		return null;
	}

	public static int getOppositeDirection(int dir) {
		switch(dir) {
		case NORTH :     return SOUTH;
		case NORTHEAST : return SOUTHWEST;
		case EAST :      return WEST;
		case SOUTHEAST : return NORTHWEST;
		case SOUTH :     return NORTH;
		case SOUTHWEST : return NORTHEAST;
		case WEST :      return EAST;
		case NORTHWEST : return SOUTHEAST;
		}
		System.out.println("Unexpected value for dir in getOppositeDirection");
		return -1;
	}

	public static int getRightDirection(int dir) {
		switch(dir) {
		case NORTH :     return WEST;
		case EAST :      return NORTH;
		case SOUTH :     return EAST;
		case WEST :      return SOUTH;
		}
		System.out.println("Unexpected value for dir in getRightDirection");
		return -1;
	}

	public static int getLeftDirection(int dir) {
		switch(dir) {
		case NORTH :     return EAST;
		case EAST :      return SOUTH;
		case SOUTH :     return WEST;
		case WEST :      return NORTH;
		}
		System.out.println("Unexpected value for dir in getLeftDirection");
		return -1;
	}

	public static boolean isSimilarAngles(double a1, double a2, double tol) {
	
		a1 += tol;
		a2 += tol;
		a1 = a1%(2*Math.PI);
		a2 = a2%(2*Math.PI);

		return (Math.abs(a1-a2) < tol);
	}

	//Given two angles, what is the smallest difference between them.
	public static double getAngleDiff(double a1,double a2) {
		a1 = a1%(2*Math.PI);
		a2 = a2%(2*Math.PI);
		double temp = Math.abs(a1-a2);
		if (temp < Math.PI) return temp;
		else return Math.abs(2*Math.PI-temp);
	}

	public static Point2D getRectangleCenter(Rectangle rect) {
		return (new Point2D.Double(rect.getX()+rect.getWidth()/2,
															 rect.getY()+rect.getHeight()/2));
	}
}











