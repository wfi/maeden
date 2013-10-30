///*maedengraphics
import java.awt.*;
import java.awt.image.ImageObserver;
//maedengraphics*/
import java.awt.Point;
import java.util.NoSuchElementException;

/*
 *@author:  Wayne Iba
 *@date:    11-13-2010
 *@version: Beta 0.1
 */

public class GOBGold extends GridObject {

    ///*maedengraphics
    private Toolkit tk = Toolkit.getDefaultToolkit();
    private Image gooold = tk.createImage("mydollar.gif");
    private ImageObserver myObsv;
    //maedengraphics*/

    //Constructor stes printchar, color
    public GOBGold(int ix, int iy, int s){
	super(ix,iy,s);
	printChar = '$';        //printchar is +
	///*maedengraphics
	myColor = Color.yellow; //color is yellow
	//maedengraphics*/
    }

    ///*maedengraphics
    public GOBGold(int ix, int iy, int s, ImageObserver obsv){
	super(ix,iy,s);
	printChar = '$';
	myObsv = obsv;
    }
    //maedengraphics*/


    //Paint gold object (yellow Dollar $ign)
    ///*maedengraphics
    public void paint(Graphics g) {
	g.translate(pix.x, pix.y);
	g.setColor(myColor);
	g.drawImage(gooold, 0, 0, scale, scale, myObsv);
	g.translate(-pix.x, -pix.y);
    }
    //maedengraphics*/
}

