package CustomBehaviour;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;

public class MeetingBehaviour extends ParallelBehaviour{
	private static final long serialVersionUID = -2058134021078011998L;
	
	private List<String> receivers;
	private MapRepresentation myMap;
	
	private String meetingPlace;
	private String meetingTopic;
	
	private ArrayList<String> openNodes;
	private HashSet<String> closedNodes;
	
	/*
	 * Starts a meeting with other agents
	 */
	public MeetingBehaviour(final Agent myagent, MapRepresentation myMap, ArrayList<String> openNodes, HashSet<String> closedNodes,
							List<String> receivers, String meetingPlace, String meetingTopic) {
		this.myAgent = myagent;
		this.myMap = myMap;
		this.receivers = receivers;
		this.meetingPlace = meetingPlace;
		this.meetingTopic = meetingTopic;
		this.openNodes = openNodes;
		this.closedNodes = closedNodes;
		
		String myPosition = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		try {
			((AbstractDedaleAgent)this.myAgent).moveTo(this.myMap.getShortestPath(myPosition, meetingPlace).get(0));
		} catch (java.lang.NullPointerException e) {
			System.out.println("Pas de chemin");
		}
		
		System.out.println("Starting a new Meeting");
		// Adding the sharing behaviour
		if (this.meetingTopic=="ShareMap") {
			this.addSubBehaviour(new ShareMapBehaviour(this.myAgent, this.myMap, this.openNodes, this.closedNodes, this.receivers));
			this.addSubBehaviour(new ReceiveMapBehaviour(this.myAgent, this.myMap));
		}
	}
}
