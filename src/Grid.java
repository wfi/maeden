// package maeden;

import java.lang.Math;
import java.util.StringTokenizer;

///*maedengraphics
import java.awt.*;
//maedengraphics*/
import java.awt.Point;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *@author:  Wayne Iba,
 *@author:  assistance from: Kristin Barquer, Cuyler Cannon, Josh Holm,
 *@author:  Brennan Johnson, Pablo Otoala, JB Schiller, Ryan Wisdom,
 *@outhor:  CS116 Fall 2011, especially Aaron Panchal Morgan Vigil, and Kelly Macdonald
 *@date:    3-12-2012
 *@version: Beta 0.5
 */

public class Grid
///*maedengraphics
  extends Frame
//maedengraphics*/
{

    // window and grid variables
    private int xCols, yRows; // logical size of grid where yRows number of rows
    private int squareSize;   //size in pixels of one side of a cell
    public int worldTime;     //the world time, a clock reflecting n * WORLD_CYCLE_TIME for n cycles through the run loop
    private ComSentence comMsg;  //
    private StringTokenizer msgTokenizer;
    private int talkDist = Integer.MAX_VALUE;  //distance in cells a message with volume talk will travel
    private int shoutDist = Integer.MAX_VALUE; //distance in cells a message with volume shout will travel
    public boolean killGrid = false;     //grid will exit if true
    ///*maedengraphics
    private Insets iTrans;
    private boolean showDisplay = true;  //set true if a graphical display is desired, false otherwise
    private Image offscreen;
    private int physWid, physHt;
    //private Graphics g;
    //maedengraphics*/

    // items
    private LinkedList<ComSentence> msgs = new LinkedList<ComSentence>();   //holds agent messages
    private LinkedList<GridObject> gobs = new LinkedList<GridObject>();   //holds world gridobjects
    private LinkedList<GOBAgent> agents; //holds world agents
    private LinkedListGOB[][] myMap;                 //holds gridobjects

    // misc (possibly temporary) variables
    private GridObject food;                   //world goal
    public static final int MAX_AGENTS = 10;
    public static final int MAEDENPORT = 7237; //host server port number
    private ServerSocket gwServer;	// server-socket for listening for connection requests

    public boolean EAT_FOOD_ENDS_IT = true;	// control if eating food terminates sim (true) or increases energy (false)
    public int WORLD_CYCLE_TIME = 50;	// replaces sleepTime to control wall-time length of simulation cycle

    // Constructors

    // initialize Grid from file: largely from Cuyler Cannon
    public Grid(String filePath, int approxWidth, boolean showD) throws FileNotFoundException, IOException {

	// Initialize ServerSocket to listen for controllers
    	/* 09/10/11
    	 * mvigil
    	 * We may need to create a select channel
    	 * in order to establish a non-blocking
    	 * loop for the server to listen on.
    	 */
    	
	try {
            gwServer = new ServerSocket(MAEDENPORT);	//create new server Socket on Maeden port
        }
        catch(IOException e) {
            System.err.println("could not listen on port: " + MAEDENPORT);
	    System.exit(1);   //exit if cannot use the port number
        }

        /** Parse Data File **/
        WorldReader gridspaceInputData = new WorldReader(filePath);

        xCols = gridspaceInputData.cols();                // get row and col sizes from world file
        yRows = gridspaceInputData.rows();

	// Initialize grid map now from read sizes
	myMap = new LinkedListGOB[xCols][yRows]; // note: non-conventional order of columns, rows
	agents = new LinkedList<GOBAgent>();

	// set cell size from desired physical window width and logical size found in file
	squareSize = approxWidth / xCols;
	approxWidth = squareSize * xCols;

        /** Read in the Character Map **/
	for (int y = 0; y < yRows; y++)
	    for (int x = 0; x < xCols; x++){
		switch (gridspaceInputData.map(x,y)) {
		case ' ':
		    // ignore, for this represents the absence of an object
		    break;
		case 'B':       /** AGENT **/
		    // place the FoodCollector here
		    GOBFoodCollect fc = new GOBFoodCollect(x,y,squareSize);
		    // start the AgentListener thread
		    AgentListener al = new AgentListener(x,y,squareSize,this,gwServer,'W');
		    al.start();
		    //System.out.println("AgentListener just started");
		    break;
		case '+': //food
		    food = new GOBFoodSupply(x,y,squareSize);
		    addGOB(food);
		    break;
		case '*': //wall
		    addGOB(new GOBWall(x,y,squareSize));
		    break;
		case '@': //rock
		    addGOB(new GOBRock(x,y,squareSize));
		    break;
		case '#': //door
		    addGOB(new GOBDoor(x,y,squareSize));
		    break;
		case '=': // narrows
		    addGOB(new GOBNarrows(x,y,squareSize));
		    break;
		case 'K': //key
		    addGOB(new GOBKey(x,y,squareSize));
		    break;
		case 'T': //hammer
		    addGOB(new GOBHammer(x,y,squareSize));
		    break;
		case 'Q': //quicksand
		    addGOB(new GOBQuicksand(x,y,squareSize));
		    break;
		case '$': //gold
		    addGOB(new GOBGold(x,y,squareSize
				       ///*maedengraphics
				       , this
				       //maedengraphics*/
				       ));
		    break;
		case 'R': // robotMonster
		    addGOB(new GOBRobot(x,y,squareSize
					///*maedengraphics
					, this
					//maedengraphics*/
					));
		    break;
		case 'G': // rayGun
		    addGOB(new GOBRayGun(x,y,squareSize
					///*maedengraphics
					, this
					//maedengraphics*/
					));
		    break;
		default:
		    System.out.println("Unrecognized symbol error at (" + x + "," + y + "): " + gridspaceInputData.map(x,y));
		    return;
		}
	    }

	///*maedengraphics
	showDisplay = showD;                                              //if true, show graphical display
	if(showDisplay) {
	    setVisible(true);
	    // Set Window Frame Size
	    iTrans = getInsets();                 // obtains the border widths (window insets)
	    physWid = approxWidth + iTrans.left + iTrans.right;
	    physHt = squareSize * yRows + iTrans.top + iTrans.bottom;
	    setSize(physWid, physHt);
	    //System.out.println("intended size: " + (physWidth + iTrans.left + iTrans.right) + "x" + (squareSize*yRows + iTrans.top + iTrans.bottom) + " and actual: " + getSize().width + "x" + getSize().height);

	    String title = gridspaceInputData.windowTitle();  // get graphics title from world file
	    setTitle(gridspaceInputData.windowTitle());
	}
	//maedengraphics*/
	//gwServer.close();                     //close server
    }

    //public accessor for the grid map
    public LinkedListGOB[][] myMap() {
	return myMap;
    }

    //public accessor for the gobs
    public LinkedList<GridObject> gobs() {
	return gobs;
    }

    //public accessor for the agents
    public LinkedList<GOBAgent> agents() {
	return agents;
    }

    //public accessor for the messages
    public LinkedList msgs() {
	return msgs;
    }


    /**
     * run: run the world
     *
     */
    public void run() {
	while(true) {
	    try { processAgentActions(); }
	    catch (Exception e) {System.out.println("run: failure reading agent actions " + e); }
	    try { sendAgentSensations(); }
	    catch (Exception e) {System.out.println("run: failure sending sensations " + e); }
	    updateWorldTime();
	    // sleep time controls the speed of the simulation
	    try {Thread.sleep(WORLD_CYCLE_TIME);} catch (Exception e) {System.out.println("error with sleeping"); }
	   
	    if(killGrid)
		cleanClose();
	    ///*maedengraphics
	    if(showDisplay){
		repaint();
	    }
	    //maedengraphics*/
	}
    }
    
    /**
     * processAgentActions: for each of the agents, if they have actions
     * to be processed, get them and do whatever needs to be done.
     */
    public void processAgentActions() {
	GOBAgent a;
	try {
	    for (Iterator<GOBAgent> i = agents.iterator(); i.hasNext(); ){ // TODO: genericize Iterator
		a = i.next();
		a.getNextCommand();           //have current agent get next command from controller process
	    }
	} catch (Exception e) { System.out.println("Failed reading the next command: " + e);}
	try {
	    for (Iterator<GOBAgent> i = agents.iterator(); i.hasNext(); ){    //process and perform each agent's action
		a = i.next();
		//Process the action only if there is a next command
		if(a.nextCommand() != null)
		    {
			a.processAction(a.nextCommand());
			a.setNeedUpdate(true);
		    }
	    }
	} catch (Exception e) { System.out.println("Failed processing the next command just read: " + e);}
	//System.out.println("About to collect messages");
	try {
	    getAgentMessages();             //places any messages generated from agent actions inside msgs linked list
	} catch (Exception e) { System.out.println("Failed processing the messages: " + e);}
	//System.out.println("Messages collected");
	boolean foundBase = false;
	try {
	    for(Iterator<GOBAgent> i = agents.iterator(); i.hasNext();) {          //remove any dead agents
		a = i.next();
		switch(a.status()) {
		case 'c':                        //agent is alive, hasn't found the food
		    if ( (a.getAgentRole()).equals("base") ){ foundBase = true; }
		    break;
		case 'd':
		    while ( a.inventory().size() > 0 )
			a.drop("drop");             //drop all items from inventory
		    a.cleanDie(); i.remove();   //agent died from lack of energy or quicksand
		    break;
		case 's': killGrid = true;		//agent found the food, end the simulation
		    break;
		default: 
		    break;
		}
	    }
	} catch (Exception e) { System.out.println("Failed in final processing: " + e);} 
	//**** need to fix this ****  .... if (!foundBase) killGrid = true;
    }
    
    /**
     * sendAgentSensations: for each agent, send their sensory information
     * LINE1: The number of lines that are going to be sent (excluding this line) *could also be d, e, or s (die, end, success)*
     * LINE2: smell direction to food
     * LINE3: inventory in form ("inv-char")
     * LINE4: visual array (as single string) in form ((row ("cell") ("cell"))(row ("cell")))
     * LINE5: ground contents of agent position in form ("cont" "cont")
     * LINE6: Agent's messages
     * LINE7: Agent's energy
     * LINE8: last action's result status (ok or fail)
     */
    public void sendAgentSensations() {
	GOBAgent a;
	for (Iterator<GOBAgent> i = agents.iterator(); i.hasNext(); ){  //for each agent do this
	    a = (GOBAgent)i.next();
	    if (a.getNeedUpdate()) {
		// 0. Maeden Directive
		a.send().println("8");             //number of subsequent lines about to be sent to agent controller
	
		// 1. send smell
		a.send().println(relDirToPt(a.pos, new Point(a.dx(), a.dy()), food.pos));
	
		// 2. send inventory
		String inv = "(";
		if (a.inventory().size() > 0){
		    //a.send().println("(\"" + a.inventory().printChar() + "\")");  //encapsulate inventory in (" ")
		    for (Iterator invItr = a.inventory().iterator(); invItr.hasNext(); ){
			inv += "\"" + ((GridObject)invItr.next()).printChar() + "\" ";
		    }
		}
		inv = inv.trim() + ")";
		a.send().println(inv); 
	
		// 3. send visual info
		a.send().println(visField(a.pos, new Point(a.dx(), a.dy())));
		//System.out.println(visField(a.pos, new Point(a.dx(), a.dy())));
	
		// 4. send ground contents of current location
		a.send().println(groundContents(a, myMap[a.pos.x][a.pos.y]));
	
		// 5. send any messages that may be heard by the agent
		sendAgentMessages(a);
	
		// 6. send agent's energy
		a.send().println(a.energy());
	
		// 7. send last-action status
		a.send().println(a.lastActionStatus());
		    
		// 8. send world time
		a.send().println(worldTime);
	    }
	    a.setNeedUpdate(false);
	}

	msgs.clear();              //once messages are sent, they don't need to be saved any longer
    }

    /*
     * groundContents iterates through the cell the agent is standing on and returns a string of chars
     * enclosed in quotes and parens to represent what is in the cell
     * Pre: a is GOBAgent who is in cell thisCell
     * Post: String is returned in form: ("cont1" "cont2" "cont3" ...)
     *       where cont is the individual contents of the cell
     */
    public String groundContents(GOBAgent a, LinkedList thisCell) {
	if (thisCell != null && ! thisCell.isEmpty()) {
	    //encapsulate contents within parentheses
	    String ground = "(";
	    //iterate through the cell, gather the print-chars
	    for(Iterator i = thisCell.iterator(); i.hasNext();) {
		GridObject gob = (GridObject) i.next();
		//if the gob is an agent (and not the one passed in) get the agent id
	        if((gob.printChar() == 'A' || gob.printChar() == 'H') && ((GOBAgent) gob != a)) {
		    ground = ground + "\"" + ((GOBAgent)gob).getAgentID() + "\" ";   // \" specifies the string "
		}
		else if(gob.printChar() != 'A' && gob.printChar() != 'H') {
		    ground += "\"" +  gob.printChar() + "\" ";
		}
	    }
	    ground = ground.trim() + ')';  //trim any leading or ending spaces, close paren
	    return ground;
	}
	return "()";
    }

    /**
     * updateWorldTime: update the world time
     */
    public void updateWorldTime(){

	// update local book keeping: time, energy(?), ...
	worldTime++;
    }
	

    // add a GridObject to the Grid
    public void addGOB(GridObject val){
	synchronized (gobs){
	    gobs.addLast(val); // add object to the last slot of the gobs linked list
	}
	if (myMap[val.pos.x][val.pos.y] == null)
	    myMap[val.pos.x][val.pos.y] = new LinkedListGOB(); // if cell is null, create new linked list
	if (myMap[val.pos.x][val.pos.y] != null){
	    LinkedListGOB lgobs = myMap[val.pos.x][val.pos.y];
	    synchronized (lgobs){
		if ( val.printChar() == 'A' || val.printChar() == 'H' )
		    lgobs.addLast(val);   // add agents to the end of cell's linked list
		else
		    lgobs.addFirst(val);  // add all other objects to the front of list
	    }
	}
    }

    // remove a GridObject from the Grid
    public void removeGOB(GridObject val) {
	try {
	    synchronized (gobs){
		if ( gobs.contains(val) )          //if GridObject is in gobs, remove it
		    gobs.remove(val);
		else throw new NoSuchElementException();
	    }
	    if ( myMap[val.pos.x][val.pos.y] != null           //if GridObject is in cell, remove it
		 && myMap[val.pos.x][val.pos.y].contains(val) )
		synchronized (myMap[val.pos.x][val.pos.y]){
		    myMap[val.pos.x][val.pos.y].remove(val);
		}
	    else throw new NoSuchElementException();
	} catch(NoSuchElementException e) { } // If val not found, then we can assume the desired result 
    }
	    

    // determine if spot has a tool
    public boolean cellHasTool(int x, int y, char tool){

	// if cell is initialized and is not empty, iterate through and see if sought item is here
	if ( myMap[x][y] != null && ! myMap[x][y].isEmpty() ){
	    GridObject gob;
	    for (Iterator e = myMap[x][y].iterator(); e.hasNext(); ){
		gob = (GridObject) e.next();
		if ("+kKtT$G".indexOf(Character.toString(gob.printChar())) >= 0
		    // (gob.printChar() == '+' || gob.printChar() == 'K' || gob.printChar() == 'T')
		    &&
		    (gob.printChar() == tool)) //if gold, food, key, or hammer is in cell, and that is tool that is wanted return true
		    return true;
	    }
	}
	return false;  //if no food, key, or hammer, return false
    }

    // returns the first tool in the indicated cell, throws noSuchElement exception if none
    public GridObject getTool(GOBAgent a, int x, int y, char tool) throws NoSuchElementException {
	if ( myMap[x][y] != null && ! myMap[x][y].isEmpty() ){          //if cell is initialized and not empty, iterate through it
	    GridObject gob;
	    for (Iterator<GridObject> e = myMap[x][y].iterator(); e.hasNext(); ){
		gob =  e.next();
		if ((true || gob.printChar() == '+' || gob.printChar() == 'K' || gob.printChar() == 'T')
		    &&
		    (gob.printChar() == tool))
		    return gob.onGrab(a);        //if food, key, or hammer, return this
	    }
	}
	throw new NoSuchElementException();  //otherwise no tool is in cell
    }
	

    /**
     * a spot will either be empty, in which case it is passable
     * or it will contain one or more objects
     * we can check an arbitrary object since either they are all shareable
     * or there can only be one
     */
    public boolean passable(Point p, GridObject gob){
	return passable(p.x, p.y, gob);
    }
    public boolean passable(int x, int y, GridObject gob){
	if ((myMap[x][y] == null) || (myMap[x][y].size() == 0))
	    return true;
	else {
	    for(Iterator<GridObject> i = myMap[x][y].iterator(); i.hasNext();) {
		GridObject gObj = i.next(); 
		//if it is an obstacle or another base agent
		if(!gObj.allowOtherGOB(gob)) {
		    return false;
		}
	    }
	}
	return true;
    }
	    

    /**
     * relDirToPt: compute the relative direction to a given point
     * with respect to a presumed agent at another point with a
     * heading, given as a point.  Index table for agent-to-target compass headings
     * x N E S W
     * N F R B L
     * E L F R B
     * S B L F R
     * W R B L F
     * INPUT: agent point, agent heading, target location
     * OUTPUT: char: one of F(orward), R(ight), L(eft) or B(ack) or H(ere)
     */
    public char relDirToPt(Point aPt, Point aDir, Point target){
	int xDisplace = target.x - aPt.x;                     //difference in x and y directions
	int yDisplace = target.y - aPt.y;
	int fDiff = aDir.x * xDisplace + aDir.y * yDisplace;  //takes into account the agent heading
	int rDiff = aDir.x * yDisplace + (- aDir.y) * xDisplace;
	if (Math.abs(rDiff) > Math.abs(fDiff)) {
	    if (rDiff > 0) return 'r';
	    else return 'l';
	} else {
	    if (fDiff > 0) return 'f';
	    else if (fDiff < 0) return 'b';
	    else return 'h';
	}
    }

    /**
     * absDirToPt: compute the absolute (compass) direction to a given target point
     * with respect to a given reference point
     * INPUT: agent point, target location
     * OUTPUT: char: one of N(orth), S(outh), E(ast) or W(est) or H(ere)
     */
    public char absDirToPt(Point aPt, Point target){
	int xdiff = aPt.x - target.x;             //difference in x and y directions
	int ydiff = aPt.y - target.y;
	if (xdiff == 0 && ydiff == 0) return 'H'; //if no difference, point is here
	if (Math.abs(xdiff) > Math.abs(ydiff)){   // absolute heading either E or W
	    if (xdiff > 0) return 'W';
	    else return 'E';
	} else {
	    if (ydiff > 0) return 'N';
	    else return 'S';
	}
    }

    /**
     * visField: extract the local visual field to send to the agent controller
     * INPUT: agent point location, and agent heading (as point)
     * OUTPUT: sequence of characters
               parens encapsulate three things: the whole string
	                                        the row
						the individual cells
	       quotes encapsulate individual cell contents				
	       (string (row1 ("cell5") ("cell4")...)(row2 ("cell5")...)...)
     * The row behind the agent is given first followed by its current row and progressing away from the agent
     * with characters left-to-right in visual field.
     */
    public String visField(Point aPt, Point heading){
	String myString = "(";
	int senseRow, senseCol;
	//iterate from one behind to five in front of agent point
	for (int relRow=-1; relRow <= 5; relRow++) {
	    //add paren for the row
	    myString += "(";
	    String rowString = "";
	    //iterate from two to the left to two to the right of agent point
	    for (int relCol=-2; relCol <= 2; relCol++){
		senseRow = aPt.x + relRow * heading.x + relCol * -heading.y;
		senseCol = aPt.y + relRow * heading.y + relCol * heading.x;
		//add cell information
	        rowString += " " + visChar(mapRef(senseRow, senseCol), heading);
	    }
	    //trim any leading or closing spaces, close row paren
	    myString += rowString.trim() + ")";
	}
	//return string with close paren
	return myString + ')';
    }

    /* visChar iterates through the gridobjects located in a cell and returns all of their printchars
     * enclosed in parens and quotes: ("cont1 cont2 cont3")
     * The one exception is the agent.  For an agent, its agent-id is returned (0-9)
     * Note: the heading of an agent is not reported at this time.
     * Pre: cellContents contains any and all gridobjects in a cell
     * Post: String ("cont1 cont2 cont3") is returned (where cont1-3 are gridobject printchars or agent IDs)
     */
    private String visChar(LinkedList cellContents, Point heading){
	String cellConts = "(";
	//if there are any gridobjects in the cell iterate and collect them
	if (cellContents != null && !cellContents.isEmpty()) {
	    //iterate through cellContents, gather printchars or agent IDs
	    for(Iterator i = cellContents.iterator(); i.hasNext();) {
		//look at the next item in the cell
		GridObject gObj = (GridObject) i.next(); 
		if(gObj.printChar() == 'A') { 		//if it is a base agent
		    if(((GOBAgent)gObj).getAgentID() < 10) {
			//get the agent id
			cellConts = cellConts + "\"" + ((GOBAgent)gObj).getAgentID() + "\" ";
		    }
		    //if it is an agent, but bad ID, put 'A' as printchar
		    else {
			System.out.println("Agent ID is greater than 10, must be less than 10");
			cellConts = cellConts + "\"" + gObj.printChar() + "\" ";
		    }
		}
		else if (gObj.printChar() == 'H') { // if it is a helper agent
		    if(((GOBAgent)gObj).getAgentID() < 10) {
			//get the agent id
			cellConts = cellConts + "\"" + ((GOBAgent)gObj).getAgentID() + "\" ";
		    }
		    //if it is an agent, but bad ID, put 'A' as printchar
		    else {
			System.out.println("Agent ID is greater than 10, must be less than 10");
			cellConts = cellConts + "\"" + gObj.printChar() + "\" ";
		    }
		}
		//if gridobject is not an agent, return its print character
		else
		    cellConts = cellConts + "\"" + gObj.printChar() + "\" ";
	    }
	    //trim leading and closing spaces
	    cellConts = cellConts.trim() + ')';
	    return cellConts;
	}
	//otherwise return a space representing no gridobject
	else
	    return "()";
    }



    /**
     * mapRef: safe map reference checking for out-of-bounds
     */
    private LinkedList mapRef(int x, int y){
	if ( (x < 0) || (x >= xCols) || (y < 0) || (y >= yRows) ) return null;
	else return myMap[x][y];
    }

    /*getAgentMessages iterates through all agents on the map and stores any messages they have in the msgs linkedlist
     *Pre: msgs has been initialized
     *Post: all messages are stored in msgs linkedlist
     */
    public void getAgentMessages() {
	for(Iterator<GOBAgent> e = agents.iterator(); e.hasNext();) {      //get all messages from all agents and store them
	    GOBAgent a = e.next();
	    if(a.hasMsg())                                       //if agent has message, store it
		msgs.addLast(a.msg());
	}

    }

    /*sendAgentMessages sends any and all relevant messages to the passed in agent
     * If an agent is within hearing distance of a message,
     * add that message to the rest of the messages that 
     * can be heard by the agent and send them all
     ***Note: It is assumed that an agent can only send one message at a time, but can receive multiple messages***
     ***Note: The messages are bound inside parentheses in form : (Message1 Message2...MessageN) ***
     *Post: all messages from all agents are sent to any other agent in its viciniy
     */
    public void sendAgentMessages(GOBAgent ag) {
	int msgDist;
	String newMsg = "";
	//iterate through the messages
        for(Iterator x = msgs.iterator(); x.hasNext();) {
	    //get the next message
	    ComSentence thisMsg = (ComSentence) x.next();
	    //if it was not sent by the current agent, check to see if agent should hear message
	    if(!(thisMsg.ID() == ag.getAgentID())) {
		//if volume is shout, set msgDist to the shout distance, otherwise to talkDist
		if(thisMsg.volume().equalsIgnoreCase("shout"))
		    msgDist = shoutDist;
		else
		    msgDist = talkDist;
		//check distance from sender
		if(Math.abs(ag.pos.x - thisMsg.origin().x) < msgDist && Math.abs(ag.pos.y - thisMsg.origin().y) < msgDist) {
		    //get direction from sender to receiver
		    for(Iterator<GOBAgent> i = agents.iterator(); i.hasNext();) {
			GOBAgent thisAgent = i.next();
			if(thisAgent.getAgentID() == thisMsg.ID()) {
			    Point agHeading = new Point(ag.dx(), ag.dy());
			    thisMsg.setDir(relDirToPt(ag.pos, agHeading, thisAgent.pos));
			    break;
			}
		    }
		    //add message to other messages
		    newMsg = newMsg + thisMsg.createComSentence();
		}
	    }
	}
	//enclose the message in parentheses (part of message syntax)
	newMsg = "(" + newMsg + ")";
	//send all relevant messages to agent
	ag.send().println(newMsg);
	//System.out.println(newMsg);
    }


    /** Draw the grid lines and then draw any objects.
     *  Someday, should make the gridlines drawn only initially and then
     *  make sure they are never touched by the grid objects to save some drawing time.
     */
    ///*maedengraphics
    public void paint(Graphics rg){
	Dimension d = getSize();
	if ( d.width != physWid || d.height != physHt )
	    setSize(physWid, physHt);
	if ( offscreen == null || offscreen.getWidth(null) != d.width || offscreen.getHeight(null) != d.height )
	    offscreen = createImage( d.width, d.height );

	Graphics g = offscreen.getGraphics();
	//System.out.println("image dimension: " + d.width + "x" + d.height);
	
	g.setColor(getBackground());
	g.fillRect(0, 0, d.width, d.height);
    
	iTrans = getInsets();             //window margins
	g.translate(iTrans.left, iTrans.top); //compensates for window margins
	g.setColor(Color.gray.brighter());          //set the color

	// draw the grid lines
	for (int i=0; i < yRows; i++)   //draw horizontal lines
	    g.drawLine(0,(i*squareSize),(xCols*squareSize),(i*squareSize));

	for(int j=0; j < xCols; j++)    //draw vertical lines
	    g.drawLine((j*squareSize),0,(j*squareSize),(yRows*squareSize));

	// draw the objects
	for(Iterator e = gobs.iterator(); e.hasNext(); ){
	    ((GridObject)e.next()).paint(g);   //each gridobject paints itself
	}
	g.translate(-iTrans.left, -iTrans.top);
	
	if ( ! rg.drawImage(offscreen, 0, 0, null) )
	    System.out.println("Didn't finish loading, . . .");
	g.dispose();

    }

    // update: override the default update
    public void update(Graphics g) { paint(g); }

    //maedengraphics*/

    /** cleanClose provides a way to cleanly close all buffers and sockets and exit the grid
     * calls the cleanDie method for each agent which closes their buffers and sockets, then shuts down serversocket
     * Only called when an agent eats the food, or when there are no agents still living in grid
     * POST: buffers and sockets closed, grid exits
     */
    public void cleanClose() {
	// *********** only for demo purposes *** remove
	//try {Thread.sleep(20000);} catch (Exception e) {System.out.println("error with sleeping"); }
	// *** remove **********************************
	if( agents != null && !agents.isEmpty() ) {  //if there are agents on the grid still
	    for(Iterator<GOBAgent> i = agents.iterator(); i.hasNext();) {  //iterate through and close their connections
		GOBAgent g =  i.next();
		g.printstats();
		g.send().println("End");              //Other agent got food, simulation ended
		g.cleanDie();
		i.remove();                        //remove agent
	    }
	}
	try {
	    // gwServer.close();                     //close server
	}
	catch(Exception e) {System.out.println("error closing server socket");}
	System.exit(4);  //exit
    }

    /** Kicks everything off
     * takes 1 required and 2 additional optional args from the command line:
     * Arg1: String (World file name (defaults to "worlds/sampleWorld3.txt"))
     * Optional Args:
     * Arg2: boolean (true if graphical display is desired)
     * Arg3: int (number of milliseconds to sleep between simulation steps.  Default=50)
     * Arg4: [to do: control EAT_FOOD_ENDS_IT flag]
     * ***Note: args must appear in this order, main looks at first arg as world file name, 2nd as boolean, and 3rd as num of agents***
     */
    public static void main(String[] a){
	int windowWidth = 600;
	Grid myGrid;
	boolean showD = true;

	// populate world from file
	try {
	    if(a.length > 0) {
		if(a.length >= 2) {
		    if(a[1].equalsIgnoreCase("false"))  //if 2nd arg is false, don't show graphical display
		      showD = false;
		}
		myGrid = new Grid(a[0], windowWidth, showD);
		if(a.length >= 3) {
		    myGrid.WORLD_CYCLE_TIME = Integer.parseInt(a[2]); // cycle time for simulation
		}
		/* TO DO: Control EAT_FOOD_ENDS_IT
		if(a.length >= 4) {
		}
		*/
	    } 
	    else {
		myGrid = new Grid("worlds/miscX1", windowWidth,showD);
	    }
	    ///*maedengraphics
	    //if(showD)  //if graphical display is desired, show the window
	    //   myGrid.setVisible(true);
	    //maedengraphics*/
	    myGrid.run();  //run the simulation
	    
	}
	catch (FileNotFoundException e) { System.out.println("Could not find file"); }
	catch (Exception e) { System.out.println("Some exception: " + e); }
    }
    
    /**
     * AgentListener
     * private inner class that listens for connection requests from agent controllers
     */
    class AgentListener extends Thread {

    	private ServerSocket srvSock;
    	private int x;
    	private int y;
    	private int squareSize;
    	private Grid grid;
    	private char head;
    	// constructor
    	AgentListener(int ix, int iy, int s, Grid mg, ServerSocket ss, char heading){
    	    srvSock = ss;
    	    x = ix;
    	    y = iy;
    	    squareSize = s;
    	    grid = mg;
    	    head = heading;
    	}
    	
    	// the run method for this thread gets called by start()
    	public void run() {
    	    Socket tSock;
    	    while (true) {
    		try {
    		    if ( agents().size() < MAX_AGENTS ){	// if game not full, . . .
    		    	tSock = srvSock.accept();		// listen for connection, and
    		    	GOBAgent gagent = new GOBAgent(x,y,squareSize,grid,tSock,head);
			grid.addGOB(gagent); // addGOB(...) is synchronized on gobs
			synchronized (agents) {
			    agents.addLast(gagent);
			}
			try { sendAgentSensations(); }
			catch (Exception e) {System.out.println("AgentListener.run(): failure sending sensations " + e); }
    		    }
		    Thread.sleep(50);
    		} catch (IOException e) { System.out.println("AgentListener.run(): failed accepting socket connection: " + e);
		} catch (Exception e) {
		    System.out.println("AgentListener.run(): some other exception: ");
		    e.printStackTrace();
		}
    	    }
    	}
    }
}
 
 