package customBehaviours;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;

public class MeetingBehaviour extends SequentialBehaviour{
	private static final long serialVersionUID = -2058134021078011998L;
	
	
	private MapRepresentation myMap;
	
	private ArrayList<String> participants;
	private String meetingTopic;
	
	private int minParticipants; // Minimum number of other agents to start the meeting

	private HashMap<String, Integer> decisionToInt;
	
	private BrainBehaviour brain;
	
	/*
	 * Starts a meeting with other agents
	 */
	public MeetingBehaviour(BrainBehaviour brain) {
		this.brain = brain;
		
		this.myAgent = brain.getAgent();
		
		this.myMap = brain.getMap();
		this.decisionToInt = brain.getDecisionToInt();
		
		this.participants = brain.getParticipants();
		this.meetingTopic = brain.getMeetingTopic();
		
		if (this.meetingTopic == "Meeting") {
			this.minParticipants = 1;
		}
	}

	public void onStart() {
		((ExploreMultiAgent)this.myAgent).sayConsole("I'm going to start a meeting with " + this.participants);
		// The agent notifies other behaviours that it is on a meeting
		((ExploreMultiAgent)this.myAgent).setOnGoingMeeting(true);
		((ExploreMultiAgent)this.myAgent).resetAlreadyCommunicated();
		
		// Add the move to the meeting
		this.addSubBehaviour(new MoveToMeetingBehaviour(this.myAgent, this.myMap, this.participants));
		// Add the confirming participation to the meeting
		this.addSubBehaviour(new ConfirmMeetingBehaviour(this.myAgent, this.participants, this.minParticipants));
		// Add the process of the meeting
		if (this.meetingTopic=="MeetingShareMap") {
			this.addSubBehaviour(new ShareMapBehaviour(this.myAgent, this.myMap, this.participants, this.minParticipants));
		}
	}
	
	public int onEnd() {
		// The agent notifies other behaviours that the meeting ended
		((ExploreMultiAgent)this.myAgent).endMeeting();
		this.brain.reset();
		return this.decisionToInt.get("Decision");
	}
}
