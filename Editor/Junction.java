import java.awt.image.BufferedImage;

import java.awt.Shape;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Area;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Line2D;
import java.awt.BasicStroke;
import java.awt.Image;

import Utils.*;
import XML.*;

public class Junction implements XMLSerializable {

	public static int junctionID=1;
	final static int maxNumRoads = 4;
	final static int NORTH = 2;
	final static int EAST = 4;
	final static int SOUTH = 6;
	final static int WEST = 8;

	final static int GIVE_WAY = 0;
	final static int SIGNALLED = 1;
	final static int BRIDGE_OVER = 2;
	final static int BRIDGE_UNDER = 3;
	final static int TURNOFF = 4;
	final static int ALTSIGNALLED = 5;
	
	final static int JUNCTION_TYPES = 6;

	final static int PRIORITY = 0;
	final static int NO_PRIORITY = 1;

	public int thisJunctionID;

	int laneWidth  = 13;
	TexturePaint junctionTexture;
	Shape s;
	float gridSize;
    
	float[] x = new float[9];
	float[] y = new float[9];
	int[] road = new int[9]; //4 roads attach (not 8) (so some redundant info).
	boolean[] isRoadStart = new boolean[9];
	double radAngle=0;  
	RoadDesigner parent;
	int junctionType = ALTSIGNALLED;
	int[] giveWayPriorities = new int[9];
	int totalTime = 60;
	boolean vehicle_Actuated = false, adaptive = false;
    
	BasicStroke dottedLine,normal,centerLine,thickLine;

	// A Junction is an array of 9 points specified by x[] and y[].
	//          1----2----3
	//          |         |
	//          8    0    4
	//          |         |
	//          7----6----5

	public Junction(float nx, float ny, int gridSize, RoadDesigner parent) {
		thisJunctionID = junctionID++;

		this.parent = parent;
		x[0] = nx;
		y[0] = ny;
		this.gridSize = gridSize;
		junctionTexture = new TexturePaint(getSignalledJunctionTexture(),
																			 new Rectangle2D.Double(0,0,14,14));
		normal = new BasicStroke(1,BasicStroke.CAP_BUTT,
														 BasicStroke.JOIN_BEVEL);
		dottedLine = new BasicStroke(2, BasicStroke.CAP_ROUND,
																 BasicStroke.JOIN_ROUND,1,
																 new float[] {5,5},0);
		thickLine = new BasicStroke(4, BasicStroke.CAP_ROUND,
																BasicStroke.JOIN_ROUND);
		centerLine = new BasicStroke(3, BasicStroke.CAP_BUTT,
																 BasicStroke.JOIN_BEVEL);
	
		for (int i=2; i<9;i+=2) road[i] = -1;
     
		setToSquare();
		setMidPoints();
		setShape();
	}

	static public String getName(int type) {
		switch (type) {
		case SIGNALLED :    return "one-at-a-time Signalled";
		case GIVE_WAY :     return "one-at-a-time Give-way";
		case BRIDGE_OVER :  return "Bridge over";
		case BRIDGE_UNDER : return "Bridge under";
		case ALTSIGNALLED : return "oposite-syncronised Signalled";
		case TURNOFF :      return "Turn-off GiveWay";
		default :           return type+"unspecfied ";
		}
	}

	//**********************************************************************
	//*********************** draw functions *******************************
	//**********************************************************************

	public void drawHandledJunction(Graphics2D g2d) {

		drawSimpleJunction(g2d);

		for (int i=2; i<9;i+=2) {    
	    g2d.setColor(Color.black);
	    g2d.fill(getHandle(x[i],y[i]));
		}
		g2d.drawString(""+junctionType,x[0]-3,y[0]+3);
	}

	public void drawSimpleJunction(Graphics2D g2d) {
		drawSimpleJunction(g2d,x,y,s);
	}

	public void drawSimpleJunction(Graphics2D g2d,float[] x,float[] y,Shape s){

		if (junctionType == SIGNALLED || junctionType == ALTSIGNALLED) {
	    g2d.setPaint(junctionTexture);
	    g2d.fill(s);
	    g2d.setColor(Color.black);
	    g2d.draw(s);
		} else if (junctionType == GIVE_WAY || junctionType == TURNOFF) {

	    g2d.setColor(Color.lightGray);
	    g2d.fill(s);
	    g2d.setColor(Color.black);

	    float x1 = -1,y1 = -1;
	    for (int i=2; i<9;i+=2) {
		
				if (giveWayPriorities[i] == NO_PRIORITY) {
					if (road[i] == -1) {
						g2d.setStroke(normal);
						g2d.drawLine((int)x[i-1],(int)y[i-1],
												 (int)x[(i+1)%8],(int)y[(i+1)%8]);
					} else {
						g2d.setStroke(dottedLine);
						g2d.drawLine((int)x[i-1],(int)y[i-1],
												 (int)x[(i+1)%8],(int)y[(i+1)%8]);
					}
				} else {
					if (x1 == -1) {
						x1 = x[i]; y1 = y[i];
					} else {
						g2d.setColor(Color.yellow); g2d.setStroke(centerLine);
						g2d.drawLine((int)x1,(int)y1,(int)x[i],(int)y[i]);
						g2d.setColor(Color.black);
					}
				}
	    }
	    g2d.setStroke(normal);
		} else { 
	    g2d.setColor(Color.lightGray);
	    g2d.fill(s);
	    g2d.setColor(Color.black);
	    g2d.setStroke(thickLine);
	    for (int i=2; i<9;i+=2) {
				if (giveWayPriorities[i] != PRIORITY) {
					g2d.drawLine((int)x[i-1],(int)y[i-1],
											 (int)x[(i+1)%8],(int)y[(i+1)%8]);
				}
	    }
	    g2d.setStroke(normal);
		}
	}

	//**********************************************************************
	//*********************** get functions ********************************
	//**********************************************************************
  
	/** Create a small square around the given point.*/
	private Shape getHandle(float x, float y) {
		return new Rectangle2D.Float(x - 2,y - 2,4,4);
	}

	/** Create a small square around the given point.*/
	private Shape getLargeHandle(float x, float y) {
		return new Rectangle2D.Float(x - 7,y - 7,14,14);
	}

	public int getID() { return thisJunctionID; }

	public Shape getShape() { return s; }

	public int getType() { return junctionType; }

	public int getPriority(int index) {
		boolean seen = false;
		for (int i=2; i<9;i+=2) {
	    if (giveWayPriorities[i] == PRIORITY) {
				if (seen && index == 1) return i;
				if (index == 0) return i;
				else seen = true;
	    }
		}
		System.out.println("Error returning from getPriority()");
		return -1;
	}

	public Line2D getNorthSide() {
		return (new Line2D.Double(x[1],y[1],x[3],y[3]));
	}
	public Line2D getEastSide() { 
		return (new Line2D.Double(x[3],y[3],x[5],y[5]));
	}
	public Line2D getSouthSide() {
		return (new Line2D.Double(x[5],y[5],x[7],y[7]));
	}
	public Line2D getWestSide() { 
		return (new Line2D.Double(x[7],y[7],x[1],y[1]));
	}

	public int getJunctionSide(Point2D p) {
		for (int i=2; i<9;i+=2) {    
	    if (getHandle(x[i],y[i]).contains(p)) return i;
		}
		System.out.println("No junctionSide Found");
		return -1;
	}

	public Point2D getMidPointsCenter() {
		return GeoUtils.getIntersectPoint(x[2],y[2],x[6],y[6],
																			x[4],y[4],x[8],y[8],false);
	}

	public int getClosestNotEmptySide(Point2D closestPoint) {
		double closestDist = 10000;
		int closestSide = -1;
	
		for (int i=2; i<9;i+=2) {    
	    if (road[i] == -1) {
				Point2D p = new Point2D.Double(x[i],y[i]);
				double dist = closestPoint.distance(p);
				if (dist < closestDist) {
					closestSide = i;
					closestDist = dist;
				}
	    }
		}
		return closestSide; 
	}
    
	//**********************************************************************
	//********************** test functions ********************************
	//**********************************************************************

	public boolean contains(Point2D p) {
		return s.contains(p);
	}

	public boolean intersects(Point2D p) {
		for (int i=2; i<9;i+=2) {    
	    if (getHandle(x[i],y[i]).contains(p)) return true;
		}
		return false;
	}

	public boolean testToAttach(Point2D roadStart,Point2D roadEnd, int roadID){
		
		int oldPos= -1;
	
		//First test wheather to deattach "road" from the junction
		for (int i=2; i<9;i+=2) {
	    if (road[i] == roadID) {
				road[i] = -1;
				oldPos = i; //Save position of where road was attached.
				//System.out.println("de-attached current path from " + i);
	    }
		}
	
		for (int i=2; i<9;i+=2) {
	    // Next get the junction handle.
	    Shape junctionHandle = getLargeHandle(x[i],y[i]);
	    // Now test wheather this road is to be attached.
	    // The place should be empty and if the road was attached before,
	    // it should be attached in the same place again.
	    if (road[i] == -1 && (oldPos == -1 || oldPos == i)) {
				if (junctionHandle.contains(roadStart)) {
					road[i] = roadID;
					isRoadStart[i] = true;
					return true;
				} else if (junctionHandle.contains(roadEnd)) {
					road[i] = roadID;
					isRoadStart[i] = false;
					return true;
				} 
	    }
		}

		//System.out.println("returned false from testToAttachJunction");
		return false;
	}

	//**********************************************************************
	//*********************** set functions ********************************
	//**********************************************************************

	public void attachRoad(int roadID, int side, boolean isStart) {
		road[side] = roadID;
		isRoadStart[side] = isStart;
	}

	public void dettachRoad(int roadID) {
		for (int i=2; i<9;i+=2) {
	    if (road[i] == roadID) road[i] = -1;
		}
	}

	public void updateJunctionLook() {
		modifyJunctionShape();
		setShape();
		rePositionAttachedRoads();

		if (junctionType == GIVE_WAY || junctionType == TURNOFF) {
	    updateGiveWayLook();
		} else if (junctionType == BRIDGE_OVER) {
	    updateBridgeOverLook() ;
		} else if (junctionType == BRIDGE_UNDER) {
	    updateBridgeUnderLook();
		}
	}

	private void updateBridgeOverLook() {
		for (int i=2; i<9;i+=2) giveWayPriorities[i] = NO_PRIORITY;
		giveWayPriorities[NORTH]= PRIORITY;
		giveWayPriorities[SOUTH]= PRIORITY;
	}

	private void updateBridgeUnderLook() {
		for (int i=2; i<9;i+=2) giveWayPriorities[i] = NO_PRIORITY;
		giveWayPriorities[EAST]= PRIORITY;
		giveWayPriorities[WEST]= PRIORITY;
	}

	private void updateGiveWayLook() {
		int max = 0;
		int side = -1;
	
		for (int i=2; i<9;i+=2) {
	    if (road[i] != -1) {
				int laneNum = parent.getRoad(road[i]).getNumLanes();
				if (max < laneNum || 
						(max == laneNum && 
						 (road[GeoUtils.getOppositeDirection(i)]) != -1)) {
					side = i;
					max = laneNum;
				}
	    }
	    giveWayPriorities[i] = NO_PRIORITY;
		}
	
		giveWayPriorities[side]= PRIORITY;
	
		int opNum=-1, leftNum=-1,  rightNum=-1;
		int op = GeoUtils.getOppositeDirection(side);
		int left = GeoUtils.getLeftDirection(side);
		int right = GeoUtils.getRightDirection(side);
	
		if (road[op] != -1) opNum = parent.getRoad(road[op]).getNumLanes();
	
		if (road[left] != -1) 
	    leftNum = parent.getRoad(road[left]).getNumLanes();
	    
		if (road[right] != -1)
	    rightNum = parent.getRoad(road[right]).getNumLanes();
	
		if (opNum >= leftNum && opNum >= rightNum) {
	    if (opNum != -1) giveWayPriorities[op] = PRIORITY;
		} else {
	    if (leftNum >= rightNum) {
				if (leftNum != -1) giveWayPriorities[left] = PRIORITY;
	    } else {
				if (rightNum != -1) giveWayPriorities[right] = PRIORITY;
	    }
		}
	}

	public void snapAttachedRoadsToGrid() {
		for (int i=2; i<9;i+=2) {
	    if (road[i] != -1) {
				if (isRoadStart[i]) parent.getRoad(road[i]).snapStartToGrid();
				else                parent.getRoad(road[i]).snapEndToGrid();
			}
		}
	}

	public void rePositionAttachedRoads() {
		//rotateToMatchOnlyRoad();
		for (int i=2; i<9;i+=2) {
	    if (road[i] != -1) {
				parent.getRoad(road[i]).modifyRoadHandle(isRoadStart[i],
																								 new Point2D.Double(x[i],y[i]));
	    }
		}
	}

	public void updateAttachedRoadShapes() {
		for (int i=2; i<9;i+=2) {
	    if (road[i] != -1) {
				parent.getRoad(road[i]).setRoadShapeVariables();
	    }
		}
	}

	private void setToSquare() {
		x[1] = x[0]-gridSize; y[1] = y[0]-gridSize;
		x[3] = x[0]+gridSize; y[3] = y[0]-gridSize;
		x[5] = x[0]+gridSize; y[5] = y[0]+gridSize;
		x[7] = x[0]-gridSize; y[7] = y[0]+gridSize;
	}

	private void setMidPoints() {
		x[2] = (x[1]+x[3])/2; y[2] = (y[1]+y[3])/2;
		x[4] = (x[3]+x[5])/2; y[4] = (y[3]+y[5])/2;
		x[6] = (x[5]+x[7])/2; y[6] = (y[5]+y[7])/2;
		x[8] = (x[7]+x[1])/2; y[8] = (y[7]+y[1])/2;
	}

	public void setShape() {
		GeneralPath jShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
		jShape.moveTo(x[1],y[1]);
		jShape.lineTo(x[3],y[3]);
		jShape.lineTo(x[5],y[5]);
		jShape.lineTo(x[7],y[7]);
		jShape.closePath();
		s = jShape;
	}

	//**********************************************************************
	//******************** utility functions *******************************
	//**********************************************************************

	private void modifyJunctionShape() {

    // Need to get the 8 following lengths:
    //            2     3       
    //          |----*----|                   1----2----3
    //        9 |         |4                  |         |
    //          *         *                   8    0    4
    //        8 |         |5                  |         |
    //          |----*----|                   7----6----5
    //            7     6

		float[] length = new float[10];

		for (int i=2; i<9;i+=2) {
	    if (road[i] != -1) {
				final Road tempRoad = parent.getRoad(road[i]);
				if (isRoadStart[i]) {
					length[i] = tempRoad.getRoadLeftWidth();
					length[i+1] = tempRoad.getRoadRightWidth();
				} else {
					length[i] = tempRoad.getRoadRightWidth();
					length[i+1] = tempRoad.getRoadLeftWidth();
				}
	    } else {
				length[i] = gridSize;
				length[i+1] = gridSize;
	    }
		}
	
		//get the total side length
		final float length23 = length[2]+length[3];
		final float length45 = length[4]+length[5];
		final float length67 = length[6]+length[7];
		final float length89 = length[8]+length[9];

		final float halfMaxYAxis = Math.max(length23,length67)/2;
		final float halfMaxXAxis = Math.max(length45,length89)/2;

		//balance out the slave ends to be at a midpoint
		if (length45 < length89) length[4] = halfMaxXAxis;
		if (length45 > length89) length[8] = halfMaxXAxis;
		if (length23 < length67) length[2] = halfMaxYAxis;
		if (length23 > length67) length[6] = halfMaxYAxis;

		//set points and mid-points
		x[1] = x[0] - halfMaxYAxis;          y[1] = y[0] - halfMaxXAxis;
		x[3] = x[0] + halfMaxYAxis;          y[3] = y[0] - halfMaxXAxis;
		x[5] = x[0] + halfMaxYAxis;          y[5] = y[0] + halfMaxXAxis;
		x[7] = x[0] - halfMaxYAxis;          y[7] = y[0] + halfMaxXAxis;
	
		x[2] = x[1] + length[2];             y[2] = y[0] - halfMaxXAxis;
		x[4] = x[0] + halfMaxYAxis;          y[4] = y[3] + length[4];
		x[6] = x[5] - length[6];             y[6] = y[0] + halfMaxXAxis;
		x[8] = x[0] - halfMaxYAxis;          y[8] = y[7] - length[8];

		final double oldangle = radAngle;
		rotate(radAngle);
		radAngle = oldangle;
	}
   
	public void rotate(double newAngle) {
	
		radAngle += newAngle; //Update global variable to track current angle.

		AffineTransform t = new AffineTransform();//Build transform object
		t.setToIdentity();                        //Initilize 
		t.translate(x[0],y[0]);                   //Translate to origin
		t.rotate(newAngle);                       //Rotate by the angle
		t.translate(-x[0],-y[0]);                 //Translate back to old position

		GeneralPath pathShape = collapseToPath();	//Get shape from point array
		pathShape.transform(t);                   //Transform the shape
		s = pathShape;                            //Update global shape variable
		extractFromPath(pathShape);               //Set point array from shape
	}

	public void rotateToMatchOnlyRoad() {
		int attachmentCount = 0, roadIndex = -1;
		Road tempRoad = null;
	
		for (int i=2; i<9;i+=2) {
	    if (road[i] != -1) {
				attachmentCount++;
				roadIndex = i;
	    }
		}

		if (attachmentCount == 1) {
	    System.out.println("Junction rotation due to singleroad.");
	    double newAngle;
	    radAngle = 0;
	    tempRoad = parent.getRoad(road[roadIndex]);
	    if (isRoadStart[roadIndex]) newAngle =tempRoad.getFirstLineAngle();
	    else newAngle = tempRoad.getLastLineAngle();
	    
	    rotate(-newAngle);
		}
	}
    
	private GeneralPath collapseToPath() {

		GeneralPath jShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
	
		jShape.moveTo(x[1],y[1]);
		for (int i=2;i<9;i++) jShape.lineTo(x[i],y[i]);
		jShape.closePath();
		return jShape;
	}
   
	private void extractFromPath(GeneralPath path) {

		float coOrds[] = new float[6];
		int index = 1;

		for (PathIterator i=path.getPathIterator(null);!i.isDone();i.next()){
	    int segType = i.currentSegment(coOrds);
	    if (index <9) {
				x[index]= coOrds[0]; 
				y[index++] = coOrds[1];
	    }
		} 
	}

	public void changeLocation(float newX, float newY) {

		//Move to new location
		final float xToChange = newX-x[0];
		final float yToChange = newY-y[0];

		x[1]+=xToChange; y[1]+=yToChange;
		x[3]+=xToChange; y[3]+=yToChange;
		x[5]+=xToChange; y[5]+=yToChange;
		x[7]+=xToChange; y[7]+=yToChange;

		x[0] = newX;
		y[0] = newY;

		modifyJunctionShape();

		setShape();
		rePositionAttachedRoads();
	}

	public void changeType() {
		junctionType = (junctionType + 1)%JUNCTION_TYPES;
	}

	public BufferedImage getSignalledJunctionTexture() {
		Image offscreen = parent.createImage(14,14);
		Graphics2D offgraphics = (Graphics2D)offscreen.getGraphics();
		BasicStroke yellowPaint = new BasicStroke(2, BasicStroke.CAP_BUTT,
																							BasicStroke.JOIN_BEVEL);
		offgraphics.setColor(Color.lightGray);
		offgraphics.fillRect(0, 0, 14, 14);	
	
		offgraphics.setColor(Color.yellow);
		offgraphics.setStroke(yellowPaint); 
	
		offgraphics.drawLine(0,0,14,0);
		offgraphics.drawLine(0,7,14,7);
		offgraphics.drawLine(0,14,14,14);
	
		offgraphics.drawLine(0,0,0,14);
		offgraphics.drawLine(7,0,7,14);
		offgraphics.drawLine(14,0,14,14);
	
		return (BufferedImage)offscreen;
	}

	//**********************************************************************
	//******************** XML SAVING **************************************
	//**********************************************************************
	public String getXMLName() {return "Junction";}

	public XMLElement saveSelf() { 
		XMLElement result = new XMLElement("Junction"); 

		result.addAttribute(new XMLAttribute("junctionType",junctionType));
		result.addAttribute(new XMLAttribute("radAngle",radAngle));
		result.addAttribute(new XMLAttribute("Actuated",vehicle_Actuated));
		result.addAttribute(new XMLAttribute("Adaptive",adaptive));

		for (int i=0; i < 9; i++) {
			result.addAttribute(new XMLAttribute("x"+i,x[i]));
			result.addAttribute(new XMLAttribute("y"+i,y[i]));
		}
		return result;
	}

	public void saveChilds(XMLSaver saver) { }
 
	public void loadSelf(XMLElement element) { 
	
		junctionType = element.getAttribute("junctionType").getIntValue();
		radAngle = element.getAttribute("radAngle").getDoubleValue();
		vehicle_Actuated = element.getAttribute("Actuated").getBooleanValue();
		adaptive = element.getAttribute("Adaptive").getBooleanValue();

		for (int i=0; i < 9; i++) {
			x[i] = element.getAttribute("x"+i).getFloatValue();
			y[i] = element.getAttribute("y"+i).getFloatValue();
		}
	}

	public void loadChilds(XMLLoader loader) { 
		//System.out.println("In Junction loadChilds()");
	}

	//**********************************************************************
	//******************** GeneralPath operations **************************
	//**********************************************************************

	public int getFirstIndex(GeneralPath p) {
		final Line2D nSide = getNorthSide();
		final Line2D eSide = getEastSide();
		final Line2D sSide = getSouthSide();
		final Line2D wSide = getWestSide();
		int index = 0;
		double x1=0,y1=0,x2=0,y2=0;
		double seg[] = new double[6];
	
		for (PathIterator i=p.getPathIterator(null);!i.isDone();i.next()) {
	    int segType = i.currentSegment(seg); 
	    
	    if (index==0) { 
				x1=seg[0];y1=seg[1]; x2=x1;y2=y1;
	    } 
	    if (index > 0) {
				x1=x2;y1=y2; x2=seg[0];y2=seg[1];

				if (GeoUtils.getIntersectPoint(x1,y1,x2,y2,nSide,false)!=null)
					return index-1;
				if (GeoUtils.getIntersectPoint(x1,y1,x2,y2,eSide,false)!=null)
					return index-1;
				if (GeoUtils.getIntersectPoint(x1,y1,x2,y2,sSide,false)!=null)
					return index-1;
				if (GeoUtils.getIntersectPoint(x1,y1,x2,y2,wSide,false)!=null)
					return index-1;
	    }
	    index++;
		}
		System.out.println("returning Error in getFirstIndex()");
		return -1;
	}

	public int getFirstDir(GeneralPath path, int index) {
		final Line2D nSide = getNorthSide();
		final Line2D eSide = getEastSide();
		final Line2D sSide = getSouthSide();
		final Line2D wSide = getWestSide();
		final Line2D line = GPathUtils.getLineSect(index,path);
		final Point2D npoint = GeoUtils.getIntersectPoint(line,nSide,false);
		final Point2D epoint = GeoUtils.getIntersectPoint(line,eSide,false);
		final Point2D spoint = GeoUtils.getIntersectPoint(line,sSide,false);
		final Point2D wpoint = GeoUtils.getIntersectPoint(line,wSide,false);
		final Point2D point = GPathUtils.getIndexPoint(index,path);
		double minDist = 10000;
		int dir = -1;

		if (npoint != null) {
	    minDist = point.distance(npoint); dir = Junction.NORTH;
		}
		if (epoint != null) {
	    final double eDist = point.distance(epoint);
	    if (eDist < minDist) {minDist = eDist; dir = Junction.EAST; }
		}
		if (spoint != null) {
	    final double sDist = point.distance(spoint);
	    if (sDist < minDist) {minDist = sDist; dir = Junction.SOUTH; }
		}
		if (wpoint != null) {
	    final double wDist = point.distance(wpoint);
	    if (wDist < minDist) {minDist = wDist; dir = Junction.WEST; }
		}
		return dir;
	}

	public Point2D getFirstIntersection(GeneralPath path, int index) {
		final Line2D nSide = getNorthSide();
		final Line2D eSide = getEastSide();
		final Line2D sSide = getSouthSide();
		final Line2D wSide = getWestSide();
		final Line2D line = GPathUtils.getLineSect(index,path);
		final Point2D npoint = GeoUtils.getIntersectPoint(line,nSide,false);
		final Point2D epoint = GeoUtils.getIntersectPoint(line,eSide,false);
		final Point2D spoint = GeoUtils.getIntersectPoint(line,sSide,false);
		final Point2D wpoint = GeoUtils.getIntersectPoint(line,wSide,false);
		final Point2D point = GPathUtils.getIndexPoint(index,path);

		double minDist = 10000;
		Point2D closestPoint=null;

		if (npoint != null) {
	    minDist = point.distance(npoint); closestPoint = npoint;
		}
		if (epoint != null) {
	    final double eDist = point.distance(epoint);
	    if (eDist < minDist) {minDist = eDist; closestPoint = epoint; }
		}
		if (spoint != null) {
	    final double sDist = point.distance(spoint);
	    if (sDist < minDist) {minDist = sDist; closestPoint = spoint; }
		}
		if (wpoint != null) {
	    final double wDist = point.distance(wpoint);
	    if (wDist < minDist) {minDist = wDist; closestPoint = wpoint; }
		}
		return closestPoint;
	}
    
	public int getFirstNonEmptyDir(GeneralPath path, int index) {
	
		Point2D closestPoint= getFirstIntersection(path,index);
		if (closestPoint == null) return -1;
		else return getClosestNotEmptySide(closestPoint);
	}

	public int getLastIndex(GeneralPath p, int pos) {
		final Line2D nSide = getNorthSide();
		final Line2D eSide = getEastSide();
		final Line2D sSide = getSouthSide();
		final Line2D wSide = getWestSide();
		int index = 0,count=0;
		double x1=0,y1=0,x2=0,y2=0;
		double seg[] = new double[6];
	
		for (PathIterator i=p.getPathIterator(null);!i.isDone();i.next()) {
			int segType = i.currentSegment(seg); 
	 
			if (index==0) { 
				x1=seg[0];y1=seg[1]; x2=x1;y2=y1;
			} 
			if (index > 0) {
				x1=x2;y1=y2; x2=seg[0];y2=seg[1];
			}
			if (index-1 == pos) {
				final Line2D line = new Line2D.Double(x1,y1,x2,y2);
				final Point2D nPoint = GeoUtils.getIntersectPoint(line,nSide,false);
				final Point2D ePoint = GeoUtils.getIntersectPoint(line,eSide,false);
				final Point2D sPoint = GeoUtils.getIntersectPoint(line,sSide,false);
				final Point2D wPoint = GeoUtils.getIntersectPoint(line,wSide,false);
				if (nPoint != null) count++;
				if (ePoint != null) count++;
				if (sPoint != null) count++;
				if (wPoint != null) count++;
				if (count == 2) return pos;
			}
			if (index-1 > pos) {
				if (GeoUtils.getIntersectPoint(x1,y1,x2,y2,nSide,false)!=null)
					return index-1;
				if (GeoUtils.getIntersectPoint(x1,y1,x2,y2,eSide,false)!=null)
					return index-1;
				if (GeoUtils.getIntersectPoint(x1,y1,x2,y2,sSide,false)!=null)
					return index-1;
				if (GeoUtils.getIntersectPoint(x1,y1,x2,y2,wSide,false)!=null)
					return index-1;
			}
			index++;
		}
		System.out.println("Seems to be a T-junction, pos= "+pos);
		return -1;
	}

	public int getLastDir(GeneralPath path, int index1,int index2) {

		if (index2 == -1) return -1;

		final Line2D line = GPathUtils.getLineSect(index2,path);
		final Line2D nSide = getNorthSide();
		final Line2D eSide = getEastSide();
		final Line2D sSide = getSouthSide();
		final Line2D wSide = getWestSide();

		if (index2 > index1) {
	    
	    if (GeoUtils.getIntersectPoint(line,nSide,false) != null)
				return Junction.NORTH;
	    if (GeoUtils.getIntersectPoint(line,eSide,false) != null)
				return Junction.EAST;
	    if (GeoUtils.getIntersectPoint(line,sSide,false) != null)
				return Junction.SOUTH;
	    if (GeoUtils.getIntersectPoint(line,wSide,false) != null)
				return Junction.WEST;
	    
	    System.out.println("returning Error in getLastDir()");
	    return -1;
		}
		final Point2D npoint = GeoUtils.getIntersectPoint(line,nSide,false);
		final Point2D epoint = GeoUtils.getIntersectPoint(line,eSide,false);
		final Point2D spoint = GeoUtils.getIntersectPoint(line,sSide,false);
		final Point2D wpoint = GeoUtils.getIntersectPoint(line,wSide,false);
		final Point2D point = GPathUtils.getIndexPoint(index2,path);
		double maxDist = 0;
		int dir = -1;

		if (npoint != null) {
	    maxDist = point.distance(npoint); dir = Junction.NORTH;
		}
		if (epoint != null) {
	    final double eDist = point.distance(epoint);
	    if (eDist > maxDist) {maxDist = eDist; dir = Junction.EAST; }
		}
		if (spoint != null) {
	    final double sDist = point.distance(spoint);
	    if (sDist > maxDist) {maxDist = sDist; dir = Junction.SOUTH; }
		}
		if (wpoint != null) {
	    final double wDist = point.distance(wpoint);
	    if (wDist > maxDist) {maxDist = wDist; dir = Junction.WEST; }
		}
		return dir;
	}

	public  Point2D getLastIntersection(GeneralPath path, int index1, 
																			int index2) {

		if (index2 == -1) return null;

		final Line2D line = GPathUtils.getLineSect(index2,path);
		final Line2D nSide = getNorthSide();
		final Line2D eSide = getEastSide();
		final Line2D sSide = getSouthSide();
		final Line2D wSide = getWestSide();
		final Point2D npoint = GeoUtils.getIntersectPoint(line,nSide,false);
		final Point2D epoint = GeoUtils.getIntersectPoint(line,eSide,false);
		final Point2D spoint = GeoUtils.getIntersectPoint(line,sSide,false);
		final Point2D wpoint = GeoUtils.getIntersectPoint(line,wSide,false);
	
		if (index2 > index1) {
	    if (npoint != null) return npoint;	 
	    if (epoint != null) return epoint;	   
	    if (spoint != null) return spoint;	   
	    if (wpoint != null) return wpoint;	   
	    System.out.println("Error in getLastIntersection()");
		}
		final Point2D point = GPathUtils.getIndexPoint(index2,path);
		double maxDist = 0;
		Point2D furthestPoint=null;

		if (npoint != null) {
	    maxDist = point.distance(npoint); furthestPoint=npoint;
		}
		if (epoint != null) {
	    final double eDist = point.distance(epoint);
	    if (eDist > maxDist) {maxDist = eDist; furthestPoint=epoint;}
		}
		if (spoint != null) {
	    final double sDist = point.distance(spoint);
	    if (sDist > maxDist) {maxDist = sDist; furthestPoint=spoint;}
		}
		if (wpoint != null) {
	    final double wDist = point.distance(wpoint);
	    if (wDist > maxDist) {maxDist = wDist; furthestPoint=wpoint;}
		}
		return furthestPoint;
	}

	public int getLastNonEmptyDir(GeneralPath path, int index1,int index2) {
	
		Point2D closestPoint= getLastIntersection(path,index1,index2);
		if (closestPoint == null) return -1;
		else return getClosestNotEmptySide(closestPoint);
	}


}







