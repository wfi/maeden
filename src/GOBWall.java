///*maedengraphics
import java.awt.Color;
//maedengraphics*/

/*
 *@author:  Wayne Iba
 *@date:    5-31-05
 *@version: Beta 0.1
 */

public class GOBWall extends GridObject {

    //Constructor sets printchar, color and shareable
    public GOBWall(int ix, int iy, int s){
	super(ix,iy,s);
	printChar = '*';       //printchar is *
	///*maedengraphics
	myColor = Color.black; //color is black
	//maedengraphics*/
	shareable = false;     //not shareable
    }

}

