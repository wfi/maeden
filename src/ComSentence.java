import java.awt.Point;

/*******************************************
 * ComSentence describes the member functions that must be present in any message creator for GOBAgents
 * @author:  Josh Holm and Wayne Iba
 * @date:    5-31-05
 * @version: Beta 0.1
 ******************************************/

public abstract class ComSentence {

    protected int agentID;        //stores the sending agent's unique ID number
    protected String volume;      //stores the volume of the message
    protected String category;    //stores the type of the message
    protected String subCategory; //stores the subtype of the message
    protected String message;     //stores final com sentence
    protected String relDirSender;//relative direction from hearer to sender
    protected String subject;     //subject of message
    protected Point msgOrigin = new Point();  //Point of message origin


    public abstract String content();            //any other part of the message
    public abstract String createComSentence();  //create a syntactically correct message from the input data

    /*setDir sets the direction from the hearer to sender
     */
    public void setDir(char g) {                 //relative direction from receiver to sender
	switch(g) {
	case 'f': relDirSender = "forward"; break;
	case 'b': relDirSender = "back"; break;
	case 'r': relDirSender = "right"; break;
	case 'l': relDirSender = "left"; break;
	case 'h': relDirSender = "here"; break;
	default: System.out.println("bad relative direction inside ComSentence"); break;
	}
    }
    //returns the sending agent's unique ID number
    public int ID() {
	return agentID;
    }

    //returns the physical origin of the message
    public Point origin() {
	return msgOrigin;
    }

    //returns the volume of the message
    public String volume() {
	return volume;
    }

    //returns the category of the message
    public String category() {
	return category;
    }

    //returns the subCategory of the message
    public String subCategory() {
	return subCategory;
    }
}
