package eu.su.mas.dedaleEtu.mas.knowledge;

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
	
	private String lastPosition;
	private String lastAction;
	
	public AgentKnowledge(String name) {
		this.name = name;
		this.map = new MapRepresentation(name);
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
	
}
