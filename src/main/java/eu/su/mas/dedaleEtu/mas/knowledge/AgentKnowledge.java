package eu.su.mas.dedaleEtu.mas.knowledge;

import java.util.HashMap;
import java.util.List;

import customBehaviours.BrainBehaviour;
import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.util.leap.Serializable;

public class AgentKnowledge implements Serializable{
	/*
	 * Contains all the knowledge about an other agent
	 */
	
	private static final long serialVersionUID = -5138729245826161091L;
	private String name;
	public MapRepresentation map;
	
	private int nbEncounters = 0;
	private long mostRecentTime = -1; // Time stamp of the most recent message received
	private long mostRecentPing = -1; // Time stamp of the most recent ping message received
	
	private String lastPosition;
	private String lastAction;
	private List<String> lastPath;
	
	private int distance = Integer.MAX_VALUE;
	private List<String> pathToAgent;
	
	private double meetUtility = 0; // How much do you want to meet me ?
	private List<String> detectedStench;
	
	public AgentKnowledge(String name) {
		this.name = name;
		this.map = new MapRepresentation(name);
	}
	
	/*
	 * Unpack a MessageContainer, returns true if message is the most recent received from the agent
	 */
	
	public boolean unpackMessage(BrainBehaviour otherBrain, ACLMessage newMessage) {
		boolean recentMessage = newMessage.getPostTimeStamp() >= this.mostRecentPing;
		if (newMessage.getProtocol().equals("PingProtocol")) {
			
			try {
				PingContainer content = (PingContainer) newMessage.getContentObject();
				
				if (recentMessage == true) {
					this.setMostRecentPing(newMessage.getPostTimeStamp());
					
					this.setLastAction(content.getLastAction());
					this.setLastPosition(content.getLastPosition());
					this.setMeetUtility(content.getMeetUtility());
					
					List<String> receivedStench = content.getDetectedStench();
					for (AgentKnowledge otherKnowledge: otherBrain.getAgentsKnowledge().values()) {
						if (otherKnowledge.getName() != this.getName() && otherKnowledge.getMostRecentPing() >= this.getMostRecentPing()) {
							receivedStench.remove(otherKnowledge.getLastPosition());
						}
					}
					this.setDetectedStench(receivedStench);
					otherBrain.replaceHuntersAndStench(this, this.detectedStench);
				}
				
				if (!this.map.hasNode(lastPosition)) {
					this.map.addNode(lastPosition, MapAttribute.open);
					otherBrain.addOpenNodes(lastPosition);
					this.map.updateIgnorance(otherBrain.getMap());
				}
			} catch (UnreadableException e) {e.printStackTrace();}
			
		} else if (newMessage.getProtocol().equals("ShareMapProtocol")) {
			this.map.fuseMap(otherBrain.getMap().getSg());
			this.map.updateIgnorance(otherBrain.getMap());
		}
		
		return recentMessage;
	}
	
	// Add the SG to the known map, updates its ignorance
	public void fuseMap(BrainBehaviour otherBrain, SerializableSimpleGraph<String, MapAttribute> newSg) {
		this.getMap().fuseMap(newSg);
		this.getMap().updateIgnorance(otherBrain.getMap());
	}

	// Adds a new last known path
	public void addNewPath(List<String> newPath) {
		this.map.updateWithPath(newPath);
		this.setLastPath(newPath);
	}
	
	// Compute the share worth of the agent
	public double getShareWorth() {
		this.meetUtility = (Math.floor(this.map.getDiffEdges()/2) + this.map.getDiffNodes()) / Math.pow(distance, 2);
		return this.meetUtility;
	}
	
	// Computes the distance from the agent and the path to it
	public void computeDistance(MapRepresentation otherMap, String otherPosition) {
		try {
			this.setPathToAgent(otherMap.getShortestPath(otherPosition, this.getLastPosition()));
			this.setDistance(this.getPathToAgent().size());
			if (this.pathToAgent.size() != 0)	return;
		} catch (java.lang.IndexOutOfBoundsException e) {
			//this.sayConsole(newAgent + " is not reachable");
		} catch (java.lang.NullPointerException e) {
			// node is not yet in map
			//this.sayConsole(newAgent + " is not reachable");
		} catch (java.lang.IllegalStateException e) {
			// One of the node is null
		}
		
		this.setPathToAgent(null);
		this.setDistance(Integer.MAX_VALUE);
	}
	
	/*
	 * Getters and setters
	 */
	public List<String> getDetectedStench() {
		return detectedStench;
	}
	
	public void setDetectedStench(List<String> newStench) {
		this.detectedStench = newStench;
	}
	
	public void removeDetectedStench(String stench) {
		this.detectedStench.remove(stench);
	}
	
	public String getName() {
		return name;
	}

	public String getLastPosition() {
		return lastPosition;
	}

	public void setLastPosition(String lastPosition) {
		this.lastPosition = lastPosition;
	}

	public String getLastAction() {
		return lastAction;
	}

	public void setLastAction(String lastAction) {
		this.lastAction = lastAction;
	}

	public long getMostRecentTime() {
		return mostRecentTime;
	}

	public void setMostRecentTime(long mostRecenteTime) {
		this.mostRecentTime = mostRecenteTime;
	}

	public int getNbEncounters() {
		return nbEncounters;
	}
	
	public void addNbEncounters(int otherEncounters) {
		this.nbEncounters += otherEncounters;
	}

	public long getMostRecentPing() {
		return mostRecentPing;
	}

	public void setMostRecentPing(long mostRecentPing) {
		this.mostRecentPing = mostRecentPing;
	}

	public List<String> getLastPath() {
		return lastPath;
	}

	public void setLastPath(List<String> lastPath) {
		if (lastPath.size() == 0)	this.lastPath = null;
		else	this.lastPath = lastPath;
	}
	
	public MapRepresentation getMap() {
		return this.map;
	}

	public double getMeetUtility() {
		return meetUtility;
	}

	public void setMeetUtility(double utility) {
		this.meetUtility = utility;
	}
	
	public void addMeetUtility(double moreUtility) {
		this.meetUtility += moreUtility;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		if (distance == 0)	this.distance = Integer.MAX_VALUE;
		else	this.distance = distance;
	}

	public List<String> getPathToAgent() {
		return pathToAgent;
	}

	public void setPathToAgent(List<String> pathToAgent) {
		this.pathToAgent = pathToAgent;
	}
}
