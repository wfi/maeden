package org.maeden.simulator;

import java.lang.Math;
///*maedengraphics
import java.awt.*;
//maedengraphics*/
import java.awt.Point;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * @author:  Wayne Iba and Josh Holm
 * @date:    12-1-2010
 * @version: Beta 0.4
 *
 * GOBAgent objects have responsibility to check legality of moves.
 * A general issue is granting authority for the object to move itself
 * but since the agent controller is always mediated by the Grid itself,
 * this may be reasonable.
 */

public class GOBAgent extends GridObject {

    // Agent global parameters, switches, flags, ...
    private boolean STOCHASTICISM = false;		// control if actions always have intended effect (when true)
    private double STOCHASTIC_RATE = 0.1;		// probability of unintended effect when STOCHASTICISM is true
    private Random randGenerator = new Random();
    private int ATTACK_LOSS = 100;			// amount of energy lost when get hit
    private int INVENTORYCAPACITY = 100;

    // OTHERS ....
    private ComSentence agentCom;      //for constructing messages
    private static int idSequence = 0; // source for unique agent IDs
    ///*maedengraphics
    private static Color[] colorSet = { Color.cyan, Color.blue, Color.green, Color.orange };
    //maedengraphics*/
    private Socket conn;               //socket connection
    private PrintWriter send;          //for writing to the socket
    private BufferedReader recv;       //for reading from the socket
    private String nextCommand;        //string to hold agent command
    private String lastActionStatus = "ok";
    private boolean haveMsg = false;
    private char status = 'c';		// continuing (?)
    private boolean needUpdate = true; // flag for indetifying if an agent acts and needs updated sensor info
    
    private int myID;                  // used to distinguish this agent from others
    private String myRole;             // base, helper, etc.

    private int dx, dy;                //heading x and y coor.
    public LinkedList<GridObject> inventory;
    private Grid myGrid;
    private static final Map<String, Integer> costs;
    //can't currently change keys to action characters
    //same character for "useKey" and "useHammer"
    static {
    	costs = new HashMap<String,Integer>();
    	costs.put("forward", 5);
    	costs.put("back", 5);
    	costs.put("turn", 3);
    	costs.put("wait", 1);
    	costs.put("grab", 2);
    	costs.put("drop", 1);
    	costs.put("useT", 15);
    	costs.put("useK", 2);
	costs.put("useG", 2);
    	costs.put("talk", 2);
    	costs.put("shout", 4);
    	costs.put("attack", 15);
    }
    private int agentEnergy = 2000;
    //private int comPayment = 1000;
    ///*maedengraphics
    //east facing agent
    private int[] epxs = {2,2,scale-2};
    private int[] epys = {scale/4, scale*3/4, scale/2};
    private Polygon eastTri = new Polygon(epxs, epys,3);

    //north facing agent
    private int[] npxs = {scale/4, scale*3/4, scale/2};
    private int[] npys = {scale-2,scale-2,2};
    private Polygon northTri = new Polygon(npxs,npys,3);

    //west facing agent
    private int[] wpxs = {scale-2,scale-2,2};
    private int[] wpys = {scale/4, scale*3/4, scale/2};
    private Polygon westTri = new Polygon(wpxs,wpys,3);

    //south facing agent
    private int[] spxs = {scale/4, scale*3/4, scale/2};
    private int[] spys = {2,2,scale-2};
    private Polygon southTri = new Polygon(spxs,spys,3);

    //arbitrary agent (for use with KeyboardController map when agent direction is not known)
    private int[] dpxs = {scale/2, scale*3/5, scale, scale * 3/5, scale/2, scale*2/5, 0, scale*2/5};
    private int[] dpys = {0, scale*2/5, scale/2, scale*3/5, scale, scale*3/5, scale/2, scale*2/5};
    private Polygon dTri = new Polygon(dpxs, dpys, 8);
    //maedengraphics*/

   
    /** Constructor for the agent for use by Grid Class
     *sets agent at position (s * ix, x * iy), connects to ss over sockets and gets an initial heading (N,S,E,W)
     *Pre: Grid is using this constructor, ss is the Socket that the agent uses to connect to the Grid, ix and iy are valid values, s > 0
     *Post:GOBAgent Object is created
     */
    public GOBAgent(int ix, int iy, int s, Grid mg, Socket sock, char heading){
	super(ix, iy, s);
	myID = idSequence++;
	myGrid = mg;
	conn = sock;
	///*maedengraphics
	myColor = colorSet[myID%colorSet.length];
	//maedengraphics*/
	setAgentHeading(heading);
	try {
	    send = new PrintWriter(conn.getOutputStream(), true);
	    recv = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	} catch(IOException e) {
	    System.out.println("GOBAgent constructor: Accept failed on port: " + myGrid.MAEDENPORT);
	    System.exit(-1);
	}
	try{
	    myRole= recv().readLine().toLowerCase();
	}
	catch (Exception e) { System.out.println("Couldn't read role from agent " + myID);}
	if (!myRole.equals("base") && !myRole.equals("helper")){
	    System.out.println("Role was: " + myRole);
	    System.out.println("Error: Role wasn't base or helper, check connection handshake protocol");
	    System.exit(-1);
	}
	send.println(myID);
	//System.out.println(myRole);
	if (myRole.equals("base")) {
	    ///*maedengraphics
	    myColor = Color.green;
	    //maedengraphics*/
	    newPrintChar('A');   //Agent's printchar is A
	} else {
	    newPrintChar('H');
	}
	inventory = new LinkedList<GridObject>();
    }


    /** Constructor for use by KeyboardController ONLY
     * Used only for drawing purposes
     * Pre: KEYBOARDCONTROLLER IS USING THE CONSTRUCTOR, s > 0, ix > 0, iy > 0
     * Post: GOBAgent object is created
     */
    public GOBAgent(int ix, int iy, int s, char heading) {
	super(ix, iy, s);
	///*maedengraphics
	myColor = Color.green;
	//maedengraphics*/
	setAgentHeading(heading);
	inventory = new LinkedList<GridObject>();
    }

    /** Override the default allowOtherGOB to check for agent's role type.
     * Helper agents may share any other agent's type, but base agents
     * only share with helper agents.
     */
    public boolean allowOtherGOB(GridObject otherGOB){
	if (myRole.equals("helper") || ((GOBAgent)otherGOB).getAgentRole().equals("helper"))
	    return true;
	else
	    return false;
    }

    public void printstats(){
	System.out.println("Energy: " + agentEnergy);
    }

    //PUBLIC ACCESSORS

    //public accessor for the grid (myGrid) in which the agent resides
    public Grid getGrid() { return myGrid; }

    //public accessors for the need sensor update flag
    public boolean getNeedUpdate() {
	return needUpdate; 
    }
    //public accessor for the id Sequence
    public int idSequence() {
	return idSequence;
    }
    //public accessor for the Socket connection
    public Socket conn() {
	return conn;
    }
    //public accessor for the printwriter
    public PrintWriter send() {
	return send;
    }
    //public accessor for the bufferedreader
    public BufferedReader recv() {
	return recv;
    }
    //public accessor for receiving the next command to be executed from the controller and caching it in nextCommand
    public void getNextCommand() {
	try {
	    // Only read in the next command if there is one
	    // if there is not, set nextCommand to null
	    if(recv.ready()){
		nextCommand = recv.readLine();
	    }
	    else {
		nextCommand = null;
	    }
	} catch (Exception e) { System.out.println("getNextCommand: Failed to receive command from controller process " + e); }
    }
    //public accessor for viewing the next command
    public String nextCommand() {
	return nextCommand;
    }
    //public accessor for dx
    public int dx() {
	return dx;
    }
    //public accessor for dy
    public int dy() {
	return dy;
    }
    //public accessor for the Agent's inventory
    public LinkedList<GridObject> inventory() {
	return inventory;
    }
    //public accessor for Agent's amount of remaining energy;
    public int energy() {
	return agentEnergy;
    }
    //public accessor for finding if agent should die or not
    public char status() {
	return status;
    }
    //public accessor for lastActionStatus (result from most recent action: either "ok" or "fail")
    public String lastActionStatus() { return lastActionStatus; }

    // ID accessor
    public int getAgentID(){ 
	return myID;
    }

    // role accessor
    public String getAgentRole(){
	return myRole;
    }

    // Grid simulation time accessor via agent
    public int simTime(){
	return myGrid.worldTime;
    }

    // PUBLIC SETTERS

    public void setNeedUpdate(boolean bool) {
	needUpdate = bool;
    }
    public void setAgentEnergy(int newValue) {
	agentEnergy = newValue;
    }

    /** 
     */
    public void decrEnergyWait() {
	agentEnergy -= costs.get("wait");
    }

    // AGENT ACTIONS

    /* moveForward checks to see if the cell in front of agent is passable.  If it is, agent moves to cell, otherwise does nothing
     * subtracts amount of energy required for moving forward *defined by forwardCost*
     */
    public void moveForward(){
	Point fSpot = new Point(pos.x + dx, pos.y + dy);
	if ( myGrid.passable(fSpot, this) ) {
	    myGrid.removeGOB(this);
	    pos.x += dx; pos.y += dy;
	    myGrid.addGOB(this);
	} else
	    lastActionFails();
	agentEnergy -= costs.get("forward");           //subtract energy required to move forward
	dieIfNoEnergy();                      //agent dies if no energy left
    }
    
    /* moveBackward checks cell behind agent.  If passable, agent moves to it, otherwise does nothing
     * subtracts amount of energy required for moving backward *defined by backCost*
     */
    public void moveBackward(){
	Point fSpot = new Point(pos.x - dx, pos.y - dy);
	if ( myGrid.passable(fSpot, this) ) {
	    myGrid.removeGOB(this);
	    pos.x -= dx; pos.y -= dy;
	    myGrid.addGOB(this);
	} else
	    lastActionFails();
	agentEnergy -= costs.get("back");              //subtract energy required to move backward
	dieIfNoEnergy();                      //agent dies if no energy left
    }

    /* turnLeft calls dieIfQuicksand. turns agent left
     * subtracts amount of energy required for turns *defined by turnCost*
     */
    public void turnLeft(){

	// if on quicksand, die
	dieIfQuicksand();
	int tmp = dx;
	dx = dy; dy = - tmp;
	agentEnergy -= costs.get("turn");              //subtract energy required to turn
	dieIfNoEnergy();                      //agent dies if no energy left
    }

    /* turnRight calls dieIfQuicksand. turns agent right
     * subtracts amount of energy required for turns *defined by turnCost*
     */
    public void turnRight(){

	// if on quicksand, die
	dieIfQuicksand();
	int tmp = dx;
	dx = - dy; dy = tmp;
	agentEnergy -= costs.get("turn");              //subtract energy required to turn
	dieIfNoEnergy();                      //agent dies if no energy left
    }

    /* attack: ....
     * hit agent in cell directly ahead.  [to do: extend to use weapons]
     */
    public void attack(){
	Point pointAhead = new Point(pos.x + dx, pos.y + dy);
	GOBAgent attackee;

	// find agent if there is one in the grid ahead
	if ( myGrid.myMap()[pointAhead.x][pointAhead.y] != null &&
	     ! myGrid.myMap()[pointAhead.x][pointAhead.y].isEmpty() ){
	    try {
		attackee = (GOBAgent) getGOBbyPrintChar('A', myGrid.myMap()[pointAhead.x][pointAhead.y]);
		// reduce their energy by attack amount
		attackee.setAgentEnergy(attackee.energy() - ATTACK_LOSS);
	    } catch (NoSuchElementException e) { agentEnergy -= 5; } // penalty for attacking when nothing there to hit
	}
	agentEnergy -= costs.get("attack");
	dieIfNoEnergy();
    }

    /* grab calls dieIfQuicksand. agent picks up item, transfer it to agent's inventory
     * subtracts amount of energy required for grabbing object *defined by grabCost*
     */
    public void grab(String gAction){
	char tool;
	GridObject grabobj;
	// if on quicksand, die
	dieIfQuicksand();
	tool = getToolChar(gAction, myGrid.myMap()[pos.x][pos.y]);

	if ( myGrid.cellHasTool(pos.x, pos.y, tool) && inventory.size() < INVENTORYCAPACITY ){
	    try {
		grabobj = myGrid.getTool(this, pos.x, pos.y, tool); // in case food supply not ready, throw NoSuchElementException
		inventory.push( grabobj );
		myGrid.removeGOB( grabobj );
		grabobj.pos = this.pos; // make inventory's position point to agent's position while being carried
	    } catch (NoSuchElementException e) {
		System.out.println("grab: myGrid.cellHasTool returns true but NoSuchElement: " + e);
		lastActionFails();
	    }
	} else {
	    System.out.println("grab: myGrid.cellHasTool returns false for tool '" + tool + "'");
	    lastActionFails();
	}
	agentEnergy -= costs.get("grab");                    //subtract energy required to grab an object
	dieIfNoEnergy();                            //agent dies if no energy is left
    }

    /* drop calls dieIfQuicksand. drop inventory item into cell, remove from inventory
     * subtracts amount of energy required for dropping object *defined by dropCost*
     * with no arguments, drops the list thing added to the inventory (i.e., front of inventory list)
     */
    public void drop(String dAction){
	GridObject dropTool = null;
	// if on quicksand, die
	dieIfQuicksand();
	dropTool = getGOBtool(dAction, inventory);

	if ( dropTool != null ){
	    inventory.remove(dropTool);
	    dropTool.onDrop(this, myGrid);
	} else {
	    lastActionFails();
	}
	agentEnergy -= costs.get("drop");              //subtract amount of energy required to drop an object
	dieIfNoEnergy();                      //agent dies if he has no energy left
    }
    
    /* use calls dieIfQuicksand. use inventory object on gridobject in front of agent
     * subtracts amount of energy required for dropping object *defined by useHammerCost/useKeyCost*
     *
     * use with no arguments attempts to use the first thing in the inventory (last thing grabbed)
     * using Food increases the agent's energy by energy-increment
     * using other objects applies the object to the object immediately in front of the agent
     */
    public void use(String uAction){ 
	GridObject useTool = null;
	// if on quicksand, die
	dieIfQuicksand();
	useTool = getGOBtool(uAction, inventory);
	Point fSpot = new Point(pos.x + dx, pos.y + dy);
	// if useobj is food, then eat and end
	if ( useTool != null && useTool.printChar() == '+'){
	    if ( myGrid.EAT_FOOD_ENDS_IT ){
		send.println("success");                                //agent succeeded in using the food
		status = 's';
		return; // this is the end */
	    } else {
		inventory.remove(useTool);
		agentEnergy += ((GOBFood)useTool).foodInc();
	    }
	} else if ( useTool != null && myGrid.myMap()[fSpot.x][fSpot.y] != null &&
		    ! myGrid.myMap()[fSpot.x][fSpot.y].isEmpty() ){
	    // assume a single object that is being acted upon
	    GridObject gob = (GridObject) myGrid.myMap()[fSpot.x][fSpot.y].getLast();
	    boolean result = gob.actedOnBy(useTool, myGrid);
	    if (!result)
		lastActionFails();
	    else
		agentEnergy -= costs.get("use" + useTool.printChar());
	    if (gob.printChar() == '#' && useTool.printChar() == 'K') {
		inventory.remove(useTool);                      // key gets consumed
	    }
	} else {
	    lastActionFails();
	    agentEnergy -= costs.get("wait");                   //agent hasn't done anything, so just subtract wait energy
	}
	dieIfNoEnergy();                                        //if out of energy, agent dies
    }

    /** getToolChar: String, LinkedList -> Char
     * extract the argument (if any) from the command
     * @param actString is the action command this agent want to perform
     * @param items 
     */
    private char getToolChar(String actString, LinkedList<GridObject> items){
	char tool;
	StringTokenizer actToks = new StringTokenizer(actString);
	actToks.nextToken();	// eat up the main action and prepare for optional argument
	if ( actToks.hasMoreTokens() ){
	    tool = Character.toUpperCase(actToks.nextToken().toCharArray()[0]); // print-char of item to
	} else {
	    if ( items.size() > 0 )
		tool = items.getFirst().printChar();	// in the case of cell contents,
	    // Grid.addGOB() attempts (but does not guarantee)
	    // to NOT grab an agent self or other if other object present
	    else
		tool = '*';
	}
	return tool;
    }

    /** Get the first grid object in the given LinkedList of items that matches the given print-char.
     * @param pc the print character of the sought-for item
     * @param items a linked list of items onstensibly containing the sought-for item
     * @return the found item
     */
    private GridObject getGOBbyPrintChar(char pc, LinkedList<GridObject> items) throws NoSuchElementException {
	for ( GridObject ldt : items ){
	    if ( ldt.printChar() == pc ) 
		return ldt;
	}
	throw new NoSuchElementException();
    }

    /** getGOBtool: String, LinkedList -> GridObject
     * Obtain a pointer to the GridObject in LinkedList<GridObject> that is indicated by the command string.
     * @param actString the action or command string for this agent
     * @param items the collection of items (usually the inventory) in which to find the desired tool
     * @return the tool indicated in the action string
     */
    private GridObject getGOBtool(String actString, LinkedList<GridObject> items){
	GridObject theTool = null;
	char tool = getToolChar(actString, items);
	try {
	    theTool = getGOBbyPrintChar(tool, items);
	} catch (NoSuchElementException e) { theTool = null; }
	return theTool;
    }

    /**
     * determine if quicksand here.  Call this function for every action
     * except forward and backward.  If quicksand is present, call an exit
     * method.
     */
    private void dieIfQuicksand(){
	if (myGrid.myMap()[pos.x][pos.y] != null)
	    for (GridObject go : (myGrid.myMap())[pos.x][pos.y]) {
		if ( go.printChar() == 'Q' ){
		    send.println("die");
		    status = 'd';
		}
	    }
    }

    /** dieIfNoEnergy determines if agent has energy left.  Call this function for every action
     * if no energy left, exit
     */
    private void dieIfNoEnergy() {
	if(agentEnergy <= 0) {
	    send.println("die");                                //agent died
	    status = 'd';
	}
    }

    /** setAgentHeading takes in a char and converts it to a dx, dy setup
     * |dx, dy    char|
     * | 0, -1 ==  N  |
     * | 0,  1 ==  S  |
     * |-1,  0 ==  W  |
     * | 1,  0 ==  E  |
     * | 0,  0 ==  ?  |
     * PRE: h is one of N, E, S, W, or ?
     * POST: dx and dy are set based on the character
     */
    private void setAgentHeading(char h){
	Character headChar = new Character(h);
	h = headChar.toUpperCase(h);
	switch (h) {
	case 'N': dx = 0; dy = -1; 
	    break;
	case 'E': dx = 1; dy = 0;
	    break;
	case 'S': dx = 0; dy = 1;
	    break;
	case 'W': dx = -1; dy = 0;
	    break;
	case '?': dx = 0; dy = 0;
	    break;
	default: System.out.println("GOBAgent:setAgentHeading: Unrecognized heading char: " + h);
	}
    }

    /** processAction
     * chooses an appropriate action based on the first letter of the action String
     * Pre: first letter of action == f, b, r, l, u, d, g, h, w, t, s, a, k
     * Post: Agent does appropriate action
     * @param action the command string that the agent controller wants to perform
     */
    public void processAction(String action) {
	String[] actionLetters =  {"f", "b", "r", "l", "u", "d", "g", "w", "t", "s", "a"}; // but not 'k' to killself
	char actionChar, origActionChar = Character.toLowerCase(action.toCharArray()[0]);
	String actionLetter;
	resetActionStatus();
	if (STOCHASTICISM && (Math.random() < STOCHASTIC_RATE)) {	// something random happened
	    actionLetter = actionLetters[randGenerator.nextInt(actionLetters.length)];
	    actionChar = actionLetter.charAt(0);
	    action = actionLetter + action.substring(1, action.length());
	    System.out.println("processAction: about to work with '" + action + "' instead of orig act: '" + origActionChar + "'");
	} else {							// do what was intended
	    actionChar = origActionChar;
	}
	switch(actionChar) {
	case 'f': moveForward();   //move forward command
	    break;
	case 'b': moveBackward();  //move backward command
	    break;
	case 'r': turnRight(); //turn right command
	    break;
	case 'l': turnLeft(); //turn left command
	    break;
	case 'u': use(action); //use command
	    break;
	case 'd': drop(action); //drop command
	    break;
	case 'g': grab(action);  //grab command
	    break;
	case 'w': // wait command
	    agentEnergy -= costs.get("wait"); dieIfNoEnergy(); dieIfQuicksand();
	    break;
	case 't': communicate(action);         //talk command
	    break;
	case 's': communicate(action);         //shout command
	    break;
	case 'a': attack();     // attack another agent
	    break;
	case 'k': agentEnergy = 0; dieIfNoEnergy();	// allow agents to kill themselves
	    break;
	default:
	    System.out.println("processAction: Illegal command " + action);
	    break;
	}
	if (actionChar != origActionChar){
	    System.out.println("processAction: actionChar != origActChar -- lastActionFails");
	    lastActionFails();
	}
    }

    /** communicate breaks up a message string and calls ComSentence(Command/Question) to compose it into the correct message form
     * Pre: message is in the form: "volume category subCategory (qType || comType) (subject) (answer)
     * Example: "talk question ask seen +" /Example
     * Example2: "talk command action lead +" /Example2
     * Post: new ComSentence(Command/Question) Object is created for Grid to send to other agents
     */
    public void communicate(String sent) {
	//System.out.println(sent);
	StringTokenizer aTokenizer = new StringTokenizer(sent);       //initialize Tokenizer
	String vol = aTokenizer.nextToken();                          //volume is first token
	//subtract appropriate amount of energy
	if(vol.equalsIgnoreCase("shout"))
	    agentEnergy -= costs.get("shout");
	else
	    agentEnergy -= costs.get("talk");
	String cat = aTokenizer.nextToken();        //category is second token
	if(cat.equalsIgnoreCase("command")) {                       //if it is a command, there are two possibilities for tokens
	    String goalActName = aTokenizer.nextToken();  //next token is the goal or action name
	    String subject = aTokenizer.nextToken();
	    String pmt = aTokenizer.nextToken();
	    int comPayment = Integer.parseInt(pmt);
	    //create new command Object
	    agentCom = new ComSentenceCommand(myID, vol, cat, goalActName, subject, comPayment, pos.y, pos.x);
	    //agent has a message
	    haveMsg = true;
	} else if (cat.equalsIgnoreCase("question")) {       //otherwise the message is a question
	    String qType = aTokenizer.nextToken();      // next token is the question type
	    String subj = aTokenizer.nextToken();       // next token is the subject of the question
	    if(aTokenizer.hasMoreTokens()) {            //if there is another token, it is the answer to the question
	        String ans = aTokenizer.nextToken();
		//create answer to question
	        agentCom = new ComSentenceQuestion(myID, vol, cat, qType, subj, ans, pos.y, pos.x);
	    }
	    else
		agentCom = new ComSentenceQuestion(myID, vol, cat, qType, subj, pos.y, pos.x);
	    //System.out.println("question created");
	    //agent has a message
	    haveMsg = true;
	}
	else
	    System.out.println("error with sentence syntax or vocab");
	dieIfNoEnergy();                 //agent dies if no energy
    }

    //true if agent has a new message to send
    public boolean hasMsg() {
	return haveMsg;
    }

    //send the ComSentence(Question/Command) for processing the action
    public ComSentence msg() {
	haveMsg = false;
	return (ComSentence) agentCom;
    }

    /** paint draws the agent with the appropriate heading if carrying
     * an object, draw oval behind agent
     @param g the current Graphics context
     */
    ///*maedengraphics
    public void paint(Graphics g){
	g.translate(pos.x*scale,pos.y*scale);
	// NOW SIGNIFY IF CARRYING ANYTHING
	if ( inventory.size() > 0 ){
	    int scaleCenter = scale / 2;
	    g.setColor(Color.black);
	    g.fillOval(scaleCenter-scale/3, scaleCenter-scale/3, 2*scale/3, 2*scale/3);
	}
	//if no heading, draw agent pointing all directions (indicates heading is unknown)
	g.setColor(myColor);
	if(dx == 0 && dy == 0) {
	    g.fillPolygon(dTri);
	    g.setColor(Color.black);
	    g.drawPolygon(dTri);
	}
	//otherwise draw agent with appropriate heading
	else {
	    if (dx == 0) // NORTH or SOUTH
		if (dy == 1) { //NORTH
		    g.fillPolygon(southTri);
		    g.setColor(Color.black);
		    g.drawPolygon(southTri);
		}
		else { //SOUTH
		    g.fillPolygon(northTri);
		    g.setColor(Color.black);
		    g.drawPolygon(northTri); }
	    else // EAST or WEST
		if (dx == 1) { //EAST
		    g.fillPolygon(eastTri);
		    g.setColor(Color.black);
		    g.drawPolygon(eastTri); }
		else { //WEST
		    g.fillPolygon(westTri);
		    g.setColor(Color.black);
		    g.drawPolygon(westTri); }
	}
	g.translate(-pos.x*scale,-pos.y*scale);
    }
    //maedengraphics*/

    /** lastActionFails: set the lastActionStatus to report that the attempted action failed
     */
    private void lastActionFails() { lastActionStatus = "fail"; }

    /** resetActionStatus: at beginning of each action processing cycle, reset lastActionStatus
     */
    private void resetActionStatus() { lastActionStatus = "ok"; }


    /**
     * closes bufferedreader, printWriter, and Socket streams
     * call function when exiting
     */
    public void cleanDie() {
	myGrid.removeGOB(this);
	try {
	    recv.close();   //close bufferedreader
	    send.close();   //close printwriter
	    conn.close();   //close socket
	}
	catch (Exception e) {}
    }

}
