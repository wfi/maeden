///*maedengraphics
import java.awt.*;
//maedengraphics*/
import java.awt.Point;
import java.util.NoSuchElementException;

/*
 *@author:  Wayne Iba
 *@date:    5-31-05
 *@version: Beta 0.1
 */

public class GOBFood extends GridObject {

    private int FOODINC = 500;

    //Constructor stes printchar, color
    public GOBFood(int ix, int iy, int s){
	super(ix,iy,s);
	printChar = '+';        //printchar is +
	///*maedengraphics
	myColor = Color.yellow; //color is yellow
	//maedengraphics*/
    }

    public int foodInc(){
	return FOODINC;
    }

    /* onDrop: override the default onDrop so that, when necessary, we call the storeFood method of
     * the FoodCollect that might be here and then remove this item.  Otherewise do the normal thing
     * reset pos and add self back to Grid
     */
    public void onDrop(GOBAgent a, Grid mg){
	try {
	    GOBFoodCollect fc = (GOBFoodCollect) mg.getTool(a, pos.x, pos.y, 'O');
	    fc.storeFood();
	} catch (NoSuchElementException e) {
	    pos = new Point(pos);
	    setPlace(pos);
	    mg.addGOB(this);
	}
    }


    //Paint food object (yellow cone)
    ///*maedengraphics
    public void paint(Graphics g) {
	g.translate(pix.x, pix.y);
	g.setColor(myColor);
	g.fillArc(2-scale*2/3,2-scale*2/3, scale*3/2, scale*3/2, -70, 55);
	g.translate(-pix.x, -pix.y);
    }
    //maedengraphics*/
}

