package org.maeden.controller;

import java.util.Random;

/**
 * class RandomAgent: demonstrates a simple agent controller extended from AbstractAgentController
 */
public class RandomAgent extends AbstractAgentController {

    String possibleActions = "fbrldgu"; // exclude the s, t, and k actions
    Random myRand;

    /** RandomAgent constructor: creates an instance
     * @param h the name or IP address of the host on which the MAEDEN simulator is running
     * @param p the port number on which the simulator is listening
     */
    public RandomAgent(String h, int p) {
        super(h, p);
        myRand = new Random();
    }
    public RandomAgent(){
        this("localhost", MAEDENPORT);
    }

    /** A degenerate sense/think/act cycle with no real thinking
     */
    public void run() {
        getSensoryInfo();       // sense
        while (currentSensePacket.getStatus().equals("CONTINUE")) {
            int randomAct = myRand.nextInt(possibleActions.length());                   // 'think'
            sendEffectorCommand(Character.toString(possibleActions.charAt(randomAct))); //  act
            getSensoryInfo();                                                           //  sense
            try {Thread.sleep(200);} catch (Exception e) {System.out.println ("sleep error");}
        }
    }

    /** main: run a RandomAgent in the simulator
     * @param args any commandline arguments (currently ignored)
     */
    public static void main(String[] args){
        RandomAgent ra = new RandomAgent();
	ra.run();
    }
    
}
