import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.Shape;
import java.awt.BasicStroke;
import java.awt.Stroke;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Polygon;
import java.awt.Image;
import java.awt.Point;
import java.awt.TexturePaint;
import java.awt.Font;

import java.awt.image.BufferedImage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import java.awt.geom.PathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
import java.awt.geom.Area;

import java.util.Vector;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.ImageIcon;

import Utils.*;
import XML.*;

public class RoadDesigner extends JPanel
	implements MouseListener, MouseMotionListener, 
						 ComponentListener, XMLSerializable {

	//Drawing modes
	public final static int APPEND_TO_START_OF_ROAD = 1;
	public final static int APPEND_TO_END_OF_ROAD = 2;
	public final static int CLICKED_ON_HANDLE = 3;
	public final static int DRAG_ROAD_HANDLE = 4;
	public final static int CLICKED_ON_JUNCTION = 5;
	public final static int DRAG_JUNCTION = 6;
	public final static int DONT_DRAW = 7;
	public final static int NO_MODE = 8;
	public final static int DRAW_INITIAL_ROAD = 9;  
	public final static int DRAG_LEFT_ROAD_EDGE = 10;
	public final static int DRAG_RIGHT_ROAD_EDGE = 11;
	public final static int DRAG_ROAD_MIDDLE = 12;
	private int drawMode;
	public final static int SELECT = 12;
	public final static int DRAW_ROAD = 13;
	public final static int DELETE = 14;
	public final static int DRAW_JUNCTION = 15;
	public final static int ORIENTATION = 16;
	private int selectedButton;
    
	//Road Drawing variables
	protected BasicStroke normal;
	private TexturePaint unknownJunction;

	//Graphics variables
	private Graphics2D backBufferG2D, offScreenG2D, gridGraphics;
	private BufferedImage backBufferImage, offScreenImage, gridImage;
	Image backgroundImage = null;
	int width, height;
	private static boolean firstDrawTime = true;
	private Rectangle drawingBounds,validDrawingArea;
	private Area clipRect;
            
	//State variables
	private Point2D savedPoint,currentHandle,currentMidPoint;
	private Road currentRoad;
	private Junction currentJunction;
	private Line2D nearestLine;
	private Popup popUpBox;

	//Mouse variables    
	private int savedSnapX=0,savedSnapY=0,snapX,snapY,x,y;
	private Point2D currentSnapPoint,currentXYPoint;
	private Shape currentRoadSide;
    
	//General variables
	public final static int gridSize = 8;
	private boolean validity = true;
	private static int roadIndex = 0;
	private Vector roads, junctions;

	protected Main parent;
	/*
	public RoadDesigner(Main parent) {
		new RoadDesigner(parent,600,600);
	}
	*/
	public RoadDesigner(Main parent) {
		super();
		this.parent = parent;
		this.width = 600;
		this.height = 600;
	
		roads = new Vector();
		junctions = new Vector();
		drawMode = DONT_DRAW;
		selectedButton = DRAW_ROAD;

		normal = new BasicStroke(1,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL);
		popUpBox = null;

		setBackground(Color.white);

		addMouseMotionListener(this);
		addMouseListener(this);
		addComponentListener(this);
	}
    
	private void setGraphics () {
			
		Dimension dim = getSize();
		width = dim.width;
		height = dim.height;

		offScreenImage = (BufferedImage)createImage(width, height);
		offScreenG2D = (Graphics2D)offScreenImage.createGraphics();
		offScreenG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
																	RenderingHints.VALUE_ANTIALIAS_ON);

		backBufferImage = (BufferedImage)createImage(width, height);
		backBufferG2D = (Graphics2D)backBufferImage.createGraphics();
		backBufferG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
																	 RenderingHints.VALUE_ANTIALIAS_ON);

		unknownJunction = new TexturePaint(getUnknownJunctionTexture(), 
																			 new Rectangle2D.Double(0,0,14,14));
		
		drawingBounds = 
			new Rectangle(gridSize,gridSize,
										((width-gridSize*2)/gridSize)*gridSize,
										((height-gridSize*2)/gridSize)*gridSize);
		clipRect = new Area(new Rectangle((int)drawingBounds.getX()-1,
																			(int)drawingBounds.getY()-1,
																			(int)drawingBounds.getWidth()+2,
																			(int)drawingBounds.getHeight()+2));
		validDrawingArea = new Rectangle((int)drawingBounds.getX()+1,
																		 (int)drawingBounds.getY()+1,
																		 (int)drawingBounds.getWidth()-2,
																		 (int)drawingBounds.getHeight()-2);

		setBackDrop();
		setBackBuffer(backBufferG2D);
	}

	public void setBackDrop() {
			
		if (backgroundImage == null) {
	    gridImage = (BufferedImage)createImage(width,height);
		} else {
	    backgroundImage = 
				backgroundImage.getScaledInstance(width,height,
																					java.awt.Image.SCALE_SMOOTH);
	    gridImage = toBufferedImage(backgroundImage);
		}
		gridGraphics = (Graphics2D)gridImage.createGraphics();
		
		if (backgroundImage == null) {
	    gridGraphics.setColor(Color.white);
	    gridGraphics.fillRect(0,0,width,height);
		} 
		gridGraphics.setColor(Color.black);
		gridGraphics.draw(drawingBounds);
		gridGraphics.setColor(Color.lightGray);
		for (int i=0; i < width; i+=gridSize) {
	    for (int j=0; j < height; j+=gridSize) {
				gridGraphics.drawRect(i, j, 1, 1);
	    }            
		}
	}

	public Dimension getPreferredSize() {
		return new Dimension(width,height);
	}

	//**********************************************************************
	//************************** Mouse Events ******************************
	//**********************************************************************

	public void mousePressed(MouseEvent e) {
		snapTo(e);

		
		//if (e.getButton() == MouseEvent.BUTTON3) { //Java 1.4
		if ((e.getModifiers() & e.BUTTON3_MASK)== e.BUTTON3_MASK) {

	    if (currentRoad != null) {
				roads.addElement(currentRoad);
				currentRoad = null;
	    }

	    if (currentJunction != null) {
				junctions.addElement(currentJunction);
				currentJunction = null;
	    }

	    drawMode = DONT_DRAW;

	    if (setNearestInputRoad()) {
				System.out.println("Road Input");
				drawMode = NO_MODE;
				if (popUpBox != null) popUpBox.dispose();
				popUpBox = new InputPopup(currentRoad,x,y);

	    } else if (setJunction(currentXYPoint)) {
				System.out.println("Right-Clicked on a Junction");
				if (popUpBox != null) popUpBox.dispose();
				popUpBox = new JunctionPopup(currentJunction,x,y);
	    }

	    setBackBuffer(backBufferG2D);
	    repaint();
	    return;
		}

		switch(selectedButton) {
		case SELECT:
	    if (setJunction(currentXYPoint)) {
				//System.out.println("Clicked On a junction");
				drawMode = CLICKED_ON_JUNCTION;
	    } else if (setHandle(currentSnapPoint)) {
				drawMode = CLICKED_ON_HANDLE;
	    }
	    break;
		case DELETE:
	    if (setJunction(currentXYPoint)) {
				currentJunction.snapAttachedRoadsToGrid();
				currentJunction = null;
	    } else if (setHandle(currentSnapPoint)) {
				currentRoad.removeRoadPoint(currentHandle);
				if (currentRoad.getNumberOfSections() < 1) {
					dettachJunction(currentRoad.getID());
					currentRoad = null;
					drawMode = DONT_DRAW;
				} else {
					testToJoinRoads();
					drawMode = NO_MODE;
				}
				currentHandle = null;
	    }
	    break;
		case DRAW_ROAD:
	    if (drawMode == APPEND_TO_START_OF_ROAD) {
				//System.out.println("APPEND_TO_START_OF_ROAD.");
	    } else if (drawMode == APPEND_TO_END_OF_ROAD) {
				//System.out.println("APPEND_TO_END_OF_ROAD.");
	    } else if (setHandle(currentSnapPoint)) {
				//System.out.println("mouseDown on a road handle.");
				drawMode = CLICKED_ON_HANDLE;
	    } else if (setRoad(x,y) && 
								 currentRoad.isInCenterLane(currentXYPoint)) {
				//System.out.println("mouseDown on road center mid-point.");
				savedPoint = currentXYPoint;
				drawMode = DRAG_ROAD_MIDDLE;
	    } else if (setRoadEdge(x,y,true)) {
				//System.out.println("mouseDown on the leftroad side.");
				nearestLine = currentRoad.getNearestLine(currentXYPoint);
				drawMode = DRAG_LEFT_ROAD_EDGE;
	    } else if (setRoadEdge(x,y,false)) {
				//System.out.println("mouseDown on the right road side.");
				nearestLine = currentRoad.getNearestLine(currentXYPoint);
				drawMode = DRAG_RIGHT_ROAD_EDGE;
	    } else if (!setRoad(x,y)) {
				//System.out.println("mouseDown on empty space");
				saveCurrentRoad();
				currentRoad = new Road(roadIndex++);
				currentRoad.setStart(snapX,snapY);
				drawMode = DRAW_INITIAL_ROAD;
	    }
	    break;
		case DRAW_JUNCTION:
	    if (setJunction(currentXYPoint)) {
				//System.out.println("mouseDown on a junction");
				drawMode = CLICKED_ON_JUNCTION;
	    } else if (setRoad(x,y)) {
				//Systsem.out.println("mouseDown on a road");
				placeJunctionInRoads();
				drawMode = NO_MODE;
	    } else {
				//System.out.println("mouseDown on empty space");
				saveCurrentRoad();
				currentRoad = null;
				drawMode = NO_MODE;
				if (currentJunction != null) junctions.add(currentJunction);
				Junction newJc = new Junction(snapX,snapY,gridSize,this);
				currentJunction = newJc;
	    }
	    break;
		case ORIENTATION:
	    if (setMidPoint(currentXYPoint)) {
				//System.out.println("MouseDown on a lane mid-point arrow.");
				currentRoad.reverseDirection();
				testToJoinRoads();
				drawMode = NO_MODE;
	    } else if (setJunction(currentXYPoint)) {
				//System.out.println("MouseDown for junction orientation.");
				savedPoint = currentXYPoint;
				drawMode = CLICKED_ON_JUNCTION;
	    }
	    break;
		}

		setBackBuffer(backBufferG2D);
		repaint();
	}

	public void mouseReleased(MouseEvent e) {
		snapTo(e);

		//if (e.getButton() == MouseEvent.BUTTON3) return; //Java 1.4
		if ((e.getModifiers() & e.BUTTON3_MASK)== e.BUTTON3_MASK) return;

		switch(selectedButton) {
		case SELECT:
	    if (drawMode == DRAG_ROAD_HANDLE) {
				currentRoad.modifyRoadHandle(currentHandle,currentSnapPoint);
				testToJoinRoads();
				updateAllJunctionLooks();
				currentRoad.setRoadShapeVariables();
	    } else if (drawMode == DRAG_JUNCTION) {
				currentJunction.changeLocation(snapX,snapY);
				currentJunction.updateAttachedRoadShapes();
				currentJunction.changeLocation(snapX,snapY);
				if (currentJunction != null) {
					testIfNeedNewRoadAttachments();
					updateAllJunctionLooks();
					currentJunction.updateAttachedRoadShapes();
				}
	    }
	    drawMode = NO_MODE;
	    break;
		case DRAW_ROAD:
	    if (drawMode == CLICKED_ON_HANDLE) {
				if (currentRoad.isStartHandle(currentHandle)) {
					drawMode = APPEND_TO_START_OF_ROAD;
				} else if (currentRoad.isEndHandle(currentHandle)) {
					drawMode = APPEND_TO_END_OF_ROAD;
				}
				break;
	    } else if (drawMode == DRAW_INITIAL_ROAD) {
				if (!currentRoad.isEndHandle(currentSnapPoint)) {
					currentRoad.appendToEndOfRoad(snapX,snapY);
				} else {
					currentRoad = null;
					drawMode = DONT_DRAW;
					break;
				}
	    } else if (drawMode == APPEND_TO_START_OF_ROAD) {
				currentRoad.appendToStartOfRoad(snapX,snapY);
	    } else if (drawMode == APPEND_TO_END_OF_ROAD) {
				currentRoad.appendToEndOfRoad(snapX,snapY);
	    } else if (drawMode == DRAG_ROAD_HANDLE) {
				currentRoad.modifyRoadHandle(currentHandle,currentSnapPoint);
	    } else if (drawMode == DRAG_LEFT_ROAD_EDGE) {
				nearestLine = null;
	    } else if (drawMode == DRAG_RIGHT_ROAD_EDGE) {
				nearestLine = null;
	    } else if (drawMode == DRAG_ROAD_MIDDLE) {
	    }
	    testToJoinRoads();
	    updateAllJunctionLooks();
	    currentRoad.setRoadShapeVariables();
	    drawMode = NO_MODE;
	    break;
		case DRAW_JUNCTION:
	    if (drawMode == DRAG_JUNCTION) {
				currentJunction.changeLocation(snapX,snapY);
				currentJunction.updateAttachedRoadShapes();
				drawMode = NO_MODE;
	    }
	    if (drawMode == CLICKED_ON_JUNCTION) {
				if (currentJunction != null) currentJunction.changeType();
	    }
	    if (currentJunction != null) {
				testIfNeedNewRoadAttachments();
				updateAllJunctionLooks();
				currentJunction.updateAttachedRoadShapes();
	    }
	    break;
		case ORIENTATION:
	    drawMode = NO_MODE;
	    break;
		}

		parent.canMoveOn(testForValidity());
		setBackBuffer(backBufferG2D);
		repaint();
	}
    
	public void mouseDragged(MouseEvent e) {
		snapTo(e);
	
		switch(selectedButton) {
		case SELECT:
	    if (drawMode == CLICKED_ON_HANDLE || drawMode == DRAG_ROAD_HANDLE){
				drawMode = DRAG_ROAD_HANDLE;
	    } else if (drawMode == CLICKED_ON_JUNCTION) {
				drawMode = DRAG_JUNCTION;
	    }
	    break; 
		case DRAW_ROAD:
	    if (drawMode == CLICKED_ON_HANDLE || drawMode == DRAG_ROAD_HANDLE){
				drawMode = DRAG_ROAD_HANDLE;
	    } else if (drawMode == DRAG_LEFT_ROAD_EDGE ||
								 drawMode == DRAG_RIGHT_ROAD_EDGE) {
 
				int distance = (int)nearestLine.ptLineDist(currentXYPoint);
				distance /= Road.laneWidth;	
				//System.out.println("distance= "+distance);
		
				if (drawMode == DRAG_LEFT_ROAD_EDGE) {
					currentRoad.setMultipleLeftLanes(distance);
				} else if (drawMode == DRAG_RIGHT_ROAD_EDGE) {
					currentRoad.setMultipleRightLanes(distance);
				} 
	    } else if (drawMode == DRAG_ROAD_MIDDLE) {
				int distance = (int)savedPoint.distance(currentXYPoint);
				distance /= 5; distance++;
				if (distance%2 == 0) distance++; //must be odd

				currentRoad.setMultipleLanes(distance);
	    }
	    break; 
		case DRAW_JUNCTION:
	    if (drawMode == CLICKED_ON_JUNCTION || drawMode == DRAG_JUNCTION) {
				drawMode = DRAG_JUNCTION;
	    }
	    break;
		case ORIENTATION:
	    if (drawMode == CLICKED_ON_JUNCTION) {
				if (y < savedPoint.getY()) {
					currentJunction.rotate(Math.toRadians(-2));
				} else {
					currentJunction.rotate(Math.toRadians(2));
				}
				currentJunction.rePositionAttachedRoads();
				currentJunction.updateAttachedRoadShapes();
				savedPoint = currentXYPoint;
	    }
			//default: System.out.println("No known draw Mode in mouseDragged");
		}
		
		repaint();
	}
	
	public void mouseMoved(MouseEvent e) {
		snapTo(e);
		
		//High-light features to help the user navigate.
		if (selectedButton == DRAW_ROAD &&
				drawMode != APPEND_TO_START_OF_ROAD &&
				drawMode != APPEND_TO_END_OF_ROAD) {
			if (getCurrentHandle()) {}
			else if (getcurrentMidPoint()) {}
			else if (getcurrentRoadSide()) {}
		}
		
		repaint();
	}
	
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void componentHidden(ComponentEvent e) {}
	public void componentMoved(ComponentEvent e) {}
	public void componentResized(ComponentEvent e) {firstDrawTime = true;}
	public void componentShown(ComponentEvent e) {}

	//**********************************************************************
	//************************** Paint *************************************
	//**********************************************************************

	/** Uses doublebuffering to repaint the Drawpanel. */
	public void paint(Graphics g) { 
		//System.out.print("*");
	
		if (firstDrawTime) {setGraphics();firstDrawTime=false;}
	
		if (currentRoad != null &&
				(drawMode == APPEND_TO_START_OF_ROAD && 
				 currentRoad.checkappendLimits(snapX,snapY))) {

	    offScreenG2D.drawImage(backBufferImage,0,0,this);
	    Road extendedRoad = currentRoad.deepClone();
	    extendedRoad.appendToStartOfRoad(snapX,snapY);
	    extendedRoad.drawRoad(offScreenG2D);
	    extendedRoad.calculateShape();
	    drawRoadIntersections(offScreenG2D,extendedRoad);
	    extendedRoad.drawDirectedMidPoints(offScreenG2D);
	    extendedRoad.drawHandles(offScreenG2D);

		} else if (currentRoad != null &&
							 (drawMode == DRAW_INITIAL_ROAD ||
								(drawMode == APPEND_TO_END_OF_ROAD && 
								 currentRoad.checkextendLimits(snapX,snapY)))) {

	    offScreenG2D.drawImage(backBufferImage,0,0,this);
	    Road extendedRoad = currentRoad.deepClone();
	    extendedRoad.appendToEndOfRoad(snapX,snapY);
	    extendedRoad.drawRoad(offScreenG2D);
	    extendedRoad.calculateShape();
	    drawRoadIntersections(offScreenG2D,extendedRoad);
	    extendedRoad.drawDirectedMidPoints(offScreenG2D);
	    extendedRoad.drawHandles(offScreenG2D);

		} else if (drawMode == DRAG_ROAD_HANDLE) {

	    offScreenG2D.drawImage(backBufferImage,0,0,this);
	    currentRoad.modifyRoadHandle(currentHandle,currentSnapPoint);
	    currentHandle = currentSnapPoint;
	    currentRoad.drawRoad(offScreenG2D);
	    currentRoad.calculateShape();
	    drawRoadIntersections(offScreenG2D,currentRoad);

		} else if (drawMode == DRAG_JUNCTION) {

	    offScreenG2D.drawImage(gridImage,0,0,this);
	    currentJunction.changeLocation(snapX,snapY);
	    drawAllOldRoads(offScreenG2D);
	    drawAllOldJunctions(offScreenG2D);
	    if (currentRoad != null) currentRoad.drawRoad(offScreenG2D);

		} else if (drawMode == DRAG_LEFT_ROAD_EDGE || 
							 drawMode == DRAG_RIGHT_ROAD_EDGE) {

	    offScreenG2D.drawImage(backBufferImage,0,0,this);
	    currentRoad.drawRoad(offScreenG2D);
	    currentRoad.calculateShape();
	    drawRoadIntersections(offScreenG2D,currentRoad);
	    currentRoad.drawDirectedMidPoints(offScreenG2D);
	    currentRoad.drawHandles(offScreenG2D);

		} else if (drawMode == CLICKED_ON_JUNCTION) {
	    
	    offScreenG2D.drawImage(gridImage,0,0,this);
	    drawAllOldRoads(offScreenG2D);
	    drawAllOldJunctions(offScreenG2D);
	    if (currentRoad != null) currentRoad.drawRoad(offScreenG2D);
	
		} else if (drawMode == NO_MODE ||
							 drawMode == CLICKED_ON_HANDLE ||
							 drawMode == DRAG_ROAD_MIDDLE) {

	    offScreenG2D.drawImage(backBufferImage,0,0,this);
	    if (currentRoad != null) {
				currentRoad.drawRoad(offScreenG2D);
				currentRoad.calculateShape();
				drawRoadIntersections(offScreenG2D,currentRoad);
				currentRoad.drawDirectedMidPoints(offScreenG2D);
				currentRoad.drawHandles(offScreenG2D);
	    }

		} else if (drawMode == DONT_DRAW) {
	    offScreenG2D.drawImage(backBufferImage,0,0,this);
		}
      
		if (currentJunction != null) {
	    offScreenG2D.setStroke(normal);
	    currentJunction.drawHandledJunction(offScreenG2D);
		}
			    
		if (Main.DEBUG) {
	    offScreenG2D.setColor(Color.black);
	    offScreenG2D.drawString("drawMode= "+
															writeOutDrawMode(drawMode),15,40);
	    offScreenG2D.drawString("selectedButton= "+
															writeOutSelectedButton(selectedButton),15,60);
	    offScreenG2D.drawString("x= "+ x + ",y= "+ y,15,80);
		}


		//Draw user helping aspects.
		if (currentHandle != null) {
	    offScreenG2D.setColor(Color.red);
	    offScreenG2D.fill(getSPointRectangle((int)currentHandle.getX(),
																					 (int)currentHandle.getY()));
		} else if (currentMidPoint != null) {
	    offScreenG2D.setColor(Color.red);
	    offScreenG2D.fill(getSPointRectangle((int)currentMidPoint.getX(),
																					 (int)currentMidPoint.getY()));
		} else if (currentRoadSide != null) {
	    offScreenG2D.setColor(Color.red);
	    offScreenG2D.fill(currentRoadSide);
		}

		offScreenG2D.clip(clipRect);

		g.drawImage(offScreenImage, 0, 0, this);
	}

	/** snap to a grid. */
	private void snapTo(MouseEvent e) { 
		x = e.getX();
		y = e.getY();
		currentXYPoint = new Point2D.Double(x,y);

		snapX = (Math.round((float)x/gridSize)*gridSize);
		snapY = (Math.round((float)y/gridSize)*gridSize);

		if (!firstDrawTime) {
			if (snapX<gridSize) snapX = gridSize;
			else if (snapX>(int)drawingBounds.getWidth()+gridSize) {
				snapX = (int)drawingBounds.getWidth()+gridSize; 
			}
			if (snapY<gridSize) snapY = gridSize;
			else if (snapY>(int)drawingBounds.getHeight()+gridSize) {
				snapY = (int)drawingBounds.getHeight()+gridSize; 
			}
		}
		currentSnapPoint = new Point2D.Double(snapX, snapY);
	}
	
	/** The backbuffer contains all roads except the current one. */
	public void setBackBuffer(Graphics2D g2d) {
		g2d.drawImage(gridImage,0,0,this);

		drawAllOldRoads(g2d);              //Draw all the old lanes.
		drawAllOldJunctions(g2d);
		drawAllOldIntersections(g2d); 
		drawAllRoadDirectedMidPoints(g2d); //Draw midpoints along path points.
		drawAllRoadHandles(g2d);           //Draw points along the path.
	}

	//**********************************************************************
	//************************** Drawing Procedures ************************
	//**********************************************************************


	/** Draw all the paths in the vector called "lanes". */
    private void drawAllOldRoads(Graphics2D g2d) {
		
			final Iterator it = roads.iterator();
			while (it.hasNext()) {
				final Road tempRoad = (Road)it.next();
				tempRoad.drawRoad(g2d);
			}
    }

	/** Draw the mid-points of all the path. */
	private void drawAllRoadDirectedMidPoints(Graphics2D g2d) {
	
		final Iterator it = roads.iterator();
		while (it.hasNext()) {
	    final Road tempRoad = (Road)it.next();
	    tempRoad.drawDirectedMidPoints(g2d);
		}
	}
    
	/** Create a small square around all the points in all the paths. */
	private void drawAllRoadHandles(Graphics2D g2d){
	
		final Iterator it = roads.iterator();
		while (it.hasNext()) {
	    final Road tempRoad = (Road)it.next();
	    tempRoad.drawHandles(g2d);
		}
	}

	private void drawAllOldJunctions(Graphics2D g2d) {
		g2d.setStroke(normal);
	
		final Iterator it = junctions.iterator();
		while (it.hasNext()) {
	    final Junction tempJunc = (Junction)it.next();
	    tempJunc.drawHandledJunction(g2d);
		}
	}
  

	private void drawAllOldIntersections(Graphics2D g2d) {

		final Iterator it = roads.iterator();
		while (it.hasNext()) {
	    final Road tempRoad = (Road)it.next();
	    drawRoadIntersections(g2d,tempRoad);
		}
	}

	private void drawRoadIntersections(Graphics2D g2d, Road road){

		if (road == null || road.getNumberOfSections() == 0) return;

		Area testArea = new Area(road.getShape());
		
		final Iterator i = roads.iterator();
		while (i.hasNext()) {
	    final Road tempRoad = (Road)i.next();
	    if (tempRoad.getNumberOfSections() >= 1 && tempRoad != road) {
				Area intersectArea = new Area(road.getShape());
				intersectArea.intersect(new Area(tempRoad.getShape()));

				g2d.setPaint(unknownJunction);
				g2d.fill(intersectArea);
				g2d.setStroke(normal);g2d.setColor(Color.red);
				g2d.draw(intersectArea);
	    }
		}
	}

	//**********************************************************************
	//******** Procedures to get infomation in response to mouse events ****
	//**********************************************************************

	private void saveCurrentRoad() {
		if (currentRoad != null) roads.addElement(currentRoad);
	}

	private void loadCurrentRoad(Road newRoad) {
		roads.removeElement(newRoad);
		currentRoad = newRoad;
	}

	private boolean setHandle(Point2D point) {
		currentHandle = null;

		if (currentRoad != null) currentHandle = currentRoad.getHandle(point);
		if (currentHandle != null) return true;

		final Iterator it = roads.iterator();
		while (it.hasNext()) {
	    final Road tempRoad = (Road)it.next();
	    currentHandle = tempRoad.getHandle(point);
	    if (currentHandle != null) {
				saveCurrentRoad();
				loadCurrentRoad(tempRoad);
				return true;
	    }
		}
		return false;
	}

	private boolean setRoadEdge(int X, int Y, boolean wantLeft) {
		Rectangle2D s = (Rectangle2D)getSPointRectangle(X,Y);
	
		if (currentRoad != null && 
				currentRoad.getRoadSide(wantLeft).intersects(s)) {
	    return true;
		}

		final Iterator it = roads.iterator();
		while (it.hasNext()) {
	    final Road tempRoad = (Road)it.next();
	    if (tempRoad.getRoadSide(wantLeft).intersects(s)) {
				saveCurrentRoad();
				loadCurrentRoad(tempRoad);
				return true;
	    }
		}

		return false;
	}

	private boolean setMidPoint(Point2D point) {
		currentMidPoint = null;

		if (currentRoad != null) {
	    currentMidPoint = currentRoad.getMidPoint(point);
	    if (currentMidPoint != null) return true;
		}

		final Iterator it = roads.iterator();
		while (it.hasNext()) {
	    final Road tempRoad = (Road)it.next();
	    currentMidPoint = tempRoad.getMidPoint(point);
	    if (currentMidPoint != null) {
				saveCurrentRoad();
				loadCurrentRoad(tempRoad);
				return true;
	    }
		}
		return false;
	}

	private boolean setJunction(Point2D point) {
	
		if (currentJunction != null) {
	    if (currentJunction.contains(point)) return true;
		}

		final Iterator it = junctions.iterator();
		while (it.hasNext()) {
	    final Junction tempJunc = (Junction)it.next();
	    if (tempJunc.contains(point)) {
				if (currentJunction != null) junctions.add(currentJunction);
				junctions.removeElement(tempJunc);
				currentJunction = tempJunc;
				return true;
	    }
		}
   
		return false;
	}

	private int getNumberOfRoadsUnder(int sX, int sY) {
		int count = 0;
		Rectangle2D s = (Rectangle2D)getSPointRectangle(sX,sY);
	
		if (currentRoad != null && currentRoad.getShape().intersects(s)) {
	    count++;
		}

		final Iterator it = roads.iterator();
		while (it.hasNext()) {
	    final Road tempRoad = (Road)it.next();
	    if (tempRoad.getShape().intersects(s)) count++;
		}
	
		return count;
	}

	private boolean setRoad(int sX, int sY) {
	
		Rectangle2D s = (Rectangle2D)getSPointRectangle(sX,sY);
	
		if (currentRoad != null && currentRoad.getShape().intersects(s)) {
	    return true;
		}

		final Iterator it = roads.iterator();
		while (it.hasNext()) {
	    final Road tempRoad = (Road)it.next();
	    if (tempRoad.getShape().intersects(s)) {
				saveCurrentRoad();
				loadCurrentRoad(tempRoad);
				return true;
	    }
		}
	
		return false;
	}

	private boolean setLargestRoad(int sX, int sY) {
		Road largestRoad=null;
		int mostLaneNum=0;
		Rectangle2D s = (Rectangle2D)getSPointRectangle(sX,sY);

		saveCurrentRoad();

		final Iterator it = roads.iterator();
		while (it.hasNext()) {
	    final Road tempRoad = (Road)it.next();
	    if (tempRoad.getShape().intersects(s)) {
				int laneNum = tempRoad.getNumLanes();
				if (laneNum > mostLaneNum) {
					largestRoad = tempRoad;
					mostLaneNum = laneNum;
				}
	    }
		}

		if (largestRoad != null) {
	    loadCurrentRoad(largestRoad);
	    return true;
		}
		return false;
	}

	private boolean isNearBounds(int x, int y) {
		boolean xOK = false , yOK = false;
		if (x < (int)(drawingBounds.getX() + gridSize)) xOK = true;
		if (x > (int)((drawingBounds.getX() + drawingBounds.getWidth()) - gridSize)) xOK = true;
	
		if (y < (int)(drawingBounds.getY() + gridSize)) yOK = true;
		if (y > (int)((drawingBounds.getY() + drawingBounds.getHeight()) - gridSize)) yOK = true;
	
		return (xOK || yOK);
	}

	private boolean setNearestInputRoad() {

		if (!isNearBounds(x,y)) {
	    System.out.println("!isNearBounds(x,y)");
	    return false;
		}

		int closestDist = 100000, currentDist = 100000;
		Road closestRoad = null;
		Point2D tempPoint = null;

		final Iterator it = roads.iterator();
		while (it.hasNext()) {
	    final Road tempRoad = (Road)it.next();

	    tempPoint = tempRoad.getStartHandle();
	    currentDist =(int)tempPoint.distance(currentXYPoint);
	    if (currentDist < closestDist) {
				closestRoad = tempRoad;
				closestDist = currentDist;
	    }

	    tempPoint = tempRoad.getEndHandle();
	    currentDist =(int)tempPoint.distance(currentXYPoint);
	    if (currentDist < closestDist) {
				closestRoad = tempRoad;
				closestDist = currentDist;
	    }
		}
		if (closestRoad != null && closestDist < 30) {
	    loadCurrentRoad(closestRoad);
	    System.out.println("Closest Road found");
	    return true;
		}
		System.out.println("No closest Road found");
		return false;
	}

	private boolean isNothingAt(int sX, int sY) {
		if (setHandle(currentSnapPoint)) return false;
		if (setMidPoint(currentXYPoint)) return false;
		if (setJunction(currentXYPoint)) return false;
		if (setRoad(x,y)) return false;
		return true;
	}


	private boolean getCurrentHandle() {
		currentHandle = null;

		if (currentRoad != null) 
	    currentHandle = currentRoad.getHandle(currentXYPoint);
      	
		final Iterator it = roads.iterator();
		while (it.hasNext()) {
	    if (currentHandle != null) return true;
	    final Road tempRoad = (Road)it.next();
	    currentHandle = tempRoad.getHandle(currentXYPoint);
		}
		return false;
	}

	private boolean getcurrentMidPoint() {
		currentMidPoint = null;
		if (currentRoad != null) {
	    currentMidPoint = currentRoad.getMidPointInLane(
																											currentRoad.getLane(0),
																											currentXYPoint);
		}

		final Iterator it = roads.iterator();
		while (it.hasNext()) {
	    if (currentMidPoint != null) return true;
	    final Road tempRoad = (Road)it.next();
	    currentMidPoint = tempRoad.getMidPointInLane(tempRoad.getLane(0),
																									 currentXYPoint);
		}
		return false;
	}

	private boolean getcurrentRoadSide() {
		currentRoadSide = null;
		if (currentRoad != null) {
	    currentRoadSide = currentRoad.getRoadSide(x,y);
		}
		final Iterator it = roads.iterator();
		while (it.hasNext()) {
			final Road tempRoad = (Road)it.next();
			if (currentRoadSide != null) return true;
			currentRoadSide = tempRoad.getRoadSide(x,y);
		}
		return false;
	}

	private void placeJunctionInRoads() {
		/*This is how it works:
			First, where do we place the center of the junction?
	  
			If there is only one road where the user clicked...
	    ... place in center of the road nearest where the user clicked.
			If there are two roads where the user clicked...
	    ...then place junction at the intersection of the two roads.
			If there are 3 roads, don't place a junction, throw an error.
	  
			Second, how do we split up the roads and attach them to the junction?

			Set the "currentRoad" to be the largest road where the user clicked.
			This "currentRoad" will definelty be attached to the junction.
			Place the junction and rotate it to align with the "currentRoad".
			For both of the 2 possible roads intersecting with the junction...
	    ...from the start of the road search through untill you find a ...
			...intersection with a side of the junction. At this point ...
			...cut off the road here and attach it to the junction side.
			Restart from the cutoff point and if there is another...
			...intersection restart the road from the junction side.

			At the end, tell the new junction to update its looks.*/
	
		//Get new junctions position and orientation. ************************

		int numRoads = getNumberOfRoadsUnder(x,y);
		setLargestRoad(x,y);
		int juncX=0,juncY=0;
		Road oRoad=null;
		int[] idToTest= new int[2]; idToTest[0]=-1;idToTest[1]=-1;
		System.out.println("Placing junction in "+numRoads+" road(s): ");
		Line2D nearestLine = currentRoad.getNearestLine(currentXYPoint);
		
		if (nearestLine == null) {
	    System.out.println("No near line at x= "+x+",y= "+y);
	    return;
		}	        
		final double lineAngle = GeoUtils.getAngle(nearestLine);
	
		if (numRoads == 1) {
	    //first work out the point the junction center should be.
	    final double x1 = nearestLine.getX1();
	    final double y1 = nearestLine.getY1();
	    final double dis = GeoUtils.getDistance((double)x,(double)y,x1,y1);
	    juncX = (int)(x1+dis*Math.cos(lineAngle));
	    juncY = (int)(y1-dis*Math.sin(lineAngle));
		} else if (numRoads == 2) {
	    Rectangle2D s = (Rectangle2D)getSPointRectangle(x,y);
	    final Iterator it = roads.iterator();
	    while (it.hasNext()) {
				final Road tempRoad = (Road)it.next();
				if (tempRoad.getShape().intersects(s)) {
					oRoad = tempRoad;
					break;
				}
	    }
	    Point2D jPoint = GPathUtils.getIntersectPoint(currentXYPoint,
																										currentRoad.getLane(0),
																										oRoad.getLane(0));
	    if (jPoint == null) {
				System.out.println("No road intersection at x= "+x+",y= "+y);
				return;
	    }	
	    juncX = (int)jPoint.getX();
	    juncY = (int)jPoint.getY();
		} else {
	    System.out.println("Complex junctions aren't implemented!!!!!");
		}

		//Place the new junction *************************************

		if (currentJunction != null) junctions.add(currentJunction);
		currentJunction = new Junction(juncX,juncY,gridSize,this);
		currentJunction.rotate(-lineAngle);

		//Split the "currentRoad" and attach the new roads onto the junction.
	
		int index1 = currentJunction.getFirstIndex(currentRoad.getLane(0));
		int dir1 = currentJunction.getFirstDir(currentRoad.getLane(0),index1);
		int index2 = currentJunction.getLastIndex(currentRoad.getLane(0),index1);
		int dir2 = currentJunction.getLastDir(currentRoad.getLane(0),index1,index2);
		if (index2 != -1) {
	    Road newRoad = currentRoad.getSectionFrom(roadIndex++,index2);
	    roads.add(newRoad);
	    currentJunction.attachRoad(newRoad.getID(),dir2,true);
	    currentJunction.updateJunctionLook();
	    newRoad.setRoadShapeVariables();
	    idToTest[0] = newRoad.getID();
		}
		currentRoad.cutOffAt(index1);
		currentRoad.appendToEndOfRoad(0,0);
		currentJunction.attachRoad(currentRoad.getID(),dir1,false);
		currentJunction.updateJunctionLook();
		currentRoad.setRoadShapeVariables();

		//Split the Other road(if exists) and attach new roads to the junction.

		if (numRoads == 2) {

	    index1 = currentJunction.getFirstIndex(oRoad.getLane(0));
	    dir1 = currentJunction.getFirstNonEmptyDir(oRoad.getLane(0),index1);
	    index2 = currentJunction.getLastIndex(oRoad.getLane(0),index1);
	    dir2 = currentJunction.getLastNonEmptyDir(oRoad.getLane(0),index1,index2);
	    if (index2 != -1) {
				Road newRoad = oRoad.getSectionFrom(roadIndex++,index2);
				roads.add(newRoad);
				currentJunction.attachRoad(newRoad.getID(),dir2,true);
				currentJunction.updateJunctionLook();
				newRoad.setRoadShapeVariables();
				idToTest[1] = newRoad.getID();
	    }
	    oRoad.cutOffAt(index1);
	    oRoad.appendToEndOfRoad(0,0);
	    currentJunction.attachRoad(oRoad.getID(),dir1,false);
	    currentJunction.updateJunctionLook();
	    oRoad.setRoadShapeVariables();
		}
		//Remove the new roads if entirely contained in the junction.

		for (int i=0;i<2;i++) {
	    if (idToTest[i] != -1) {
				Road tempRoad = getRoad(idToTest[i]);
				if (currentJunction.contains(tempRoad.getEndHandle())) {
					deleteRoad(idToTest[i]);
					currentJunction.dettachRoad(idToTest[i]);
				}
	    }
		}	
	}
    
	//**********************************************************************
	//**************************** test Functions **************************
	//**********************************************************************

	private void testToJoinRoads() {
	
		final Point2D currentRoadStart = currentRoad.getStartHandle();
		final Point2D currentRoadEnd = currentRoad.getEndHandle();
	
		final Iterator it = roads.iterator();
		while (it.hasNext()) {
	    final Road tempRoad = (Road)it.next();  
	    final Point2D tempRoadStart = tempRoad.getStartHandle();
	    final Point2D tempRoadEnd = tempRoad.getEndHandle();
	    
	    // Can't join roads at junctions.
	    
	    if (currentRoadEnd.equals(tempRoadStart) && 
					getJunctionAtPoint(currentRoadEnd) == null) {
				currentRoad.mergeRoad(tempRoad);
	    } else if (currentRoadStart.equals(tempRoadStart) && 
								 getJunctionAtPoint(currentRoadStart) == null) {
				currentRoad.reverseDirection();
				currentRoad.mergeRoad(tempRoad);
	    } else if (currentRoadStart.equals(tempRoadEnd) && 
								 getJunctionAtPoint(currentRoadStart) == null) {
				tempRoad.reverseDirection();
				currentRoad.reverseDirection();
				currentRoad.mergeRoad(tempRoad);
	    } else if (currentRoadEnd.equals(tempRoadEnd)&& 
								 getJunctionAtPoint(currentRoadEnd) == null) {  
				tempRoad.reverseDirection();
				currentRoad.mergeRoad(tempRoad);
			} else continue;
	    
	    dettachJunction(tempRoad.getID());
	    roads.removeElement(tempRoad);
	    setBackBuffer(backBufferG2D);
	    break;
		}
	
		testToAttachRoadToJunction(currentRoad.getID(),
															 currentRoad.getStartHandle(),
															 currentRoad.getEndHandle());
	}
    
	private void testToAttachRoadToJunction(int roadID,Point2D roadStart, 
																					Point2D roadEnd) {
		//test whether to Attach To Junction.
		if (currentJunction != null) {
	    if (currentJunction.testToAttach(roadStart,roadEnd,roadID)) {
				//System.out.println("attached current path");
				currentHandle = null;
	    }
		}
		Junction savedJunction = null;
		boolean newCurrentJunction = false;

		final Iterator it = junctions.iterator();
		while (it.hasNext()) {
	    final Junction tempJunc = (Junction)it.next();
	    if (tempJunc.testToAttach(roadStart,roadEnd,roadID)) {
				//System.out.println("attached current path");
				currentHandle = null;
				newCurrentJunction = true;
				savedJunction = tempJunc;
	    }
		}

		if (newCurrentJunction) {
	    if (currentJunction != null) junctions.add(currentJunction);
	    junctions.removeElement(savedJunction);
	    currentJunction = savedJunction;
		}
	}

	private void testIfNeedNewRoadAttachments() {
		if (currentRoad != null) {
	    testToAttachRoadToJunction(currentRoad.getID(),
																 currentRoad.getStartHandle(),
																 currentRoad.getEndHandle());
		}
		final Iterator it = roads.iterator();
		while (it.hasNext()) {
			final Road tempRoad = (Road)it.next();
			testToAttachRoadToJunction(tempRoad.getID(),
																 tempRoad.getStartHandle(),
																 tempRoad.getEndHandle());
		}
	}

	private void updateAllJunctionLooks() {
		if (currentJunction != null) {
	    currentJunction.updateJunctionLook();
		}
		final Iterator it = junctions.iterator();
		while (it.hasNext()) {
	    final Junction tempJunc = (Junction)it.next();
	    tempJunc.updateJunctionLook();
		}
	}

	private boolean testForValidity() {

		if (currentRoad != null && 
				!testLaneForValidity(currentRoad.getLane(0))) return false;

		final Iterator it = roads.iterator();
		while (it.hasNext()) {
			final Road tempRoad = (Road)it.next();
			if (!testLaneForValidity(tempRoad.getLane(0))) return false;
		}
		return true;
	}

	private boolean testLaneForValidity(GeneralPath lane) {
		if (!testPointForValidity(GPathUtils.getStartOfPath(lane))) return false;
		if (!testPointForValidity(GPathUtils.getEndOfPath(lane))) return false;
		return true;
	}

	private boolean testPointForValidity(Point2D p) {
		if (!testWheatherPointInDrawingArea(p)) return true;
		if (testWheatherPointInJunction(p)) return true;
		return false;
	}

	public boolean testWheatherPointInDrawingArea(Point2D p) {
		return validDrawingArea.contains(p);
	}

	public boolean testWheatherPointInJunction(Point2D p) {
		if (currentJunction != null) {
	    if (currentJunction.intersects(p)) return true;
		}
		final Iterator it = junctions.iterator();
		while (it.hasNext()) {
	    final Junction tempJunc = (Junction)it.next();
	    if (tempJunc.intersects(p)) return true;
		}
		return false;
	}

	//**********************************************************************
	//************************* Usefull Functions **************************
	//**********************************************************************

	private Shape getSPointRectangle(int x, int y) {
		return new Rectangle2D.Double(x - 3,y - 3,6,6);
	}

	private Shape getLPointRectangle(int x, int y) {
		return new Rectangle2D.Double(x - 6,y - 6,12,12);
	}

	private void dettachJunction(int roadID) {
		//test whether to dettach To Junction.
		if (currentJunction != null) currentJunction.dettachRoad(roadID);

		final Iterator it = junctions.iterator();
		while (it.hasNext()) {
	    final Junction tempJunc = (Junction)it.next();
	    tempJunc.dettachRoad(roadID);
		}
	}

	private Junction getJunctionAtPoint(Point2D point) {
	
		if (currentJunction != null) {
	    if (currentJunction.intersects(point)) return currentJunction;
		}
		final Iterator it = junctions.iterator();
		while (it.hasNext()) {
	    final Junction tempJunc = (Junction)it.next();
	    if (tempJunc.intersects(point)) return tempJunc;
		}
	
		return null;
	}

	private String writeOutDrawMode(int mode) {
		switch(mode) {
		case APPEND_TO_START_OF_ROAD: return "APPEND_TO_START_OF_ROAD";
		case APPEND_TO_END_OF_ROAD: return "APPEND_TO_END_OF_ROAD";
		case CLICKED_ON_HANDLE: return "CLICKED_ON_HANDLE";
		case DRAG_ROAD_HANDLE: return "DRAG_ROAD_HANDLE";
		case CLICKED_ON_JUNCTION: return "CLICKED_ON_JUNCTION";
		case DRAG_JUNCTION: return "DRAG_JUNCTION";
		case DRAG_LEFT_ROAD_EDGE: return "DRAG_LEFT_ROAD_EDGE";
		case DRAG_RIGHT_ROAD_EDGE: return "DRAG_RIGHT_ROAD_EDGE";
		case DRAG_ROAD_MIDDLE: return "DRAG_ROAD_MIDDLE";
		case DONT_DRAW: return "DONT_DRAW";
		case NO_MODE: return "NO_MODE";
		default: return "unknow mode";
		}
	}

	private String writeOutSelectedButton(int mode) {
		switch(mode) {
		case SELECT: return "SELECT";
		case DRAW_ROAD: return "DRAW_ROAD";
		case DRAW_JUNCTION: return "DRAW_JUNCTION";
		case DELETE: return "DELETE";
		case ORIENTATION: return "ORIENTATION;";
		default: return "unknow mode";
		}
	}

	public BufferedImage getUnknownJunctionTexture() {
		Image offscreen = createImage(14,14);
		Graphics2D offgraphics = (Graphics2D)offscreen.getGraphics();
	
		offgraphics.setColor(Color.lightGray);
		offgraphics.fillRect(0, 0, 14, 14);	
	
		Font ff = new Font("Serif",Font.BOLD,12);
		offgraphics.setFont(ff);
		offgraphics.setColor(Color.red);
		offgraphics.drawString("?",1,13);	
	
		return (BufferedImage)offscreen;
	}


	BufferedImage toBufferedImage(Image image) {
		// This code ensures that all the pixels in the image are loaded.
		image = new ImageIcon(image).getImage();
	
		// Create the buffered image.
		BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), 
																										image.getHeight(null), 
																										BufferedImage.TYPE_INT_RGB);
	
		// Copy image to buffered image.
		Graphics g = bufferedImage.createGraphics();
    
		// Clear background and paint the image.
		g.setColor(Color.white);
		g.fillRect(0, 0, image.getWidth(null), image.getHeight(null));
		g.drawImage(image, 0, 0, null);
		g.dispose();
    
		return bufferedImage;
	}


	//**********************************************************************
	//************************ public get Functions ************************
	//**********************************************************************

	public Road getRoad(int roadIndex) {
		if (currentRoad != null &&
				currentRoad.getID() == roadIndex) return currentRoad;
		final Iterator it = roads.iterator();
		while (it.hasNext()) {
	    final Road tempRoad = (Road)it.next();
	    if (tempRoad.getID() == roadIndex) return tempRoad;
		}
		System.out.println("Didn't find road with index " + roadIndex);
		return null;
	}

	private void deleteRoad(int roadID) {
		Road roadToDelete=null;

		if (currentRoad != null &&
				currentRoad.getID() == roadID) {currentRoad = null; return;}

		final Iterator it = roads.iterator();
		while (it.hasNext()) {
	    final Road tempRoad = (Road)it.next();
	    if (tempRoad.getID() == roadID) {
				roadToDelete = tempRoad;
	    }
		}
		if (roadToDelete != null) roads.removeElement(roadToDelete);
		else System.out.println("No road with this ID to delete");
	}

	public int getjunctionSide(Point2D p) {
		Junction tempJunc = getJunctionAtPoint(p);
		if (tempJunc != null) {
	    final int jSide = tempJunc.getJunctionSide(p);
	    //System.out.println("jSide= "+jSide);
	    return jSide;
		}
		System.out.println("invalid junction point at getjunctionSide()");
		return -1;
	}


	private void clipRoad(Road road) {
		//snap lane points to bounds.
		final Point2D p1 = new Point2D.Double(drawingBounds.getX(),
																					drawingBounds.getY());
		final Point2D p2 = new Point2D.Double(drawingBounds.getX()+
																					drawingBounds.getWidth(),
																					drawingBounds.getY());
		final Point2D p3 = new Point2D.Double(drawingBounds.getX()+
																					drawingBounds.getWidth(),
																					drawingBounds.getY()+
																					drawingBounds.getHeight());
		final Point2D p4 = new Point2D.Double(drawingBounds.getX(),
																					drawingBounds.getY()+
																					drawingBounds.getHeight());
	
		final Line2D l1 = new Line2D.Double(p1,p2);
		final Line2D l2 = new Line2D.Double(p2,p3);
		final Line2D l3 = new Line2D.Double(p3,p4);
		final Line2D l4 = new Line2D.Double(p4,p1);

		road.checkBoundsWith(l1,l2,l3,l4,drawingBounds);
	}

	public Vector clipAllRoads() {
	
		if (currentRoad != null) {
	    roads.addElement(currentRoad);
	    currentRoad = null;
	    drawMode = DONT_DRAW;
		}

		final Iterator it = roads.iterator();
		while (it.hasNext()) {
	    final Road tempRoad = (Road)it.next();
	    clipRoad(tempRoad);
		}

		setBackBuffer(backBufferG2D);

		return roads;
	}

	public void drawAllNeatenedRoads(Graphics2D offgraphics) {
	
		Junction tempJunc;
		final Iterator it = roads.iterator();
		while (it.hasNext()) {
	    final Road tempRoad = ((Road)it.next()).deepClone();
	    
	    if (tempRoad.isSymetrical()) {
				tempJunc = getJunctionAtPoint(tempRoad.getStartHandle());
				if (tempJunc != null) {
					tempRoad.appendToStartOfRoad((int)tempJunc.x[0],
																			 (int)tempJunc.y[0]);
				}

				tempJunc = getJunctionAtPoint(tempRoad.getEndHandle());
				if (tempJunc != null) {
					tempRoad.appendToEndOfRoad((int)tempJunc.x[0],
																		 (int)tempJunc.y[0]);
				}
	    }
	    clipRoad(tempRoad);

	    tempRoad.drawRoad(offgraphics);
		}
	}

	public Vector getAllJunctions() {
	
		if (currentJunction != null) {
	    junctions.addElement(currentJunction);
	    currentJunction = null;
	    setBackBuffer(backBufferG2D);
	    drawMode = DONT_DRAW;
		}
	
		return junctions;
	}
  
	//**********************************************************************
	//******************** functions to interface to the outside world *****
	//**********************************************************************

	public void resetSelectedStuff() {
		currentHandle = null;
		currentMidPoint = null;
		currentRoadSide = null;
	}

	public void setSelectMode() {
		resetSelectedStuff();
		selectedButton = SELECT;
	}

	public void clear() {
		roads.removeAllElements();
		junctions.removeAllElements();
		currentJunction = null;
		currentRoad = null;
		currentHandle = null;
		resetSelectedStuff();
		setBackBuffer(backBufferG2D);
		repaint();
		drawMode = DONT_DRAW;
	}

	public void setDrawRoadMode() {
		resetSelectedStuff();
		selectedButton = DRAW_ROAD;
	}

	public void setDrawJunctionMode() {
		resetSelectedStuff();
		selectedButton = DRAW_JUNCTION;
	}

	public void setDeleteMode() {
		resetSelectedStuff();
		selectedButton = DELETE;
	}

	public void setOrientationMode() {
		resetSelectedStuff();
		selectedButton = ORIENTATION;
	}

	public int getjunctionID(Point2D p) {
		if (!testWheatherPointInDrawingArea(p)) return RoadNetwork.MAPEDGE;
		Junction tempJunc = getJunctionAtPoint(p);
		if (tempJunc != null) return tempJunc.getID();
	
		System.out.println("invalid junction point at getjunctionID()");
		return -1;
	}

	public Image getbackdrop() {

		Image offscreen;
		if (backgroundImage == null) {
	    offscreen = createImage(getSize().width, getSize().height);
		} else {
	    offscreen = toBufferedImage(backgroundImage);
		}
		Graphics2D offgraphics = (Graphics2D)offscreen.getGraphics();
		offgraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
																 RenderingHints.VALUE_ANTIALIAS_ON);
		offgraphics.setColor(Color.black);
		if (backgroundImage == null) {
	    offgraphics.fillRect(0, 0, getSize().width, getSize().height); 

	    offgraphics.setColor(Color.white);
	    offgraphics.fillRect((int)drawingBounds.getX(),
													 (int)drawingBounds.getY(), 
													 (int)drawingBounds.getWidth(), 
													 (int)drawingBounds.getHeight());	
		} else {
	    offgraphics.drawRect((int)drawingBounds.getX(),
													 (int)drawingBounds.getY(), 
													 (int)drawingBounds.getWidth(), 
													 (int)drawingBounds.getHeight());	
		}
	
		//Draw all roads.
		drawAllNeatenedRoads(offgraphics);
	
		//Draw all Junctions
		offgraphics.setStroke(normal);
		final Iterator it = junctions.iterator();
		while (it.hasNext()) {
	    final Junction tempJunc = (Junction)it.next();
	    tempJunc.drawSimpleJunction(offgraphics);
		}
		//offgraphics.clip(drawingBounds);

		return offscreen;
	}

	//**********************************************************************
	//******************** XML SAVING **************************************
	//**********************************************************************
	public String getXMLName() {return "Design_Canvass";}
    
	public XMLElement saveSelf() { 
		XMLElement result = new XMLElement("Design_Canvass"); 
		result.addAttribute(new XMLAttribute("Width",getSize().width));
		result.addAttribute(new XMLAttribute("Height",getSize().height));
		return result;
	}

	public void saveChilds(XMLSaver saver) { 

		if (currentJunction != null) {
	    junctions.addElement(currentJunction);
	    currentJunction = null;
		}

		final Iterator it2 = junctions.iterator();
		while (it2.hasNext()) {
	    final Junction tempJunc = (Junction)it2.next();
	    saver.saveObject(tempJunc);
		}

		if (currentRoad != null) {
	    roads.addElement(currentRoad);
	    currentRoad = null;
		}

		final Iterator it = roads.iterator();
		while (it.hasNext()) {
	    final Road tempRoad = (Road)it.next();
	    saver.saveObject(tempRoad);
		}

		drawMode = DONT_DRAW;
		setBackBuffer(backBufferG2D);
	}

	public void loadSelf(XMLElement element) { 
		//System.out.println("In RoadDesigner loadSelf()");
		width = element.getAttribute("Width").getIntValue();
		height = element.getAttribute("Height").getIntValue();
		setSize(width,height);
		firstDrawTime = true;
	}

	public void loadChilds(XMLLoader loader) { 
		//System.out.println("In RoadDesigner loadChilds()");
	
		junctions.removeAllElements();

		while (loader.getNextElementName().equals("Junction")) {
	    Junction newJunc = new Junction(0,0,gridSize,this);
	    loader.loadObject(newJunc);
	    newJunc.setShape();
	    junctions.addElement(newJunc);
		}

		roadIndex = 0;
		roads.removeAllElements();
		
		while (loader.getNextElementName().equals("Road")) {
	    currentRoad = new Road(roadIndex++);
	    loader.loadObject(currentRoad);
	    currentRoad.setRoadShapeVariables();
	    testToJoinRoads();
	    roads.addElement(currentRoad);
		}

		currentRoad = null;
		updateAllJunctionLooks();
	}

	public void refreshCanvass() {
		repaint();
		setGraphics();
		repaint();
	}
}









