package customBehaviours;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.graphstream.graph.Node;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentKnowledge;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;

public class BrainBehaviour extends FSMBehaviour {

	/**
	 * Brain of the agent, FSM moving in between the movement Behaviours
	 */
	private static final long serialVersionUID = 4687269091623129126L;
	
	private MapRepresentation myMap;
	private HashMap<String, AgentKnowledge> agentsKnowledge = new HashMap<String, AgentKnowledge>();
	
	private ArrayList<String> openNodes = new ArrayList<String>();
	private HashSet<String> closedNodes = new HashSet<String>();

	private HashMap<String, Integer> decisionToInt = new HashMap<String, Integer>();
	private String lastDecision;
	private List<String> lastPath;
	
	private boolean isStuck = false;
	private int timeStuck = 0;
	
	private boolean explorationFinished = false;
	
	// The agents I'm interested in
	private ArrayList<AgentKnowledge> interestingAgents;
	
	public BrainBehaviour (Agent myAgent, HashSet<String> agentNames) {
		this.myAgent = myAgent;
		this.myMap = new MapRepresentation("me");
		
		for (String agentName: agentNames) {
			this.agentsKnowledge.put(agentName, new AgentKnowledge(agentName));
		}
		
		this.decisionToInt.put("Decision", 0);
		this.decisionToInt.put("Exploration", 1);
		this.decisionToInt.put("SeekMeeting", 2);
		this.decisionToInt.put("Patrol", 3);
	}
	
	public void onStart() {
		this.registerFirstState(new DecisionBehaviour(this), "Decision");
		this.registerTransition("Decision", "Decision", (int) this.decisionToInt.get("Decision"));
		
		// Exploration transitions
		this.registerState(new ExploMultiBehaviour(this), "Exploration");
		
		this.registerTransition("Decision", "Exploration", (int) this.decisionToInt.get("Exploration"));
		this.registerTransition("Exploration", "Decision", (int) this.decisionToInt.get("Decision"));
		
		// SeekMeeting transitions
		this.registerState(new SeekMeetingBehaviour(this), "SeekMeeting");
		
		this.registerTransition("Decision", "SeekMeeting", (int) this.decisionToInt.get("SeekMeeting"));
		this.registerTransition("SeekMeeting", "Decision", (int) this.decisionToInt.get("Decision"));
		
		// Patrol transitions
		this.registerState(new PatrolBehaviour(this), "Patrol");
		
		this.registerTransition("Decision", "Patrol", (int) this.decisionToInt.get("Patrol"));
		this.registerTransition("Patrol", "Decision", (int) this.decisionToInt.get("Decision"));
	}
	
	/*
	 * Knowledge methods
	 */
	
	public ExploreMultiAgent getAgent() {
		return (ExploreMultiAgent)this.myAgent;
	}
	
	public MapRepresentation getMap() {
		return this.myMap;
	}
	
	public HashMap<String, AgentKnowledge> getAgentsKnowledge(){
		return this.agentsKnowledge;
	}
	
	public HashSet<String> getAgentsAround(){
		return ((ExploreMultiAgent)this.myAgent).getAgentsAround();
	}
	
	/*
	 * Decision methods
	 */
	
	public HashMap<String, Integer> getDecisionToInt(){
		return this.decisionToInt;
	}
	
	public String getLastDecision() {
		return this.lastDecision;
	}
	
	public void setLastDecision(String newLastDecision) {
		this.lastDecision = newLastDecision;
	}
	
	public List<String> getLastPath() {
		return lastPath;
	}

	public void setLastPath(List<String> lastPath) {
		this.lastPath = lastPath;
	}
	
	/* 
	 * Topology methods
	 */
	
	public ArrayList<String> getOpenNodes(){
		return this.openNodes;
	}
	
	public void setOpenNodes(ArrayList<String> newOpenNodes) {
		this.openNodes = newOpenNodes;
	}
	
	public void addOpenNodes(String newOpenNode) {
		if (!this.getMap().hasNode(newOpenNode))	this.openNodes.add(newOpenNode);
	}
	
	public void removeOpenNodes(String node) {
		this.openNodes.remove(node);
	}
	
	public HashSet<String> getClosedNodes(){
		return this.closedNodes;
	}
	
	public void setClosedNodes(HashSet<String> newClosedNodes) {
		this.closedNodes = newClosedNodes;
	}
	
	public void addClosedNodes(String newNode) {
		if (!this.closedNodes.contains(newNode))	this.closedNodes.add(newNode);
	}
	
	public void fuseMap(MapRepresentation otherMap) {
		this.myMap.fuseMap(otherMap.getSg());
	}
	
	public void fuseMap(SerializableSimpleGraph<String, MapAttribute> otherSg) {
		this.myMap.fuseMap(otherSg);
	}
	
	public void updateNodesWithMap() {
		// What is my knowledge of the current map?
        Iterator<Node> iterGraph=this.getMap().getGraph().iterator();
        while(iterGraph.hasNext()){
            Node n=iterGraph.next();
            if (MapAttribute.valueOf((String)n.getAttribute("ui.class")).toString().equals("open")) {
            	this.addOpenNodes(n.getId());
            }
            else if (MapAttribute.valueOf((String)n.getAttribute("ui.class")).toString().equals("closed")) {
            	this.addClosedNodes(n.getId());
            }
        }
	}

	public boolean isStuck() {
		return isStuck;
	}

	public void setStuck(boolean isStuck) {
		this.isStuck = isStuck;
	}

	public int getTimeStuck() {
		return timeStuck;
	}
	
	public void setTimeStuck(int newTime) {
		this.timeStuck = newTime;
	}
	
	public void addTimeStuck(int moreTime) {
		this.timeStuck += moreTime;
	}
	
	public void resetStuck() {
		this.setStuck(false);
		this.setTimeStuck(0);
	}

	public ArrayList<AgentKnowledge> getInterestingAgents() {
		return interestingAgents;
	}

	public void setInterestingAgents(ArrayList<AgentKnowledge> interestingAgents) {
		this.interestingAgents = interestingAgents;
	}

	public boolean isExplorationFinished() {
		return explorationFinished;
	}

	public void setExplorationFinished(boolean explorationFinished) {
		this.explorationFinished = explorationFinished;
	}
}
