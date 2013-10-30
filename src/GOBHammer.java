///*maedengraphics
import java.awt.Color;
import java.awt.Graphics;
//maedengraphics*/
/*
 *@author:  Wayne Iba
 *@date:    5-31-05
 *@version: Beta 0.1
 */

public class GOBHammer extends GridObject {

    //Constructor sets printchar, color
    public GOBHammer(int ix, int iy, int s){
	super(ix,iy,s);
	printChar = 'T';      //printchar is T
	///*maedengraphics
	myColor = Color.gray; //color is gray
	//maedengraphics*/
    }

    //Paint hammer object (shape of a T)
    ///*maedengraphics
    public void paint(Graphics g) {
	g.translate(pix.x, pix.y);
	g.setColor(Color.DARK_GRAY);
	g.fillRect(scale/4, scale/2-scale/10, scale - scale/3, scale/5);
	g.setColor(myColor);
	g.fillRect(2,scale/4, scale/4, scale/2);
	g.translate(-pix.x, -pix.y);
    }
    //maedengraphics*/
}

