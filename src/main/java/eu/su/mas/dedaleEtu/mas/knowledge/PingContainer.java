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
	
	// My last action -> Exploration, SeekMeeting, Hunt
	private String lastAction;
	
	// Do I want to meet you?
	private double meetUtility;	
	
	// where is the golem ?
	// null if I don't know
	// == "position" of golem if I know where it is
	private List<String> detectedStench;
	
	public PingContainer(ExploreMultiAgent myAgent, String otherAgent) {
		this.setLastPosition(myAgent.getCurrentPosition());
		
		this.setDetectedStench(myAgent.getStenchAround());
		
		this.setLastAction(myAgent.getBrain().getLastDecision());
		this.setMeetUtility(myAgent.getBrain().getAgentsKnowledge().get(otherAgent).getMeetUtility());
	}

	private void setDetectedStench(List<String> stenchAround) {
		this.detectedStench = stenchAround;
	}
	
	public List<String> getDetectedStench() {
		return detectedStench;
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

	public double getMeetUtility() {
		return meetUtility;
	}

	public void setMeetUtility(double meetUtility) {
		this.meetUtility = meetUtility;
	}
}
