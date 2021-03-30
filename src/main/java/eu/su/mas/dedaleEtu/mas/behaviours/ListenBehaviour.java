package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import CustomBehaviour.MeetingBehaviour;
import CustomBehaviour.PingPositionBehaviour;
import dataStructures.tuple.Couple;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import CustomClass.MessageContainer;

/**
 * This example behaviour try to send a hello message (every 3s maximum) to agents Collect2 Collect1
 * @author hc
 *
 */
public class ListenBehaviour extends TickerBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2058134622078011998L;
	
	/**
	 * List of the other agents' names
	 */
	private List<String> receivers;
	
	/**
	 * Map known
	 */
	private MapRepresentation myMap;
	
	private ArrayList<String> openNodes;
	private HashSet<String> closedNodes;
	
	/**
	 * An agent informs its friends that it is willing to communicate.
	 * @param myagent the agent who posses the behaviour
	 *  
	 */
	public ListenBehaviour (final Agent myagent, List<String> receivers, MapRepresentation myMap,
			ArrayList<String> openNodes, HashSet<String> closedNodes) {
		super(myagent, 1000);
		
		this.myAgent = myagent;
		this.receivers = receivers;
		this.myMap = myMap;
		this.openNodes = openNodes;
		this.closedNodes = closedNodes;
	}

	@Override
	public void onTick() {
		this.myAgent.addBehaviour(new PingPositionBehaviour(this.myAgent, this.receivers));

		ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
		msg.setSender(this.myAgent.getAID());
		for (String agentName : this.receivers) {
			msg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
		}
		
		final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);			

		final ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
		final int compareNames = msg.getSender().getLocalName().compareTo(this.myAgent.getAID().getLocalName());
		
		if (msgReceived != null && compareNames != 0) {
			System.out.println("Got a new message");
			String meetingTopic = null;
			String otherPosition = null;
			
			if (compareNames == -1) {
				otherPosition = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
			} else {
				otherPosition = (String)msg.getContent();
			}
			
			if (msgReceived.getProtocol() == "ListenProtocol") {
				meetingTopic = "ShareMap";		
			}
			this.myAgent.addBehaviour(new MeetingBehaviour(this.myAgent, this.myMap, this.openNodes, this.closedNodes,
					this.receivers, otherPosition, meetingTopic));
		}
	}
}