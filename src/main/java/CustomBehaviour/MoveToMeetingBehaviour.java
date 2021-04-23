package CustomBehaviour;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;

public class MoveToMeetingBehaviour extends SimpleBehaviour {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6190520842356490984L;

	/*
	 * An agent moves towards the location of a meeting
	 */
	private boolean finished = false;
	
	private MapRepresentation myMap;
	private ArrayList<String> participants;
	
	private String myPosition;
	private String meetingPlace;
	
	public MoveToMeetingBehaviour(final Agent myagent, MapRepresentation myMap, ArrayList<String> participants) {
		this.myAgent = myagent;
		this.myMap = myMap;
		this.participants = participants;
	}
	
	public void onStart() {
		this.myPosition = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		
		// Get the closest node to all the participants as MeetingPlace
		// yet to implement
		// For now we only get closer by 1 to the lowest id agent
		String lowestName = "";
		for (String otherAgent: this.participants) {
			for (String otherAgent2: this.participants) {
				if (otherAgent.compareTo(otherAgent2) >= 0) {
					lowestName = otherAgent2;
				}
			}
		}
		
		this.meetingPlace = ((ExploreMultiAgent)this.myAgent).getMyKnowledge(lowestName).getLastPosition();
		((ExploreMultiAgent)this.myAgent).sayConsole("The meeting is going to take place at " + this.meetingPlace);
	}
	
	@Override
	public void action() {
		// Compute the path to the meeting place
		List<String> path = new ArrayList<String>();
		this.myPosition = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		try {
			path = this.myMap.getShortestPath(this.myPosition, this.meetingPlace);
		} catch (java.lang.NullPointerException e) {
			// The meeting place is undiscovered
			((ExploreMultiAgent)this.myAgent).sayConsole("I don't know how to get to the meeting place. I'll stay here.");
			this.finished = true;
		}
		
		// Terminates if arrived
		if (path.size() == 0) {
			this.finished = true;
		} else {
			// Move towards the destination
			String nextNode = path.get(0);
			
			try {
				((ExploreMultiAgent)this.myAgent).setIntention(nextNode);
				((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
				
				// Terminates if stuck
				String newPosition = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
				if (myPosition.equals(newPosition)) {
					((ExploreMultiAgent)this.myAgent).sayConsole("I arrived at the meeting place.");
					this.finished = true;
				}
				this.myPosition = newPosition;	
			} catch(java.lang.RuntimeException e) {
				// Tried to reach an illegal position (? cause unknown)
				((ExploreMultiAgent)this.myAgent).sayConsole(this.myPosition + "  " + nextNode);
				e.printStackTrace();
				this.finished = true;
			}
		}

	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return this.finished;
	}

}
