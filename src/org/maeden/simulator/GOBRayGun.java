package org.maeden.simulator;
///*maedengraphics
import java.awt.*;
import java.awt.image.ImageObserver;
//maedengraphics*/

/*
 *@author:  Wayne Iba
 *@date:    10-29-13
 */

public class GOBRayGun extends GridObject {

    ///*maedengraphics
    private Toolkit tk = Toolkit.getDefaultToolkit();
    private Image rayGun = tk.createImage("ray-gun.gif");
    private ImageObserver myObsv;
    //maedengraphics*/

    //constructor sets printchar and color
    public GOBRayGun(int ix, int iy, int s){
	super(ix,iy,s);
	printChar = 'G';       //printchar is G
	///*maedengraphics
	myColor = Color.green; //color is gray for possible white background
	//maedengraphics*/
    }

    ///*maedengraphics
    public GOBRayGun(int ix, int iy, int s, ImageObserver obsv){
	super(ix,iy,s);
	printChar = 'G';       //printchar is G
	myColor = Color.green; //color is gray for possible white background
	myObsv = obsv;
    }
    //maedengraphics*/


    //Paint key object
    ///*maedengraphics
    public void paint(Graphics g) {
	g.translate(pix.x, pix.y);
	g.drawImage(rayGun, 0, 0, scale, scale, myObsv);
	g.translate(-pix.x, -pix.y);
    }
    //maedengraphics*/
}

