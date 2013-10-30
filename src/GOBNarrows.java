///*maedengraphics
import java.awt.*;
//maedengraphics*/

/**
 *@author:  Wayne Iba
 *@date:    5-31-05
 *@version: Beta 0.1
 * Allows an agent to pass only if not carrying anything
 */

public class GOBNarrows extends GridObject {

    //Constructor sets printchar, color, shareable
    public GOBNarrows(int ix, int iy, int s){
	super(ix,iy,s);
	printChar = '=';       //printchar is =
	///*maedengraphics
	myColor = Color.black; //color is black
	//maedengraphics*/
	shareable = false;     //not shareable
    }

    // Over-ride defaul function to check agent's inventory
    //if agent not carrying anything, shareable, otherwise not shareable
    public boolean allowOtherGOB(GridObject otherGOB){
	if (((GOBAgent)otherGOB).inventory == null || ((GOBAgent)otherGOB).inventory.isEmpty())
	    return true;
	else
	    return false;
    }

    // Paint narrows object (squares in each corner of cell)
    ///*maedengraphics
    public void paint(Graphics g) {
	g.translate(pix.x, pix.y);
	g.setColor(myColor);
	int postThickness = (int) (scale * .3);
	// draw posts at the four corners
	g.fillRect(0,0, postThickness, postThickness);
	g.fillRect(0, scale - postThickness, postThickness, postThickness);
	g.fillRect(scale - postThickness,0, postThickness, postThickness);
	g.fillRect(scale - postThickness, scale - postThickness, postThickness, postThickness);
	g.translate(-pix.x, -pix.y);
    }
    //maedengraphics*/
}

