///*maedengraphics
import java.awt.*;
//maedengraphics*/
/*
 *@author:  Wayne Iba
 *@date:    5-31-05
 *@version: Beta 0.1
 *
 * GOBFoodCollect models a home-base or receptical where an unlimited amount of food can be
 * deposited and stored (effectively consuming the food items that are dropped there).
 */

public class GOBFoodCollect extends GOBFood {

    private int amountDeposited = 0;

    //Constructor stes printchar, color
    public GOBFoodCollect(int ix, int iy, int s){
	super(ix,iy,s);
	printChar = 'O';        //printchar is O (letter Oh)
	///*maedengraphics
	myColor = Color.black;
	//maedengraphics*/
    }

    /* storeFood: override default onDrop method.  Increment amountDeposited
     * and remove the dropped food piece.
     */
    public void storeFood(){
	amountDeposited++;
    }


    //Paint food object (yellow cone)
    ///*maedengraphics
    public void paint(Graphics g) {
	g.translate(pix.x, pix.y);
	g.setColor(myColor);
	g.drawArc(2-scale*2/3,2-scale*2/3, scale*3/2, scale*3/2, -70, 55);
	//
	g.drawLine(scale*1/6, scale*1/6, scale*5/6, scale*1/3);
	g.drawLine(scale*1/6, scale*1/6, 2+scale*1/3, scale*5/6);
	//
	g.translate(-pix.x, -pix.y);
    }
    //maedengraphics*/
}

