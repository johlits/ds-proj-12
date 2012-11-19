import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.Shape;
import java.awt.BasicStroke;
import java.awt.Polygon;

import java.awt.geom.PathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
import java.awt.geom.Area;

import Utils.*;
import XML.*;

public class Road implements XMLSerializable {

    public final static int laneWidth = 13;
    public final static int maxLaneNum = 9;
    public final static double halfPI = Math.PI/2;
    public final static BasicStroke laneSurface = new BasicStroke(laneWidth, 
						      BasicStroke.CAP_BUTT,
						      BasicStroke.JOIN_ROUND);
    public final static BasicStroke centerLine = new BasicStroke(3, 
						     BasicStroke.CAP_BUTT,
						     BasicStroke.JOIN_BEVEL);
    public final static BasicStroke normal = new BasicStroke(1,
						 BasicStroke.CAP_BUTT,
						 BasicStroke.JOIN_BEVEL);
    public final static BasicStroke laneBackGround = new BasicStroke(
						       laneWidth+2, 
					               BasicStroke.CAP_BUTT,
					               BasicStroke.JOIN_ROUND);


    private int iD;
    private GeneralPath[] lanes;
    private boolean singleLane = true;

    private Shape roadShape;
    private Shape leftSide;
    private Shape rightSide;

    public int busyness;

    public Road(int index) {
       iD = index;
       lanes = new GeneralPath[maxLaneNum];
       busyness = 100;
    }

    //**********************************************************************
    //*********************** draw functions *******************************
    //**********************************************************************

    /** Draws all the paths in "pathArray" (a GeneralPath[]) */
    public void drawRoad(Graphics2D g2d) {

	if (singleLane) {

	    g2d.setStroke(laneBackGround); g2d.setColor(Color.black);
	    g2d.draw(lanes[0]);

	    g2d.setStroke(laneSurface); g2d.setColor(Color.lightGray);
	    g2d.draw(lanes[0]);
	} else {
	    for (int i=1; i < maxLaneNum; i++) {
		if (lanes[i] != null) {

		    g2d.setStroke(laneBackGround); g2d.setColor(Color.black);
		    g2d.draw(lanes[i]);

		    g2d.setStroke(laneSurface); g2d.setColor(Color.lightGray);
		    g2d.draw(lanes[i]);
		}
	    }
	    g2d.setStroke(centerLine); g2d.setColor(Color.yellow);
	    g2d.draw(lanes[0]);
	}
    }


    public void drawHandles(Graphics2D g2d) {
      if (lanes[0] == null) return;
      g2d.setStroke(normal); g2d.setColor(Color.gray);
	
      double seg[] = new double[6];
      Shape s;

      for (PathIterator i=lanes[0].getPathIterator(null);!i.isDone();i.next()){
	  int segType = i.currentSegment(seg);    
	  s = getSPointRectangle((int)seg[0],(int)seg[1]);
	  g2d.fill(s);
      }
    }

    public void drawDirectedMidPoints(Graphics2D g2d) {
	
	g2d.setStroke(normal);
       
	if (singleLane && lanes[0] != null) {
	    drawLaneDirectedMidPoints(g2d,lanes[0]);
	} else {
	    for (int i=1; i < maxLaneNum; i++) {
		if (lanes[i] != null) drawLaneDirectedMidPoints(g2d,lanes[i]);
	    }
	}
    }

    private void drawLaneDirectedMidPoints(Graphics2D g2d,GeneralPath lane) {
	
	double seg[] = new double[6];
	double angle,x1=0,y1=0,x2=0,y2=0;
	int index = 0;
	Shape s;
	
	for (PathIterator i=lane.getPathIterator(null);!i.isDone();i.next()){
	    int segType = i.currentSegment(seg);    

	    if (index==0) {
		x1 = seg[0] ;y1 = seg[1];
	    } else if (index == 1) {
		x2 = seg[0]; y2 = seg[1];
	    } else if (index > 1) {
		x1=x2;y1=y2;
		x2 = seg[0]; y2 = seg[1];
	    }
	    if (index > 0) {  
		angle = GeoUtils.getAngle(x2,y2,x1,y1);
		s = getPointTriangleAtAngle((x1+x2)/2,(y1+y2)/2,angle);
		
		g2d.setColor(Color.lightGray);
		g2d.fill(s);

		g2d.setColor(Color.black); 
		g2d.draw(s);
		//g2d.setColor(Color.red); 
		//g2d.drawString(""+iD,(int)((x1+x2)/2)-4,(int)((y1+y2)/2)+4);
	    }
	    index++;
	}
    }

    //**********************************************************************
    //*********************** get functions ********************************
    //**********************************************************************

    public Point2D getStartHandle() {
	return GPathUtils.getStartOfPath(lanes[0]);
    }

    public Point2D getEndHandle() {
	return GPathUtils.getEndOfPath(lanes[0]);
    }

    public Point2D getHandle(Point2D point) {
       if (lanes[0] == null) return null;

      double seg[] = new double[6];
      Shape s;
	
      for (PathIterator i=lanes[0].getPathIterator(null);!i.isDone();i.next()){
          int segType = i.currentSegment(seg);
          s = getLPointRectangle((int)seg[0],(int)seg[1]);
          if (s.contains(point)) return (new Point2D.Double(seg[0], seg[1]));
       }
       return null;
    }

    public Point2D getMidPoint(Point2D point) {
	Point2D returnPoint = null;

	for (int i=0;i < maxLaneNum;i++) {
	    if (lanes[i] != null) {
		returnPoint = getMidPointInLane(lanes[i],point);
		if (returnPoint != null) {return returnPoint;}
	    }
	}
	return null;
    }

    /** 
     * returns the mid-point arrow on "lane" that matched "point".
     */
    public Point2D getMidPointInLane(GeneralPath lane,Point2D point) {
		
	double angle;
	double seg[] = new double[6];
	PathIterator pi = lane.getPathIterator(null);
	double x1 = 0;double y1 = 0;
	double x2 = 0;double y2 = 0;
	int index = 0;
	Shape s;
	
	while (!pi.isDone()) {
	    int segType = pi.currentSegment(seg);
	    if (index==0) {
		x1 = seg[0];y1 = seg[1];
	    } else if (index == 1) {
		x2 = seg[0];y2 = seg[1];
	    } else if (index > 1) {
		x1=x2;y1=y2;
		x2 = seg[0];y2 = seg[1];
	    }
	    if (index > 0) {  
		angle = GeoUtils.getAngle(x2,y2,x1,y1);
		s = getPointTriangleAtAngle((x1+x2)/2,(y1+y2)/2,angle);  
		if (s.contains(point)) {
		    return (new Point2D.Double((x1+x2)/2, (y1+y2)/2));
		}
	    }
	    index++;
	    pi.next();
	}
	return null;
    }

    public Line2D getNearestLine(Point2D point) {
	return GPathUtils.getNearestLine(point,lanes[0]);
    }

    public Line2D getLine(int index) {
	return GPathUtils.getLineSect(index,lanes[0]);
    }
    
    public Shape getShape() {
	return roadShape;
    }

    public Shape getRoadSide(int X, int Y) {
	Rectangle2D s = (Rectangle2D)getSPointRectangle(X,Y);
	if (leftSide.intersects(s)) {return leftSide;}
	if (rightSide.intersects(s)) {return rightSide;}
	return null;
    }

    public Shape getRoadSide(boolean wantLeft) {
	if (wantLeft) return leftSide;
	else return rightSide;
    }
    public Shape getRoadLeftSide() {return leftSide;}
    public Shape getRoadRightSide() {return rightSide;}

    public int getID() {return iD;}

    public GeneralPath getLane(int index) {
	for (int i=0; i < maxLaneNum; i++) if (i ==index) return lanes[i];
	System.out.println("invalid index in getLane!");
	return null;
    }

    public Point2D getLaneStartPoint(int index) {
	return GPathUtils.getStartOfPath(getLane(index));
    }

    public Point2D getLaneEndPoint(int index) {
	return GPathUtils.getEndOfPath(getLane(index));
    }

    public GeneralPath[] getLanes() {return lanes;}

    public int getNumLanes() {
	return (singleLane) ? 1 : (getNumArrayElements()-1);
    }

    public double getLastLineAngle() {
	return GeoUtils.getAngle(GPathUtils.getLastLine(lanes[0]));
    }

    public double getFirstLineAngle() {
	return GeoUtils.getAngle(GPathUtils.getFirstLine(lanes[0]));
    }

    public int getNumberOfSections() {
	return GPathUtils.getNumPathSections(lanes[0]);
    }

    public int getRoadWidth() {
	return getNumArrayElements()*(laneWidth+1);
    }

    public int getNumLeftLanes() {
	int num = 0;
        for (int i=1; i <maxLaneNum; i+=2) if (lanes[i] != null) num++;
	return num;
    }

    public int getNumRightLanes() {
	int num = 0;
	for (int i=2; i <maxLaneNum; i+=2) if (lanes[i] != null) num++;
	return num;
    }

    public int getRoadLeftWidth() {
	int num = 0;
        for (int i=1; i <maxLaneNum; i+=2) if (lanes[i] != null) num++;
	//System.out.println("getRoadLeftWidth() "+num);
	//return ((num*(laneWidth+1)) + laneWidth/2);
	return (num==0) ? laneWidth/2 + 2: ((num*(laneWidth+1)) + laneWidth/2);
    }

    public int getRoadRightWidth() {
	int num = 0;
	for (int i=2; i <maxLaneNum; i+=2) if (lanes[i] != null) num++;
	//System.out.println("getRoadRightWidth() "+num);
	//return ((num*(laneWidth+1)) + laneWidth/2);
	return (num==0) ? laneWidth/2 + 2: ((num*(laneWidth+1)) + laneWidth/2);
    }

    private int getInnerLLaneIndex() {
	for (int i=maxLaneNum-2; i >0; i-=2) if (lanes[i] != null) return i;
	return 0;
    }

    private int getInnerRLaneIndex() {
	for (int i=maxLaneNum-1; i >0; i-=2) if (lanes[i] != null) return i;
	return 0;
    }

    //**********************************************************************
    //******************* set functions and ********************************
    //******************* calculate functions ******************************
    //**********************************************************************

    public void setStart(int x, int y){
	//System.out.println("Road " + iD +" started at " + x + "," + y);
	lanes[0] = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
	lanes[0].moveTo(x,y);
    }

    public void setSingleLane(boolean singleLane) {
	this.singleLane = singleLane;
    }

    public void setLanes(GeneralPath[] lanes) {
	this.lanes = lanes;
    }

     public void setCenterPath(GeneralPath l0) {
	lanes[0] = l0;
    }

    public void setRoadShapeVariables() {
	calculateLeftSide();
	calculateRightSide();
	calculateShape();
    }

    public void calculateShape() {
	GeneralPath leftBounds = calculateBoundaryPath(true);
	GeneralPath rightBounds = calculateBoundaryPath(false);

	rightBounds = GPathUtils.reversePath(rightBounds);
	leftBounds = GPathUtils.joinPaths(leftBounds,rightBounds);
	leftBounds.closePath();
	
	roadShape = leftBounds;
    }

    public void calculateLeftSide() {leftSide = calculateRoadSide(true);}
    public void calculateRightSide() {rightSide = calculateRoadSide(false);}

    private Shape calculateRoadSide(boolean wantLeft) {
	BasicStroke stroke1 = new BasicStroke(1,BasicStroke.CAP_BUTT,
					     BasicStroke.JOIN_MITER);
	BasicStroke stroke2 = new BasicStroke(1,BasicStroke.CAP_BUTT,
					      BasicStroke.JOIN_MITER);
	
	Shape outline = stroke1.createStrokedShape(calculateBoundaryPath(wantLeft));
	return stroke2.createStrokedShape(outline);
    }

    public GeneralPath calculateBoundaryPath(boolean wantleft) {

	GeneralPath newPath = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

	double angle1, angle2, biAngle,specialAngle;
	double seg[] = new double[6];
	final PathIterator pi = lanes[0].getPathIterator(null);
	double x1=0,y1=0,x2=0,y2=0,x3=0,y3=0;
	int index=0,disAway,disAway2,laneIndex;

	if (wantleft) {
	    laneIndex = getInnerLLaneIndex();
	    disAway = getDistBetweenLanes(laneIndex) + laneWidth/2 + 2;
	} else {
	    laneIndex = getInnerRLaneIndex();
	    disAway = getDistBetweenLanes(laneIndex) - laneWidth/2 - 2;
	}

	while (!pi.isDone()) {
	    int segType = pi.currentSegment(seg);
	     // Set up 3 points along this path
	    if (index==0)         {x1 = seg[0];y1 = seg[1];
	    } else if (index == 1){x2 = seg[0];y2 = seg[1];
	    } else if (index == 2){x3 = seg[0];y3 = seg[1];
	    } else if (index > 2) {x1=x2;y1=y2;x2=x3;y2=y3;x3=seg[0];y3=seg[1];
	    }
	    // The lines are being set up, move to appropriate place.
	    if (index == 1) {
	       angle1 = GeoUtils.getAngle(x1,y1,x2,y2)+halfPI;
	       newPath.moveTo((int)(x1+disAway*Math.cos(angle1)),
			      (int)(y1-disAway*Math.sin(angle1)));
	    }
	    // Need to end the line segments along the angle bisector.
	    if (index >= 2) {
		angle1 = GeoUtils.getAngle(x2,y2,x1,y1);
		angle2 = GeoUtils.getAngle(x2,y2,x3,y3);
		
		if (angle1 > angle2) biAngle = (angle1+angle2)/2;
		else                 biAngle = (angle1+angle2)/2 - Math.PI;
			
		specialAngle = (angle1+Math.PI) + (Math.PI-biAngle);

		if (singleLane) disAway2 = disAway;
		else {
		    disAway2 = getDistBetweenLanes(laneIndex,specialAngle);
		    if (disAway2 < 0) disAway2-=(laneWidth/2 +2);
		    else disAway2+=(laneWidth/2 + 2);
		}

		newPath.lineTo((int)(x2+disAway2*Math.cos(biAngle)),
			       (int)(y2-disAway2*Math.sin(biAngle)));
	    }
	    index++;
	    pi.next();
	}

	if (index == 1) {  //Path is only a point.
	    newPath.moveTo((int)x1,(int)y1);
	} else if (index == 2) {  //Path only has two points, draw that line.
	    angle1 = GeoUtils.getAngle(x1,y1,x2,y2)+halfPI;
	    newPath.lineTo((int)(x2+disAway*Math.cos(angle1)),
			   (int)(y2-disAway*Math.sin(angle1)));
	} else if (index > 2) { //finish off line.
	    angle1 = GeoUtils.getAngle(x2,y2,x3,y3)+ halfPI;
	    newPath.lineTo((int)(x3+disAway*Math.cos(angle1)),
			   (int)(y3-disAway*Math.sin(angle1)));
	}
	return newPath;
    }

    //**********************************************************************
    //********************** test functions ********************************
    //**********************************************************************

    public boolean isEmpty() {return (getNumArrayElements() == 0);}

    public boolean isEndHandle(Point2D point) {
	return point.equals(lanes[0].getCurrentPoint());
    }

    public boolean isSingleLane() {return (singleLane);}

    public boolean isSymetrical() {
	return (getNumLeftLanes() == getNumRightLanes());
    }

    public boolean isStartHandle(Point2D point) {
	return point.equals(GPathUtils.getStartOfPath(lanes[0]));
    } 

    public boolean isInCenterLane(Point2D point) {
	return (getMidPointInLane(lanes[0],point) != null); 
    }

    public boolean isSameLaneArrangement(Road tempRoad) {
	for (int i=0;i < maxLaneNum;i++) {
	    if (lanes[i] != null && tempRoad.getLane(i) == null) return false;
	    if (lanes[i] == null && tempRoad.getLane(i) != null) return false;
	}
	return true;
    }

    //for all lane start and ends get the closest edge line to it and 
    //snap to that edge.
    public void checkBoundsWith(Line2D l1,Line2D l2,Line2D l3,
				Line2D l4,Rectangle2D boundingBox) {
	//left lanes are odd numbers.
	Point2D stestPoint,etestPoint,sPoint,ePoint,pChange;
	Line2D  sLine, eLine;

	stestPoint = GPathUtils.getStartOfPath(lanes[0]);
	etestPoint = GPathUtils.getEndOfPath(lanes[0]);
	
	for (int i=1; i <maxLaneNum; i++) {
	    if (lanes[i] != null) {
		if (i%2 != 0) {
		    sPoint = GPathUtils.getStartOfPath(lanes[i]);	  
		    ePoint = GPathUtils.getEndOfPath(lanes[i]);
		    sLine  = GPathUtils.getFirstLine(lanes[i]);
		    eLine  = GPathUtils.getLastLine(lanes[i]);
		} else {
		    sPoint = GPathUtils.getEndOfPath(lanes[i]);
		    ePoint = GPathUtils.getStartOfPath(lanes[i]);	
		    sLine  = GPathUtils.getLastLine(lanes[i]);
		    eLine  = GPathUtils.getFirstLine(lanes[i]);
		}

		if (isCloseToEdge(stestPoint,l1,l2,l3,l4)) {
		  if (boundingBox.contains(sPoint)) {
		      pChange = extendToBounds(sPoint,sLine,l1,l2,l3,l4);
		  } else {
		      pChange=clipByBounds(sPoint,sLine,l1,l2,l3,l4);
		  }
		  lanes[i]=GPathUtils.modifyPathPoint(lanes[i],sPoint,pChange);
		}

		if (isCloseToEdge(etestPoint,l1,l2,l3,l4)) {
		  if (boundingBox.contains(ePoint)) {
		      pChange = extendToBounds(ePoint,eLine,l1,l2,l3,l4);
		  } else {
		      pChange=clipByBounds(ePoint,eLine,l1,l2,l3,l4);
		  }
		  lanes[i]=GPathUtils.modifyPathPoint(lanes[i],ePoint,pChange);
		}
	    }
	}
    }

    private boolean isCloseToEdge(Point2D p,Line2D l1,
				  Line2D l2,Line2D l3,Line2D l4) {
	if (l1.ptSegDist(p) < 2) return true;
	if (l2.ptSegDist(p) < 2) return true;
	if (l3.ptSegDist(p) < 2) return true;
	if (l4.ptSegDist(p) < 2) return true;
	return false;
    }

    private Point2D clipByBounds(Point2D p0,Line2D l0,Line2D l1,
				 Line2D l2,Line2D l3,Line2D l4) {
	
	Point2D p1 = GeoUtils.getIntersectPoint(l1,l0,false);
	Point2D p2 = GeoUtils.getIntersectPoint(l2,l0,false);
	Point2D p3 = GeoUtils.getIntersectPoint(l3,l0,false);
	Point2D p4 = GeoUtils.getIntersectPoint(l4,l0,false);
	
	return getClosestPoint(p0,p1,p2,p3,p4);
    }

    //Gives the closest intersection point of l0 with either l1,l2,l3 or l4.
    private Point2D extendToBounds(Point2D p0,Line2D l0,Line2D l1,Line2D l2,
				   Line2D l3,Line2D l4) {
			
	Point2D p1 = GeoUtils.getIntersectPoint(l1,l0,true);
	Point2D p2 = GeoUtils.getIntersectPoint(l2,l0,true);
	Point2D p3 = GeoUtils.getIntersectPoint(l3,l0,true);
	Point2D p4 = GeoUtils.getIntersectPoint(l4,l0,true);

	return getClosestPoint(p0,p1,p2,p3,p4);
    }

    private Point2D getClosestPoint(Point2D p0,Point2D p1,Point2D p2,
				    Point2D p3,Point2D p4) {

	Point2D returnPoint = null;
	double d1,d2,d3,d4,smallDist = 100000000;

	if (p1 != null) {
	    d1 = p0.distance(p1);
	    if (d1 <= smallDist) {smallDist = d1; returnPoint = p1;}
	}
	if (p2 != null) {
	    d2 = p0.distance(p2);
	    if (d2 <= smallDist) {smallDist = d2; returnPoint = p2;}
	}
	if (p3 != null) {
	    d3 = p0.distance(p3);
	    if (d3 <= smallDist) {smallDist = d3; returnPoint = p3;}
	}
	if (p4 != null) {
	    d4 = p0.distance(p4);
	    if (d4 <= smallDist) {smallDist = d4; returnPoint = p4;}
	}
	
	if (returnPoint ==null) System.out.println("returnpoint is null");
	return returnPoint;  
    }

    //**********************************************************************
    //*********************** GeneralPath[] manipulation functions *********
    //**********************************************************************

    public void appendToStartOfRoad(int x, int y) {
	lanes[0] = GPathUtils.appendToStart(x,y,lanes[0]);
	setMultipleLanes(-1);
    }

    public void appendToEndOfRoad(int x, int y) {
	lanes[0].lineTo(x,y);
	setMultipleLanes(-1);
    }

    public boolean checkappendLimits(int x, int y) {

	final double a1 = GeoUtils.getAngle((double)x,(double)y,
					    getStartHandle().getX(),
					    getStartHandle().getY());
	final double a2 = getFirstLineAngle();
	final double angleDiff = Math.toDegrees(GeoUtils.getAngleDiff(a1,a2));
	if (angleDiff <= 175) return true; else return false;
    }

    public boolean checkextendLimits(int x, int y) {
		
	final double a1 = GeoUtils.getAngle(getEndHandle().getX(),
					    getEndHandle().getY(),
					    (double)x,(double)y);
	final double a2 = getLastLineAngle();
	final double angleDiff = Math.toDegrees(GeoUtils.getAngleDiff(a1,a2));
	//System.out.println("a1= "+Math.toDegrees(a1)+
	//	   " a2= "+Math.toDegrees(a2)+
	//	   " angleDiff= "+angleDiff);
	if (angleDiff <= 175) return true; else return false;
    }

    public void reverseDirection() {
	lanes[0] = GPathUtils.reversePath(lanes[0]);
	setMultipleLanes(-1);
    }

    public void modifyRoadHandle(Point2D pToChange, Point2D newP) {
	lanes[0] = GPathUtils.modifyPathPoint(lanes[0],pToChange,newP);
	setMultipleLanes(-1);
    }

    public void modifyRoadHandle(boolean isStartPoint, Point2D newP) {
	if (isStartPoint) {
	    lanes[0] = GPathUtils.modifyPathPoint(lanes[0],
				     GPathUtils.getStartOfPath(lanes[0]),newP);
	} else {
	    lanes[0] = GPathUtils.modifyPathPoint(lanes[0],
				     GPathUtils.getEndOfPath(lanes[0]), newP);
	}
	    
	setMultipleLanes(-1);
    }

    public void removeRoadPoint(Point2D pointToKill) {
	lanes[0] = GPathUtils.removePathPoint(lanes[0],pointToKill);
	if (lanes[0] == null) setEmpty();
	else setMultipleLanes(-1);
    }

    public void mergeRoad(Road roadToMerge) {
	lanes[0] = GPathUtils.mergePaths(lanes[0],roadToMerge.getLane(0));
	setMultipleLanes(-1);
    }


    public void setMultipleLeftLanes(int multipleNum) {
	//left lanes are odd numbers.

	if (multipleNum == 0 && getNumArrayElements() <= 2) {
	    setMultipleLanes(1);
	    return;
	} else if (multipleNum == 1 && getNumArrayElements() <= 2) {
	    setMultipleLanes(3);
	    return;
	}

	multipleNum = 2*multipleNum-1;

	for (int i=1; i < maxLaneNum; i++) {
	    if (i%2!=0) {
		if (i <= multipleNum) {
		    lanes[i] = new GeneralPath();
		    singleLane = false;
		}else lanes[i] = null;
	    }
	}
	setMultipleLanes(-1);
    }

    public void setMultipleRightLanes(int multipleNum) {
	//left lanes are EVEN numbers.

	if (multipleNum == 0 && getNumArrayElements() <= 2) {
	    setMultipleLanes(1);
	    return;
	} else if (multipleNum == 1 && getNumArrayElements() <= 2) {
	    setMultipleLanes(3);
	    return;
	}

	multipleNum = 2*multipleNum;

	for (int i=2; i < maxLaneNum; i++) {
	    if (i%2==0 && i!=0) {
		if (i <= multipleNum) {
		    lanes[i] = new GeneralPath();
		    singleLane = false;
		} else lanes[i] = null;
	    }
	}
	setMultipleLanes(-1);
    }

    public void setMultipleLanes(int numLanes) {
	
	if (numLanes == 1) {
	    for (int i=1; i < maxLaneNum; i++) lanes[i] = null;
	    singleLane = true;
	    return;
	} else if (numLanes == -1) {
	    for (int i=1; i < maxLaneNum; i++) {
		if (lanes[i] != null)
		    lanes[i] = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
	    }
	} else {
	    //multiply road to have "numLanes" lanes.
	    if (numLanes > maxLaneNum) numLanes = maxLaneNum;
	    for (int i=1; i < maxLaneNum; i++) {
		if (i<numLanes) {
		    lanes[i] = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
		} else {
		    lanes[i] = null;
		}
	    }
	    singleLane = false;
	}
	
	double angle1, angle2, biAngle, specialAngle;
	double seg[] = new double[6];
	final PathIterator pi = lanes[0].getPathIterator(null);
	double x1=0,y1=0,x2=0,y2=0,x3=0,y3=0;
	int index = 0; int disAway;
	
	while (!pi.isDone()) {
	    int segType = pi.currentSegment(seg);
	     // Set up 3 points along this path
	    if (index==0) {          x1 = seg[0];y1 = seg[1];
	    } else if (index == 1) { x2 = seg[0];y2 = seg[1];
	    } else if (index == 2) { x3 = seg[0];y3 = seg[1];
	    } else if (index > 2) {
		x1=x2;y1=y2;x2=x3;y2=y3;x3=seg[0];y3=seg[1];
	    }
	    // The lines are being set up, move to appropriate place.
	    if (index == 1) {
		angle1 = GeoUtils.getAngle(x1,y1,x2,y2) + halfPI;;
		for (int i=1; i < maxLaneNum; i++) {	
		    if (lanes[i] != null) {
			disAway = getDistBetweenLanes(i);
			lanes[i].moveTo((int)(x1+disAway*Math.cos(angle1)),
					(int)(y1-disAway*Math.sin(angle1)));
		    }
		}
	    }
	    // Need to end the line segments along the angle bisector.
	    if (index >= 2) {
		//get line 1's backward angle.
		angle1 = GeoUtils.getAngle(x2,y2,x1,y1);
		
		//get line 2's angle
		angle2 = GeoUtils.getAngle(x2,y2,x3,y3);
				
		//get angle bisector, always on the same relative side.
		if (angle1 > angle2) biAngle = (angle1+angle2)/2;
		else                 biAngle = (angle1+angle2)/2 - Math.PI;
		
		specialAngle = (angle1+Math.PI) + (Math.PI-biAngle);
		
		for (int i=1; i < maxLaneNum; i++) {	
		 if (lanes[i] != null) { 
		     disAway = getDistBetweenLanes(i,specialAngle);
		     lanes[i].lineTo((int)(x2+disAway*Math.cos(biAngle)),
				     (int)(y2-disAway*Math.sin(biAngle)));
		 }
		}
	    }
	    index++;
	    pi.next();
	}
	
	if (index == 2) {  //Path only has two points, draw that line.
	    
	    angle1 = GeoUtils.getAngle(x1,y1,x2,y2) + halfPI;
	    
	    for (int i=1; i < maxLaneNum; i++) {
	     if (lanes[i] != null) { 
		 disAway = getDistBetweenLanes(i);
		 lanes[i].lineTo((int)(x2+disAway*Math.cos(angle1)),
				 (int)(y2-disAway*Math.sin(angle1)));
	     }
	    }
	} else if (index > 2) {
	    angle1 = GeoUtils.getAngle(x2,y2,x3,y3) + halfPI;
	    
	    for (int i=1; i < maxLaneNum; i++) {	
		if (lanes[i] != null) { 
		    disAway = getDistBetweenLanes(i);
		    lanes[i].lineTo((int)(x3+disAway*Math.cos(angle1)),
				    (int)(y3-disAway*Math.sin(angle1)));
		}
	    }
	    
	}
	for (int i=1; i < maxLaneNum; i++) {
	    if (lanes[i] != null && i%2 == 0) { 
		lanes[i] = GPathUtils.reversePath(lanes[i]);
	    }
	}
    }

    public Road getSectionFrom(int newID, int index) {
	Road newRoad = this.deepClone(newID);
	GeneralPath l0 = newRoad.getLane(0);
	l0 = GPathUtils.getPathFromIndex(index,l0);
	newRoad.setCenterPath(l0);
	newRoad.setMultipleLanes(-1);
	return newRoad;
    }

    public Road getSectionFrom(int newID,double x, double y) {
      double seg[] = new double[6];
      int index = 0;
	
      Road newRoad = this.deepClone(newID);
      GeneralPath l0 = newRoad.getLane(0);
		
      for (PathIterator i=l0.getPathIterator(null);!i.isDone();i.next()){
	  int segType = i.currentSegment(seg);    
	  if (x == seg[0] && y == seg[1]) {
	      l0 = GPathUtils.getPathFromIndex(index,l0);
	      break;
	  }
	  index++;
      }		
      newRoad.setCenterPath(l0);
      newRoad.setMultipleLanes(-1);
      return newRoad;
    }
    
    public void cutOffAt(int index) {
	lanes[0] = GPathUtils.getPathToIndex(index,lanes[0]);
	setMultipleLanes(-1);
    }

    public void cutOffAt(double x, double y) {
      double seg[] = new double[6];
      int index = 0;
			
      for (PathIterator i=lanes[0].getPathIterator(null);!i.isDone();i.next()){
	  int segType = i.currentSegment(seg);    
	  if (x == seg[0] && y == seg[1]) {
	      lanes[0] = GPathUtils.getPathToIndex(index,lanes[0]);
	      break;
	  }
	  index++;
      }		
      setMultipleLanes(-1);
    }
  
    public Road splitRoad(double x, double y, int newIndex) {
	
	int closestindex=0,index = 0;
	double closestDistance = 10000;
	double angle=0,x1=0,y1=0,x2=0,y2=0;
	double seg[] = new double[6];
			
	for (PathIterator i=lanes[0].getPathIterator(null);!i.isDone();i.next()){
	    int segType = i.currentSegment(seg);    
	    if (index==0) {
		x1 = seg[0] ; y1 = seg[1]; x2 = seg[0]; y2 = seg[1];
	    }
	    if (index > 0) {
		x1=x2;y1=y2; x2 = seg[0]; y2 = seg[1];
		final int ydiff = (int)Math.abs(y2-y1);
		final int xdiff = (int)Math.abs(x2-x1);
		if (ydiff > xdiff) {
		    angle = GeoUtils.getAngle(x1,y1,x2,y2)+Math.PI;
		    System.out.println("YDiff bigger");
		} else {
		    angle = GeoUtils.getAngle(x1,y1,x2,y2);
		    System.out.println("XDiff bigger");
		}
		final double tDist = GeoUtils.getDistance(seg[0],seg[1],x,y);
		if (tDist < closestDistance) {
		    closestindex = index;
		    closestDistance = tDist;
		}
	    }
	    index++;
	}

	int xx1 = (int)(x-30*Math.cos(angle));
	int yy1 = (int)(y-30*Math.sin(angle));
   	int xx2 = (int)(x+30*Math.cos(angle));
        int yy2 = (int)(y+30*Math.sin(angle));
	GeneralPath tP1=null,tP2=null;

	if (closestindex == 0) {
	    tP1 = GPathUtils.getPathToIndex(closestindex,lanes[0]);
	    tP1.lineTo(xx1,yy1);

	    tP2 = GPathUtils.getPathFromIndex(closestindex+1,lanes[0]);
	    tP2 = GPathUtils.appendToStart(xx2,yy2,tP2);
	} else if (closestindex == index-1) {
	    tP1 = GPathUtils.getPathToIndex(closestindex-1,lanes[0]);
	    tP1.lineTo(xx1,yy1);

	    tP2 = GPathUtils.getPathFromIndex(closestindex,lanes[0]);
	    tP2 = GPathUtils.appendToStart(xx2,yy2,tP2);
	} else {
	    tP1 = GPathUtils.getPathToIndex(closestindex-1,lanes[0]);
	    tP1.lineTo(xx1,yy1);

	    tP2 = GPathUtils.getPathFromIndex(closestindex+1,lanes[0]);
	    tP2 = GPathUtils.appendToStart(xx2,yy2,tP2);
	}

	lanes[0] = tP2;
	setMultipleLanes(-1);

	Road newRoad = this.deepClone(newIndex);
	lanes[0] = tP1;
	setMultipleLanes(-1);
	return newRoad;
    }

    //**********************************************************************
    //************************* Usefull Functions **************************
    //**********************************************************************
    
    public Road deepClone(int newID) {
	Road newRoad = new Road(newID);
	newRoad.setLanes(deepClone(lanes));
	newRoad.setSingleLane(singleLane);
	return newRoad;
    }

    public Road deepClone() {
	Road newRoad = new Road(iD);
	newRoad.setLanes(deepClone(lanes));
	newRoad.setSingleLane(singleLane);
	return newRoad;
    }

    /**
     * Copy by value all the array elements of a GeneralPath[]
     */
    private GeneralPath[] deepClone(GeneralPath[] tempA) {
	GeneralPath[] arrayClone = new GeneralPath[maxLaneNum];
	for (int i=0;i < maxLaneNum;i++) {
	    if (tempA[i] != null) arrayClone[i] = new GeneralPath(tempA[i]);
	}
	return arrayClone;
    }

    /**
     * returns the number of initilized elements of a GeneralPath[]
     */
    public int getNumArrayElements() {
	int num = 0;
	for (int i=0; i < maxLaneNum; i++) {
	    if (lanes[i] != null) num++;
	}
	return num;
    }

    private void setEmpty() {
	for (int i=0; i < maxLaneNum; i++) lanes[i] = null;
    }

    /**
     * A function that given an index to an array of GeneralPaths "i",
     * returns the distance the next GeneralPath should be from the centerline
     */
    private int getDistBetweenLanes(int i) {
	if (i==0) return 0;

	else if (i%2 != 0) return ( (i)  *(laneWidth/2+1) + 2);
	else               return (-(i-1)*(laneWidth/2+1) - 2);
    }

    private int getDistBetweenLanes(int i, double a) {
	if (i==0) return 0;

	int diagonalLength = (int) ((double)(laneWidth/2+1)/Math.sin(a));

	if (i%2 != 0) return ((i)*diagonalLength + 2);
	else          return (-(i-1)*diagonalLength - 2);
    }

    private Shape getSPointRectangle(int x, int y) {
	return new Rectangle2D.Double(x - 3,y - 3,6,6);
    }

    private Shape getLPointRectangle(int x, int y) {
	return new Rectangle2D.Double(x - 8,y - 8,16,16);
    }

    /** Create a small triangle with the center at ("x","y") and pointing
     * at an angle of "angle"
     */
    private Shape getPointTriangleAtAngle(double x, double y, double angle) {

      x = GeoUtils.getXPointAtAngle(x,angle,-7.0);
      y = GeoUtils.getYPointAtAngle(y,angle,-7.0);
      final int xx1 = (int)GeoUtils.getXPointAtAngle(x,angle-(Math.PI/8),15.0);
      final int yy1 = (int)GeoUtils.getYPointAtAngle(y,angle-(Math.PI/8),15.0);
      final int xx2 = (int)GeoUtils.getXPointAtAngle(x,angle+(Math.PI/8),15.0);
      final int yy2 = (int)GeoUtils.getYPointAtAngle(y,angle+(Math.PI/8),15.0);
      
      return new Polygon(new int[] {(int)x,xx1,xx2},
			 new int[] {(int)y,yy1,yy2},3);
    }

    //**********************************************************************
    //******************** XML SAVING **************************************
    //**********************************************************************
    public String getXMLName() {return "Road";}

    public XMLElement saveSelf() { 
      XMLElement result = new XMLElement("Road"); 
      double seg[] = new double[6];
      int index = 0;
	
      result.addAttribute(new XMLAttribute("Busyness",busyness));

      String laneStr = "";
      for (int i=0; i < maxLaneNum; i++) {
	  if (lanes[i] != null) laneStr = laneStr+ "1";
	  else laneStr = laneStr+ "0";
      }
      result.addAttribute(new XMLAttribute("Lanes",laneStr));
		
      for (PathIterator i=lanes[0].getPathIterator(null);!i.isDone();i.next()){
	  int segType = i.currentSegment(seg);    
	  result.addAttribute(new XMLAttribute("X"+index,(int)seg[0]));
	  result.addAttribute(new XMLAttribute("Y"+index,(int)seg[1]));
	  index++;
      }
      return result;
    }

    public void saveChilds(XMLSaver saver) { }

    public void loadSelf(XMLElement element) { 
	//System.out.println("In Road loadSelf()");
	int x=0,y=0;
	XMLAttribute[] atts = element.getAttributesArray();
	
	busyness = element.getAttribute("Busyness").getIntValue();

	lanes[0] = new GeneralPath();
	for (int i=2;i < atts.length;i+=2) {
	    x = atts[i].getIntValue();
	    y = atts[i+1].getIntValue();
	    if (i==2) lanes[0].moveTo(x,y);
	    else lanes[0].lineTo(x,y);
	}

	String laneStr = element.getAttribute("Lanes").getStringValue();
	for (int i=1; i < maxLaneNum; i++) {
	    if (laneStr.substring(i,i+1).equals("1")) {
		singleLane = false;
		lanes[i] = new GeneralPath();
	    }
	}
	setMultipleLanes(-1);
    }

    public void loadChilds(XMLLoader loader) { 
	//System.out.println("In Road loadChilds()");
    }

}






