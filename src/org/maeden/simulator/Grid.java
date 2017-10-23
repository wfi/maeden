package org.maeden.simulator;

import java.lang.Math;
import java.util.StringTokenizer;
///*maedengraphics
import java.awt.*;
//maedengraphics*/
import java.awt.Point;
import java.util.Collections;
import java.util.List;
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
 *@author:  CS116 Fall 2011, especially Aaron Panchal Morgan Vigil, and Kelly Macdonald
 *@date:    3-12-2012
 *@version: Beta 0.5
 */

public class Grid
///*maedengraphics
    extends Frame
//maedgraphics*/
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

    // items and agents and stuff
    private List<ComSentence> msgs = Collections.synchronizedList(new LinkedList<ComSentence>());   //holds agent messages
    private List<GridObject> gobs = Collections.synchronizedList(new LinkedList<GridObject>());   //holds world gridobjects
    private List<GOBAgent> agents; //holds world agents
    private LinkedListGOB[][] myMap;                 //holds gridobjects
    private SensoryPacketSender sps;

    // misc (possibly temporary) variables
    private GridObject food;                   //world goal
    public static final int MAEDENPORT = 7237; //host server port number
    private ServerSocket gwServer;  // server-socket for listening for connection requests

    public boolean EAT_FOOD_ENDS_IT = true; // control if eating food terminates sim (true) or increases energy (false)
    public int WORLD_CYCLE_TIME = 200;      // replaces sleepTime to control wall-time length of simulation cycle
    public int counter = 0;
    private static AgentListener theAl;
    // Constructors

    /**
     * Construct a Maeden server: read world definiton from file (largely from Cuyler Cannon),
     * populate the grid with GridObjects as found in the world file,
     * start the AgentListener thread for handling connection requests from clients,
     * and display the window (if appropriate).
     * @param filePath the filesystem path to the world definition file
     * @param approxWidth the target size for the resulting window that displays the world
     * @param showD whether or not to actually display the world
     */
    public Grid(String filePath, int approxWidth, boolean showD) throws FileNotFoundException, IOException {
        try {
            gwServer = new ServerSocket(MAEDENPORT);        //create new server Socket on Maeden port
        } catch(IOException e) {
            System.err.println("could not listen on port: " + MAEDENPORT);
            //System.exit(1);   //exit if cannot use the port number
        }

        /** Parse Data File **/
        WorldReader gridspaceInputData = new WorldReader(filePath);

        xCols = gridspaceInputData.cols();                // get row and col sizes from world file
        yRows = gridspaceInputData.rows();

        // Initialize grid map now from read sizes
        myMap = new LinkedListGOB[xCols][yRows]; // note: non-conventional order of columns, rows
        agents = Collections.synchronizedList(new LinkedList<GOBAgent>());

        // set cell size from desired physical window width and logical size found in file
        squareSize = approxWidth / xCols;
        approxWidth = squareSize * xCols;

        // Read in the Character Map
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
                    AgentListener al = new AgentListener(x,y,squareSize,this,'W');
                    theAl = al;
                    theAl.start();
                    counter++;
                    System.out.println(counter);
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

        // set up the graphics stuff
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

        // initialize the SensoryPacketSender
        sps = new SensoryPacketSender(myMap, food);
    }

    //public accessor for the grid map
    public LinkedListGOB[][] myMap() {
        return myMap;
    }

    //public accessor for the gobs
    public List<GridObject> gobs() {
        return gobs;
    }

    //public accessor for the agents
    public List<GOBAgent> agents() {
        return agents;
    }

    //public accessor for the messages
    public List msgs() {
        return msgs;
    }


    /**
     * run: run the world
     *
     */
    public void run() {

        while(!killGrid) {
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
        try {
            for (GOBAgent a : agents) {
                a.getNextCommand();           //have current agent get next command from controller process
                //System.out.println("processing agent " + a.getAgentID() + " with action: " + a.nextCommand());
            }
        } catch (Exception e) { System.out.println("Failed reading the next command: " + e);}
        try {
            for (GOBAgent a : agents) {    //process and perform each agent's action
                //Process the action only if there is a next command
                Integer count = 0;
                if(a.nextCommand() != null)
                    {
                        a.processAction(a.nextCommand());
                        a.setNeedUpdate(true);
                        count += 1;
                    }
                else {
                    a.decrEnergyWait(); // otherwise, deduct the wait cost from agent's energy
                }
                if (count > 1){
                    Collections.shuffle(agents);
                }
            }
        } catch (Exception e) {
            System.out.println("Failed processing the next command just read");
            e.printStackTrace();
        }
        //System.out.println("About to collect messages");
        try {
            getAgentMessages();             //places any messages generated from agent actions inside msgs linked list
        } catch (Exception e) { System.out.println("Failed processing the messages: " + e);}
        //System.out.println("Messages collected");
        try {
            for(Iterator<GOBAgent> i = agents.iterator(); i.hasNext(); ) {          //remove any dead agents
                GOBAgent a = i.next();
                switch(a.status()) {
                case 'd':                       // die: agent died from lack of energy or quicksand
                    while ( a.inventory().size() > 0 )
                        a.drop("drop");         // drop all items from inventory before removing agent
                    sps.sendSensationsToAgent(a, "DIE");
                    a.cleanDie(); i.remove();
                    break;
                case 's':
                    sps.sendSensationsToAgent(a, "SUCCESS");
                    success();
                    break;
                case 'c':                       // continuing: agent is alive, hasn't found the food
                default:
                    break;
                }
            }
        } catch (Exception e) { System.out.println("Failed in final processing: " + e);}
    }

    /**
     * sendAgentSensations: for each agent that is ready for it as determined by getNeedUpdate(),
     * send their sensory information
     */
    public void sendAgentSensations() {
        for (GOBAgent a : agents) {
            if (a.getNeedUpdate())
                sps.sendSensationsToAgent(a);
        }
        //**** WARNING: review this logic -- since not all agents may receive sensory updates
        msgs.clear();              //once messages are sent, they don't need to be saved any longer
    }


    public void success(){
      killGrid = true;      // success: agent found the food, end the simulation
      cleanClose();
      try {

          gwServer.close(); //moved closing the server socket to the run function in order to not close it prematurely
          System.out.println("server should be closed.");
      }
      catch(Exception e) {System.out.println("error closing server socket");}

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
            gobs.add(val); // add object to the last slot of the gobs linked list
        }
        if (myMap[val.pos.x][val.pos.y] == null)
            myMap[val.pos.x][val.pos.y] = new LinkedListGOB(); // if cell is null, create new linked list
        if (myMap[val.pos.x][val.pos.y] != null){
            LinkedListGOB lgobs = myMap[val.pos.x][val.pos.y];
            synchronized (lgobs){
                if ( val.printChar() == 'A' || val.printChar() == 'H' )
                    lgobs.add(val);   // add agents to the end of cell's linked list
                else
                    lgobs.add(0,val);  // add all other objects to the front of list
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
            for (GridObject gob : myMap[x][y]) {
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
            for (GridObject gob : myMap[x][y]) {
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
            for(GridObject gObj : myMap[x][y]) {
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
    public static char relDirToPt(Point aPt, Point aDir, Point target){
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


    /*getAgentMessages iterates through all agents on the map and stores any messages they have in the msgs linkedlist
     *Pre: msgs has been initialized
     *Post: all messages are stored in msgs linkedlist
     */
    public void getAgentMessages() {
        for(GOBAgent a : agents) {                      //get all messages from all agents and store them
            if(a.hasMsg())                                      //if agent has message, store it
                msgs.add(a.msg());
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
    public String sendAgentMessages(GOBAgent ag) {
        int msgDist;
        String newMsg = "";
        //iterate through the messages
        for(ComSentence thisMsg : msgs) {
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
                    for(GOBAgent thisAgent : agents) {
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
        //  group2 changed this because we want to be able to add the massges to a Sting Object.
        return newMsg;
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
        for(GridObject gob : gobs) {
            gob.paint(g);   //each gridobject paints itself
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
     * POST: buffers and sockets closed, grid exits
     */
    public void cleanClose() {
        // *********** only for demo purposes *** remove
        //try {Thread.sleep(20000);} catch (Exception e) {System.out.println("error with sleeping"); }
        // *** remove **********************************
        if( agents != null && !agents.isEmpty() ) {  //if there are agents on the grid still
            for(GOBAgent g : agents) {  //iterate through and close their connections
                sendAgentSensations();
                g.printstats();
                //g.send().println("End");              //Other agent got food, simulation ended
                sps.sendSensationsToAgent(g, "END");
                g.cleanDie();
            }
            agents.clear();
        }
        /*try {
          gwServer.close();
          }
          catch(Exception e) {System.out.println("error closing server socket");}*/
        //System.exit(4);  //exit
    }
    //socket.Shutdown(SocketShutdown.Both);
    //socket.Close();

    /** print the proper usage of the program
     */
    public static void useage() {
        System.out.println("Usage: java org.maeden.simulator.Grid [world-string] [display-bool] [simspeed-int] [eatfoodends-bool]\n"
                           + "where\n"
                           + "    world-string: is the path to the world file\n"
                           + "    display-bool: is true/false whether you want the world displayed\n"
                           + "    simspeed-int: number of milliseconds per simulation step\n"
                           + "    eatfoodends-bool: true/false whether any agent eating the food ends the simulation");
    }

    /** Kicks everything off
     * takes 3 optional args from the command line:
     * Arg1: String (World file name (defaults to "worlds/sampleWorld3.txt"))
     * Optional Args:
     * Arg2: boolean (true if graphical display is desired)
     * Arg3: int (number of milliseconds to sleep between simulation steps.  Default=50)
     * Arg4: control EAT_FOOD_ENDS_IT flag
     * ***Note: args must appear in this order, main looks at first arg as world file name, 2nd as boolean, and 3rd as num of agents***
     * @param args array of String command-line arguments
     */
    public static void main(String[] args){
        int windowWidth = 600;
        Grid myGrid = null;
        boolean showD = true;

        // populate world from file
        try {
            switch ( args.length ) {
            case 0: myGrid = new Grid("worlds/miscX1", windowWidth, showD); break;
            case 1: myGrid = new Grid(args[0], windowWidth, showD); break;
            case 2: myGrid = new Grid(args[0], windowWidth, args[1].equalsIgnoreCase("true")); break;
            case 3: myGrid = new Grid(args[0], windowWidth, args[1].equalsIgnoreCase("true"));
                myGrid.WORLD_CYCLE_TIME = Integer.parseInt(args[2]); break;
            case 4: myGrid = new Grid(args[0], windowWidth, args[1].equalsIgnoreCase("true"));
                myGrid.WORLD_CYCLE_TIME = Integer.parseInt(args[2]);
                myGrid.EAT_FOOD_ENDS_IT = args[3].equalsIgnoreCase("true"); break;
            default: useage();
            }
            ///*maedengraphics
            //if(showD)  //if graphical display is desired, show the window
            //   myGrid.setVisible(true);
            //maedengraphics*/
            if (myGrid != null) myGrid.run();  //run the simulation
            Thread.sleep(2000);
        }
        catch (InterruptedException e){

        }
        catch (FileNotFoundException e) { System.out.println("Could not find file"); }
        catch (Exception e) { System.out.println("Some exception: " + e);
      }

      theAl.interrupt();
      try{
        Thread.sleep(50);
      }
      catch(Exception e){

      }
          System.out.println(theAl.isAlive());
          myGrid.dispose();
            System.out.println("end of main");
    }




    /**
     * AgentListener
     * private inner class that listens for connection requests from agent controllers
     */
    class AgentListener extends Thread {


        private int x;
        private int y;
        private int squareSize;
        private Grid grid;
        private char head;
        // constructor
        AgentListener(int ix, int iy, int s, Grid mg, char heading){

            x = ix;
            y = iy;
            squareSize = s;
            grid = mg;
            head = heading;
        }

        /** the run method for this AgentListener thread gets called by start() */
        public void run() {
            Socket tSock;



            while (!Thread.currentThread().isInterrupted()) {
                try {
                    tSock = gwServer.accept();           // listen for connection, and
                    GOBAgent gagent = new GOBAgent(x,y,squareSize,grid,tSock,head);
                    grid.addGOB(gagent); // addGOB(...) is synchronized on gobs
                    synchronized (agents) {
                        agents.add(gagent);
                    }
                    try { sps.sendSensationsToAgent(gagent); }
                    catch (Exception e) {System.out.println("AgentListener.run(): failure sending sensations ");
                        e.printStackTrace(); }
                    Thread.sleep(50);

                }

                catch (IOException e) { System.out.println("AgentListener.run(): failed accepting socket connection: " + e);
                } catch (Exception e) {
                    System.out.println("AgentListener.run(): some other exception: ");
                    e.printStackTrace();
                }
            }
            System.out.println("out of the while");







        }
    }
}
