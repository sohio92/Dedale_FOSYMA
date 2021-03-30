package CustomBehaviour;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import CustomClass.MessageContainer;
import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ReceiveMapBehaviour extends TickerBehaviour{
	private static final long serialVersionUID = -2058139161078011998L;
	
	private MapRepresentation myMap;
	private ArrayList<String> openNodes = new ArrayList<String>();
	private HashSet<String> closedNodes = new HashSet<String>();
	private List<String> abandonedNodes;
	
	public ReceiveMapBehaviour(final Agent myagent, MapRepresentation myMap) {
		super(myagent, 1000);
		
		this.myAgent = myagent;
		this.myMap = myMap;
		this.openNodes = ((ExploreMultiAgent)this.myAgent).getOpenNodes();
		this.closedNodes = ((ExploreMultiAgent)this.myAgent).getClosedNodes();
		this.abandonedNodes = ((ExploreMultiAgent)this.myAgent).getAbandonedNodes();
	}

	@Override
	protected void onTick() {
		System.out.println("Listen for a map");
		// TODO Auto-generated method stub
		final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);			

		final ACLMessage msg = this.myAgent.receive(msgTemplate);
		final int compareNames = msg.getSender().getLocalName().compareTo(this.myAgent.getAID().getLocalName());
		if (msg != null &&  compareNames != 0 && msg.getProtocol() == "SharingProtocol") {
			// Get the content
			MessageContainer contenu = null;
			try {
				contenu = (MessageContainer)msg.getContentObject();
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			SerializableSimpleGraph<String, MapAttribute> receivedSg = contenu.getGraph();
			ArrayList<String> receivedOpen = contenu.getOpen();
			HashSet<String> receivedClosed = contenu.getClosed();
			String receivedIntentions = contenu.getIntention();

			// Update the agent's map by mixing the two together
			this.myMap.fuseMap(receivedSg);
			System.out.println("Fused MAP : "+((AbstractDedaleAgent)this.myAgent).getLocalName());

			receivedOpen.removeAll(this.closedNodes);
			this.openNodes.removeAll(receivedClosed);
			
			this.openNodes.addAll(receivedOpen);
			this.closedNodes.addAll(receivedClosed);
			
			// We yield an explorable node to the agent
			this.openNodes.remove(receivedIntentions);
			this.closedNodes.add(receivedIntentions);
			this.abandonedNodes.add(receivedIntentions);
			
			((ExploreMultiAgent)this.myAgent).setAbandonedNodes(this.abandonedNodes);
			
			this.stop();
		}		
	}

}
