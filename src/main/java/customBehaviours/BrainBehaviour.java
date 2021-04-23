package customBehaviours;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentKnowledge;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
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
	
	// Current meeting's information, null if no meeting going on
	private String meetingTopic;
	private ArrayList<String> participants;
	
	public BrainBehaviour (Agent myAgent, MapRepresentation myMap, HashSet<String> agentNames) {
		this.myAgent = myAgent;
		this.myMap = myMap;
		
		for (String agentName: agentNames) {
			this.agentsKnowledge.put(agentName, new AgentKnowledge(agentName));
		}
		
		this.decisionToInt.put("Decision", 0);
		this.decisionToInt.put("Exploration", 1);
		this.decisionToInt.put("Meeting", 2);
	}
	
	public void onStart() {
		this.registerFirstState(new DecisionBehaviour(this), "Decision");
		this.registerState(new ExploMultiBehaviour(this), "Exploration");
		this.registerState(new MeetingBehaviour(this), "Meeting");
		
		this.registerTransition("Decision", "Decision", (int) this.decisionToInt.get("Decision"));
		
		this.registerTransition("Decision", "Exploration", (int) this.decisionToInt.get("Exploration"));
		this.registerTransition("Exploration", "Decision", (int) this.decisionToInt.get("Decision"));
		
		this.registerTransition("Decision", "Meeting", (int) this.decisionToInt.get("Meeting"));
		this.registerTransition("Meeting", "Decision", (int) this.decisionToInt.get("Decision"));
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
	 * Meeting methods
	 */
	
	public void endMeeting() {
		this.meetingTopic = null;
		this.participants = null;
	}
	
	public String getMeetingTopic() {
		return this.meetingTopic;
	}
	
	public ArrayList<String> getParticipants() {
		return this.participants;
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
		this.openNodes.add(newOpenNode);
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
		this.closedNodes.add(newNode);
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
}
