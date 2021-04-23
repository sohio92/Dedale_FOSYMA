package customBehaviours;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import dataStructures.serializableGraph.SerializableNode;
import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

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
public class ExploMultiBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;

	/**
	 * Nodes known but not yet visited
	 */
	private ArrayList<String> openNodes;

	/**
	 * Visited nodes
	 */
	private HashSet<String> closedNodes;
	
	private String nextNode;
	private HashMap<String, Integer> decisionToInt;
	
	private BrainBehaviour brain;

	public ExploMultiBehaviour(BrainBehaviour brain) {
		super(brain.getAgent());
		
		this.brain = brain;
		this.myMap = brain.getMap();
		this.decisionToInt = brain.getDecisionToInt();
		
		this.openNodes = brain.getOpenNodes();
		this.closedNodes = brain.getClosedNodes();
	}

	@Override
	public void action() {
		if(this.myMap.getMigration()==true) {
			((ExploreMultiAgent)this.myAgent).loadAllMaps();
		}
		
		// Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		
		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
			
			// New path
			List<String> nextPath = new ArrayList<String>();
			
			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			try {
				this.myAgent.doWait(500);
			} catch (Exception e) {
				e.printStackTrace();
			}

			//1) remove the current node from openlist and add it to closedNodes.
			this.closedNodes.add(myPosition);
			this.brain.addClosedNodes(myPosition);
			
			this.openNodes.remove(myPosition);
			this.brain.removeOpenNodes(myPosition);

			this.myMap.addNode(myPosition,MapAttribute.closed);

			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			while(iter.hasNext()){
				String nodeId=iter.next().getLeft();
				if (!this.closedNodes.contains(nodeId)){
					if (!this.openNodes.contains(nodeId)){
						this.openNodes.add(nodeId);
						this.brain.addOpenNodes(nodeId);
						this.myMap.addNode(nodeId, MapAttribute.open);
						this.myMap.addEdge(myPosition, nodeId);	
						((ExploreMultiAgent)this.myAgent).addDiffNodes(1);
					}else{
						//the node exist, but not necessarily the edge
						this.myMap.addEdge(myPosition, nodeId);
					}
					((ExploreMultiAgent)this.myAgent).addDiffEdges(1);
					if (nextNode==null) nextNode=nodeId;
				}
			}

			//3) while openNodes is not empty, continues.
			if (this.openNodes.isEmpty()){
				((ExploreMultiAgent)this.myAgent).sayConsole("Exploration successufully done.");			
			}else{
				//4) select next move.
				//4.1 If there exist one open node directly reachable, go for it,
				//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
				if (this.nextNode==null){
					//no directly accessible openNode
					//chose one, compute the path and take the first step.
					try {
						nextPath = this.myMap.getShortestPath(myPosition, this.openNodes.get(0));
						this.nextNode = nextPath.get(0);
					} catch(java.lang.IndexOutOfBoundsException | java.lang.NullPointerException e){
						this.nextNode = myPosition;
					}
				}

				//list of observations associated to the currentPosition
				List<Couple<Observation,Integer>> lObservations= lobs.get(0).getRight();
				//System.out.println(this.myAgent.getLocalName()+" - State of the observations : "+lobs);	
			}
			
		this.brain.setLastPath(nextPath);
		((ExploreMultiAgent)this.myAgent).moveToIntention(this.nextNode, nextPath);
		}
		
	}

	@Override
	public int onEnd() {
		this.brain.reset();
		return this.decisionToInt.get("Decision");
	}
}
