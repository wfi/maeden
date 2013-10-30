
import java.awt.Point;

/************************************
 * ComSentenceCommand creates a communication sentence that is compatible between agents in the grid.
 * It stores the agent ID along with the volume, cateory, subcategory, goal/action name, and the object specified by the goal or action (if provided)
 * @author: Josh Holm and Wayne Iba
 * @date:   5-31-05
 * @version: Beta 0.1
 ***********************************/

public class ComSentenceCommand extends ComSentence {
   
    private String comType;     //subject of the message
    private int payment;

    /*constructor for a command where the object of the goal or action is specified
     *Pre: ID = sending agent's ID number
           vol == talk || shout
           cat == command
           subCat == goal || action
           goalActName is valid goal or action name
           object == object of the goal or action
     *Post: all data is stored in the appropriate location
     */
    public ComSentenceCommand(int ID, String vol, String cat, String goalActName, String object, int paymt, int r, int c) {
	agentID = ID;
	volume = vol;
	category = cat;
	comType= goalActName;
	subject = object;
	payment = paymt;
	msgOrigin = new Point(c, r);
	//System.out.println(agentID + " " + volume + " " + category + " " + comType + " " + subject + " " + payment);
    }

    /* constructs a communication sentence in the form:
     * content of the message:
     * ID number, volume of the message, category of the message, subcategory of the message, comType of the command, and (optional object of the message)
     * "( ID volume category subCategory comType )"  OR
     * "( ID volume category subCategory comType command )"
     * depending on whether the comType object is specified in the constructor or not
     * Pre: agentID, volume, category, subCategory, comType are all initialized
     * Post: returns appropriate string message
     */

    public String createComSentence() {
	if(subject == null) {
	    message = "(" + agentID + " " + relDirSender + " "  + volume + " (" + category + " " + comType + ") " + payment +  ")";
	    //System.out.println(message);
	    return message;
	}
	else {
	  message = "(" + agentID + " " + relDirSender + " " + volume + " (" + category + " " + comType + " " + subject + ") " + payment + ")";
	  //System.out.println(message);
	  return message;
	}
    }

    //returns the last part of the message
    public String content() {
	if(subject != null)
	    return comType + " " + subject;
	else
	    return comType;
    }
}
