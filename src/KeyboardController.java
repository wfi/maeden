
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * class KeyboardController: Provides a Keyboard user-interface to a Grid world simulation.
 * Currently, only provides smell direction to the food, agent inventory, visual picture,
 * and ground contents of current location.  Accepts commands from the keyboard and communicates
 * with the Grid simulation.  Other agents in field of view appear as a four-pointed star
 * since the heading is not reported by the server.
 * 
 *@author:  Josh Holm, Wayne Iba
 *@date:    2-25-12
  @version: Beta 0.2
 */
public class KeyboardController extends Frame {
    
    //private data field
    private String myID;
    private static final int MAEDENPORT = 7237;         // uses port 1237 on localhost
    private Insets iTrans;
    private static final int cellSize = 60;             // sets the width and height of individual visual cells
    private static final int cx = 5;                    //sets size for the visual array
    private static final int ry = 7;
    private static final int dashHeight=280;// height of panel for dashboard (apx 3.5 * number of items to display)

    private LinkedList<GridObject> visField;        // stores GOB's for painting the visual field
    private GridDisplay gd;                         //for graphical display of map
    private Dashboard db;
    private boolean termOut = false;
    
    protected GridClient gc;

    /**
     * KeyboardController constructor takes a string and an int
     * and creates a socket and connects with a serverSocket
     * PRE: h is a string and p is an int (preferably above 1024)
     * POST: GridClient connects to Grid via network sockets
     */
    public KeyboardController(String h, int p) {
	gc = new GridClient(h, p);
	visField = new LinkedList<GridObject>(); //the visual field contents will be held in linked list
	setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
	iTrans = getInsets();
	setTitle("Manual Agent: " + myID);                          //window title
	//setTitle("Agent View");                          //window title for generating figure for paper
	System.out.println("left:" + iTrans.left + " right:" + iTrans.right + " top:" + iTrans.top + " bottom:" + iTrans.bottom);
	setSize(cx * cellSize + iTrans.left + iTrans.right,
		ry * cellSize + dashHeight + iTrans.top + iTrans.bottom); //resize based on window cutoff
	gd = new GridDisplay(cx, ry, cellSize);  //initialize the graphical display
	db = new Dashboard(cx * cellSize, dashHeight, gc.gridOut);
	add(gd);
	add(db);

	setVisible(true);
    }

 
    /**
     * sendEffectorCommand sends the specified command to the grid
     * *NOTE: GOBAgent only looks at first letter of command string unless talk or shout is sent*
     * pre: command is either f, b, l, r, g, u, d, "talk" + message, or "shout" + message
     * post: command is sent via the printwriter
     */
    public void sendEffectorCommand(String command) {
	gc.gridOut.println(command);
    }
 
    /**
     * getSensoryInfo via the GridClient component
     */
    public void getSensoryInfo() {
	SensoryPacket sp = new SensoryPacket(gc.gridIn);
	String[] rawSenses = sp.getRawSenseData();
	// 1: get the smell info
	String heading = rawSenses[0];
	// 2: get the inventory
	String inventory = rawSenses[1];
	// 3: get the visual info
	String info = rawSenses[2];
	processRetinalField(info);
	// 4: get ground contents
	String ground = rawSenses[3];
	// 5: get messages
	String messages = rawSenses[4]; //CHECKS MESSAGES ****CHANGE****
	// 6: energy
	String energy = rawSenses[5];
	// 7: lastActionStatus
	String lastActionStatus = rawSenses[6];
	// 8: world time
	String worldTime = rawSenses[7];

	// store or update according to the data just read. . . .
	gd.updateGDObjects(visField);
	db.updateLabels(heading, inventory, ground, messages, energy, lastActionStatus, worldTime);
    }

    /* processRetinalField: takes a string input from the Maeden server and converts it into the GridObjects
     * Pre: String info contains list of list of list of chars(?)
     * Post: visual raphical map is constructed
     */
    private void processRetinalField(String info) {
	StringTokenizer visTokens = new StringTokenizer(info, "(", true);
	visTokens.nextToken();
	visField.clear();
	for (int i = 6; i >= 0; i--) {              //iterate backwards so character printout displays correctly
	    visTokens.nextToken();
	    for (int j=0; j <=4; j++) {             //iterate through the columns
		visTokens.nextToken();
		String visChars = visTokens.nextToken();
		char[] visArray = visChars.toCharArray();
		for(int x = 0; x < visChars.length(); x++) {
		    char cellChar = visArray[x];
		    switch(cellChar) {    //add the GridObjects for the graphical display
		    case ' ': break;
		    case '@': visField.addLast(new GOBRock(j, i, cellSize)); break;         //Rock
		    case '+': visField.addLast(new GOBFood(j, i, cellSize)); break;         //Food
		    case '#': visField.addLast(new GOBDoor(j, i, cellSize)); break;         //Door
		    case '*': visField.addLast(new GOBWall(j, i, cellSize)); break;         //Wall
		    case '=': visField.addLast(new GOBNarrows(j, i, cellSize)); break;      //Narrows
		    case 'K': visField.addLast(new GOBKey(j, i, cellSize)); break;          //Key
		    case 'T': visField.addLast(new GOBHammer(j, i, cellSize)); break;       //Hammer
		    case 'Q': visField.addLast(new GOBQuicksand(j, i, cellSize)); break;    //Quicksand
		    case 'O': visField.addLast(new GOBFoodCollect(j, i, cellSize)); break;  //Food Collection
		    case '$': visField.addLast(new GOBGold(j, i, cellSize, gd)); break;   //Gold
		    case 'R': visField.addLast(new GOBRobot(j, i, cellSize, gd)); break;  // Robot Monster
		    case 'G': visField.addLast(new GOBRayGun(j, i, cellSize, gd)); break;  // Robot-Monster-Killing Ray-Gun
		    default:
			if(cellChar >= '0' && cellChar <= '9' && i == 5 && j == 2)
			    visField.addLast(new GOBAgent(j, i, cellSize, 'N'));
			else if((cellChar >= '0' && cellChar <= '9') || cellChar == 'H')
			    visField.addLast(new GOBAgent(j, i, cellSize, '?'));
		    }
		}
		//System.out.println("i: " + i + " j: " + j);
	    }
	}
    }

 
    /**
     * run iterates through program commands
     * pre: sockets are connected to each other
     * post: program is run and exited when the agent reaches the food
     */
    public void run() {
	getSensoryInfo();
	while(true) {
	    gd.repaint();
	    getSensoryInfo();

        }
    }


    /**
     * main: creates new gridclient w/ the name of the server machine and the port number
     * pre: none
     * post: the program is run
     */
    public static void main(String [] args) {
	KeyboardController client = new KeyboardController("localhost", MAEDENPORT);
	client.run();
    }
    
    //-------------------------------------------------------------------------
 
    public class Dashboard extends Panel {
	
	private Label foodHeading;
	private Label foodIs;
	private Label invIs;
	private Label invObject;
	private Label groundIs;
	private Label groundList;
	private Label energyIs;
	private Label energyNum; 
	private Label msgIs;
	private Label msgInfo;
	private Label textIs;
	private TextField text;
	private Label lastActStatIs;
	private Label lastActStat;
	private Label worldTimeIs;
	private Label worldTime;

	//private Panel prompts;
	//private Panel vals;

	public Dashboard(int width, int height, PrintWriter pw){
	    /*
	    //setFont(new Font("GENEVA", Font.BOLD, 14));
	    setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
	    prompts = new Panel(new GridLayout(0, 1));
	    vals =  new Panel(new GridLayout(0, 1));
	    //setLayout(new GridLayout(0, 2));//note: rows expand as components added
	    setSize(width, height);
	    foodIs = new Label("The food is:");
	    foodHeading = new Label(" ", Label.LEFT);
	    invIs = new Label("Inventory:");
	    invObject= new Label(" ", Label.LEFT);
	    groundIs= new Label("Ground:");
	    groundList= new Label(" ", Label.LEFT);
	    energyIs= new Label("Energy:");
	    energyNum= new Label(" ", Label.LEFT);
	    msgIs= new Label ("Message:");
	    msgInfo= new Label (" ", Label.LEFT);
	    textIs= new Label("Command:");
	    text = new TextField(30);
	    lastActStatIs = new Label("Prev result:");
	    lastActStat = new Label(" ", Label.LEFT);
	    text.addActionListener(new GridDisplayListener(pw, text));
	    */

	    //setFont(new Font("GENEVA", Font.BOLD, 14));
	    setLayout(new GridLayout(0, 2, 0, 0));//note: rows expand as components added
	    setSize(width, height);
	    foodIs = new Label("The food is:  ", Label.RIGHT);
	    foodHeading = new Label(" ", Label.LEFT);
	    invIs = new Label("Inventory:  ", Label.RIGHT);
	    invObject= new Label(" ", Label.LEFT);
	    groundIs= new Label("Ground:  ", Label.RIGHT);
	    groundList= new Label(" ", Label.LEFT);
	    energyIs= new Label("Energy:  ", Label.RIGHT);
	    energyNum= new Label(" ", Label.LEFT);
	    msgIs= new Label ("Message:  ", Label.RIGHT);
	    msgInfo= new Label (" ", Label.LEFT);
	    textIs= new Label("Command:  ", Label.RIGHT);
	    text = new TextField(17);
	    lastActStatIs = new Label("Prev result:  ", Label.RIGHT);
	    lastActStat = new Label(" ", Label.LEFT);
	    worldTimeIs = new Label("World time:  ", Label.RIGHT);
	    worldTime = new Label(" ", Label.LEFT);
	    text.addActionListener(new GridDisplayListener(pw, text));

	    // msgInfo.setFont(new Font("GENEVA", Font.BOLD, 10));
	    foodIs.setSize(width/2, 50);
	    foodHeading.setSize(width/2, 50);
	    invIs.setSize(150, 50);
	    invObject.setSize(150, 60);
	    groundIs.setSize(150, 60);
	    groundList.setSize(150, 60);
	    energyIs.setSize(150, 60);
	    msgIs.setSize(150, 60);
	    msgInfo.setSize(150, 60);
	    textIs.setSize(150, 60);
	    text.setSize(150, 60);
	    lastActStat.setSize(150, 60);
	    worldTime.setSize(150, 60);
	    add(foodIs);
	    add(foodHeading);
	    add(invIs);
	    add(invObject);
	    add(groundIs);
	    add(groundList);
	    add(energyIs);
	    add(energyNum);
	    add(msgIs);
	    add(msgInfo);
	    add(textIs);
	    add(text);
	    add(lastActStatIs);
	    add(lastActStat);
	    add(worldTimeIs);
	    add(worldTime);

	    /*
	    prompts.add(foodIs);
	    vals.add(foodHeading);
	    prompts.add(invIs);
	    vals.add(invObject);
	    prompts.add(groundIs);
	    vals.add(groundList);
	    prompts.add(energyIs);
	    vals.add(energyNum);
	    prompts.add(msgIs);
	    vals.add(msgInfo);
	    prompts.add(textIs);
	    vals.add(text);
	    prompts.add(lastActStatIs);
	    vals.add(lastActStat);

	    add(prompts);
	    add(vals);
	    */

	    //	    validate();
	    
	    //	    System.out.println(foodIs.getWidth() + " " + foodIs.getHeight());
	}

	//, String h, String inv, String g, String e, String m) {
	public void updateLabels(String h, String inv, String g, String m, String e, String res, String wt){
	    foodHeading.setText(h);
	    invObject.setText(inv);
	    groundList.setText(g);
	    msgInfo.setText(m);
	    energyNum.setText(e);
	    lastActStat.setText(res);
	    worldTime.setText(wt);
	    repaint();
	}


	class GridDisplayListener implements ActionListener{
	    
	    PrintWriter gridOut;
	    TextField text;
	    
	    public GridDisplayListener(PrintWriter pw, TextField tf){
		gridOut=pw;
		text=tf;
		//System.out.println("initialized");
	    }

	    public void actionPerformed(ActionEvent e){
		CommandCheck check = new CommandCheck();
		String command = text.getText();
		command = check.validateCommand(command);
		gridOut.println(command);
		text.setText("");
		//System.out.println("SENT: " + text.getText().toLowerCase());
	    }
	    
	}
	
		
    }

    public class CommandCheck {
	    
	private String commandString;   //stores the string that is sent in by the user
	    	   
	private void printHelp() {
	    System.out.println("Maeden help information");
	    System.out.println("Allowable commands are:");
	    System.out.println("f: move forward");
	    System.out.println("b: move backward");
	    System.out.println("r: turn right");
	    System.out.println("l: turn left");
	    System.out.println("g: grab an object in the current spot");
	    System.out.println("d: drop an object currently being carried");
	    System.out.println("u: apply a carried object (tool or food)");
	    System.out.println("a: attack an agent ahead");
	    System.out.println("w: wait");
	    System.out.println("k: remove yourself from world");
	    System.out.println("?: print this help information");
	}
	
	/**
	 * invalidCommand: string -> boolean
	 * In general, a command is invalid if its length is 0 (the user just pressed enter),
	 * or the command starts with an invalid letter (one that's not fbrldguwst).
	 * If the first letter of the command is g, if additional requirments are not met the command
	 * is invalid. A valid "g command" is of the form "g [item]" where [item] begins with +, k or t.   
	 */
	public boolean invalidCommand(String commandString)
	{
	    return (commandString.length() == 0
		    || "fbrldguwstka?".indexOf(commandString.substring(0,1)) < 0);
	}

	public String validateCommand(String commandString) {
	    try {
		    
		commandString=commandString.toLowerCase();
		while ( invalidCommand(commandString) )
		    {
			printHelp();
			return null;
		    }
	    }
	    catch(Exception e) {System.out.println("validateCommand: " + e);}
	    return commandString;
	}
    }


}
