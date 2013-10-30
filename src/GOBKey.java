///*maedengraphics
import java.awt.*;
//maedengraphics*/

/*
 *@author:  Wayne Iba
 *@date:    5-31-05
 *@version: Beta 0.1
 */

public class GOBKey extends GridObject {

    //constructor sets printchar and color
    public GOBKey(int ix, int iy, int s){
	super(ix,iy,s);
	printChar = 'K';       //printchar is K
	///*maedengraphics
	myColor = Color.gray; //color is gray for possible white background
	//maedengraphics*/
    }

    //Paint key object
    ///*maedengraphics
    public void paint(Graphics g) {
	g.translate(pix.x, pix.y);
	g.setColor(myColor);
	g.fillOval(2,scale/4, scale/4, scale/2);
	g.fillRect(scale/6, scale/2-scale/10, scale/2, scale/5);
	g.fillRect(2*scale/3, scale/2-scale/10, scale/4, scale/3);
	g.translate(-pix.x, -pix.y);
    }
    //maedengraphics*/
}

