package eu.su.mas.dedaleEtu.mas.knowledge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import customBehaviours.BrainBehaviour;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;

import java.io.Serializable;

public class PingContainer implements Serializable {
	
	/**
	 * A ping container containing information held by myAgent about an otherAgent
	 */
	private static final long serialVersionUID = -612646830079332162L;

	// My last position
	private String lastPosition;
	
	// My last action
	private String lastAction;
	
	// My last path
	private List<String> lastPath;
	
	// Do I want to meet you?
	private boolean wantToMeet;
	
	public PingContainer(ExploreMultiAgent myAgent, String otherAgent) {
		this.setLastPosition(myAgent.getCurrentPosition());
		
		this.setLastPath(myAgent.getBrain().getLastPath());
		this.setLastAction(myAgent.getBrain().getLastDecision());
		this.setWantToMeet(myAgent.getBrain().getAgentsKnowledge().get(otherAgent).isWantToMeet());
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
}
