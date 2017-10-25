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
    private LinkedListGOB[][] myMap; //holds gridobjects
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
     * LINE0: Simulation status. One of: DIE, END, SUCCESS, CONTINUE
     * LINE1: smell direction to food
     * LINE2: inventory in form ("inv-char")
     * LINE3: visual array (as single string) in form ((row ("cell") ("cell"))(row ("cell")))
     * LINE4: ground contents of agent position in form ("cont" "cont")
     * LINE5: Agent's messages
     * LINE6: Agent's energy
     * LINE7: last action's result status (ok or fail)
     * LINE8: Second world time
     * @param a the agent to which the information should be sent
     */
    @SuppressWarnings("unchecked")
    public void sendSensationsToAgent(GOBAgent a){
    sendSensationsToAgent(a, "CONTINUE");
    }
    public void sendSensationsToAgent(GOBAgent a, String status) {
        if (a.getNeedUpdate()) {
            JSONArray jsonArray = new JSONArray();
            JSONArray invArray = new JSONArray();

            // We added String.valueOf to make sure that everything that is send is a String
            jsonArray.add(status); // 0. send status
            jsonArray.add(String.valueOf(Grid.relDirToPt(a.pos, new Point(a.dx(), a.dy()), food.pos))); // 1. send smell
            if (a.inventory().size() > 0){
                for(GridObject gob : a.inventory()){
                    invArray.add(String.valueOf(gob.printChar()));
                }
            }
            jsonArray.add(invArray); //2. send inv
            jsonArray.add(visField(a.pos, new Point(a.dx(), a.dy()))); // 3. send visual info
            jsonArray.add(groundContents(a, myMap[a.pos.x][a.pos.y]));  // 4.send contents of current location
            jsonArray.add(null); // 5. send any messages that may be heard by the agent
            jsonArray.add(a.energy());  // 6. send agent's energy
            jsonArray.add(String.valueOf(a.lastActionStatus()));// 7. send last-action status
            jsonArray.add(a.simTime()); // 8. send world time
            a.send().println(jsonArray); // send JsonArray
            a.setNeedUpdate(false);
        }
    }

    /**
     * visField: extract the local visual field to send to the agent controller
     * INPUT: agent point location, and agent heading (as point)
     * OUTPUT: JSONArray <JSONArray <JSONArray>>
     * See README.SensoryMotor for more description and examples.
     * The row behind the agent is given first followed by its current row and progressing away from the agent
     * with characters left-to-right in visual field.
     */
    public JSONArray visField(Point aPt, Point heading){
        int senseRow, senseCol;
        JSONArray bigArray = new JSONArray();
        //iterate from one behind to five in front of agent point
        for (int relRow=5; relRow >= -1; relRow--) {
            JSONArray rowArray = new JSONArray();
            //iterate from two to the left to two to the right of agent point
            for (int relCol=-2; relCol <= 2; relCol++){
                senseRow = aPt.x + relRow * heading.x + relCol * -heading.y;
                senseCol = aPt.y + relRow * heading.y + relCol * heading.x;
                //add cell information
                rowArray.add(visChar(mapRef(senseRow, senseCol), heading));
            }
            bigArray.add(rowArray);
        }
        return bigArray;
    }

    /** visChar iterates through the gridobjects located in a cell and returns all of their printchars
     * as strings as elements in a JSONArray
     * The one exception is the agent.  For an agent, its agent-id is returned (0-9)
     * Note: the heading of an agent is not reported at this time.
     * Pre: cellContents contains any and all gridobjects in a cell
     * Post: JSONArray with string of objects in each part
     * @param cellContents the GOBs in a particular cell
     * @param heading (which is not used)
     * @return a String that represents a list of items in the cell
     */
    private JSONArray visChar(List<GridObject> cellContents, Point heading){
        JSONArray cellConts = new JSONArray();
        //if there are any gridobjects in the cell iterate and collect them
        if (cellContents != null && !cellContents.isEmpty()) {
            //iterate through cellContents, gather printchars or agent IDs
            for(GridObject gObj : cellContents) {
                if(gObj.printChar() == 'A') {           //if it is an agent
                    cellConts.add(String.valueOf(((GOBAgent)gObj).getAgentID()));
                } else {        //if gridobject is not an agent, return its print character
                    cellConts.add(String.valueOf(gObj.printChar()));
                }
            }
            return cellConts;
        }
        //otherwise return a space representing no gridobject
        else
            return cellConts;
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


    /**
     * groundContents iterates through the cell the agent is standing on and returns a list of strings
     * to represent what is on the ground at that position
     * Pre: a is GOBAgent who is in cell thisCell
     * Post: JSONArray is returned containing a list of strings as the elements of the Array
     **/
    public JSONArray groundContents(GOBAgent a, List<GridObject> thisCell) {
        JSONArray ground = new JSONArray();
        if (thisCell != null && ! thisCell.isEmpty()) {
            //encapsulate contents within parentheses
            //iterate through the cell, gather the print-chars
            for(GridObject gob : thisCell){
                //if the gob is an agent (and not the one passed in) get the agent id
                if ((gob.printChar() == 'A' || gob.printChar() == 'H') && ((GOBAgent) gob != a)) {
                    ground.add(String.valueOf(((GOBAgent)gob).getAgentID()));
                } else if (gob.printChar() != 'A' && gob.printChar() != 'H') {
                    ground.add(String.valueOf(gob.printChar()));
                }
            }
            return ground;
        }
        return ground;
    }

}
