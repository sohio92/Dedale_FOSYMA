package customBehaviours;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
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
	
	private int explorationTimeOut = 0;
	private boolean explorationFinished = false;
	private boolean huntFinished = false;
	private List<String> huntingHistory = new ArrayList<String>();
	
	// Stench detected
	private List<String> golemStench;
	private Hashtable<AgentKnowledge, List<String>> huntersAndStench = new Hashtable<AgentKnowledge, List<String>>();
	private String lastStenchDetected;
	
	// The agents I'm interested in
	private ArrayList<AgentKnowledge> interestingAgents;
	
	// SeekMeetingTimeOut
	private int timeSoughtMeeting = 0;
	private int waitOutMeeting = 5;

	
	
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
		this.decisionToInt.put("Hunt", 4);
		this.decisionToInt.put("HuntFinished", 5); //do nothing
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
		
		// Hunting transitions
		this.registerState(new HuntBehaviour(this), "Hunt");
		
		this.registerTransition("Decision", "Hunt", (int) this.decisionToInt.get("Hunt"));
		this.registerTransition("Hunt", "Decision", (int) this.decisionToInt.get("Decision"));
	
		this.registerLastState(new HuntFinishedBehaviour(this), "HuntFinished");
		
		this.registerTransition("Hunt", "HuntFinished", (int) this.decisionToInt.get("HuntFinished"));
		this.registerTransition("Exploration", "HuntFinished", (int) this.decisionToInt.get("HuntFinished"));
		
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
	
	public void finishExploration(){
		for (AgentKnowledge otherKnowledge: this.getAgentsKnowledge().values()) {
			otherKnowledge.setMeetUtility(0);
		}
		this.setExplorationFinished(true);
		((ExploreMultiAgent)this.myAgent).sayConsole("Exploration successufully done.");
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
		if (!this.openNodes.contains(newOpenNode)) {
			this.openNodes.add(newOpenNode);
		}
		if (!this.getMap().hasNode(newOpenNode))	{
			this.getMap().addNode(newOpenNode, MapAttribute.open);
		}
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
		this.getMap().addNode(newNode, MapAttribute.closed);
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
	
	public boolean isHuntFinished() {
		return huntFinished;
	}

	public void setExplorationFinished(boolean explorationFinished) {
		this.explorationFinished = explorationFinished;
	}
	
	public void setHuntFinished(boolean huntFinished) {
		this.huntFinished = huntFinished;
	}

	public List<String> getHuntingHistory() {
		return huntingHistory;
	}

	public void setHuntingHistory(List<String> huntingHistory) {
		this.huntingHistory = huntingHistory;
	}
	
	public void addHuntingHistory(String newNode) {
		this.huntingHistory.add(newNode);
	}

	public List<String> getGolemStench() {
		return golemStench;
	}
	
	public void updateGolemStench() {
		this.golemStench = ((ExploreMultiAgent)this.myAgent).getStenchAround();
		if (this.golemStench.size() > 0)	this.setLastStenchDetected(this.golemStench.get(0));
	}

	public void setGolemStench(List<String> golemStench) {
		this.golemStench = golemStench;
	}

	public int getTimeSoughtMeeting() {
		return timeSoughtMeeting;
	}

	public void setTimeSoughtMeeting(int timeSoughtMeeting) {
		this.timeSoughtMeeting = timeSoughtMeeting;
	}
	
	public void addTimeSoughtMeeting(int moreTime) {
		this.timeSoughtMeeting += moreTime;
	}

	public int getWaitOutMeeting() {
		return waitOutMeeting;
	}

	public void setWaitOutMeeting(int waitOutMeeting) {
		this.waitOutMeeting = waitOutMeeting;
	}
	
	public void addWaitOutMeeting(int moreMeeting) {
		this.waitOutMeeting += moreMeeting;
	}

	public Hashtable<AgentKnowledge, List<String>> getHuntersAndStench() {
		return huntersAndStench;
	}

	public void setHuntersAndStench(Hashtable<AgentKnowledge, List<String>> huntersAndStench) {
		this.huntersAndStench = huntersAndStench;
	}
	
	public void replaceHuntersAndStench(AgentKnowledge hunter, List<String> stench) {
		this.huntersAndStench.put(hunter, stench);
		hunter.setDetectedStench(stench);
	}

	public String getLastStenchDetected() {
		return lastStenchDetected;
	}

	public void setLastStenchDetected(String lastStenchDetected) {
		this.lastStenchDetected = lastStenchDetected;
	}

	public int getExplorationTimeOut() {
		return explorationTimeOut;
	}

	public void addExplorationTimeOut(int newTimeOut) {
		this.explorationTimeOut += newTimeOut;
	}
	
	public void resetExplorationTimeOut() {
		this.explorationTimeOut = 0;
	}
	
}
