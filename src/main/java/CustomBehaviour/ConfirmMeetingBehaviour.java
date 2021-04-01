package CustomBehaviour;

import java.io.IOException;
import java.util.ArrayList;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

public class ConfirmMeetingBehaviour extends SimpleBehaviour {
	/*
	 * An agent confirms that he is (at least) not alone for the meeting
	 */
	private static final long serialVersionUID = 32741213271413068L;

	private boolean finished = false;
	
	private ArrayList<String> participants;
	private int minParticipants; // Minimum number of participants to start meeting
	
	public ConfirmMeetingBehaviour(final Agent myagent, ArrayList<String> participants, int minParticipants) {
		this.myAgent = myagent;
		this.participants = participants;
		this.minParticipants = minParticipants;
		
		// Maybe add a waiting time?
		((ExploreMultiAgent)this.myAgent).sayConsole("I'm going to make sure my friends are there.");
	}
	
	@Override
	public void action() {
		int agentsAround = 0;
		for (String otherAgent: ((ExploreMultiAgent)this.myAgent).getAgentsAround()) {
			if (this.participants.contains(otherAgent)) {
				agentsAround ++;
			}
		}
		
		if (agentsAround >= this.minParticipants) {
			((ExploreMultiAgent)this.myAgent).sayConsole("Enough friends are around me to start the meeting.");
			finished = true;
		}
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return finished;
	}
}
