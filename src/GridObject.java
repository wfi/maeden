///*maedengraphics
import java.awt.*;
//maedengraphics*/
import java.awt.Point;
/**
 * Super-class for objects appearing in a Grid world.
 *
 *@author:  Wayne Iba
 *@date:    5-31-05
 *@version: Beta 0.1  
 */

public class GridObject {
    protected char printChar = 'w';
    public Point pos;    // The logical cell x, y (column,row) location of this object;
    protected Point pix; // The screen location of this object
    ///*maedengraphics
    public Color myColor = Color.blue;
    //maedengraphics*/
    protected int scale;
    protected boolean shareable = true;

    /*
     *Constructor sets the point, scale, and physical point of the GridObject
     */
    public GridObject(int ix, int iy, int s){
	pos = new Point(ix,iy);      //point in cells
	pix = new Point(ix*s, iy*s); //physical point
	scale = s;                   //scale
    }

    //public accessor for the print character
    public char printChar() {
	return printChar;
    }

    //public method for setting new print character
    public void newPrintChar(char newPChar) {
	printChar = newPChar;
    }

    /**
     * whether this object will share location with the "otherGOB", which
     * is typcially an agent.  By default, returns value of shareable flag.
     * If GridObject allows others to be in same location, then true, else false
     */
    public boolean allowOtherGOB(GridObject otherGOB){ return shareable; }

    // sets this GridObject's location to the given point
    public void setPlace(Point p){
	pos.x = p.x; pos.y = p.y;
	pix.x = p.x*scale; pix.y = p.y*scale;
    }

    /*
     * certain GridObjects can be acted upon by others
     * defaults to nothing
     */
    public boolean actedOnBy(GridObject tool, Grid gw){ return false; } // default is return false

    /* onGrab: return this object by default.  Sub-classes may override
     * and perform arbitrary actions before returning a GridObject (not necessarily
     * the same one that was acted on).
     */
    public GridObject onGrab(GOBAgent a){
	return this;
    }

    /* onDrop: reset pos and add self back to Grid
     */
    public void onDrop(GOBAgent a, Grid mg){
	pos = new Point(pos);
	setPlace(pos);
	mg.addGOB(this);
    }

    /*
     * GridObjects take care of painting themselves so that Grid does not have to
     * simply calls this paint function
     */
    ///*maedengraphics
    public void paint(Graphics g) {
	g.translate(pix.x, pix.y);
	g.setColor(myColor);
	g.fillRect(0,0, scale, scale);
	g.translate(-pix.x, -pix.y);
    }
    //maedengraphics*/
}
