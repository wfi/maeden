///*maedengraphics
import java.awt.*;
//maedengraphics*/

/*
 *@author:  Wayne Iba
 *@date:    5-31-05
 *@version: Beta 0.1
 */

public class GOBRock extends GridObject {

    //Constructor sets printchar, color, shareable
    public GOBRock(int ix, int iy, int s){
	super(ix,iy,s);
	printChar = '@';          //printchar is @
	///*maedengraphics
	myColor = Color.darkGray; //color is dark Gray
	//maedengraphics*/
	shareable = false;        //not shareable
    }

    /*
     * can be acted on by a hammer
     * if so, remove this rock
     */
    public boolean actedOnBy(GridObject tool, Grid gw){
	if ( tool.getClass().getName().equals("GOBHammer") ){
	    gw.removeGOB(this);
	    return true;
	}
	// otherwise, return false
	return false;
    }

    //Paint Rock object (large dark gray oval)
    ///*maedengraphics
    public void paint(Graphics g) {
	g.translate(pix.x, pix.y);
	g.setColor(myColor);
	g.fillRoundRect(3,3, scale-5, scale-5, scale/3, scale/3);
	g.translate(-pix.x, -pix.y);
    }
    //maedengraphics*/
}

