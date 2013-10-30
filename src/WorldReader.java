import java.io.*;
import java.util.LinkedList;

/**
 *@author:  Cuyler Cannon, Wayne Iba
 *@date:    7/6/2009, 5-31-05
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
    
    private LinkedList myPreAgents = new LinkedList();
    private String windowTitle;
    private int logWidth, logHeight;
    private char map [][];    // map[rows][columns]
    
    public WorldReader(String file) throws FileNotFoundException, IOException {
        
        // Open the file
        //FileReader dataFile = new FileReader(file);
        BufferedReader dataFile = new BufferedReader(new FileReader(file));
        // Fetch title (FIRST LINE)
        windowTitle = readLine(dataFile);
        //System.out.println("(WorldReader): \"" + windowTitle + "\"");
        
        // Fetch logical width (SECOND LINE)
        logWidth = readInt(dataFile);
        //System.out.println("(WorldReader): Logical Width: " + logWidth);
        
        // Fetch logical height (SECOND LINE)
        logHeight = readInt(dataFile);
        //System.out.println("(WorldReader): Logical Height: " + logHeight);
        
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
        readCharMap(dataFile, logWidth, logHeight);
        
        // Close data file
        dataFile.close();
        
    }
    
    private String readLine(BufferedReader dataFile) throws IOException {
        // Takes everything from current point in file up to next LF
        
        StringBuffer line = new StringBuffer();
        int curSymbol = dataFile.read();
        
        while ((curSymbol != -1) && (curSymbol != 10)) {
            line.append((char)curSymbol);
            curSymbol = dataFile.read();
        }
            
        return line.toString();
        
    }
    
    private int readInt(BufferedReader dataFile) throws IOException {
        // Takes everything up to next LF or space,
        // returning it as an int.
        
        StringBuffer word = new StringBuffer();
        int curSymbol = dataFile.read();
        
        while ((curSymbol != -1) && (curSymbol != 10) && (curSymbol != 32)) {
            word.append((char)curSymbol);
            curSymbol = dataFile.read();
        }
        
        int retVal = 0;
        try {
            retVal = Integer.parseInt(word.toString());
        }
        catch (NumberFormatException q) {
            System.out.println("(WorldReader): File Format Error!" + word.toString());
            System.exit(1);
        }
        
        return retVal;
        
    }
    
    private void readCharMap(BufferedReader dataFile, int cols, int rows) throws IOException {
        // Takes the next rows * columns non-LF chars and puts them into a char array
        
        int curSymbol;
        for (int h = 0; h < rows; h++) {
            for (int w = 0; w <= cols; w++) { // <= because of LF's
                curSymbol = dataFile.read();
                if (w < logWidth)
                    map[w][h] = (char)curSymbol;
            }
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
