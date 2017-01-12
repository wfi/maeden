import java.io.*;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.InputMismatchException;

/**
 *@author:  Cuyler Cannon, Wayne Iba
 *@date:    1/11/2017, 7/6/2009, 5-31-05
 *@version: Beta 0.1
 * 
 * The WorldReader processes a Maeden world file and builds a temporary
 * representation that can be used by the Grid constructor to initialize
 * the Grid.  
 * 
 * The format of the input file must be as follows:
 * FIRST LINE: Name of the World -- spaces are allowed.  This gets used as the
 * name of the window and the world
 * SECOND LINE: Grid dimensions -- x and y (columns and rows) dimensions of grid
 * Subsequent lines: world layout; world must be surrounded by walls.  Currently,
 *   obstacles in {#@*}, semi-obstacles in {Q=} and tools in {TK+} are supported
 *   by the Grid class, but the WorldReader treats the world contents simply as text
 *   and does not inspect the contents.
 */

public class WorldReader {
    
    private Scanner worldFileScanner;
    private LinkedList myPreAgents = new LinkedList();
    private String windowTitle;
    private int logWidth, logHeight;
    private char map [][];    // map[rows][columns]
    
    public WorldReader(String file) throws FileNotFoundException, IOException, InputMismatchException  {
        
        // Open the file
        //BufferedReader dataFile = new BufferedReader(new FileReader(file));
	worldFileScanner = new Scanner(new File(file));
        // Fetch title (FIRST LINE)
        windowTitle = worldFileScanner.nextLine().trim();
        //System.out.println("(WorldReader): \"" + windowTitle + "\"");
        
        // Fetch logical width (SECOND LINE)
        logWidth = worldFileScanner.nextInt();
        //System.out.println("(WorldReader): Logical Width: " + logWidth);
        
        // Fetch logical height (SECOND LINE)
        logHeight = worldFileScanner.nextInt();
        //System.out.println("(WorldReader): Logical Height: " + logHeight);

	// Flush rest of second line
	worldFileScanner.nextLine();
        
	//System.out.println("Ready to read 3rd line:");
        // Fetch agent information: (THIRD LINE)
	//String thirdLine = readLine(dataFile);
	//String[] aInfo = thirdLine.split("\\s");
	//int numAgents = Integer.parseInt(aInfo[0]);
	//int j = 1;
	//for (int i = 0; i < numAgents; i++){
	//    myPreAgents.addLast(new PreAgent(aInfo[j++].toCharArray()[0],
	//				     Integer.parseInt(aInfo[j++]),
	//				     Integer.parseInt(aInfo[j++])));
	//}
	    
        // Fetch map
        map = new char[logWidth][logHeight];
        readCharMap(logWidth, logHeight);
        
        // Close data file
        //dataFile.close();
	worldFileScanner.close();
        
    }
    
    
    private void readCharMap(int cols, int rows) throws IOException {
        // Takes the next rows * columns non-LF chars and puts them into a char array
        
        char curSymbol;
        for (int h = 0; h < rows; h++) {
	    String aLine = worldFileScanner.nextLine();
            for (int w = 0; w < cols; w++) { 
                curSymbol = aLine.charAt(w);
		//System.out.print(curSymbol);
                if (w < logWidth)
                    map[w][h] = curSymbol;
            }
	    //System.out.println();
        }
        
    }
    
    public String windowTitle() { return windowTitle; }
    public int cols() { return logWidth; }
    public int rows() { return logHeight; }
    //public char[][] map() { return map; }
    public char map(int x, int y) { return map[x][y]; }
    
    /*
    public PreAgent popPA(){
	if (! myPreAgents.isEmpty() )
	    return (PreAgent) myPreAgents.removeFirst();
	System.out.println("Tried to pop pre-agent from empty list");
	System.exit(-2);
	return null;
    }

    public class PreAgent {
	public char ihead;
	public int xCol;
	public int yRow;

	public PreAgent(char h, int x, int y){
	    ihead = h;
	    xCol = x;
	    yRow = y;
	}
    }
    */
}
