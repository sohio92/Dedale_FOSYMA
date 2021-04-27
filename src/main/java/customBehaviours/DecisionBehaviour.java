package customBehaviours;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.lang.Math;
import java.util.Random;

import org.graphstream.graph.Node;

import eu.su.mas.dedaleEtu.mas.knowledge.AgentKnowledge;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import jade.core.behaviours.OneShotBehaviour;


public class DecisionBehaviour extends OneShotBehaviour {
	
	/**
	 * An agent decides what next step he is going to take
	 */
	
	private static final long serialVersionUID = -5937410765476622526L;
	
	private String decision;	
	private HashMap<String, Integer> decisionToInt;
	
	private BrainBehaviour brain;
	private ExploreMultiAgent myAgent;
	
	// Agents around me
	private HashSet<String> agentsAround;
	
	// Information about my environment
	private HashSet<String> whoWantsToMeet;	
	
	// Agents that I want to meet
	private ArrayList<AgentKnowledge> interestingAgents = new ArrayList<AgentKnowledge>();
	
	public DecisionBehaviour(BrainBehaviour brain){
		this.brain = brain;
		
		this.myAgent = brain.getAgent();
		this.decisionToInt = brain.getDecisionToInt();
	}

	@Override
	public void action() {
		this.agentsAround = ((ExploreMultiAgent)this.myAgent).getAgentsAround();
		if (this.agentsAround.size() != 0) {
			//((ExploreMultiAgent)this.myAgent).sayConsole("The other agents in my surroundings are : " + this.agentsAround);
		}
		
		this.retrieveInformation();
		this.decision = this.takeDecision();
			
		if (!this.decision.equals(brain.getLastDecision())) {
			((ExploreMultiAgent)this.myAgent).sayConsole("I took the " + decision + " decision.");
		}
	}

	// Retrieve the information necessary to take a decision
	private void retrieveInformation() {
		// How far are the other agents?
		for (AgentKnowledge otherAgent : this.brain.getAgentsKnowledge().values()) {
			otherAgent.computeDistance(this.brain.getMap(), this.myAgent.getCurrentPosition());
		}
		
		// Who wants to meet me?
		this.whoWantsToMeet = new HashSet<String>();
		for (String otherAgent: this.agentsAround) {
			if (this.brain.getAgentsKnowledge().get(otherAgent).getMeetUtility() >= 1) {
				this.whoWantsToMeet.add(otherAgent);
			}
		}
		
		// Check golem
		
		//
	}

	// Takes a decision based on surroundings
	private String takeDecision() {
		String decision;
		
		// Start the decision process, default behavior is exploration
		if (this.brain.isExplorationFinished() == false) {
			decision = "Exploration";
			
			// Get the agents whom I want to share with
			this.interestingAgents = new ArrayList<AgentKnowledge>();
			for (AgentKnowledge otherKnowledge: this.brain.getAgentsKnowledge().values()) {
				double shareWorth = otherKnowledge.getShareWorth();
				if (shareWorth >= 5) {
					this.interestingAgents.add(otherKnowledge);
				} //else	((ExploreMultiAgent)this.myAgent).sayConsole("I considered " + otherKnowledge.getName() + " but it is not worth it.");

			}
			this.brain.setInterestingAgents(this.interestingAgents);
			
			//((ExploreMultiAgent)this.myAgent).sayConsole("I want to meet : " + this.interestingAgents);
			
			// Start a meeting with the interesting agents
			if (this.interestingAgents.size() != 0)	decision = "SeekMeeting";
			
		} else {
			decision = "Patrol";
		}
		
		// Maybe send them more to the most interested agents?
		// Sending my current path if there are agents around me
		if (this.agentsAround.size() >= 1)	{
			this.myAgent.addBehaviour(new SharePathBehaviour(this.brain));
			this.myAgent.addBehaviour(new ShareMapBehaviour(this.brain, this.agentsAround));
		}

		return decision;
	}
	
	// Returns true if it is worth sharing a map with the otherAgent, considering if it wants to talk with me or not
	public double shareWorth(AgentKnowledge otherAgent) {	
		double utility = otherAgent.getShareWorth();
		if (this.whoWantsToMeet.contains(otherAgent.getName()))	utility *= 2;
	
		return utility;
	}
	
	// Manages the case where agents are stuck
	/*
	 public void manageStuck(int minTime) {
		this.decision = "Decision";
		MapRepresentation map = this.brain.getMap();
		
		if (this.brain.isStuck() == true) {
			if (this.brain.getTimeStuck() >= minTime) {
				String nextNode = "";
				Random rand = new Random();

				ArrayList<String> openNodes = this.brain.getOpenNodes();
				if (openNodes.size() > 1) {
					nextNode = openNodes.get(rand.nextInt(openNodes.size()));
				} else {
					HashSet<String> closedNodes = this.brain.getClosedNodes();
					int rI = rand.nextInt(closedNodes.size());
					int i = 0;
					for (String node : closedNodes) {
						if (i == rI) {
							nextNode = node;
							break;
						}
						i++;
					}
				}
				nextNode = map.getShortestPath(this.brain.getAgent().getCurrentPosition(), nextNode).get(0);
				this.brain.getAgent().sayConsole("I'm stuck and moving out the way to " + nextNode);
				((ExploreMultiAgent) this.myAgent).moveToIntention(nextNode, nextNode);
			} else {
				this.brain.getAgent().sayConsole("I'm stuck and waiting to see if it changes");
				this.brain.addTimeStuck(1);
			}
		} else {
			this.brain.getAgent().sayConsole("I'm not stuck anymore!");
			this.brain.resetStuck();
		}
	}
	*/
	
	public int onEnd() {
		this.brain.setLastDecision(this.decision);
		return this.decisionToInt.get(this.decision);
	}

}
