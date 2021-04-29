package customBehaviours;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentKnowledge;
import jade.core.behaviours.OneShotBehaviour;

public class SeekMeetingBehaviour extends OneShotBehaviour {
	
	/**
	 * Finds the most interesting agent and gets closer to it (tries to intercept its path)
	 */
	private static final long serialVersionUID = 1095651279250091269L;
	
	private BrainBehaviour brain;
	private ExploreMultiAgent myAgent;
	private MapRepresentation map;
	private String myPosition;
	
	private ArrayList<AgentKnowledge> interestingAgents;
	private HashMap<String, Integer> decisionToInt;

	private List<String> pathToFollow = new ArrayList<String>();
	
	public SeekMeetingBehaviour (BrainBehaviour brain) {
		this.brain = brain;
		this.myAgent = brain.getAgent();
		this.decisionToInt = this.brain.getDecisionToInt();
	}

	@Override
	public void action() {
		this.map = this.brain.getMap();
		
		if(this.map.getMigration()==true) {
			this.myAgent.loadAllMaps();
		}
		
		if (this.myPosition!=null){
			this.interestingAgents = this.brain.getInterestingAgents();
			
			// Ordering the interesting agents by utility
			this.interestingAgents.sort(Comparator.comparing(a -> a.getMeetUtility()));
			this.myPosition = this.myAgent.getCurrentPosition();
			
			// Computes the intercept path to the most interesting agent
			for (AgentKnowledge otherAgent: this.interestingAgents) {
				// If there is no way to reach the agent, then we skip it
				otherAgent.computeDistance(this.map, this.myPosition);
				if (otherAgent.getLastPosition() == null)	continue;
				else if (otherAgent.getPathToAgent() == null)	continue;
				else if (otherAgent.getDistance() == Integer.MAX_VALUE)	continue;
				
				List<String> lastPath = otherAgent.getLastPath();
				if (otherAgent.getLastPath() == null) {
					this.pathToFollow = otherAgent.getPathToAgent();
					break;
				} else {
					for (int i = 1; i <= lastPath.size(); i++) {
						try {
							this.pathToFollow = this.map.getShortestPath(this.myPosition, lastPath.get(-i));
							break;
						} catch (NullPointerException e) {
							continue;
						} catch (IndexOutOfBoundsException e) {
							continue;
						}
					}
					break;
				}
			}
			
			// Check maybe can't get to any of them
			
			((ExploreMultiAgent)this.myAgent).discover();
			this.brain.setLastPath(this.pathToFollow);
			if (this.pathToFollow.size() > 1)	((ExploreMultiAgent)this.myAgent).moveToIntention(this.pathToFollow.get(0), this.pathToFollow);
		}
	}

	@Override
	public int onEnd() {
		this.brain.deregisterState("SeekMeeting");
		
		this.brain.registerState(new SeekMeetingBehaviour(this.brain), "SeekMeeting");
		
		this.brain.registerTransition("Decision", "SeekMeeting", (int) this.decisionToInt.get("SeekMeeting"));
		this.brain.registerTransition("SeekMeeting", "Decision", (int) this.decisionToInt.get("Decision"));
		
		return this.decisionToInt.get("Decision");
	}
}
