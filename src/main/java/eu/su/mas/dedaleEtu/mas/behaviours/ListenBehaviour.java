package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import CustomBehaviour.MeetingBehaviour;
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
	
	/**
	 * An agent informs its friends that it is willing to communicate.
	 * @param myagent the agent who posses the behaviour
	 *  
	 */
	public ListenBehaviour (final Agent myagent, List<String> receivers, MapRepresentation myMap) {
		super(myagent, 3000);
		
		this.receivers = receivers;
		this.myMap = myMap;
	}

	@Override
	public void onTick() {		
		
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		//A message is defined by : a performative, a sender, a set of receivers, (a protocol),(a content (and/or contentOBject))
		ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
		msg.setSender(this.myAgent.getAID());
		for (String agentName : this.receivers) {
			msg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
		}
		msg.setProtocol("ListenProtocol");

		if (myPosition!=""){
			//System.out.println("Agent "+this.myAgent.getLocalName()+ " is trying to reach its friends");
			try {
				msg.setContentObject(myPosition);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//Mandatory to use this method (it takes into account the environment to decide if someone is reachable or not)
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		}
		
		/*
		 * Listens for other agents.
		 * 
		 * If an agent is heard, it moves closer to guaranty communication
		 */
		final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);			

		final ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
		final int compareNames = msg.getSender().getLocalName().compareTo(this.myAgent.getAID().getLocalName());
		
		if (msgReceived != null && compareNames != 0) {
			String meetingTopic = null;
			String otherPosition = null;
			
			if (compareNames == -1) {
				otherPosition = myPosition;
			} else {
				otherPosition = (String)msg.getContent();
			}
			
			if (msgReceived.getProtocol() == "ListenProtocol") {
				meetingTopic = "ShareMap";		
			}
			new MeetingBehaviour(this.myAgent, this.myMap, this.receivers, otherPosition, meetingTopic);
		}
	}
}