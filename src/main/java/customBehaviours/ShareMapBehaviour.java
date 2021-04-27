package customBehaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentKnowledge;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class ShareMapBehaviour extends OneShotBehaviour {

	/**
	 * Sends my map to the other agents
	 */

	private static final long serialVersionUID = -2058134622078521998L;

	/**
	 * Map which is going to be shared
	 */
	private MapRepresentation myMap;

	/**
	 * List of other agents
	 */
	private HashSet<String> receivers;

	private BrainBehaviour brain;

	public ShareMapBehaviour(BrainBehaviour brain, HashSet<String> receivers) {
		this.myAgent = brain.getAgent();
		this.brain = brain;

		this.receivers = receivers;
	}

	public ShareMapBehaviour(BrainBehaviour brain, ArrayList<AgentKnowledge> receivers) {
		this.myAgent = brain.getAgent();
		this.brain = brain;

		this.receivers = new HashSet<String>();
		for (AgentKnowledge otherKnowledge : receivers) {
			this.receivers.add(otherKnowledge.getName());
		}
	}

	public void onStart() {
		this.myMap = brain.getMap();
		// ((ExploreMultiAgent)this.myAgent).sayConsole("Sending map to my friends.");
	}

	@Override
	public void action() {
		for (String otherAgent : this.receivers) {
			AgentKnowledge otherKnowledge = ((ExploreMultiAgent) this.myAgent).getMyKnowledge(otherAgent);
			if (otherKnowledge.getLastAction() != null && (otherKnowledge.getLastAction().equals("Exploration")
					|| otherKnowledge.getLastAction().equals("SeekMeeting")) == false) {
				continue;
			} else {

				// Prepare the message
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setSender(this.myAgent.getAID());

				msg.addReceiver(new AID(otherAgent, AID.ISLOCALNAME));
				msg.setProtocol("ShareMapProtocol");

				// Retrieve what the other agent is missing
				MapRepresentation otherMap = otherKnowledge.getMap();
				// SerializableSimpleGraph<String, MapAttribute> missingSg =
				// otherMap.getMissingFromMap(this.myMap);

				try {
					msg.setContentObject(((ExploreMultiAgent) this.myAgent).getBrain().getMap().getSg());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// Send the Message
				// ((ExploreMultiAgent)this.myAgent).sayConsole("I'm sending my map to " +
				// otherAgent);
				((AbstractDedaleAgent) this.myAgent).sendMessage(msg);
			}
		}
	}
}