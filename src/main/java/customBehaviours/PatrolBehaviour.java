package customBehaviours;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.graphstream.graph.Graph;
import org.graphstream.algorithm.Toolkit;

import dataStructures.serializableGraph.SerializableNode;
import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentKnowledge;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class PatrolBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;
	private Graph g;
	
	private String nextNode;
	private HashMap<String, Integer> decisionToInt;
	
	private BrainBehaviour brain;

	public PatrolBehaviour(BrainBehaviour brain) {
		super(brain.getAgent());
		
		this.brain = brain;
		this.decisionToInt = brain.getDecisionToInt();
	}

	@Override
	public void action() {
		if(this.brain.getMap().getMigration()==true) {
			((ExploreMultiAgent)this.myAgent).loadAllMaps();
		}
		
		try {
			this.myAgent.doWait(500);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		this.nextNode = null;
		
		if (myPosition!=null){		
			// New path
			List<String> nextPath = new ArrayList<String>();

			this.myMap = this.brain.getMap();
			this.g = this.myMap.getGraph();		
	
			// Seek the closest friends to share my map!
			int lowestSize = Integer.MAX_VALUE;
			for (AgentKnowledge otherAgent: this.brain.getAgentsKnowledge().values()) {
				if (otherAgent.getLastAction() != null && (otherAgent.getLastAction().equals("Exploration") || otherAgent.getLastAction().equals("SeekMeeting"))) {
					otherAgent.computeDistance(this.myMap, myPosition);
					if (otherAgent.getDistance() < lowestSize) {
						nextPath = otherAgent.getPathToAgent();
						this.nextNode = nextPath.get(0);
					}
				}
			}
				
			// Seek the closest detected stench if no agent to share with
			List<List<String>>pathsToAgents = new ArrayList<List<String>>();
			Hashtable<AgentKnowledge, List<String>> huntersAndStench = this.brain.getHuntersAndStench();
			if (this.nextNode == null) {
				for (AgentKnowledge hunter: huntersAndStench.keySet()) {
					hunter.computeDistance(this.myMap, myPosition);
					if (hunter.getPathToAgent() != null) {
						pathsToAgents.add(hunter.getPathToAgent());
					}
				}			
				if (pathsToAgents.size() > 0) {
					Collections.sort(pathsToAgents, Comparator.comparing(a -> a.size()));
					nextPath = pathsToAgents.get(0);
					this.nextNode = nextPath.get(0);
					
					String stenchLocation = nextPath.get(nextPath.size() - 1);
					
					this.brain.setLastStenchDetected(stenchLocation);
					((ExploreMultiAgent)this.myAgent).sayConsole("A stench was found ! Heading to " + stenchLocation);
				} else if (this.brain.getLastStenchDetected() != null) {
					
					nextPath = this.myMap.getShortestPath(myPosition, this.brain.getLastStenchDetected());
					
					if (nextPath.size() == 0) {
						this.brain.setLastStenchDetected(null);
					} else {
						this.nextNode = nextPath.get(0);
						((ExploreMultiAgent)this.myAgent).sayConsole("I remember the golem was in " + this.brain.getLastStenchDetected() + " last time !");
					}
				} else {
					// Continue a bit on the previous chosen path
					if (this.brain.getLastPath() != null) {
						int pathProgress = this.brain.getLastPath().indexOf(myPosition);
						if (pathProgress == -1 || this.brain.getLastPath().size() <= 2) {
							// We are not on the previous path (maybe impossible?)
						} else if (pathProgress <= Math.floor(this.brain.getLastPath().size()/2)) {
							// We want to do at least half of the previous path to make another decision
							this.nextNode = this.brain.getLastPath().get(pathProgress + 1);
							nextPath = this.brain.getLastPath();
						}
					}
				}
			}
			
			// Otherwise randomly wander
			if (nextPath.size() == 0) {
				List<Couple<String,List<Couple<Observation,Integer>>>> lobs = ((AbstractDedaleAgent)this.myAgent).observe();
				Collections.shuffle(lobs);			
				this.nextNode = lobs.get(0).getLeft();
				nextPath.add(myPosition);
				nextPath.add(this.nextNode);
			}
			

			//list of observations associated to the currentPosition
			//List<Couple<Observation,Integer>> lObservations= lobs.get(0).getRight();
			//System.out.println(this.myAgent.getLocalName()+" - State of the observations : "+lobs);	
			
			//((ExploreMultiAgent)this.myAgent).sayConsole("I want to go to " + this.nextNode + " I am following this path : " + nextPath);
			this.brain.setLastPath(nextPath);
			((ExploreMultiAgent)this.myAgent).moveToIntention(this.nextNode, nextPath);
		}
	}		
	

	@Override
	public int onEnd() {
		this.brain.deregisterState("Patrol");
		this.brain.registerState(new PatrolBehaviour(this.brain), "Patrol");
		
		this.brain.registerTransition("Decision", "Patrol", (int) this.decisionToInt.get("Patrol"));
		this.brain.registerTransition("Patrol", "Decision", (int) this.decisionToInt.get("Decision"));
		
		return this.decisionToInt.get("Decision");
	}
}
