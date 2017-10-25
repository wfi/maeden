package org.maeden.controller;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import java.io.BufferedReader;
import static java.lang.Math.toIntExact;

/**
 * Simple class for representing 'pre-processed' sensory packets.
 * Agents can bypass the low-level 'raw' sensory data and especially the problem of parsing
 * the contents of the visual field by accessing an 2D array of Lists of world objects.
 *
 * @author: Wayne Iba
 * @version: 2017090901
 */
public class SensoryPacket {

    String status;
    String smell;
    List<Character> inventory;
    ArrayList<ArrayList<Vector<String>>> visualArray;
    List<Character> groundContents;
    String messages;
    int energy;
    boolean lastActionStatus;
    int worldTime;
    JSONArray rawSenseData;

    /**
     * constructor that reads the raw data from the server via the provided BufferedReader
     * over the Socket that connects the controller to the Grid server.
     * Initiates some amount of preprocessing on the raw (JSON) data received from the Grid server.
     * @param gridIn the BufferedReader to read from the Grid server
     */
    public SensoryPacket(BufferedReader gridIn) {
        visualArray = new ArrayList<ArrayList<Vector<String>>>();
        for (int row = 0; row < 7; row++) {
            visualArray.add(row, new ArrayList<Vector<String>>());
            for (int col = 0; col < 5; col++) {
                visualArray.get(row).add(col, new Vector<String>());
            }
        }
        rawSenseData = getRawSenseDataFromGrid(gridIn);
        initPreProcessedFields(rawSenseData);
    }

    /**
     * another constructor takes in the sensory data as parameters instead of using a BufferedReader
     */
    public SensoryPacket(String inptStatus, String inptSmell, List<Character> inptInventory,
                         ArrayList<ArrayList<Vector<String>>> inptVisualArray,
                         List<Character> inptGroundContents, String inptMessages,
                         Integer inptEnergy, Boolean inptLastActionStatus, Integer inptWorldTime) {
        status = inptStatus;
        smell = inptSmell;
        inventory = inptInventory;
        visualArray = inptVisualArray;
        groundContents = inptGroundContents;
        messages = inptMessages;
        energy = inptEnergy;
        lastActionStatus = inptLastActionStatus;
        worldTime = inptWorldTime;
    }

    /**
     * Just read the raw JSON data into various arrays.
     * <p>
     * The JSONArray contains data indexed as follows (and documented in README.SensoryMotor):<br>
     * 0: simulation status, one of: CONTINUE, DIE, SUCCESS, or END<br>
     * 1: smell (food direction)<br>
     * 2: inventory<br>
     * 3: visual contents<br>
     * 4: ground contents<br>
     * 5: messages<br>
     * 6: remaining energy<br>
     * 7: lastActionStatus<br>
     * 8: world time<br>
     *
     * @param gridIn the reader connected to the server
     * @return the array of String representing the raw (unprocessed) sensory data starting with smell
     */
    protected JSONArray getRawSenseDataFromGrid(BufferedReader gridIn) {
        JSONArray jsonArray = null;
        try {
            JSONParser jsonParser = new JSONParser();
            Object object = jsonParser.parse(gridIn.readLine()); // unpack the JsonArray.
            jsonArray = (JSONArray) object;
        } catch (Exception e){
            e.getMessage();
            System.exit(1); // exits if all the elements in the JsonArray are null.
        }
        return jsonArray;
    }

    /**
     * Perform any pre-processing, especially on the visual data
     * @param rawSenseData the raw unprocessed sense data
     */
    protected void initPreProcessedFields(JSONArray rawSenseData){
        try {
            this.status = (String) rawSenseData.get(0);                  // status
            this.smell = (String) rawSenseData.get(1);                   // smell
            this.inventory = (ArrayList) rawSenseData.get(2);            // process inventory
            processRetinalField((JSONArray) rawSenseData.get(3));        // visual field
            this.groundContents = (ArrayList) rawSenseData.get(4);       // ground contents
            this.messages = (String) rawSenseData.get(5);            // messages: *** Revisit this!! ***
            this.energy = toIntExact((Long) rawSenseData.get(6));        // energy
            this.lastActionStatus = ((String) rawSenseData.get(7)).equalsIgnoreCase("ok");            // lastActionStatus
            this.worldTime = toIntExact((Long) rawSenseData.get(8));     // world Time
        } catch (NullPointerException e){ e.getMessage();
        }
    }

    /**
     * Process the single string representing all the rows and column contents of the visual sensory data
     * and convert it to a 2D array of Vectors of Strings.
     * @param info the JSONArray (2D with each cell as JSONArray of contents) of the portion of grid visible to agent
     */
    protected void processRetinalField(JSONArray info) {
        for (int i = 6; i >= 0; i--) {
            JSONArray q = (JSONArray) info.get(i);
            for (int j = 0; j <= 4; j++) {
                JSONArray z = (JSONArray) q.get(j);
                for (int k = 0; k < z.size(); k++) {
                    visualArray.get(i).get(j).add((String) z.get(k));
                }
            }
        }
    }


    /** Get the status of the agent in the simulation.  Refer to documentation and/or code
     * for definitive details but either is a number of raw lines to be subsequently processed
     * or is one of "DIE", "END", or "SUCCEED".  This will not typically be used by agents.
     * @return the status of the simulation
     */
    public String getStatus(){ return status; }

    /**
     * @return the string direction toward the food source as one of forward, back, left or right
     */
    public String getSmell(){ return smell; }

    /**
     * @return the current contents of the inventory as a list
     */
    public List<Character> getInventory(){ return inventory; }

    /**
     * @return the array of lists of strings representing what is currently within the field of view
     */
    public ArrayList<ArrayList<Vector<String>>> getVisualArray(){ return visualArray; }

    /**
     * @return the list of characters on the ground where the agent is standing
     */
    public List<Character> getGroundContents(){ return groundContents; }

    /**
     * NOTE: This may be out of sync with the Grid server and may need to be a list or something else.
     * @return the messages shouted or talked by other agents in the environment
     */
    public String getMessages(){ return messages; }

    /**
     * @return the remaining energy as indicated by the sensory information from the server
     */
    public int getEnergy(){ return energy; }

    /**
     * @return whether the last action was successful (true) or not (false)
     */
    public boolean getLastActionStatus(){ return lastActionStatus; }

    /**
     * @return the world time
     */
    public int getWorldTime(){ return worldTime; }

    /**
     * @return the array of Strings representing the raw sensory data
     */
    public JSONArray getRawSenseData(){return rawSenseData; }

    /**
     * Renders the visual information as semi-formatted string, making no allowances for
     * cells with more than one object
     */
    public void printVisualArray(){
        for ( ArrayList<Vector<String>> row : visualArray ){
            for ( Vector<String> cell : row ){
                if ( cell != null ){
                    System.out.print('[');
                    for (String s : cell)
                        System.out.print(s);
                    System.out.print(']');
                } else {
                    System.out.print("[ ]");
                }
            }
            System.out.println();
        }
        System.out.println();
    }
    
}
