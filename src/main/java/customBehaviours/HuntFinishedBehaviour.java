package customBehaviours;

import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import jade.core.behaviours.OneShotBehaviour;

public class HuntFinishedBehaviour extends OneShotBehaviour {

	/**
	 * Called when the hunt is finished, do nothing
	 */
	
	private static final long serialVersionUID = -1203632411861130699L;
	
	private BrainBehaviour brain;
	
	public HuntFinishedBehaviour(BrainBehaviour brain) {
		this.brain = brain;
		this.myAgent = brain.getAgent();
		
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
	

}
