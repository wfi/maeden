import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.LinkedList;
import java.util.Iterator;


/**************************************
 * class GridDisplay provides methods for providing a graphic for an agent's visual field
 *@author:  Josh Holm and Wayne Iba
 *@date:    5-31-05
 *@version: Beta 0.1
 *************************************/

public class GridDisplay extends Canvas {

    private int colX, rowY, dashHeight;           //ints for holding the number of rows and columns (y and x values)
    private LinkedList gridObjects;   //container for holding gridobjects to be drawn
    private int cellSize;             //for holding the size in pixels of one cell
    //private Dashboard dashboard;


    /*Constructor takes in the number of rows, columns, and the size of a cell
     * Pre: cx == number of columns
     *      ry == number of rows
     *      cSize == size of one cell in pixels
     * Post: new graphics window is created with (cx * cSize x ry * cSize) as the size of the window
     */
    public GridDisplay(int cx, int ry, int cSize) {
	colX = cx;
	rowY = ry;
	cellSize = cSize;
	gridObjects = new LinkedList();
	setSize(colX * cellSize, rowY * cellSize + dashHeight); //size of the window
	setVisible(true);

	setSize(colX * cellSize, rowY * cellSize);

    }

    //stores the gridObjects to be drawn and calls the paint GridDisplay's paint function
    public void updateGDObjects(LinkedList gobs) {

	//dashboard.updateLabels(h, inv, g, e, m);
	
	gridObjects.clear();
	for (Iterator i = gobs.iterator(); i.hasNext(); ) {
	    gridObjects.addLast(i.next());
	}
    }

    /*paint draws gridlines and draws all gridObjects for the current agent
     * Pre: gridObjects is not empty and contains GridObjects
     * Post: all gridlines and gridobjects are drawn in the window
     */
    public void paint(Graphics g) {
	//iTrans = getInsets();                                       //compensates for the frame cutoff
	//g.translate(iTrans.left, iTrans.top);
	g.setColor(Color.gray.brighter());          //set the color
	for(int i = 0; i < rowY; i++)                                         //paint the horizontal gridlines
	    g.drawLine(0, (i * cellSize), (colX * cellSize), (i * cellSize));
	for(int j = 0; j < colX; j++)                                         //paint the vertical gridlines
	    g.drawLine((j * cellSize), 0, (j * cellSize), (rowY * cellSize));

	if(gridObjects != null && gridObjects.size() != 0) {                    //paint the individual gridobjects
	    for(Iterator i = gridObjects.iterator(); i.hasNext();) {
		//all gridobjects in the visual field are stored in visField
		GridObject obj = (GridObject) i.next();
		obj.paint(g);
	    }
	}

	g.setColor(Color.black);
	g.drawLine(0, (rowY * cellSize)-1, (colX * cellSize), (rowY * cellSize)-1);//draw border line

    }


}
