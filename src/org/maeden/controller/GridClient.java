package org.maeden.controller;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * class GridClient : useful for any controller that wants to connect and interact with
 * the Grid simulator
 *
 *@author:  Wayne Iba
 *@version: 20140613
 */

public class GridClient {
    protected Socket gridSocket;                        // socket for communicating w/ server
    protected PrintWriter gridOut;                      // takes care of output stream for sockets
    protected BufferedReader gridIn;                    // bufferedreader for input reading
    protected String myID;
    public static final int MAEDENPORT = 7237;          // uses port 1237 on localhost


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
     * getSensoryPacket : this should return a SensoryPacket corresponding to lines read from
     * the Grid.  Note: this drains the information available on the socket connecting to the Grid server,
     * and thus, will block if the information has already been read since the agent controller's last action.
     * @return the latest sensory information from the Grid server wrapped in a SensoryPacket
     */
    public SensoryPacket getSensoryPacket() {
        SensoryPacket sp = new SensoryPacket(gridIn);
        return sp;
    }


    /**
     * effectorSend : this should consume a String which will be the action to perform.
     * Most actions are single characters but once we implement communication, we'll want
     * to be able to send messages.
     * Commands are assumed to be single characters, and currently only supports
     * one single-letter argument
     * @param command the String command that the controller wants to execute in the world
     */
    public void effectorSend(String command) {
        JSONObject toSend = new JSONObject();
        toSend.put("command", command.substring(0,1));
        if(command.length() > 1){
            JSONArray args = new JSONArray();
            args.add(command.substring(2,3));
            toSend.put("arguments", args);
        }
        gridOut.println(toSend);
        //System.out.println(".... as " + toSend.toString());
    }
        

}
