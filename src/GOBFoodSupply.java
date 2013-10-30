import java.util.NoSuchElementException;
///*maedengraphics
import java.awt.*;
//maedengraphics*/

/*
 *@author:  Wayne Iba
 *@date:    2-25-2012
 *@version: Beta 0.5
 *
 * GOBFoodSupply models a potentially very large food source which can be
 * repeatedly accessed, yielding food pieces.
 * 2/25/2012: originally, time interval was action steps, but with introduction of simulation time we needed to revise
 * 12/2/2010: add time interval between which cannot produce food piece.
 */

public class GOBFoodSupply extends GOBFood {

    private int amountSupplied = 0;
    private int lastSupplyTime = Integer.MIN_VALUE;
    private int FOOD_YIELD_INTERVAL = 10; // number cycles through Grid.run loop, each having WORLD_CYCLE_TIME duration

    //Constructor stes printchar, color
    public GOBFoodSupply(int ix, int iy, int s){
	super(ix,iy,s);
	printChar = '+';        //printchar is +
	///*maedengraphics
	myColor = Color.yellow; //color is yellow
	//maedengraphics*/
    }

    /* onGrab: override default onGrab method.  Increment amountSupplied
     * and return a food piece.
     */
    public GridObject onGrab(GOBAgent a){
	if ( lastSupplyTime + ( FOOD_YIELD_INTERVAL * a.getGrid().WORLD_CYCLE_TIME ) < a.simTime() ){
	    amountSupplied++;
	    lastSupplyTime = a.simTime();
	    return new GOBFood(pos.x, pos.y, scale);
	} else
	    throw new NoSuchElementException();
    }

}

