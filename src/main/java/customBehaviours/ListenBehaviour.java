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
import eu.su.mas.dedaleEtu.mas.knowledge.AgentKnowledge;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.PingContainer;
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
	
	private int maxRange; // Maximum range of surroundings

	public ListenBehaviour (final Agent myagent) {		
		this.myAgent = myagent;
		this.maxRange = ((ExploreMultiAgent)this.myAgent).getMaxRange();
	}
	
	@Override
	public void action() {
		// Check if all objects are currently loaded
		if (((ExploreMultiAgent)this.myAgent).isLoaded() == true) {
			this.listen();
		}
	}
	
	private void listen() {		
		// Receiving a message
		final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);			
		final ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
		
		if (msgReceived != null) {
			// Checking if the received message is an echo
			String otherName = msgReceived.getSender().getLocalName();
			final int compareNames = otherName.compareTo(this.myAgent.getAID().getLocalName());
			
			// Handling of the content
			if (compareNames != 0) {
				// Let other behaviours know that an agent was found at the known position
				if (msgReceived.getProtocol().equals("PingProtocol")) {
					this.pingProcess(otherName, msgReceived);
				}
				// Update the knowledge of the agent's future path
				else if (msgReceived.getProtocol().equals("SharePathProtocol")) {
					//((ExploreMultiAgent)this.myAgent).sayConsole("Received a " + msgReceived.getProtocol() + " message from " + otherName);
					this.sharePathProcess(otherName, msgReceived);
				}
				// Update our map
				else if (msgReceived.getProtocol().equals("ShareMapProtocol")) {
					//((ExploreMultiAgent)this.myAgent).sayConsole("Received a " + msgReceived.getProtocol() + " message from " + otherName);
					this.shareMapProcess(otherName, msgReceived);
				}
			}
		}
	}
	
	private void pingProcess(String otherName, ACLMessage msgReceived) {
		AgentKnowledge otherKnowledge = ((ExploreMultiAgent)this.myAgent).getBrain().getAgentsKnowledge().get(otherName);
		// Unpacks the message and checks if it is relevant
		if (otherKnowledge.unpackMessage(((ExploreMultiAgent)this.myAgent).getBrain(), msgReceived) == true) {
			//((ExploreMultiAgent)this.myAgent).sayConsole(otherName + "'s last known position is " + otherKnowledge.getLastPosition());
			otherKnowledge.computeDistance(((ExploreMultiAgent)this.myAgent).getBrain().getMap(), ((ExploreMultiAgent)this.myAgent).getCurrentPosition());
			((ExploreMultiAgent)this.myAgent).checkAgentAround(otherName, this.maxRange);
		};
		
		((ExploreMultiAgent)this.myAgent).getBrain().fuseMap(otherKnowledge.getMap());
	}
	
	private void sharePathProcess(String otherName, ACLMessage msgReceived) {
		List<String> lastPath;
		try {
			lastPath = (List<String>) msgReceived.getContentObject();
			
			AgentKnowledge otherKnowledge = ((ExploreMultiAgent)this.myAgent).getMyKnowledge(otherName);
			if (msgReceived.getPostTimeStamp() > otherKnowledge.getMostRecentPing())	otherKnowledge.addNewPath(lastPath);
			
			BrainBehaviour brain = ((ExploreMultiAgent)this.myAgent).getBrain();
			brain.getMap().updateWithPath(lastPath);
			//brain.updateNodesWithMap();
			
			otherKnowledge.getMap().updateIgnorance(brain.getMap());
			
			//((ExploreMultiAgent)this.myAgent).sayConsole("I know that " + otherName + " will follow : " + lastPath);
		} catch (UnreadableException e) {e.printStackTrace();}
	}
	
	private void shareMapProcess(String otherName, ACLMessage msgReceived) {
		try {
			SerializableSimpleGraph<String, MapAttribute> otherSg = (SerializableSimpleGraph<String, MapAttribute>) msgReceived.getContentObject();
			
			// Update my map
			((ExploreMultiAgent)this.myAgent).getBrain().fuseMap(otherSg);
			
			// Update the other agent's last known map
			((ExploreMultiAgent) this.myAgent).getBrain().getAgentsKnowledge().get(otherName)
					.unpackMessage(((ExploreMultiAgent) this.myAgent).getBrain(), msgReceived);
		} catch (UnreadableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}