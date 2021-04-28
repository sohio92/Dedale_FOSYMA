package customBehaviours;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
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
		
		if (myPosition!=null){		
			// New path
			List<String> nextPath = new ArrayList<String>();

			this.myMap = brain.getMap();
			this.g = this.myMap.getGraph();
			
			// Seek the closest friends to share my map!
			if (this.brain.isStuck()) {
				int lowestSize = Integer.MAX_VALUE;
				for (AgentKnowledge otherAgent: this.brain.getAgentsKnowledge().values()) {
					if (otherAgent.getLastAction() != null && otherAgent.getLastAction().equals("Exploration")) {
						otherAgent.computeDistance(this.myMap, myPosition);
						if (otherAgent.getDistance() < lowestSize) {
							nextPath = otherAgent.getPathToAgent();
							this.nextNode = nextPath.get(0);
						}
					}
				}
			}
			
			// Otherwise randomly wander
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs = ((AbstractDedaleAgent)this.myAgent).observe();
			Collections.shuffle(lobs);
			
			/*
			 * while (nextPath.size() <= 1) {

				nextPath = this.myMap.getShortestPath(myPosition, Toolkit.randomNode(this.g).getId());
				((ExploreMultiAgent)this.myAgent).sayConsole("THIS IS MY PATH COMPUTING " + nextPath);
			}
			((ExploreMultiAgent)this.myAgent).sayConsole("DONE " + nextPath);
			*/
			

			if (nextPath.size() == 0) {
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
