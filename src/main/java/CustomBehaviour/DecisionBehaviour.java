package CustomBehaviour;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.lang.Math;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.ExploMultiBehaviour;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;

public class DecisionBehaviour extends TickerBehaviour{
	
	/**
	 * An agent decides what next step he is going to take
	 */
	
	private static final long serialVersionUID = -5937410765476622526L;
	private MapRepresentation myMap;
	
	private ArrayList<String> openNodes = new ArrayList<String>();
	private HashSet<String> closedNodes = new HashSet<String>();
	private ArrayList<String> abandonedNodes = new ArrayList<String>();
	
	private Behaviour currentBehaviour;
	private ExploMultiBehaviour currentExplo;
	private String lastDecision;
	
	private ArrayList<String> interestingAgents; // interesting agents in the vicinity
	
	private static int tickLength = 1000;
	private int meetingMaxLength = 6000; // A meeting's max length (prevents stalling)
	private int timeElapsedSinceMeeting = 0;

	public DecisionBehaviour(final Agent myagent, MapRepresentation myMap){
		super(myagent, tickLength);
		
		this.myAgent = myagent;
		this.myMap = myMap;	
	}
	
	public void onStart() {
		currentExplo = new ExploMultiBehaviour((AbstractDedaleAgent) this.myAgent, this.myMap, this.openNodes, this.closedNodes, this.abandonedNodes);
		this.myAgent.addBehaviour(currentExplo);
		
		currentBehaviour = currentExplo;
		this.lastDecision = "Exploration";
	}
	
	@Override
	public void onTick() {
		List<String> agentsAround = ((ExploreMultiAgent)this.myAgent).getAgentsAround();
		if (agentsAround.size() != 0) {
			((ExploreMultiAgent)this.myAgent).sayConsole("The other agents in my surroundings are : " + agentsAround);
		}
		
		this.interestingAgents = new ArrayList<String>();
		
		String decision = this.takeDecision();
		
		if (!this.lastDecision.equals(decision)) {
			((ExploreMultiAgent)this.myAgent).sayConsole("I took the " + decision + " decision.");
		}
		this.lastDecision = decision;
		
		this.executeDecision(decision);
	}

	// Takes a decision based on surroundings
	private String takeDecision() {
		String decision = "";
		
		// If a meeting is going, on do nothing
		if (((ExploreMultiAgent)this.myAgent).getOnGoingMeeting() == true) {
			// If its been going for too long then stop it
			decision = "ContinueMeeting";
			this.timeElapsedSinceMeeting += this.tickLength;
			
			if(this.timeElapsedSinceMeeting >= this.meetingMaxLength) {
				((ExploreMultiAgent)this.myAgent).endMeeting();
				this.timeElapsedSinceMeeting = 0;
				decision = "StopMeeting";
			}
		} 
		
		// Else start the decision process
		else {
			decision = "Exploration";
			// If the exploration is done, change to patrol mode
			if (currentExplo.done() == true) {
				decision = "Patrol";
			}
			
			// Get the agents in the vicinity
			List<String> agentsAround = ((ExploreMultiAgent)this.myAgent).getAgentsAround();
			
			// Get the agents who are worth sharing with
			if (agentsAround.size() != 0) {
				for (String otherAgent: agentsAround) {
					if (this.shareWorth(10, otherAgent)) {this.interestingAgents.add(otherAgent);}
					else {((ExploreMultiAgent)this.myAgent).sayConsole("I considered " + otherAgent + " but it is not worth it.");}
				}
				
				// Start a meeting with the interesting agents
				if (this.interestingAgents.size() != 0) {decision = "MeetingShareMap";}
			}
		}
		
		return decision;
	}
	
	// Execute a decision
	private void executeDecision(String decision) {

		if (decision == "Exploration" && currentExplo != currentBehaviour) {
			this.myAgent.removeBehaviour(currentBehaviour);
			currentBehaviour = currentExplo;
			currentBehaviour.restart();
		}
		
		else if (decision == "MeetingShareMap") {
			currentExplo.block();
			currentBehaviour = new MeetingBehaviour(this.myAgent, this.myMap,
					this.openNodes, this.closedNodes, this.abandonedNodes, this.interestingAgents, decision);
			this.myAgent.addBehaviour(currentBehaviour);
		}
		
		else if (decision == "Patrol") {
			//Not yet implemented
		}
	}
	
	// Returns true if it is worth sharing a map with the otherAgent
	public boolean shareWorth(int threshold, String otherAgent) {	
		MapRepresentation otherMap = ((ExploreMultiAgent)this.myAgent).getMyKnowledge(otherAgent).map;
		return Math.floor(otherMap.getDiffEdges()/2) + otherMap.getDiffNodes() >= threshold;
	}

}
