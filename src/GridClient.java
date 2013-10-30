import java.io.*;
import java.net.*;

/**
 * class GridClient : useful for any controller that wants to connect and interact with the Grid simulator
 *
 *@author:  Wayne Iba
 *@date:    3/4/2012
 *@version: Beta 0.4
 */

public class GridClient {
    protected Socket gridSocket;			// socket for communicating w/ server
    protected PrintWriter gridOut;                      // takes care of output stream for sockets
    protected BufferedReader gridIn;			// bufferedreader for input reading
    protected String myID;
    public static final int MAEDENPORT = 7237;       // uses port 1237 on localhost


    /**
    public GridClient(){
	this("localhost", EDENPORT);
    }
    */
    public GridClient(String h, int p) {
	registerWithGrid(h, p);
    }

    /**
     * registerWithGrid takes a string and an int
     * and creates a socket with the specified network name and port number
     * PRE: h is the name of the machine on the network, p is the port number of the server socket
     * POST: socket connects with the server socket on the given host
     */
    public void registerWithGrid(String h, int p) {
        try {
	    // connects to h machine on port p
            gridSocket = new Socket(h, p);

	    // create output stream to communicate with grid
            gridOut = new PrintWriter(gridSocket.getOutputStream(), true); 
	    gridOut.println("base"); // send role to server

	    //buffered reader reads from input stream from grid
            gridIn = new BufferedReader(new InputStreamReader(gridSocket.getInputStream()));
	    myID = gridIn.readLine(); // read this agent's ID number
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + h);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + h);
            System.exit(1);
        }
    }
	
    /**
     * sensoryGet : this should return an array of strings corresponding to lines read from
     * the Grid.  This will centralize the format wrt future changes
    /**
     * getSensoryInfo gets the direcion to the food
     * LINE0: # of lines to be sent or one of: die, success, or End
     * LINE1: smell (food direction)
     * LINE2: inventory
     * LINE3: visual contents
     * LINE4: ground contents
     * LINE5: messages
     * LINE6: remaining energy
     * LINE7: lastActionStatus
     * LINE8: world time
     * pre: gridIn is initialized and connected to the grid server socket
     * post: heading stores direction to the food f, b, l, r, or h
     */
    public String[] sensoryGet() {
	String[] result = new String[8];
	try {
	    String status = gridIn.readLine().toLowerCase();
	    if((status.equals("die") || status.equals("success")) || status.equals("end")) {
		System.out.println("Final status: " + status);
		System.exit(1);
	    }
	    if ( ! status.equals("8") ){
		System.out.println("getSensoryInfo: Unexpected number of data lines - " + status);
		System.exit(1);
	    }
	    // 1: get the smell info
	    result[0] = gridIn.readLine();
	    // 2: get the inventory
	    result[1] = gridIn.readLine();
	    // 3: get the visual info
	    result[2] = gridIn.readLine();
	    // 4: get ground contents
	    result[3] = gridIn.readLine();
	    // 5: get messages
	    result[4] = gridIn.readLine(); //CHECKS MESSAGES ****CHANGE****
	    // 6: energy
	    result[5] = gridIn.readLine();
	    // 7: lastActionStatus
	    result[6] = gridIn.readLine();
	    // 8: world time
	    result[7] = gridIn.readLine();
	}
	catch(Exception e) {}

	return result;
    }


    /**
     * effectorSend : this should consume a String which will be the action to perform.
     * Most actions are single characters but once we implement communication, we'll want
     * to be able to send messages.
     *
     * *NOTE: GOBAgent only looks at first letter of command string unless talk or shout is sent*
     */
    public void effectorSend(String command) {
	gridOut.println(command);
    }
	

}
