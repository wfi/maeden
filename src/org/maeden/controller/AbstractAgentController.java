package org.maeden.controller;


/**
 * class AbstractAgentController: Provides a starting place for writing agent controllers
 * for use in the Maeden simulator.  
 * 
 * @author:  Wayne Iba
 * @date:    4-11-2017
 */
public abstract class AbstractAgentController {
    
    //private data field
    protected static final int MAEDENPORT = 7237;         // uses port 1237 on localhost

    protected GridClient gc;
    protected SensoryPacket currentSensePacket;

    /**
     * AbstractAgentController constructor takes a string and an int
     * and creates a socket and connects with a serverSocket
     * PRE: h is a string and p is an int (preferably above 1024)
     * POST: GridClient connects to Grid via network sockets
     * @param h the name or IP address of the host on which the MAEDEN simulator is running
     * @param p the port number on which the simulator is listening
     */
    public AbstractAgentController(String h, int p) {
	gc = new GridClient(h, p);
    }

    public AbstractAgentController() {
	this("localhost", MAEDENPORT);
    }

 
    /**
     * sendEffectorCommand sends the specified command to the grid
     * *NOTE: GOBAgent only looks at first letter of command string unless talk or shout is sent*
     * pre: command is either f, b, l, r, g, u, d, "talk" + message, or "shout" + message
     * post: command is sent via the printwriter
     * @param command the intended command to be performed by the agent in the simulator
     */
    public void sendEffectorCommand(String command) {
	gc.gridOut.println(command);
    }
 
    /**
     * getSensoryInfo via the GridClient component and store its raw sense data in currentRawSenseData
     * which can be unpacked at will
     */
    public void getSensoryInfo() {
	currentSensePacket = gc.getSensoryPacket();
    }

}
