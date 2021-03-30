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
	
	/*
	 * Starts a meeting with other agents
	 */
	public MeetingBehaviour(final Agent myagent, MapRepresentation myMap, 
							List<String> receivers, String meetingPlace, String meetingTopic) {
		this.myAgent = myagent;
		this.myMap = myMap;
		this.receivers = receivers;
		this.meetingPlace = meetingPlace;
		this.meetingTopic = meetingTopic;
		
		String myPosition = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		try {
			((AbstractDedaleAgent)this.myAgent).moveTo(this.myMap.getShortestPath(myPosition, meetingPlace).get(0));
		} catch (java.lang.NullPointerException e) {
			System.out.println("Pas de chemin");
		}
		
		
		// Adding the sharing behaviour
		if (this.meetingTopic=="ShareMap") {
			new ShareMapBehaviour(this.myAgent, this.myMap, ((ExploreMultiAgent)this.myAgent).getOpenNodes(), ((ExploreMultiAgent)this.myAgent).getClosedNodes(), this.receivers);
			new ReceiveMapBehaviour()
		}
		// Adding the receiving behaviour
	}
}
