package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import CustomClass.MessageContainer;

/**
 * This behaviour allows an agent to explore the environment and learn the associated topological map.
 * The algorithm is a pseudo - DFS computationally consuming because its not optimised at all.</br>
 * 
 * When all the nodes around him are visited, the agent randomly select an open node and go there to restart its dfs.</br> 
 * This (non optimal) behaviour is done until all nodes are explored. </br> 
 * 
 * Warning, this behaviour does not save the content of visited nodes, only the topology.</br> 
 * Warning, this behaviour is a solo exploration and does not take into account the presence of other agents (or well) and indefinitely tries to reach its target node
 * @author hc
 *
 */
public class ExploMultiBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	private boolean finished = false;

	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;

	/**
	 * Nodes known but not yet visited
	 */
	private List<String> openNodes;
	/**
	 * Nodes yielded to other agents
	 */
	private List<String> abandonedNodes;
	/**
	 * Visited nodes
	 */
	private Set<String> closedNodes;


	public ExploMultiBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, ArrayList<String> openNodes, HashSet<String> closedNodes) {
		super(myagent);
		this.myMap=myMap;
		this.openNodes=openNodes;
		this.closedNodes=closedNodes;
		
		this.abandonedNodes=new ArrayList<String>();
	}

	@Override
	public void action() {

		if(this.myMap==null)
			this.myMap= new MapRepresentation();
		
		//0) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
	
		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition

			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			try {
				this.myAgent.doWait(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// Try to receive another agent's map
			final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);			

			final ACLMessage msg = this.myAgent.receive(msgTemplate);
			if (msg != null) {
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
				
				// Update our nodes
				receivedOpen.removeAll(this.closedNodes);
				this.openNodes.removeAll(receivedClosed);
				
				this.openNodes.addAll(receivedOpen);
				this.closedNodes.addAll(receivedClosed);
				
				// We yield an explorable node to the agent
				this.openNodes.remove(receivedIntentions);
				this.closedNodes.add(receivedIntentions);
				this.abandonedNodes.add(receivedIntentions);
			}

			//1) remove the current node from openlist and add it to closedNodes.
			this.closedNodes.add(myPosition);
			this.openNodes.remove(myPosition);

			this.myMap.addNode(myPosition,MapAttribute.closed);

			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			String nextNode=null;
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			while(iter.hasNext()){
				String nodeId=iter.next().getLeft();
				if (!this.closedNodes.contains(nodeId)){
					if (!this.openNodes.contains(nodeId)){
						this.openNodes.add(nodeId);
						this.myMap.addNode(nodeId, MapAttribute.open);
						this.myMap.addEdge(myPosition, nodeId);	
					}else{
						//the node exist, but not necessarily the edge
						this.myMap.addEdge(myPosition, nodeId);
					}
					if (nextNode==null) nextNode=nodeId;
				}
			}

			//3) while openNodes is not empty, continues.
			if (this.openNodes.isEmpty()){
				// Explore the nodes that were yielded so that we don't leave any doubts regarding the explorable parts of the map
				if (!this.abandonedNodes.isEmpty()) {
					this.openNodes.addAll(this.abandonedNodes);
					this.closedNodes.removeAll(this.abandonedNodes);
					this.abandonedNodes = new ArrayList<String>();
					System.out.println("Roaming the yielded parts, removing doubt");	
				}else {
					//Explo finished
					finished=true;
					System.out.println("Exploration successufully done, behaviour removed.");	
				}				
			}else{
				//4) select next move.
				//4.1 If there exist one open node directly reachable, go for it,
				//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
				if (nextNode==null){
					//no directly accessible openNode
					//chose one, compute the path and take the first step.
					nextNode=this.myMap.getShortestPath(myPosition, this.openNodes.get(0)).get(0);
				}

				//list of observations associated to the currentPosition
				List<Couple<Observation,Integer>> lObservations= lobs.get(0).getRight();
				System.out.println(this.myAgent.getLocalName()+" - State of the observations : "+lobs);
				
				((ExploreMultiAgent)this.myAgent).setIntention(nextNode);
				((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			}

		}
	}

	@Override
	public boolean done() {
		return finished;
	}

}
