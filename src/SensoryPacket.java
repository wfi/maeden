import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.util.StringTokenizer;

/**
 * Simple class for representing 'pre-processed' sensory packets.
 * Agents can bypass the low-level 'raw' sensory data and especially the problem of parsing
 * the contents of the visual field by accessing an array of Lists of GridObjects.
 *
 * @author: Wayne Iba
 * @version: 20140613
 */
public class SensoryPacket {

    String status;
    String smell;
    List<Character> inventory;
    List<Character>[][] visualArray = (List<Character>[][])new ArrayList[7][5];
    List<Character> groundContents;
    String messages;
    int energy;
    boolean lastActionStatus;
    int worldTime;
    String[] rawSenseData;

    /**
     * constructor that reads the raw data from the server via the provided BufferedReader
     * and performs some amount of preprocessing on that raw data.
     */
    public SensoryPacket(BufferedReader gridIn){
	rawSenseData = getRawSenseDataFromGrid(gridIn);
	initPreProcessedFields(rawSenseData);
    }

    /**
     * Just read the raw data into an array of String.  Initialize the status field from line 0
     *
     * LINE0: # of lines to be sent or one of: die, success, or End
     * LINE1: smell (food direction)
     * LINE2: inventory
     * LINE3: visual contents
     * LINE4: ground contents
     * LINE5: messages
     * LINE6: remaining energy
     * LINE7: lastActionStatus
     * LINE8: world time
     * @param gridIn the reader connected to the server
     * @return the array of String representing the raw (unprocessed) sensory data starting with smell
     */
    protected String[] getRawSenseDataFromGrid(BufferedReader gridIn){
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
	    this.status = status;
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
	catch(Exception e) { e.printStackTrace(); }
	return result;
    }

    /**
     * Perform any pre-processing, especially on the visual data
     * @param rawSenseData the raw unprocessed sense data
     */
    protected void initPreProcessedFields(String[] rawSenseData){
	// smell
	this.smell = rawSenseData[0];
	// process inventory
	this.inventory = new ArrayList<Character>();
	for(char item : rawSenseData[1].replaceAll("[\\(\"\\)\\s]+","").toCharArray())
	    this.inventory.add(item);
	// visual field
	processRetinalField(rawSenseData[2]);
	// ground contents
	this.groundContents = new ArrayList<Character>();
	for(char item : rawSenseData[3].replaceAll("[\\(\"\\)\\s]+","").toCharArray())
	    this.groundContents.add(item);
	// messages: *** Revisit this!! ***
	this.messages = rawSenseData[4];
	// energy
	this.energy = Integer.parseInt(rawSenseData[5]);
	// lastActionStatus
	this.lastActionStatus = rawSenseData[6].equalsIgnoreCase("ok");
	// world Time
	this.worldTime = Integer.parseInt(rawSenseData[7]);
    }

    /**
     * Process the single string representing all the rows and column contents of the visual sensory data
     * and convert it to a 2D array of character objects, null when the cell is empty
     * @param info the visual sensory data string (structered as parenthesized list of lists) from server
     */
    protected void processRetinalField(String info) {
	StringTokenizer visTokens = new StringTokenizer(info, "(", true);
	visTokens.nextToken();
	for (int i = 6; i >= 0; i--) {              //iterate backwards so character printout displays correctly
	    visTokens.nextToken();
	    for (int j=0; j <=4; j++) {             //iterate through the columns
		visTokens.nextToken();
		char[] visArray = visTokens.nextToken().replaceAll("[\\(\"\\)\\s]+","").toCharArray();
		for(char item : visArray)
		    if ( visualArray[i][j] != null )
			visualArray[i][j].add(item);
		    else
			visualArray[i][j] = new ArrayList<Character>((List<Character>)Arrays.asList(item));
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
     * @return the array of lists of GridObjects that are currently within the field of view
     */
    public List<Character>[][] getVisualArray(){ return visualArray; }

    /**
     * @return the list of GridObjects on the ground where the agent is standing
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
    public String[] getRawSenseData(){ return rawSenseData; }

    /**
     * Renders the visual information as semi-formatted string, making no allowances for
     * cells with more than one object
     */
    public void printVisualArray(){
	for ( List<Character>[] row : visualArray ){
	    for ( List<Character> cell : row ){
		if ( cell != null ){
		    System.out.print('[');
		    for (Character c : cell)
			System.out.print(c);
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