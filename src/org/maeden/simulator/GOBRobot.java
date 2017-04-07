package org.maeden.simulator;
///*maedengraphics
import java.awt.*;
import java.awt.image.ImageObserver;
//maedengraphics*/
/*
 *@author:  Wayne Iba
 *@date:    10-29-13
 */

public class GOBRobot extends GridObject {

    ///*maedengraphics
    private Toolkit tk = Toolkit.getDefaultToolkit();
    private Image robotMonster = tk.createImage("robot-monster.gif");
    private ImageObserver myObsv;
    //maedengraphics*/

    //Constructor sets printchar, and shareable
    public GOBRobot(int ix, int iy, int s){
	super(ix,iy,s);
	printChar = 'R';     //printchar is R
	///*maedengraphics
	myColor = Color.green; //color is green
	//maedengraphics*/
	shareable = false;   //not shareable
    }

    ///*maedengraphics
    public GOBRobot(int ix, int iy, int s, ImageObserver obsv){
	super(ix,iy,s);
	printChar = 'R';
	myObsv = obsv;
	shareable = false;
    }
    //maedengraphics*/

    /*
     * Robot can be acted upon by a RayGon
     * if so, remove the Robot
     */
    public boolean actedOnBy(GridObject tool, Grid gw){
	// if tool is a key, destroy self (destroy key in GOBAgent)
	if ( tool.getClass().getName().equals("GOBRayGun") ){
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
	g.drawImage(robotMonster, 0, 0, scale, scale, myObsv);
	g.translate(-pix.x, -pix.y);
    }
    //maedengraphics*/
}

