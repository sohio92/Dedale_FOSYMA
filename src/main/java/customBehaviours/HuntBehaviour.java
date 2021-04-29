package customBehaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import dataStructures.serializableGraph.SerializableNode;
import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentKnowledge;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class HuntBehaviour extends OneShotBehaviour{
	
	/**
	 * Hunts the golem
	 */
	private static final long serialVersionUID = 8407533481381014005L;

	private BrainBehaviour brain;
	
	private String chasseProto = "PingChasseProtocol";
	private HashMap<String, Integer> decisionToInt;

	public HuntBehaviour(BrainBehaviour brain) {
		this.brain = brain;
		this.myAgent = brain.getAgent();
		this.decisionToInt = brain.getDecisionToInt();
		
	}

	@Override
	public void action() {
		if(this.brain.getMap().getMigration()==true) {
			((ExploreMultiAgent)this.myAgent).loadAllMaps();
		}
		
		try {
			this.myAgent.doWait(((ExploreMultiAgent)this.myAgent).getTimeSleep());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		Hashtable<AgentKnowledge, List<String>> huntersAndStench = this.brain.getHuntersAndStench();

		// Retrieving the detected stench
		List<String> golemStench = new ArrayList<String>(this.brain.getGolemStench());
		List<String> huntingHistory = this.brain.getHuntingHistory();
		
		// on laisse celui qui a moins de choix prendre une place
		// si egalité lordre lexico fait la diff
		huntersAndStench.forEach((hunter, detectedStench) -> {
			if (detectedStench.size() < golemStench.size() ||
					(detectedStench.size() == golemStench.size() && this.myAgent.getName().compareTo(hunter.getName()) == -1)) {
				//this.brain.getAgent().sayConsole("je laisse la place a " + hunter.getName());
				golemStench.removeAll(detectedStench);
			}
		});
		
		
		// Updating history
		this.brain.addHuntingHistory(myPosition);
		
		// Choosing which stench, depending on its distance to other hunters
		String nextNode = null;
		Collections.shuffle(golemStench);
		for (String otherNode: golemStench) {
			if (otherNode.equals(myPosition))	continue;
			if (huntingHistory.contains(otherNode))	continue;
			
			nextNode = otherNode;
			break;
		}
		
		// If we couldn't find any node, we have to check in the history
		if (nextNode == null) {
			for (String otherNode: golemStench) {
				if (otherNode.equals(myPosition))	continue;
				if (huntingHistory.contains(otherNode))	{
					nextNode = otherNode;
					break;
				}
			}
			//nextNode = huntingHistory.get(huntingHistory.size()-1);
		}
		
		// Doing this in case observation range greater than one
		if (nextNode != null) { //if one possibility is good 
			List<String> nextPath = this.brain.getMap().getShortestPath(myPosition, nextNode);
			if (nextPath.size() > 0) {
				nextNode = nextPath.get(0);	
				this.brain.setLastPath(nextPath);
				((ExploreMultiAgent)this.myAgent).moveToIntention(nextNode, nextPath);
			}
		}
	}


	/*
	void add_yellow_page(String service){
		DFAgentDescription dfd = new DFAgentDescription();
		dfd .setName(this.myAgent.getAID()); // The agent AID
		ServiceDescription sd = new
				ServiceDescription () ;
		sd.setType(service); // You have to give a name to each service your agent offers
		sd.setName(this.myAgent.getLocalName());//(local)name of the agent
		dfd.addServices(sd) ;
		//Register the service
		try {
			DFService.register(this.myAgent, dfd);
		} catch (FIPAException fe) {
			fe . printStackTrace () ; }
	}
	*/
	
	@Override
	public int onEnd() {
		this.brain.deregisterState("Hunt");
		
		this.brain.registerState(new HuntBehaviour(this.brain), "Hunt");
		
		this.brain.registerTransition("Decision", "Hunt", (int) this.decisionToInt.get("Hunt"));
		this.brain.registerTransition("Hunt", "Decision", (int) this.decisionToInt.get("Decision"));
		
		this.brain.registerTransition("Hunt", "HuntFinished", (int) this.decisionToInt.get("HuntFinished"));
		
		if (this.brain.isHuntFinished() == true)	return this.decisionToInt.get("HuntFinished");
		else	return this.decisionToInt.get("Decision");
	}

}
