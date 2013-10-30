
import java.awt.Point;
/************************************************************************
 * class ComSentenceQuestion implements the interface ComSentence 
 * Provides methods for creating an agent message
 * @author Josh Holm and Wayne Iba
 * @Date 5-31-05
 * @version Beta 0.1
 ***********************************************************************/

public class ComSentenceQuestion extends ComSentence {

   
    private String qType;       //stores what the message relates to
    private String subject;     //stores the subject of the message
    private String answer;      //*optional* stores an answer to a question
    


    /*constructor for a question where the answer to a question is provided
     *Pre: ID = sending agent's ID number
           vol == talk || shout
           cat == question
           subCat == ask || answer
           qType == have || seen || path-around || state || help-status
           subject == subject of the question/answer to
	   answer == yes || no || not-available || (symbol) || goal name || integer
     *Post: all data is stored in the appropriate location
     */
    public ComSentenceQuestion(int ID, String vol, String cat, String qT, String subj, String ans, int r, int c) {
	agentID = ID;          //sending agent's id
	volume = vol;
	category = cat;
	qType = qT;
	subject = subj;
	answer = ans;
	msgOrigin = new Point(c, r);   //origin of the message
    }

    /*constructor for a question where the answer to a question is provided
     *Pre: ID = sending agent's ID number
           vol == talk || shout
           cat == question
           subCat == ask || answer
           qType == have || seen || path-around || state || help-status
           subject == subject of the question/answer to
	   answer == yes || no || not-available || (symbol) || goal name || integer
     *Post: all data is stored in the appropriate location
     */
    public ComSentenceQuestion(int ID, String vol, String cat, String qT, String subj, int r, int c) {
	agentID = ID;         //sending agent's id
	volume = vol;
	category = cat;
	qType = qT;
	//System.out.println("qType inside ComQuest: " + qType);
	subject = subj;
	msgOrigin = new Point(c, r);   //origin of the message
    }

    /* constructs a communication sentence in the form:
     * content of the message:
     * ID number, direction, volume, category of message, subcategory of message, question type, subject, and *optional* answer
     * "( ID dir volume category subCategory qType subject )"  OR
     * "( ID dir volume category subCategory qType subject answer )"
     * depending on whether the comType object is specified in the constructor or not
     * Pre: agentID, relDirSender, volume, category, subCategory, comType are all initialized
     * Post: returns appropriate string message
     */
    public String createComSentence() {
	if(answer == null) {
	    message = "(" + agentID + " " + relDirSender + " " + volume
		+ " ("	+ category + " " + qType + " " + subject + "))";
	    //System.out.println("inside question about to send message:");
	    //System.out.println(message);
	    return message;
	}
	else {
	    message = "(" + agentID + " "  + relDirSender + " " + volume
		+ " (" + category + " " + qType + " " + subject + " " + answer + "))";
	    //System.out.println("inside question about to send message:");
	    //System.out.println(message);
	    return message;
	}
    }


    //returns the rest of the message
    public String content() {
	if(qType != "answer")
	    return qType + " " + subject;
	else
	    return qType + " " + subject + " " + answer;
    }
}
