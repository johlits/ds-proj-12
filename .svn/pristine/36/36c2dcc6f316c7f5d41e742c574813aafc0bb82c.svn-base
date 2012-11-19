import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Scrollbar;
import java.awt.Insets;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.FileReader;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.Vector;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.JApplet;
import javax.swing.JSlider;
import javax.swing.JComboBox;
import javax.swing.Box;
import javax.swing.JMenuBar;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JFileChooser;
import javax.swing.ImageIcon;
import javax.swing.WindowConstants;


import javax.swing.filechooser.*;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.net.URL;

import XML.*;
import Utils.*;

/**
 * The main class deals with button clicks and menu items.
 * It acts as a mediator between the Road-Designer and the Simulation.
 * @author <a href="mailto:tf98@doc.ic.ac.uk">Thomas Fotherby</a>
 */
public class Main extends JFrame implements ActionListener,XMLSerializable {

	public static final boolean DEBUG = false;
	public static final int speedOptions = 4;
	public static final int ROADDESIGNER = 0;
	public static final int ROADSIM = 1;

	int panelIndex = 0;

	ButtonGroup   buttonGroup,speedOptionsgroup;
	JToggleButton selectButton,drawRoadButton,drawJunctionButton,
                deleteButton,orientateButton;
	
	JButton changeToDesigner,changeToSim,playPauseButton,graphButton,statsButton;
	JSlider framerate;
	JPanel  buttonPanel;


	JMenuBar  menuBar;
	JMenu     menu,display,carSpeedsMenuItem;
	JMenuItem newMenuItem,saveMenuItem,appletSave,loadMenuItem,quitMenuItem;

	JRadioButtonMenuItem[] carSpeedOptions;
	JCheckBoxMenuItem recordData,showData,pathsMenuItem,loadBackDrop,communicate;

	ImageIcon openIcon, saveIcon,newIcon,quitIcon;
	ImageIcon desIcon,simIcon,selIcon,delIcon,roadIcon,rotateIcon,juncIcon;
	ImageIcon playIcon,pauseIcon,graphIcon,statsIcon,clockIcon;

  GridBagLayout      gridBag;

	int     savedSpeed=0,savedTiming=50;
	boolean savedPause = false;
	String  backdropFileName = "null";

	RoadDesigner designPanel;
	SimPanel     simPanel;
	RoadNetwork  roadNetwork;

	private AppletButton isApplet=null;

	/**
	 * Called as the application starts. Loads the constructor.
	 */
	public static void main(String[] args) {
		Main window = new Main(null);
		window.setTitle("Visual Traffic Simulation");
		window.pack();
    //Main.centre(window);
    window.setVisible(true);
	}
  
	/**
	 * The Constructor sets the buttons up and initilises the Road-Editor panel.
	 */
	public Main(AppletButton webLoader) {

		isApplet = webLoader;
		getContentPane().setLayout(new BorderLayout());

		//Load all the icons needed.
		openIcon =   getImageIcon("icon/open.gif");
		saveIcon =   getImageIcon("icon/save.gif");
		newIcon =    getImageIcon("icon/new.gif");
		quitIcon =   getImageIcon("icon/warning.gif");
		
		desIcon =    getImageIcon("icon/Editor.gif");
		simIcon =    getImageIcon("icon/goodcar.gif");
		selIcon =    getImageIcon("icon/small_hand.gif");
		delIcon =    getImageIcon("icon/remove_icon.gif");
		roadIcon =   getImageIcon("icon/road.gif");
		rotateIcon = getImageIcon("icon/rotate.gif");
		juncIcon =   getImageIcon("icon/junction.gif");
		
		playIcon =   getImageIcon("icon/play.gif");
		pauseIcon =  getImageIcon("icon/pause.gif");
		graphIcon =  getImageIcon("icon/graph.gif");
		statsIcon =  getImageIcon("icon/stats.gif");
		clockIcon =  getImageIcon("icon/clock.gif");

		//Loads the roadDesigner panel
		designPanel = new RoadDesigner(this);

		buttonGroup = new ButtonGroup();

		//Create the menu bar.
		menuBar = new JMenuBar();
        
		//Build the menu-bar.
		menu = new JMenu("File");
		menuBar.add(menu);
		display = new JMenu("Display");
		menuBar.add(display);

		//a group of Menu Items.
		newMenuItem = addMenuItem("New",newIcon,menu);
		saveMenuItem = addMenuItem("Save",saveIcon,menu);
		appletSave = addMenuItem("Save as an Applet",saveIcon,menu);
		loadMenuItem = addMenuItem("Load",openIcon,menu);
		quitMenuItem = addMenuItem("Quit",quitIcon,menu);
		loadBackDrop = addCheckBoxMenuItem("Load Backdrop",false,display);
		showData = addCheckBoxMenuItem("Show Information",true,display);
		recordData = addCheckBoxMenuItem("Record Data",true,display);
		pathsMenuItem = addCheckBoxMenuItem("Show Junction Paths",false,display);
		communicate = addCheckBoxMenuItem("Junctions Communicate",false,display);
		speedOptionsgroup = new ButtonGroup();
		carSpeedsMenuItem = addCarSpeedOptions("Car speed",speedOptions,
																					 speedOptionsgroup,display);
			
		//Define Initial setup
		setInitialWidgetStates();
		gridBag = new GridBagLayout();

		setControls(ROADDESIGNER);

		// close window action
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new CloseWindow());
	}

	private void setUpDesignerButtons() {
       
		buttonPanel = new JPanel();
		buttonPanel.setLayout(gridBag);
		int i=0;

		//Add all the buttons.
	  changeToSim = addButton(simIcon,0,i++,2);
		addSeperator(0,i++);

		selectButton = addToggleButton(selIcon,0,i,1,false);
		orientateButton = addToggleButton(rotateIcon,1,i++,1,false);
		deleteButton = addToggleButton(delIcon,0,i++,1,false);
		addSeperator(0,i++);

		drawRoadButton = addToggleButton(roadIcon,0,i++,2,true);
		drawJunctionButton = addToggleButton(juncIcon,0,i++,2,false);
	}

    
	private void setUpSimButtons() {
	
		buttonPanel = new JPanel();
		buttonPanel.setLayout(gridBag);
		int i=0;

		//Add all the buttons.
		changeToDesigner = addButton(desIcon,0,i++,2);
		addSeperator(0,i++);

		playPauseButton = addButton(pauseIcon,0,i++,2);
		addSeperator(0,i++);

		graphButton = addButton(graphIcon,0,i,1);
		statsButton = addButton(statsIcon,1,i++,1);
		addSeperator(0,i++);

		addLabel(clockIcon,0,i++,2);
		framerate = addSlider(JSlider.VERTICAL,1,101,savedTiming,0,i++,2);
	}

	/**
	 * Tests if the roadDesigner is in a valid state to move to a simulation. If
	 * it is then the Sim button is enabled.
	 *
	 * @param decision true if valid to change to sim.
	 */
	public void canMoveOn(boolean decision) {
		changeToSim.setEnabled(decision);
	}

	public void setInitialWidgetStates() {
		saveMenuItem.setEnabled(isApplet==null);
		appletSave.setEnabled(isApplet==null);
		loadMenuItem.setEnabled(isApplet==null);

		pathsMenuItem.setState(false);
		pathsMenuItem.setEnabled(false);

		recordData.setState(true);
		recordData.setEnabled(false);
		
		showData.setState(true);
		showData.setEnabled(false);

		loadBackDrop.setState(false);
		loadBackDrop.setEnabled(isApplet==null);

		communicate.setState(false);
		communicate.setEnabled(false);

		carSpeedsMenuItem.setEnabled(false);
	}

	static void centre(Component c) {
    Toolkit tk = Toolkit.getDefaultToolkit();
    Dimension screen = tk.getScreenSize();
    Dimension ltsa   = c.getSize();
    double x = (screen.getWidth()-ltsa.getWidth())/2;
    double y = (screen.getHeight()-ltsa.getHeight())/2;
    c.setLocation((int)x, (int)y );
  }

	private void swapto(int index) {
		if (index==panelIndex) return;
		else {
			panelIndex = index;
			setControls(panelIndex);
		}
		repaint(); //hack to solve display problem
	}

	private void setControls(int index) {

		if (index == ROADDESIGNER) {

			if (simPanel != null) simPanel.stopSim();
			this.getContentPane().removeAll();

	    setUpDesignerButtons(); 

			this.getContentPane().add(menuBar,BorderLayout.NORTH);
	    this.getContentPane().add(buttonPanel,BorderLayout.WEST);
	    this.getContentPane().add(designPanel,BorderLayout.CENTER);
	    this.getContentPane().validate();

	    roadNetwork = null;
	    simPanel = null;
	   
	    designPanel.setDrawRoadMode();
	    drawRoadButton.setSelected(true);
	    
			pathsMenuItem.setEnabled(false);
			recordData.setEnabled(false);
			showData.setEnabled(false);
			loadBackDrop.setEnabled(isApplet==null);
			communicate.setState(false);
			communicate.setEnabled(false);
			carSpeedsMenuItem.setEnabled(false);
			
	    designPanel.repaint();

		} else if (index == ROADSIM) {

	    roadNetwork= new RoadNetwork(designPanel,savedSpeed);
	    simPanel = new SimPanel(designPanel.getbackdrop(),
															roadNetwork,savedTiming);

	    this.getContentPane().removeAll();
	    setUpSimButtons(); 

			this.getContentPane().add(menuBar,BorderLayout.NORTH);
	    this.getContentPane().add(buttonPanel,BorderLayout.WEST);
	    this.getContentPane().add(simPanel,BorderLayout.CENTER);
	    this.getContentPane().validate();

			pathsMenuItem.setEnabled(true);
	    recordData.setEnabled(true);
	    showData.setEnabled(true);
			loadBackDrop.setEnabled(false);
			communicate.setEnabled(true);
			carSpeedsMenuItem.setEnabled(true);

	    framerate.addChangeListener(new SliderListener());
	    simPanel.startSim();

		} else System.out.println("UnIdentified panel index");
	}
	
	/**
	 * Handles events.
	 * @param ae an <code>ActionEvent</code> value
	 */
	public void actionPerformed(ActionEvent ae) {
	
		//File Menu ******************************************************

		if (ae.getSource() == newMenuItem) { 
			newFile(); 
		}
	  
		if (ae.getSource() == saveMenuItem) {
			saveAs();
		}

		if (ae.getSource() == appletSave) {
			saveAsApplet();
		}

		if (ae.getSource() == loadMenuItem) {
			load();
		}

		if (ae.getSource() == quitMenuItem) {
			quitAll();
		}

		//display menu item *****************************************************

		if (ae.getSource() == pathsMenuItem) {
			JunctionModel.DRAWPATHS = pathsMenuItem.getState();
		}

		if (ae.getSource() == recordData) {
			if (recordData.getState()) {
				graphButton.setEnabled(true);
				simPanel.setRecordData(true);
			} else {
				graphButton.setEnabled(false);
				simPanel.setRecordData(false);
			}
		}

		if (ae.getSource() == showData) {
	    simPanel.setShowData(showData.getState());
		}


		if (ae.getSource() == loadBackDrop) {
			if (loadBackDrop.getState()) {
				JFileChooser filechooser = new JFileChooser("./"); 
				filechooser.addChoosableFileFilter(new PicFilter());
				
				int returnval = filechooser.showOpenDialog(this); 
				
				if(returnval == JFileChooser.APPROVE_OPTION){
					
					File currentFile = filechooser.getSelectedFile();
					if (currentFile.canRead()) {
						backdropFileName = currentFile.getAbsolutePath();
						Image backdrop = getToolkit().getImage(backdropFileName);
						designPanel.backgroundImage = backdrop;
					} else {
						backdropFileName = "null";
						designPanel.backgroundImage = null;
						System.out.println("No readable file selected");
					} 
				} else {
					System.out.println("Load Backdrop command cancelled by user.");
				}
			} else {
				backdropFileName = "null";
				designPanel.backgroundImage = null;
			}

			designPanel.setBackDrop();
			designPanel.refreshCanvass();
		}
		
		if (ae.getSource() == communicate) {
			simPanel.setJunctionComm(communicate.getState());
		}
	
		//Constant Buttons **************************************************

		if (ae.getSource() == changeToDesigner) {
			swapto(ROADDESIGNER);
		}

		if (ae.getSource() == changeToSim) {
			swapto(ROADSIM);
		}

		//Designer Buttons **************************************************
	
		if (ae.getSource() == selectButton) {
	    designPanel.setSelectMode();
		}
		if (ae.getSource() == drawRoadButton) {
	    designPanel.setDrawRoadMode();
		}
		if (ae.getSource() == drawJunctionButton) {
	    designPanel.setDrawJunctionMode();
		}
		if (ae.getSource() == deleteButton) {
	    designPanel.setDeleteMode();
		}
		if (ae.getSource() == orientateButton) {
	    designPanel.setOrientationMode();
		}

		//Sim Buttons **************************************************

		if (ae.getSource() == playPauseButton) {
	    if (savedPause) {
				savedPause = false; 
				playPauseButton.setIcon(pauseIcon);
	    } else {
				savedPause = true;
				playPauseButton.setIcon(playIcon);
	    }
	    simPanel.setPauseState(savedPause);
		}

		if (ae.getSource() == graphButton) {
	    new GraphPopup(simPanel);
		}

		if (ae.getSource() == statsButton) {
			new StatsPopup(simPanel);
		}
		
		for (int i=0;i<speedOptions;i++) {
			if (ae.getSource() == carSpeedOptions[i]) {
				savedSpeed = i;
				roadNetwork.carGenerator.setCarSpeeds(savedSpeed);
			}
		}
	}

	private void quitAll(){
		if (isApplet!=null) {
			this.dispose();
			isApplet.ended();
		} else {
			System.exit(0);
		}
	}

	private void invalidateState() {     
		validate();
	}

	private ImageIcon getImageIcon(String iconName) {
		return new ImageIcon(this.getClass().getResource(iconName));
	}
	
	//**********************************************************************
	//*********************** File handling ********************************
	//**********************************************************************
	
	private void newFile() {
		designPanel = new RoadDesigner(this);
		setControls(ROADDESIGNER);
		setInitialWidgetStates();
		panelIndex = ROADDESIGNER;
		backdropFileName = "null";
		invalidateState();
		repaint(); //hack to solve display problem
	}

	private void saveAs() {
		JFileChooser fc = new JFileChooser("Saves/");
		int returnVal = fc.showSaveDialog(this);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			System.out.println("Saving: " + file.getName());
			try {
				XMLSaver saver=new XMLSaver(file);
				saver.saveObject(this);
					saver.close();
			} catch (IOException e) {
				System.out.println("IOException when saving");
			} 
		} else {
			System.out.println("Save command cancelled by user.");
		}
	}

	//Save as an applet generates 3 files
	// 1)A build script;
	// 2)An XML file specifing the road network
	// 3)An html file to run the applet.
	private void saveAsApplet() {
		JFileChooser fc = new JFileChooser("AppletSaves/");
		int returnVal = fc.showSaveDialog(this);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			System.out.println("Saving: " + file.getName());
			try {
				//Save this file as XML
				XMLSaver saver=new XMLSaver(file);
				saver.saveObject(this);
				saver.close();
				
				//Generate the html file that will run the applet
				//The html will come from a special skeleton file.
				String[] args1 = {"NeededFiles/skeleton.txt",
													"AppletSaves/"+file.getName()+".html",
													"THEWIDTH",
													""+designPanel.getSize().width,
													"THEHEIGHT",
													""+designPanel.getSize().height,
													"THESPECFILE",
													""+file.getName(),
													"THEARCHIVE",
													file.getName()+".jar"};
				
				new Utils.TextRep(args1); //A text search and replace class.
				
				String[] args2 = {"NeededFiles/AppletJarrer.sh",
													"AppletSaves/"+file.getName()+".sh",
													"THEJARNAME",
													file.getName()+".jar",
													"THESPECFILE",
													""+file.getName()};

				new Utils.TextRep(args2); //A text search and replace class.

				String[] args3 = {"NeededFiles/AppletJarrer.bat",
													"AppletSaves/"+file.getName()+".bat",
													"THEJARNAME",
													file.getName()+".jar",
													"THESPECFILE",
													""+file.getName()};

				new Utils.TextRep(args3); //A text search and replace class.
				
			} catch (IOException e) {
					System.out.println("IOException when saving");
			} 
		} else {
			System.out.println("Save command cancelled by user.");
		}
	}

	private void load() {
			JFileChooser fc = new JFileChooser("Saves/");
	    int returnVal = fc.showOpenDialog(this);
	    
	    if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				System.out.println("Loading: " + file.getName());
				try {
					XMLLoader loader=new XMLLoader(file);
					loader.loadObject(this);
					loader.close();
				} catch (Exception e) {
					System.out.println("Loading failed, "+
														 "make sure you load the correct file type");
				} 
	    } else {
				System.out.println("Load command cancelled by user.");
	    }
	}

	//**********************************************************************
	//******************** XML SAVING **************************************
	//**********************************************************************

	/**
	 * @return a <code>String</code> that is the name of this XML element
	 */
		public String getXMLName() {return "Road_InfraStructure";}

	/**
	 * Saves all the attributes of this class.
	 * @return a <code>XMLElement</code> value
	 */
	public XMLElement saveSelf() { 
		XMLElement result = new XMLElement("Road_InfraStructure"); 
		result.addAttribute(new XMLAttribute("Width",getWidth()));
		result.addAttribute(new XMLAttribute("Height",getHeight()));
		result.addAttribute(new XMLAttribute("savedSpeed",savedSpeed));
		result.addAttribute(new XMLAttribute("savedTiming",savedTiming));
		result.addAttribute(new XMLAttribute("BackDropFile",backdropFileName));

		return result;
	}

	/**
	 * Calls a save for the children of this class.
	 * @param saver a <code>XMLSaver</code> value
	 */
	public void saveChilds(XMLSaver saver) { 
		saver.saveObject(designPanel);
	}

	/**
	 * Loads all the attributes of this class.
	 * @param element a <code>XMLElement</code> value
	 */
	public void loadSelf(XMLElement element) { 
		designPanel = new RoadDesigner(this);
		setControls(ROADDESIGNER);
		
		int width = element.getAttribute("Width").getIntValue();
		int height = element.getAttribute("Height").getIntValue();
		savedSpeed = element.getAttribute("savedSpeed").getIntValue();
		savedTiming = element.getAttribute("savedTiming").getIntValue();
		backdropFileName = element.getAttribute("BackDropFile").getStringValue();

		if (!backdropFileName.equals("null")) {
	    Image backdrop = getToolkit().getImage(backdropFileName);
	    designPanel.backgroundImage = backdrop;
			loadBackDrop.setState(true);
		} else {
			designPanel.backgroundImage = null;
			loadBackDrop.setState(true);
		}
	}

	/**
	 * Calls a load for the children of this class.
	 * @param loader a <code>XMLLoader</code> value
	 */
	public void loadChilds(XMLLoader loader) { 
		loader.loadObject(designPanel);

		panelIndex = ROADDESIGNER;
		pack();
		designPanel.refreshCanvass();
		repaint(); //hack to solve display problem
	}

	//**********************************************************************
	//******************** GUI Component Operations ************************
	//**********************************************************************

	private JButton addButton(ImageIcon icon,int x,int y,int w) {
		JButton button = new JButton(icon);
		GridBagConstraints con = new GridBagConstraints();
		con.anchor = GridBagConstraints.NORTH;
		con.fill = GridBagConstraints.BOTH;
		con.gridx = x;
		con.gridy = y;
		con.gridwidth=w;
		gridBag.setConstraints(button,con);
		
		button.addActionListener(this);   
		buttonPanel.add(button);
		return button;
	}

	private JToggleButton addToggleButton(ImageIcon icon,int x,int y,int w,
																				boolean sel){
		JToggleButton button = new JToggleButton(icon,sel);
		GridBagConstraints con = new GridBagConstraints();
		con.anchor = GridBagConstraints.NORTH;
		con.fill = GridBagConstraints.BOTH;
		con.gridx = x;
		con.gridy = y;
		con.gridwidth=w;
		gridBag.setConstraints(button,con);
				
		button.addActionListener(this);
		buttonGroup.add(button);
		buttonPanel.add(button);
		return button;
	}

	private JSlider addSlider(int rule,int start, int end, int value,
													 int x,int y,int w){ 
		JSlider slider = new JSlider(rule,start,end,value);
		GridBagConstraints con = new GridBagConstraints();
		con.gridx = x;
		con.gridy = y;
		con.gridwidth=w;
		con.gridheight = GridBagConstraints.REMAINDER;
		gridBag.setConstraints(slider,con);

		slider.setInverted(true);
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(5);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);

		buttonPanel.add(slider);
		return slider;
	}

	private void addLabel(ImageIcon icon,int x,int y,int w) {
		JLabel l = new JLabel(icon);
		GridBagConstraints con = new GridBagConstraints();
		con.anchor = GridBagConstraints.NORTH;
		con.gridx = x;
		con.gridy = y;
		con.gridwidth=w;
		gridBag.setConstraints(l,con);

		buttonPanel.add(l);
	}

	private void addSeperator(int x,int y) { 
		Component c = Box.createRigidArea(new Dimension(30,30));
		GridBagConstraints con = new GridBagConstraints();
		con.gridx = x;
		con.gridy = y;
		gridBag.setConstraints(c,con);

		buttonPanel.add(c);
	}

	private JMenuItem addMenuItem(String message, ImageIcon icon, JMenu menu) {
		
		JMenuItem menuItem = new JMenuItem(message,icon);
		menu.add(menuItem);
		menuItem.addActionListener(this);
		
		return menuItem;
	}

	private JCheckBoxMenuItem addCheckBoxMenuItem(String message, boolean state,
																							 JMenu menu) {
		JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(message,state);
		menu.add(menuItem);
		menuItem.addActionListener(this);
		
		return menuItem;
	}


	private JMenu addCarSpeedOptions(String message, int ops , 
																	 ButtonGroup buttonGroup,JMenu menu) {

		carSpeedOptions = new JRadioButtonMenuItem[ops];
		int speed = 30;

		JMenu subMenu = new JMenu(message);
		menu.add(subMenu);
		subMenu.addActionListener(this);
				
		for (int i=0;i<ops;i++) {
			if (i%2 == 0) {
				carSpeedOptions[i] = new JRadioButtonMenuItem((speed-5) + "-" + 
																											(speed+5) + " mph");
			} else {
				carSpeedOptions[i] = new JRadioButtonMenuItem(speed+" mph");
				speed += 10;
			}

			if (savedSpeed == i) carSpeedOptions[i].setSelected(true);
			carSpeedOptions[i].addActionListener(this);
			buttonGroup.add(carSpeedOptions[i]);
			subMenu.add(carSpeedOptions[i]);
		}

		return subMenu;
	}

	//**********************************************************************
	//******************** Internal Class **********************************
	//**********************************************************************

	/** Listens to the slider. */
	class SliderListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			JSlider source = (JSlider)e.getSource();
	    savedTiming = (int)source.getValue();
	    simPanel.changeDelay(savedTiming);
		}
	}

	
  class CloseWindow extends WindowAdapter {
    public void windowClosing(WindowEvent e) {
      quitAll();
    }
    
    public void windowActivated(WindowEvent e) {}
  }

	/**
	 * This class is a Filter to a filechooser so only image files can be seen.
	 * @author Thomas Fotherby
	 */
	private class PicFilter extends FileFilter { 
	
		public final static String jpg = "jpg";
		public final static String jpeg = "jpeg";
		public final static String gif = "gif";
	
		/** Gets the extention of a file eg tom.gif = gif */
		private String getExtension(File f){ 
	    String ext = null; 
	    String s = f.getName(); 
	    int i = s.lastIndexOf('.'); 
	    
	    if(i>0 && i<s.length() -1){ ext = s.substring(i+1).toLowerCase(); } 
	    return ext; 
		} 
	
		/** returns true if the extention is *.gif, *.jpg, *.jpeg */
		public boolean accept(File f){ 
	    if(f.isDirectory()){ return true;}
	    
	    String extension = getExtension(f); 

	    if(extension != null){ 
				if(extension.equals(jpg)){return true;} 
				if(extension.equals(jpeg)){return true;} 
				if(extension.equals(gif)){return true;} 
				else{return false;} 
	    }     
	    return false; 
		} 
	
		/** returns a message */
		public String getDescription(){return("Image files only");} 
	}
}







