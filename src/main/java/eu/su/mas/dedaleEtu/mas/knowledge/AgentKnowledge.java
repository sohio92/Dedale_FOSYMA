package eu.su.mas.dedaleEtu.mas.knowledge;

import java.util.HashMap;
import java.util.List;

import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.util.leap.Serializable;

public class AgentKnowledge implements Serializable{
	/*
	 * Contains all the knwoledge about an other agent
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
	
	
	private double meetUtility = 0; // How much do you want to meet me ?
	
	public AgentKnowledge(String name) {
		this.name = name;
		this.map = new MapRepresentation(name);
	}
	
	/*
	 * Unpack a MessageContainer, returns true if message is the most recent received from the agent
	 */
	
	public boolean unpackMessage(ACLMessage newMessage) {
		boolean recentMessage = newMessage.getPostTimeStamp() >= this.mostRecentPing;
		
		if (newMessage.getProtocol().equals("PingProtocol")) {
			
			try {
				PingContainer content = (PingContainer) newMessage.getContentObject();
				
				if (recentMessage == true) {
					this.lastAction = content.getLastAction();
					this.lastPosition = content.getLastPosition();
					this.meetUtility = content.getMeetUtility();
				}
				
				if (!this.map.hasNode(lastPosition)) {
					this.map.addNode(lastPosition, MapAttribute.open);
				}
			} catch (UnreadableException e) {e.printStackTrace();}
			
		} else if (newMessage.getProtocol().equals("ShareMapProtocol")) {
			
		}
		
		return recentMessage;
	}
	
	// Adds a new last known path
	public void addNewPath(List<String> newPath) {
		this.map.updateWithPath(newPath);
		this.setLastPath(newPath);
	}
	/*
	 * Getters and setters
	 */
	
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
		this.lastPath = lastPath;
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
}
