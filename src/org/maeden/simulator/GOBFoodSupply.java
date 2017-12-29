package org.maeden.simulator;
import java.util.NoSuchElementException;
///*maedengraphics
import java.awt.*;
//maedengraphics*/

/**
 * GOBFoodSupply models a potentially very large food source which can be
 * repeatedly accessed, yielding food pieces.  Food may be yielded on a period based on the last time the
 * food was harvested.  The FOOD_YIELD_INTERVAL controls the number of through the main simulation loop, Grid.run().
 * Since that loop runs every WORLD_CYCLE_TIME milliseconds, the approximate wall-time between harvests is
 * FOOD_YIELD_INTERVAL * WORLD_CYCLE_TIME.

 *@author:  Wayne Iba
 *@date:    2017-10-01
 */

public class GOBFoodSupply extends GOBFood {

    private int amountSupplied = 0;
    private int lastSupplyTime = Integer.MIN_VALUE;
    /** number cycles through Grid.run loop, each having WORLD_CYCLE_TIME duration */
    protected int FOOD_YIELD_INTERVAL = 50;

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
        if ( lastSupplyTime + FOOD_YIELD_INTERVAL  <  a.simTime() ){
            amountSupplied++;
            lastSupplyTime = a.simTime();
            return new GOBFood(pos.x, pos.y, scale);
        } else
            throw new NoSuchElementException();
    }

}

