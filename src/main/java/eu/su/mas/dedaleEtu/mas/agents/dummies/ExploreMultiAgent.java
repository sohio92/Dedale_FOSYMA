package eu.su.mas.dedaleEtu.mas.agents.dummies;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.ExploMultiBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ListenBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;

/**
 * <pre>
 * ExploreSolo agent. 
 * It explore the map using a DFS algorithm.
 * It stops when all nodes have been visited.
 *  </pre>
 *  
 * @author hc
 *
 */

public class ExploreMultiAgent extends AbstractDedaleAgent {

	private static final long serialVersionUID = -6431752665590433727L;
	private MapRepresentation myMap;
	private ArrayList<String> openNodes = new ArrayList<String>();
	private HashSet<String> closedNodes = new HashSet<String>();
	private List<String> abandonedNodes;
	private String intentions;
	

	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 
	 * 			1) set the agent attributes 
	 *	 		2) add the behaviours
	 *          
	 */
	protected void setup(){

		super.setup();
	
	    /**
	     * Get the other agents' names
	     */
		List<String> agentNames = getAgentsList();
		
		List<Behaviour> lb=new ArrayList<Behaviour>();
		
		/************************************************
		 * 
		 * ADD the initial behaviours of the Agent here
		 * 
		 ************************************************/
		this.myMap = new MapRepresentation();
		this.myMap.prepareMigration();
		lb.add(new ExploMultiBehaviour(this,this.myMap, this.openNodes, this.closedNodes));
		lb.add(new ListenBehaviour(this, agentNames, this.myMap, this.openNodes, this.closedNodes));
		
		
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */
		
		
		addBehaviour(new startMyBehaviours(this,lb));
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");

	}
	
	private List <String> getAgentsList(){
		AMSAgentDescription[] agentsDescriptionCatalog = null;
		List <String> agentsNames= new ArrayList<String>();
		try {
			SearchConstraints c = new SearchConstraints();
			c.setMaxResults(new Long(-1));
			agentsDescriptionCatalog = AMSService.search(this, new
					AMSAgentDescription(), c);
		}
		catch (Exception e) {
			System.out.println("Problem searching AMS: " + e );
			e.printStackTrace();
		}
		for (int i=0; i<agentsDescriptionCatalog.length; i++){
			AID agentID = agentsDescriptionCatalog[i].getName();
			agentsNames.add(agentID.getLocalName());
		}
		return agentsNames;
	}
	
	public String getIntention() {
		return this.intentions;
	}
	public void setIntention(String newIntention) {
		this.intentions = newIntention;
	}
	
	public ArrayList<String> getOpenNodes(){
		return this.openNodes;
	}
	public void setOpenNodes(ArrayList<String> newOpen){
		this.openNodes = newOpen;
	}
	
	public HashSet<String> getClosedNodes(){
		return this.closedNodes;
	}
	public void setClosedNodes(HashSet<String> newClosed){
		this.closedNodes = newClosed;
	}
	
	public List<String> getAbandonedNodes(){
		return this.abandonedNodes;
	}
	public void setAbandonedNodes(List<String> newAbandon){
		this.abandonedNodes = newAbandon;
	}
}
