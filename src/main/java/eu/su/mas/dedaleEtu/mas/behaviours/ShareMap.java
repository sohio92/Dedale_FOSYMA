package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import dataStructures.tuple.Couple;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

import CustomClass.MessageContainer;

/**
 * This example behaviour try to send a hello message (every 3s maximum) to agents Collect2 Collect1
 * @author hc
 *
 */
public class ShareMap extends TickerBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2058134622078521998L;
	
	/**
	 * Container of the message to send
	 **/
	private MessageContainer contenu;
	/**
	 * Map which is going to be shared
	 */
	private MapRepresentation myMap;
	
	/**
	 * List of other agents
	 */
	private List<String> receivers;
	
	/**
	 * An agent tries to contact its friend and to give him its current position
	 * @param myagent the agent who posses the behaviour
	 *  
	 */
	public ShareMap (final Agent myagent, MapRepresentation myMap, ArrayList<String> openNodes, HashSet<String> closedNodes, List<String> receivers) {
		super(myagent, 3000);
		
		this.myMap = myMap;
		this.contenu = new MessageContainer(null, openNodes, closedNodes, ((ExploreMultiAgent)this.myAgent).getIntention());
		this.receivers = receivers;
	}

	@Override
	public void onTick() {		
		this.contenu.updateSg(this.myMap.getSg());
		
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		//A message is defined by : a performative, a sender, a set of receivers, (a protocol),(a content (and/or contentOBject))
		ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
		msg.setSender(this.myAgent.getAID());
		for (String agentName : this.receivers) {
			msg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
		}
		msg.setProtocol("UselessProtocol");

		if (myPosition!=""){
			//System.out.println("Agent "+this.myAgent.getLocalName()+ " is trying to reach its friends");
			try {
				msg.setContentObject(this.contenu);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//Mandatory to use this method (it takes into account the environment to decide if someone is reachable or not)
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		}
	}
}