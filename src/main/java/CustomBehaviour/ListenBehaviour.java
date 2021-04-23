package CustomBehaviour;

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
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ListenBehaviour extends CyclicBehaviour{

	/**
	 * An agent listen for other agents in its surroundings
	 */
	private static final long serialVersionUID = -2058134622078011998L;
	
	private int maxRange = 4; // Maximum range of surroundings
	private List<String> receivers;
	
	public ListenBehaviour (final Agent myagent) {		
		this.myAgent = myagent;
		this.receivers = ((ExploreMultiAgent)this.myAgent).getAgentsNames();
	}
	
	@Override
	public void action() {
		// Check if all objects are currently loaded
		if (((ExploreMultiAgent)this.myAgent).isLoaded() == true) {
			this.listen();
		}
	}
	
	private void listen() {
		// Is the agent in a meeting
		boolean meeting = ((ExploreMultiAgent)this.myAgent).getOnGoingMeeting();
		
		// Receiving a message
		final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);			
		final ACLMessage msgReceived = this.myAgent.receive(msgTemplate);

		// Checking if the received message is an echo
		String otherName = msgReceived.getSender().getLocalName();
		final int compareNames = otherName.compareTo(this.myAgent.getAID().getLocalName());
		
		// Handling of the content
		if (msgReceived != null && compareNames != 0) {
			
			
			// Let other behaviours know that an agent was found at the known position
			if (msgReceived.getProtocol().equals("PingProtocol")) {
				this.pingProcess(otherName, msgReceived);
			}
			// Update our map
			else if (msgReceived.getProtocol().equals("ShareMapProtocol")) {
				((ExploreMultiAgent)this.myAgent).sayConsole("I received a " + msgReceived.getProtocol() + " message from " + otherName);
				this.shareMapProcess(otherName, msgReceived, meeting);
			}
			else if (msgReceived.getProtocol().equals("PingChasseProtocol")) {
				this.huntProcess(otherName, msgReceived);
			}
		}
		
		// If an agent is too far away, it is removed from the surroundings
		((ExploreMultiAgent)this.myAgent).clearSurroundings(this.maxRange);
	}
	
	private void huntProcess(String otherName, ACLMessage msgReceived) {
		// TODO Auto-generated method stub
		String otherPosition = msgReceived.getContent();
		//((ExploreMultiAgent)this.myAgent).addAgentsAround(otherName, otherPosition, msgReceived.getPostTimeStamp());
	}

	private void pingProcess(String otherName, ACLMessage msgReceived) {
		String otherPosition = msgReceived.getContent();
		((ExploreMultiAgent)this.myAgent).addAgentsAround(otherName, otherPosition, msgReceived.getPostTimeStamp());
	}
	
	private void shareMapProcess(String otherName, ACLMessage msgReceived, boolean meeting) {
		// We now have communicated with the agent -> Acknowledge
		if (meeting == true) {
			((ExploreMultiAgent)this.myAgent).addAlreadyCommunicated(otherName);
		}
		
		// We update our map (even if no meeting, we might be scraping another one)
		SerializableSimpleGraph<String, MapAttribute> otherSg;
		try {
			otherSg = (SerializableSimpleGraph<String, MapAttribute>) msgReceived.getContentObject();
			((ExploreMultiAgent)this.myAgent).updateMap(otherName, otherSg);
		} catch (UnreadableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}