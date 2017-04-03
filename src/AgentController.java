
public class AgentController {

	protected GridClient gc;
	public static final String HOSTNAME = "localhost";
	public static final int MAEDENPORT = 7237;
	
	public AgentController() {
		gc = new GridClient(HOSTNAME, MAEDENPORT);
	}
	
	public static void main(String args[]) {
		AgentController ac = new AgentController();
		while(true) {
			ac.gc.effectorSend("l");
		}
	}
}
