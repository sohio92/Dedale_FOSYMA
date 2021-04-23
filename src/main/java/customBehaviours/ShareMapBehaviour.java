package customBehaviours;

import java.io.IOException;
import java.util.ArrayList;
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

/**
 * This example behaviour try to send a hello message (every 3s maximum) to agents Collect2 Collect1
 * @author hc
 *
 */
public class ShareMapBehaviour extends SimpleBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2058134622078521998L;
	
	private boolean finished = false;
	
	/**
	 * Map which is going to be shared
	 */
	private MapRepresentation myMap;
	
	/**
	 * List of other agents
	 */
	private ArrayList<String> receivers;
	private double minReceivers;
	
	/**
	 * An agent tries to contact its friend and to give him its current position
	 * @param myagent the agent who posses the behaviour
	 *  
	 */
	public ShareMapBehaviour (final Agent myagent, MapRepresentation myMap, ArrayList<String> receivers, int minReceivers) {
		this.myAgent = myagent;
		this.myMap = myMap;
		this.receivers = receivers;
		
		// The sharing is considered complete if we received a map from half of the recipients
		this.minReceivers = Math.round(minReceivers / 2);
	}
	
	public void onStart() {
		((ExploreMultiAgent)this.myAgent).sayConsole("I'm going to send my map to my friends.");
	}
	
	@Override
	public void action() {
		for (String otherAgent: this.receivers) {
			// Prepare the message
			ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
			msg.setSender(this.myAgent.getAID());
			
			msg.addReceiver(new AID(otherAgent, AID.ISLOCALNAME));
			msg.setProtocol("ShareMapProtocol");
			
			// Retrieve what the other agent is missing
			MapRepresentation otherMap = ((ExploreMultiAgent)this.myAgent).getMyKnowledge(otherAgent).map;
			SerializableSimpleGraph<String, MapAttribute> missingSg = otherMap.getMissingFromMap(this.myMap);
			
			try {
				msg.setContentObject(missingSg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// Send the Message
			((ExploreMultiAgent)this.myAgent).sayConsole("I'm sending my map to " + otherAgent);
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		}
		
		if (((ExploreMultiAgent)this.myAgent).getAlreadyCommunicated().size() >= minReceivers) {
			((ExploreMultiAgent)this.myAgent).sayConsole("Enough friends have me sent me map for me to stop sharing");
			this.finished = true;
		}
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return finished;
	}
}