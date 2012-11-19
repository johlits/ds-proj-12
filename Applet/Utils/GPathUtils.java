package Utils;

import java.awt.geom.PathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Line2D;
import java.awt.Point;

public abstract class GPathUtils {

	//**********************************************************************
	//**********************************************************************
	//******************* Usefull GeneralPath() Functions  *****************
	//**********************************************************************
	//**********************************************************************

	/** returns the starting point of "path" */
	public static Point2D getStartOfPath(GeneralPath path) {
		double seg[] = new double[6];
		PathIterator pi = path.getPathIterator(null);
		int segType = pi.currentSegment(seg);
		return (new Point2D.Double(seg[0],seg[1]));
	}

	/** returns the ending point of "path" */
	public static Point2D getEndOfPath(GeneralPath path) {
		return (path.getCurrentPoint());
	}

	/** returns the number of path sections. */
	public static int getNumPathSections(GeneralPath path) {
		return getNumPathPoints(path)-1;
	}

	/** returns the number path points of "path". */
	public static int getNumPathPoints(GeneralPath path) {
		int count = 0;
		for (PathIterator i=path.getPathIterator(null);!i.isDone();i.next()) {
	    count++;
		}
		return count;
	}

	/** 
	 * returns a path equal to "path" but with an extra initial section
	 */
	public static GeneralPath appendToStart(int startX, int startY,
																					GeneralPath path) {
		GeneralPath newPath= new GeneralPath(GeneralPath.WIND_EVEN_ODD);
		newPath.moveTo(startX,startY);
	
		double coOrds[] = new double[6];
		for (PathIterator i=path.getPathIterator(null);!i.isDone();i.next()){
	    int segType = i.currentSegment(coOrds);
	    newPath.lineTo((int)coOrds[0],(int)coOrds[1]);
		} 
		return newPath;
	}

	public static Point2D getIndexPoint(int pos, GeneralPath path) {
		double x1=0,y1=0,x2=0,y2=0;
		int index = 0;
		double seg[] = new double[6];
	
		for (PathIterator i=path.getPathIterator(null);!i.isDone();i.next()) {
	    int segType = i.currentSegment(seg);    
	    
	    if (index==0) { 
				x1=seg[0];y1=seg[1]; x2=x1;y2=y1;
	    } 
	    if (index > 0) {
				x1=x2;y1=y2; x2=seg[0];y2=seg[1];
				if (pos+1 == index) return (new Point2D.Double(x1,y1));
	    }
	    index++;
		}
		System.out.println("Error in getIndexPoint: 0<=pos<"+ index);
		return null;
	}

	/** Returns a Line2D indexed by pos. 1st line section is pos =0 */
	public static Line2D getLineSect(int pos, GeneralPath path) {
		double x1=0,y1=0,x2=0,y2=0;
		int index = 0;
		double seg[] = new double[6];
	
		for (PathIterator i=path.getPathIterator(null);!i.isDone();i.next()) {
	    int segType = i.currentSegment(seg);    
	    
	    if (index==0)          { x1 = seg[0]; y1 = seg[1];
	    } else if (index == 1) { x2 = seg[0]; y2 = seg[1];
	    } else if (index > 1)  { x1=x2;y1=y2; x2 = seg[0]; y2 = seg[1];}

	    if (index > 0) {
				if (pos+1 == index) return (new Line2D.Double(x1,y1,x2,y2));
	    }
	    index++;
		}
		System.out.println("Error: (pos out of  bounds) 0<=pos<"+ index);
		return null;
	}

	public static Line2D getLastLine(GeneralPath path) {
		double seg[] = new double[6];
		double x1 = 0; double y1 = 0; double x2 = 0; double y2 = 0;
		int index=0;

		for (PathIterator i=path.getPathIterator(null);!i.isDone();i.next()){
	    int segType = i.currentSegment(seg);
	    if (index==0) {
				x1 = seg[0];y1 = seg[1];x2 = seg[0];y2 = seg[1];
	    } else if (index == 1) {
				x2 = seg[0];y2 = seg[1];
	    } else if (index > 1) {
				x1=x2;y1=y2;
				x2 = seg[0];y2 = seg[1];
	    }
	    index++;
		}
		return (new Line2D.Double(x1,y1,x2,y2));
	}

	public static Line2D getFirstLine(GeneralPath path) {
		double seg[] = new double[6];
		double x1 = 0; double y1 = 0; double x2 = 0; double y2 = 0;
		int index=0;

		for (PathIterator i=path.getPathIterator(null);!i.isDone();i.next()){
	    int segType = i.currentSegment(seg);
	    if (index==0) {
				x1 = seg[0];y1 = seg[1];x2 = seg[0];y2 = seg[1];
	    } else if (index == 1) {
				return (new Line2D.Double(x1,y1, seg[0],seg[1]));
	    }
	    index++;
		}
		return (new Line2D.Double(x1,y1,x2,y2));
	}

	public static Line2D getNearestLine(Point2D point,GeneralPath path) {
		Line2D currentLine, nearestLine = null;
		double currentDistance, nearestDistance = 100000.0;
		double seg[] = new double[6];
		double x1=0,y1=0,x2=0,y2=0;
		int index=0;

		for (PathIterator i=path.getPathIterator(null);!i.isDone();i.next()){
	    int segType = i.currentSegment(seg);
	    if (index==0) {
				x1 = seg[0]; y1 = seg[1]; x2 = seg[0]; y2 = seg[1];
	    } else if (index > 0) {
				x1=x2; y1=y2; x2 = seg[0]; y2 = seg[1];
				currentLine = new Line2D.Double(x1,y1,x2,y2);
				currentDistance = currentLine.ptSegDist(point);
				if (currentDistance < nearestDistance) {
					nearestDistance = currentDistance;
					nearestLine = currentLine;
				}
	    }
	    index++;
		}
		return nearestLine;
	}

	/*
	 * Reverses a path so that afterwards, the old start is the new end.
	 */
	public static GeneralPath reversePath(GeneralPath path) {

		GeneralPath newPath= new GeneralPath(GeneralPath.WIND_EVEN_ODD);
		int numOfLinePoints = getNumPathPoints(path);
		double seg[] = new double[6];

		for (int i=numOfLinePoints; i>0; i--) {
	
	    final PathIterator pi2 = path.getPathIterator(null);
	    
	    for (int j=0; j<(i-1); j++) {
				pi2.next();
	    } 
	    int segType = pi2.currentSegment(seg);

	    if (i == numOfLinePoints) {
				newPath.moveTo((int)seg[0],(int)seg[1]);
	    } else {
				newPath.lineTo((int)seg[0],(int)seg[1]);
	    }
		}
		return newPath; 
	}

	/** alters p1 to add p2 onto it */
	public static GeneralPath joinPaths(GeneralPath p1, GeneralPath p2) {
		double seg[] = new double[6];
	
		for (PathIterator i=p2.getPathIterator(null);!i.isDone();i.next()){
	    int segType = i.currentSegment(seg);
	    p1.lineTo((int)seg[0],(int)seg[1]);
		}
		return p1;
	}

	/** alters p1 to add p2 onto it, without p2 1st point */
	public static GeneralPath mergePaths(GeneralPath p1, GeneralPath p2) {
		double seg[] = new double[6];
		PathIterator i=p2.getPathIterator(null);i.next();
		while (!i.isDone()) {
	    int segType = i.currentSegment(seg);
	    p1.lineTo((int)seg[0],(int)seg[1]);
	    i.next();
		}
		return p1;
	}

	/** 
	 * returns a path equal to "path" but with "pointToKill" removed.
	 */
	public static GeneralPath removePathPoint(GeneralPath path,
																						Point2D pointToKill) {

		GeneralPath newPath= new GeneralPath(GeneralPath.WIND_EVEN_ODD);

		double seg[] = new double[6];
		for (PathIterator i=path.getPathIterator(null);!i.isDone();i.next()){
	    int segType = i.currentSegment(seg);
	    switch(segType) {
	    case PathIterator.SEG_MOVETO:
				if (GeoUtils.isSamePoint(seg[0], seg[1],pointToKill)) {
					i.next();
					segType = i.currentSegment(seg);
					if (i.isDone()) {
						return null;
					} else {
						newPath.moveTo((int)seg[0],(int)seg[1]);
					}
				} else {
					newPath.moveTo((int)seg[0],(int)seg[1]);
				}
				break;
	    case PathIterator.SEG_LINETO:
				if (!GeoUtils.isSamePoint(seg[0], seg[1],pointToKill)) {
					newPath.lineTo((int)seg[0],(int)seg[1]);
				}
				break;
	    }
		}
		return newPath;
	}


	/** 
	 * returns a path equal to "path" but 
	 * with the path point "pointToChange" at location of "newPoint".
	 */
	public static GeneralPath modifyPathPoint(GeneralPath path, 
																						Point2D pointToChange, Point2D newPoint) {

		GeneralPath newPath= new GeneralPath(GeneralPath.WIND_EVEN_ODD);
		boolean seen = false;

		double seg[] = new double[6];
		PathIterator pi = path.getPathIterator(null);

		while (!pi.isDone()) {
	    int segType = pi.currentSegment(seg);
	    switch(segType) {
	    case PathIterator.SEG_MOVETO:
				if (GeoUtils.isSamePoint(seg[0],seg[1],pointToChange)&& !seen){
					newPath.moveTo((int)newPoint.getX(),(int)newPoint.getY());
					seen = true;
				} else {
					newPath.moveTo((int)seg[0],(int)seg[1]);
				}
				break;
	    case PathIterator.SEG_LINETO:
				if (GeoUtils.isSamePoint(seg[0],seg[1],pointToChange)&& !seen){
					newPath.lineTo((int)newPoint.getX(),(int)newPoint.getY());
					seen = true;
				} else {
					newPath.lineTo((int)seg[0],(int)seg[1]);
				}
				break;
	    }
	    pi.next();
		}
		return newPath;
	}

	public static Point2D getIntersectPoint(Line2D l,GeneralPath p) {
		return getIntersectPoint(l.getX1(),l.getY1(),l.getX2(),l.getY2(),p);
	}

	public static Point2D getIntersectPoint(double x1, double y1, double x2, 
																					double y2, GeneralPath p) {
		double x3=0,y3=0,x4=0,y4=0;
		int index = 0;
		double seg[] = new double[6];
	
		for (PathIterator i=p.getPathIterator(null);!i.isDone();i.next()) {
	    int segType = i.currentSegment(seg);    
	    
	    if (index==0) {
				x3 = seg[0] ;y3 = seg[1];
	    } else if (index == 1) {
				x4 = seg[0]; y4 = seg[1];
	    } else if (index > 1) {
				x3=x4;y3=y4;
				x4 = seg[0]; y4 = seg[1];
	    }
	    if (index > 0 && Line2D.linesIntersect(x1,y1,x2,y2,x3,y3,x4,y4)) {
				double l1m,l2m;
				//if lines are vertical, cater for a divide by 0:
				if (x2==x1) {
					l2m = (y4-y3)/(x4-x3);
					return (new Point2D.Double(x1,(l2m*x1)+y3));
				} else if (x4==x3) {
					l1m = (y2-y1)/(x2-x1);
					return (new Point2D.Double(x3,(l1m*x3)+y1));
				} else {
					l1m = (y2-y1)/(x2-x1);
					l2m = (y4-y3)/(x4-x3);
		    
					double x = ((-l1m)*x1) + y1 + (l2m*x3) -y3;
					x = x/(l2m-l1m);
					double y= l1m*(x-x1) + y1;
		    
					return (new Point2D.Double(x,y));
				}
	    }
	    index++;
		}
		return null;
	}


	public static Point2D getIntersectPoint(Point2D point,
																					GeneralPath p1,GeneralPath p2) {

		Point2D intersectionPoint, returnPoint=null;

		double x1=0,y1=0,x2=0,y2=0,closestDist = 100000;
		int index = 0;
		double seg[] = new double[6];
	
		for (PathIterator i=p1.getPathIterator(null);!i.isDone();i.next()) {
	    int segType = i.currentSegment(seg); 

	    if (index==0) { 
				x1=seg[0];y1=seg[1]; x2=x1;y2=y1;
	    } 
	    if (index > 0) {
				x1=x2;y1=y2; x2=seg[0];y2=seg[1];

				intersectionPoint = getIntersectPoint(x1,y1,x2,y2,p2);
				if (intersectionPoint != null) {
					double dist = intersectionPoint.distance(point);
					if (dist <= closestDist) {
						returnPoint = intersectionPoint;
						closestDist = dist;
					}
				}
	    }
	    index++;
		}
		return returnPoint;
	}

	public static GeneralPath getPathToIndex(int point,GeneralPath path) {

		GeneralPath newPath = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

		double x1=0,y1=0,x2=0,y2=0;
		int index = 0;
		double seg[] = new double[6];
		for (PathIterator i=path.getPathIterator(null);!i.isDone();i.next()) {
	    int segType = i.currentSegment(seg);    
	    
	    if (index == 0) { 
				newPath.moveTo((int)seg[0],(int)seg[1]);
	    } 
	    if (index > 0) {
				if (index > point) return newPath;
				newPath.lineTo((int)seg[0],(int)seg[1]);
	    }
	    index++;
		}
		return newPath;
	}

	public static GeneralPath getPathFromIndex(int point,GeneralPath path) {

		GeneralPath newPath = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
		double x1=0,y1=0,x2=0,y2=0;
		int index = 0;
		double seg[] = new double[6];
		boolean gotThere = false;
		for (PathIterator i=path.getPathIterator(null);!i.isDone();i.next()) {
	    int segType = i.currentSegment(seg);    
	    if (gotThere) {
				newPath.lineTo((int)seg[0],(int)seg[1]);
	    } else {
				if (point == index) {
					newPath.moveTo((int)seg[0],(int)seg[1]);
					gotThere = true;
				}
	    } 
	    index++;
		}
		return newPath;
	}

	public static int getAverageDirection(GeneralPath path) {
		double seg[] = new double[6];
		PathIterator pi = path.getPathIterator(null);
		int segType = pi.currentSegment(seg);
	    
		return GeoUtils.calculateGeneralDirection((int)seg[0],(int)seg[1],
																							(int)path.getCurrentPoint().getX(),
																							(int)path.getCurrentPoint().getY());
	}


	public static double getAverageAngle(GeneralPath path) {
		double seg[] = new double[6];
		PathIterator pi = path.getPathIterator(null);
		int segType = pi.currentSegment(seg);
	    
		return GeoUtils.getAngle(seg[0],seg[1],
														 path.getCurrentPoint().getX(),
														 path.getCurrentPoint().getY());
	}


	public static boolean isSameDirection(GeneralPath p1,GeneralPath p2,
																				double radAngleTollerance) {
		//FIX ME : Really not sure about this logic.
		//two paths are the same direction if the lines drawn from their
		//end points are in the same direction.
		//Remember that 360 and 1 are similar angles.

		final double tollerance = radAngleTollerance;

		double a1 = getAverageAngle(p1)+tollerance;
		double a2 = getAverageAngle(p2)+tollerance;
		a1 = a1%(2*Math.PI);
		a2 = a2%(2*Math.PI);

		return (Math.abs(a1-a2) < tollerance);
	}
}















