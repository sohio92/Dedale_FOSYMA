package customBehaviours;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class SharePathBehaviour extends OneShotBehaviour{

	/**
	 * Sends my map to the other agents
	 */
	
	
	private static final long serialVersionUID = -2058101622078521998L;
	
	
	/**
	 * List of other agents
	 */
	private HashSet<String> receivers;
	private BrainBehaviour brain;
	
	public SharePathBehaviour (BrainBehaviour brain) {
		this.myAgent = brain.getAgent();
		this.brain = brain;
		
		this.receivers = ((ExploreMultiAgent)this.myAgent).getAgentsNames();
	}
	
	public void onStart() {
		((ExploreMultiAgent)this.myAgent).sayConsole("Sending current path to my friends.");
	}
	
	@Override
	public void action() {
		for (String otherAgent: this.receivers) {
			// Prepare the message
			ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
			msg.setSender(this.myAgent.getAID());
			
			msg.addReceiver(new AID(otherAgent, AID.ISLOCALNAME));
			msg.setProtocol("SharePathProtocol");
						
			try {
				msg.setContentObject((Serializable) this.brain.getLastPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// Send the Message
			//((ExploreMultiAgent)this.myAgent).sayConsole("I'm sending my current path " + (Serializable) this.brain.getLastPath() + "to " + otherAgent);
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		}
	}
}