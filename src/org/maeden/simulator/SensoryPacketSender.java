package org.maeden.simulator;

import java.awt.Point;
import java.util.List;
import org.json.simple.JSONArray;

/**
 * The Grid server counterpart to the org.maeden.controller.SensoryPacket.
 * Bundles the relevant sensory data and sends it to an agent controller.
 *
 * @author: Wayne Iba
 * @version: 2017093001
 */
public class SensoryPacketSender
{
    private int xCols, yRows;
    private LinkedListGOB[][] myMap;                 //holds gridobjects
    private GridObject food;

    /** Constructor
     * @param myMap providing access to the Grid's contents
     */
    public SensoryPacketSender(LinkedListGOB[][] myMap, GridObject food){
        this.myMap = myMap;
        this.xCols = myMap.length;
        this.yRows = myMap[0].length;
        this.food = food;
    }

    /**
     * sendSensationsToAgent: send sensory information to given agent
     * LINE1: Simulation status. One of: DIE, END, SUCCESS, CONTINUE
     * LINE2: smell direction to food
     * LINE3: inventory in form ("inv-char")
     * LINE4: visual array (as single string) in form ((row ("cell") ("cell"))(row ("cell")))
     * LINE5: ground contents of agent position in form ("cont" "cont")
     * LINE6: Agent's messages
     * LINE7: Agent's energy
     * LINE8: last action's result status (ok or fail)
     * @param a the agent to which the information should be sent
     */
    @SuppressWarnings("unchecked")
    public void sendSensationsToAgent(GOBAgent a) {
        if (a.getNeedUpdate()) {
            JSONArray jsonArray = new JSONArray();
            // We added String.valueOf to make sure that everything that is send is a String.
            jsonArray.add(String.valueOf(Grid.relDirToPt(a.pos, new Point(a.dx(), a.dy()), food.pos))); // 1. send smell
            String inv = "(";
            if (a.inventory().size() > 0){
                for (GridObject gob : a.inventory()) {
                    inv += "\"" + gob.printChar() + "\" ";
                }
            }
            inv = inv.trim() + ")";
            jsonArray.add(String.valueOf(inv)); // 2. send inventory
            jsonArray.add(String.valueOf(visField(a.pos, new Point(a.dx(), a.dy())))); // 3. send visual info
            jsonArray.add(String.valueOf(groundContents(a, myMap[a.pos.x][a.pos.y])));  // 4.send contents of current location
            //jsonArray.add(String.valueOf(sendAgentMessages(a)));  // 5. send any messages that may be heard by the agent
            jsonArray.add("[]");
            jsonArray.add(String.valueOf(a.energy()));  // 6. send agent's energy
            jsonArray.add(String.valueOf(a.lastActionStatus()));// 7. send last-action status
            jsonArray.add(String.valueOf(a.simTime())); // 8. send world time
            a.send().println(jsonArray); // send JsonArray
            a.setNeedUpdate(false);
        }
    }

    /**
     * visField: extract the local visual field to send to the agent controller
     * INPUT: agent point location, and agent heading (as point)
     * OUTPUT: sequence of characters
     * parens encapsulate three things: the whole string,
     * the row, the individual cells. The contents of individual cells are
     * lists of strings. See README.SensoryMotor for more description and examples.
     * The row behind the agent is given first followed by its current row and progressing away from the agent
     * with characters left-to-right in visual field.
     */
    public String visField(Point aPt, Point heading){
        String myString = "(";
        int senseRow, senseCol;
        //iterate from one behind to five in front of agent point
        for (int relRow=-1; relRow <= 5; relRow++) {
            //add paren for the row
            myString += "(";
            String rowString = "";
            //iterate from two to the left to two to the right of agent point
            for (int relCol=-2; relCol <= 2; relCol++){
                senseRow = aPt.x + relRow * heading.x + relCol * -heading.y;
                senseCol = aPt.y + relRow * heading.y + relCol * heading.x;
                //add cell information
                rowString += " " + visChar(mapRef(senseRow, senseCol), heading);
            }
            //trim any leading or closing spaces, close row paren
            myString += rowString.trim() + ")";
        }
        //return string with close paren
        return myString + ')';
    }

    /** visChar iterates through the gridobjects located in a cell and returns all of their printchars
     * enclosed in parens and quotes: ("cont1 cont2 cont3")
     * The one exception is the agent.  For an agent, its agent-id is returned (0-9)
     * Note: the heading of an agent is not reported at this time.
     * Pre: cellContents contains any and all gridobjects in a cell
     * Post: String ("cont1 cont2 cont3") is returned (where cont1-3 are gridobject printchars or agent IDs)
     * @param cellContents the GOBs in a particular cell
     * @param heading (which is not used)
     * @return a String that represents a list of items in the cell
     */
    private String visChar(List<GridObject> cellContents, Point heading){
        String cellConts = "(";
        //if there are any gridobjects in the cell iterate and collect them
        if (cellContents != null && !cellContents.isEmpty()) {
            //iterate through cellContents, gather printchars or agent IDs
            for(GridObject gObj : cellContents) {
                if(gObj.printChar() == 'A') {           //if it is an agent
                    cellConts = cellConts + "\"" + ((GOBAgent)gObj).getAgentID() + "\" ";
                } else {        //if gridobject is not an agent, return its print character
                    cellConts = cellConts + "\"" + gObj.printChar() + "\" ";
                }
            }
            //trim leading and closing spaces
            cellConts = cellConts.trim() + ')';
            return cellConts;
        }
        //otherwise return a space representing no gridobject
        else
            return "()";
    }

    
    /**
     * mapRef: safe map reference checking for out-of-bounds indexing
     * @param x the horizontal index
     * @param y the vertical index
     */
    private List<GridObject> mapRef(int x, int y){
        if ( (x < 0) || (x >= xCols) || (y < 0) || (y >= yRows) ) return null;
        else return myMap[x][y];
    }


    /*
     * groundContents iterates through the cell the agent is standing on and returns a string of chars
     * enclosed in quotes and parens to represent what is in the cell
     * Pre: a is GOBAgent who is in cell thisCell
     * Post: String is returned in form: ("cont1" "cont2" "cont3" ...)
     *       where cont is the individual contents of the cell
     */
    public String groundContents(GOBAgent a, List<GridObject> thisCell) {
        if (thisCell != null && ! thisCell.isEmpty()) {
            //encapsulate contents within parentheses
            String ground = "(";
            //iterate through the cell, gather the print-chars
            for(GridObject gob : thisCell){
                //if the gob is an agent (and not the one passed in) get the agent id
                if ((gob.printChar() == 'A' || gob.printChar() == 'H') && ((GOBAgent) gob != a)) {
                    ground = ground + "\"" + ((GOBAgent)gob).getAgentID() + "\" ";   // \" specifies the string "
                } else if (gob.printChar() != 'A' && gob.printChar() != 'H') {
                    ground += "\"" +  gob.printChar() + "\" ";
                }
            }
            ground = ground.trim() + ')';  //trim any leading or ending spaces, close paren
            return ground;
        }
        return "()";
    }

}
