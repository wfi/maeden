///*maedengraphics
import java.awt.Color;
import java.awt.Graphics;
//maedengraphics*/
/*
 *@author:  Wayne Iba
 *@date:    5-31-05
 *@version: Beta 01
 */

public class GOBDoor extends GridObject {

    //Constructor sets printchar, color, and shareable
    public GOBDoor(int ix, int iy, int s){
	super(ix,iy,s);
	printChar = '#';     //printchar is #
	///*maedengraphics
	myColor = Color.red; //color is red
	//maedengraphics*/
	shareable = false;   //not shareable
    }

    /*
     * door can be acted upon by a key
     * if so, remove the door
     */
    public boolean actedOnBy(GridObject tool, Grid gw){
	// if tool is a key, destroy self (destroy key in GOBAgent)
	if ( tool.getClass().getName().equals("GOBKey") ){
	    gw.removeGOB(this);
	    return true;
	}
	// otherwise, the action failed
	return false;
    }
    // Paints door object
    ///*maedengraphics
    public void paint(Graphics g) {
	g.translate(pix.x, pix.y);
	g.setColor(myColor);
	g.fillRect(scale/5, scale/5, scale*3/5, scale*4/5);
	g.setColor(Color.black);
	g.drawRect(scale/5, scale/5, scale*3/5, scale*4/5);
	g.fillOval(scale*4/7, scale*3/5, scale/8, scale/8);
	g.translate(-pix.x, -pix.y);
    }
    //maedengraphics*/
}

