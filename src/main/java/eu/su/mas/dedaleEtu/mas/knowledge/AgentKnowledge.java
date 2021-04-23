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
	
	
	private boolean wantToMeet = false; // Does the agent wants to meet me?
	
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
				this.lastPath = content.getLastPath();
				
				if (recentMessage == true) {
					this.lastAction = content.getLastAction();
					this.lastPosition = content.getLastPosition();
					this.wantToMeet = content.isWantToMeet();
				}
				
				if (!map.hasNode(lastPosition)) {
					map.addNode(lastPosition, MapAttribute.open);
				}
				
				if (!this.lastPath.equals("")) {
					System.out.println("TESTTLSTM");
					for (int i=0; i<this.lastPath.size()-1; i++) {
						this.map.addNode(this.lastPath.get(i), MapAttribute.open);
						this.map.addNode(this.lastPath.get(i+1), MapAttribute.open);
						this.map.addEdge(this.lastPath.get(i), this.lastPath.get(i+1));
					}
				}
			} catch (UnreadableException e) {e.printStackTrace();}
			
		} else if (newMessage.getProtocol().equals("ShareMapProtocol")) {
			
		}
		
		return recentMessage;
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

	public boolean isWantToMeet() {
		return wantToMeet;
	}

	public void setWantToMeet(boolean wantToMeet) {
		this.wantToMeet = wantToMeet;
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
}
