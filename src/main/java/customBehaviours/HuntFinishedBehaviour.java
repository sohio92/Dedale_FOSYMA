package customBehaviours;

import java.util.HashMap;

import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import jade.core.behaviours.OneShotBehaviour;

public class HuntFinishedBehaviour extends OneShotBehaviour {

	/**
	 * Called when the hunt is finished, do nothing
	 */
	
	private static final long serialVersionUID = -1203632411861130699L;
	
	private BrainBehaviour brain;
	private HashMap<String, Integer> decisionToInt;
	
	public HuntFinishedBehaviour(BrainBehaviour brain) {
		this.brain = brain;
		this.myAgent = brain.getAgent();
		
		this.decisionToInt = this.brain.getDecisionToInt();
		
	}
	@Override
	public void action() {
		((ExploreMultiAgent)this.myAgent).sayConsole("I finished the game! The intruders have been blocked.");
		
		try {
			this.myAgent.doWait(((ExploreMultiAgent)this.myAgent).getTimeSleep());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public int onEnd() {
		this.brain.deregisterState("HuntFinished");
		
		this.brain.registerState(new HuntFinishedBehaviour(this.brain), "HuntFinished");
		
		this.brain.registerTransition("Hunt", "HuntFinished", (int) this.decisionToInt.get("HuntFinished"));
		this.brain.registerTransition("Exploration", "HuntFinished", (int) this.decisionToInt.get("HuntFinished"));
		
		this.brain.registerTransition("Decision", "HuntFinished", (int) this.decisionToInt.get("HuntFinished"));
		this.brain.registerTransition("HuntFinished", "Decision", (int) this.decisionToInt.get("Decision"));
		
		return this.decisionToInt.get("Decision");
	}

}
