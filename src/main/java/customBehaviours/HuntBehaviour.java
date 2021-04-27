package customBehaviours;

import java.io.IOException;
import java.util.ArrayList;
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
import eu.su.mas.dedaleEtu.mas.knowledge.AgentKnowledge;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class HuntBehaviour extends OneShotBehaviour{
	
	/**
	 * Hunts the golem
	 */
	private static final long serialVersionUID = 8407533481381014005L;

	private BrainBehaviour brain;
	
	private String chasseProto = "PingChasseProtocol";
	private HashMap<String, Integer> decisionToInt;

	public HuntBehaviour(BrainBehaviour brain) {
		this.brain = brain;
		this.myAgent = brain.getAgent();
		this.decisionToInt = brain.getDecisionToInt();
		
	}

	@Override
	public void action() {
		if(this.brain.getMap().getMigration()==true) {
			((ExploreMultiAgent)this.myAgent).loadAllMaps();
		}
		
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		String lastPosition = myPosition;
		String nextNode=null;
		
		int time_sleep = 0;
		
		// Who is currently hunting with me?
		HashSet<String> hunters = new HashSet<String>();
		for (AgentKnowledge otherKnowledge: this.brain.getAgentsKnowledge().values()) {
			if (otherKnowledge.getLastAction().equals("Hunt"))	hunters.add(otherKnowledge.getName());
		}

		HashSet<String> golemStench = ((ExploreMultiAgent)this.myAgent).getStenchAround();
		
		// à partir du moment du moment où on détecte, on commence un historique
		
		// tu vas essayer de ne jamais 
		
		// container qu'est-ce qu'on sait des golems
		// list de positons possibles
		// à partir de Stench, on va ajouter des positions possibles (qui seront elles mêmes envoyées
		// de manière régulière aux autres agents via un Ping)
		
		//The move action (if any) should be the last action of your behaviour
		List<String> list = new ArrayList<String>();
		for (int i = 1; i < lobs.size(); i++) {
			Couple<String, List<Couple<Observation, Integer>>> couple = lobs.get(i);
			//for (Couple<String, List<Couple<Observation, Integer>>> couple : lobs) {
			if (couple.getRight().size() > 0){
				if (!couple.getLeft().equals(abord)){
					list.add(couple.getRight().get(0).getLeft().getName());
					[    ]
				}
				else {
					remove += 1;
				}
			}
			else {
				list.add(null);
			}
		}
		
		int index = list.indexOf("Stench");
		//ne sent rien autour de lui
		if (index==-1) {
			/*removing the current position from the list of target, not necessary as 
			 * to stay is an action but allow quicker random move
			 */
			System.out.println("ATCHOUM !!!!!!!!!!!!!!!!!!");
			int moveId=1+r.nextInt(lobs.size()-1);
			((AbstractDedaleAgent)this.myAgent).moveTo(lobs.get(moveId).getLeft());
		}
		else {
			System.out.println("JE SENS!!!!!!!!!!!!!!!!!!");
			System.out.println("jai suprimer "+ (remove - 1) + "deplacement ");
			//ping la futur pos
			String pos=lobs.get(index+remove).getLeft();

			
			
			
			//A message is defined by : a performative, a sender, a set of receivers, (a protocol),(a content (and/or contentOBject))
			if (pos!=""){
				// Sending my position

				//Mandatory to use this method (it takes into account the environment to decide if someone is reachable or not)
				ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
				msg.setSender(this.myAgent.getAID());
				msg.setProtocol(chasseProto);
				
				for (String otherAgent: listchasseur) {
					System.out.println(((AbstractDedaleAgent)myAgent).getName() + " send to " + otherAgent + " with protocol : "+ msg.getProtocol());
					msg.addReceiver(new AID(otherAgent, AID.ISLOCALNAME));
				}
				msg.setContent(pos);
				((AbstractDedaleAgent)myAgent).sendMessage(msg);
			}

			((AbstractDedaleAgent)this.myAgent).moveTo(pos);
		}
		try {
			Thread.sleep(time_sleep);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/*
	void add_yellow_page(String service){
		DFAgentDescription dfd = new DFAgentDescription();
		dfd .setName(this.myAgent.getAID()); // The agent AID
		ServiceDescription sd = new
				ServiceDescription () ;
		sd.setType(service); // You have to give a name to each service your agent offers
		sd.setName(this.myAgent.getLocalName());//(local)name of the agent
		dfd.addServices(sd) ;
		//Register the service
		try {
			DFService.register(this.myAgent, dfd);
		} catch (FIPAException fe) {
			fe . printStackTrace () ; }
	}
	*/
	
	@Override
	public int onEnd() {
		this.brain.deregisterState("Hunt");
		
		this.brain.registerState(new HuntBehaviour(this.brain), "Hunt");
		
		this.brain.registerTransition("Decision", "Hunt", (int) this.decisionToInt.get("Hunt"));
		this.brain.registerTransition("Hunt", "Decision", (int) this.decisionToInt.get("Decision"));
		
		return this.decisionToInt.get("Decision");
	}

}
