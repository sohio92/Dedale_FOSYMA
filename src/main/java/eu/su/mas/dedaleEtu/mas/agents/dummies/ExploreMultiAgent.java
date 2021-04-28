package eu.su.mas.dedaleEtu.mas.agents.dummies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.graphstream.graph.Node;

import customBehaviours.BrainBehaviour;
import customBehaviours.DecisionBehaviour;
import customBehaviours.ExploMultiBehaviour;
import customBehaviours.ListenBehaviour;
import customBehaviours.PingPositionBehaviour;
import customBehaviours.ShareMapBehaviour;
import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;

import java.util.HashSet;
import java.util.Iterator;

import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentKnowledge;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;

/**
 * <pre>
 * ExploreSolo agent. 
 * It explore the map using a DFS algorithm.
 * It stops when all nodes have been visited.
 *  </pre>
 *  
 * @author hc
 *
 */

public class ExploreMultiAgent extends AbstractDedaleAgent {

	private static final long serialVersionUID = -6431752665590433727L;
	

	// Name of the application's other agents
	private HashSet<String> agentNames;
	
	private BrainBehaviour brain;

	// Current destination of the agent
	private List<String> path;
	
	// Agents detected in the vicinity with their last known position
	private HashSet<String> agentsAround = new HashSet<String>();
	
	// An agent's listen and ping behaviours, stored here to be able to stop and resume them at will
	private PingPositionBehaviour pingPositionBehaviour;
	private ListenBehaviour listenBehaviour;
	
	// Agents with whom the agent already has communicated during a meeting
	private boolean onGoingMeeting = false;
	private ArrayList<String> alreadyCommunicated;
	
	// Information about current loading state
	private boolean loaded = false;
	
	// What is my surrounding's range
	private int maxRange = 4;
	
	// Time to sleep between each step to see whats going on
	private int timeSleep = 500;
	
	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 
	 * 			1) set the agent attributes 
	 *	 		2) add the behaviours
	 *          
	 */
	protected void setup(){

		super.setup();
	
	    /**
	     * Get the other agents' names
	     */
		this.agentNames = getAgentsList();
		List<Behaviour> lb=new ArrayList<Behaviour>();
		
		/************************************************
		 * 
		 * ADD the initial behaviours of the Agent here
		 * 
		 ************************************************/
		
		// Instantiating all the maps, serializing them
		
		
		// Ping behaviour : let other agents know where I am
		this.pingPositionBehaviour = new PingPositionBehaviour(this);
		lb.add(this.pingPositionBehaviour);
		// Listen behaviour : know where the other agents are
		this.listenBehaviour = new ListenBehaviour(this);
		lb.add(this.listenBehaviour);
		// Decision behaviour : decides what happens next
		this.brain = new BrainBehaviour(this, this.agentNames);
		lb.add(this.brain);
		
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */
		this.serializeAllMaps();
		
		addBehaviour(new startMyBehaviours(this,lb));
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");

	}
	
	public void sayConsole(String content) {
		// Prints a new line in the console
		if (content.contains("surroundings"))return;
		System.out.println(this.getLocalName() + ": " + content);
	}
	
	public void serializeAllMaps() {
		// Prepare all maps for migration
		this.loaded = false;
		
		this.getBrain().getMap().prepareMigration();
		
		for(AgentKnowledge otherKnowledge: this.getBrain().getAgentsKnowledge().values()) {
			otherKnowledge.map.prepareMigration();
		}
	}
	public void loadAllMaps() {
		// Restore all maps from migrated state
		this.getBrain().getMap().loadSavedData();
		for (AgentKnowledge otherKnowledge: this.brain.getAgentsKnowledge().values()) {
			otherKnowledge.map.loadSavedData();
		}
		this.loaded = true;
	}
	
	public HashSet <String> getAgentsList(){
		// Get the list of all the agents through the yellow pages
		AMSAgentDescription[] agentsDescriptionCatalog = null;
		HashSet <String> agentsNames= new HashSet<String>();
		try {
			SearchConstraints c = new SearchConstraints();
			c.setMaxResults(new Long(-1));
			agentsDescriptionCatalog = AMSService.search(this, new
					AMSAgentDescription(), c);
		}
		catch (Exception e) {
			System.out.println("Problem searching AMS: " + e );
			e.printStackTrace();
		}
		for (int i=0; i<agentsDescriptionCatalog.length; i++){
			AID agentID = agentsDescriptionCatalog[i].getName();
			if (!agentID.getLocalName().equals(this.getAID().getLocalName()))	agentsNames.add(agentID.getLocalName());
		}
		return agentsNames;
	}
	
	public List<String> getPath() {
		return this.path;
	}
	public void setPath(List<String> newIntention) {
		this.path = newIntention;
	}
	
	public AgentKnowledge getMyKnowledge(String otherName) {
		// Return the knowledge about the given agent
		for (AgentKnowledge otherKnowledge: this.getBrain().getAgentsKnowledge().values()) {
			if (otherKnowledge.getName().equals(otherName)) {
				return otherKnowledge;
			}
		}
		return null;
	}
	public void updateOthersIgnorance() {
		// Increments the difference in known edges and known nodes after a step from myPosition to newNode
		for (AgentKnowledge otherKnowledge: this.getBrain().getAgentsKnowledge().values()) {
			otherKnowledge.map.updateIgnorance(this.getBrain().getMap());
		}
	}
	
	public HashSet<String> getAgentsAround() {
		return this.agentsAround;
	}
	
	public void checkAgentAround(String newAgent, Integer maxDistance) {
		AgentKnowledge newKnowledge = this.getBrain().getAgentsKnowledge().get(newAgent);
		
		if (maxDistance >= newKnowledge.getDistance()) {
			// Add an agent to the vicinity and updates its last position
			if (!this.agentsAround.contains(newAgent)) {
				this.agentsAround.add(newAgent);
			}
		} else {
			// Removes an agent from the vicinity
			if (this.agentsAround.contains(newAgent)) {
				this.agentsAround.remove(newAgent);
			}
		}

	}
	
	public void removeAgentsAround(String otherAgent) {
		this.agentsAround.remove(otherAgent);
	}
	
	public void clearSurroundings(int maxDistance) {
		// If an agent is too far, it is removed from the surroundings
		String myPosition = this.getCurrentPosition();
		for (AgentKnowledge otherKnowledge: this.getBrain().getAgentsKnowledge().values()) {
			int distance = 0;
			try {
				distance = this.getBrain().getMap().getShortestPath(myPosition, otherKnowledge.getLastPosition()).size();
			} catch (NullPointerException e) {
				distance = maxDistance + 1;
			} catch (java.lang.IndexOutOfBoundsException e) {
				distance = maxDistance + 1;
			}
			
			if (distance > maxDistance || distance == 0) {
				this.removeAgentsAround(otherKnowledge.map.getOwner());
			}
		}
	}
	
	public void addAgentName(String newAgent) {
		this.agentNames.add(newAgent);
	}
	public void removeAgentName(String otherAgent) {
		this.agentNames.remove(otherAgent);
	}
	
	public HashSet<String> getAgentsNames(){
		return this.agentNames;
	}
	public PingPositionBehaviour getPingBehaviour() {
		return this.pingPositionBehaviour;
	}
	public ListenBehaviour getListenBehaviour() {
		return this.listenBehaviour;
	}
	
	public void updateMap(String otherAgent, SerializableSimpleGraph<String, MapAttribute> otherSg) {
		this.sayConsole("I'm updating my map with that of " + otherAgent);
		// Update the "me" map
		this.getBrain().getMap().fuseMap(otherSg);
		
		// Update the other agent's last known map
		AgentKnowledge otherKnowledge = this.getMyKnowledge(otherAgent);
		otherKnowledge.map.fuseMap(this.getBrain().getMap().getSg());
		this.updateOthersIgnorance();
	}
	
	public void addDiffNodes(int increment) {
		for (AgentKnowledge otherKnowledge: this.brain.getAgentsKnowledge().values()) {
			otherKnowledge.map.addDiffNodes(increment);
		}
	}
	public void addDiffEdges(int increment) {
		for (AgentKnowledge otherKnowledge: this.brain.getAgentsKnowledge().values()) {
			otherKnowledge.map.addDiffEdges(increment);
		}
	}
	
	public List<String> getAlreadyCommunicated(){
		return this.alreadyCommunicated;
	}
	
	public void addAlreadyCommunicated(String otherAgent) {
		if (!this.alreadyCommunicated.contains(otherAgent)) {
			this.alreadyCommunicated.add(otherAgent);
		}
	}
	
	public void removeAlreadyCommunicated(String otherAgent) {
		this.alreadyCommunicated.remove(otherAgent);
	}
	
	public void resetAlreadyCommunicated() {
		this.alreadyCommunicated = new ArrayList<String>();
	}
	
	public boolean getOnGoingMeeting() {
		return this.onGoingMeeting;
	}
	public void setOnGoingMeeting(boolean newTruth) {
		this.onGoingMeeting = newTruth;
	}
	
	public void endMeeting() {
		this.setOnGoingMeeting(false);
		this.resetAlreadyCommunicated();
	}
	
	public boolean isLoaded() {
		return this.loaded;
	}
	
	public void moveToIntention(String newNode, List<String> newPath) {
		this.setPath(newPath);
		this.getBrain().setStuck(!this.moveTo(newNode));
	}
	
	public void moveToIntention(String newNode) {
		this.getBrain().setStuck(!this.moveTo(newNode));
	}
	
	public BrainBehaviour getBrain() {
		return this.brain;
	}
	
	public void addOpenNode(String newNode) {
		this.getBrain().getMap().addNode(newNode, MapAttribute.open);
		this.getBrain().addOpenNodes(newNode);
	}

	public int getMaxRange() {
		return maxRange;
	}

	public void setMaxRange(int maxRange) {
		this.maxRange = maxRange;
	}
	
	// Discovers environment, if there exists a directly reachable open node, returns it
	public String discover() {
		//List of observable from the agent's current position
		List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this).observe();//myPosition
		
		/**
		 * Just added here to let you see what the agent is doing, otherwise he will be too quick
		 */
		try {
			this.doWait(this.getTimeSleep());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String myPosition = this.getCurrentPosition();
		MapRepresentation map = this.getBrain().getMap();
		
		//1) remove the current node from openlist and add it to closedNodes.
		this.getBrain().addClosedNodes(myPosition);
		this.getBrain().removeOpenNodes(myPosition);
		map.addNode(myPosition,MapAttribute.closed);
		
		// If we are where an agent was supposed to be, then we don't know where it is anymore
		for (AgentKnowledge otherKnowledge: this.getBrain().getAgentsKnowledge().values()) {
			if (myPosition == otherKnowledge.getLastPosition())	otherKnowledge.setLastPosition(null);
		}

		//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
		Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
		List<String> nextOpen = new ArrayList<String>();
		String nodeId = "";
		while(iter.hasNext()){
			nodeId=iter.next().getLeft();
			if (!this.getBrain().getClosedNodes().contains(nodeId)){
				if (!this.getBrain().getOpenNodes().contains(nodeId)){
					this.getBrain().addOpenNodes(nodeId);
					map.addNode(nodeId, MapAttribute.open);
					map.addEdge(myPosition, nodeId);
					this.addDiffNodes(1);
				}else{
					//the node exist, but not necessarily the edge
					if (map.addEdge(myPosition, nodeId) == true)	this.addDiffEdges(1);
				}
				nextOpen.add(nodeId);
			}
		}
		
		String nodeOpen = "";
		if (nextOpen.size() > 0) {
			Collections.shuffle(nextOpen);
			nodeOpen = nextOpen.get(0);
		}
		
		//list of observations associated to the currentPosition
		//List<Couple<Observation,Integer>> lObservations= lobs.get(0).getRight();
		//this.sayConsole(" - State of the observations : "+lobs);	
		
		//?this.brain.fuseMap(map);
		return nodeOpen;
	}
	
	// Returns what we smell
	public List<String> getStenchAround(){
		List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this).observe();
		Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
		
		List<String> golemStench = new ArrayList<String>();
		//while (iter.hasNext()) {
			//Couple<String,List<Couple<Observation,Integer>>> nodeObserved = iter.next();
		for (Couple<String,List<Couple<Observation,Integer>>> nodeObserved : lobs) {		
			// If something is observed
			if (nodeObserved.getRight().size() > 0) {
				// For everything that is observed, check if we smell the golem's stench
				for (Couple<Observation,Integer> observation: nodeObserved.getRight()) {
					// Maybe wrong string value
					if (observation.getLeft().toString().equals("Stench"))	{
						golemStench.add(nodeObserved.getLeft());
					}
				}
			}
		}
		
		return golemStench;
	}
	
	public int getTimeSleep() {
		return this.timeSleep;
	}
}
