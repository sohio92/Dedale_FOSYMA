package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import dataStructures.serializableGraph.SerializableNode;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ChassB extends SimpleBehaviour{

	private MapRepresentation mymap;
	private boolean finished;
	private String chasseProto = "PingChasseProtocol";

	public ChassB(Agent myAgent, MapRepresentation myMap) {
		// TODO Auto-generated constructor stub
		super(myAgent);
		this.mymap = myMap;
		this.finished = false;
		add_yellow_page("Chasse");
		
	}

	@Override
	public void action() {
		
		//ecoute de msg
		final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		final ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
		String abord = null;
		
		if (msgReceived != null) {
			System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBb");
			System.out.println(msgReceived.getProtocol());
			// Let other behaviours know that an agent was found at the known position
			if (msgReceived.getProtocol().equals(chasseProto)) {
				System.out.println("::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
				abord = msgReceived.getContent();
			}
		}
		
		
		
		
		if (abord != null) {
			System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		}
		//0) init des variable 
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		String lastPosition = myPosition;
		String nextNode=null;
		int time_sleep = 0;
		List<String> listchasseur = get_agent(myAgent, "Chasse");
		// suppr notre agent dans la list
		listchasseur.remove(myAgent.getName());
		System.out.println(listchasseur.size());
		
		List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
		Random r= new Random();
		
		//The move action (if any) should be the last action of your behaviour
		System.out.println(this.myAgent.getLocalName() + " voit " +  lobs + " depui " + ((AbstractDedaleAgent)this.myAgent).getCurrentPosition());

		List<String> list = new ArrayList<String>();
		int remove = 1; // on supprime la place actuel
		for (int i = 1; i < lobs.size(); i++) {
			Couple<String, List<Couple<Observation, Integer>>> couple = lobs.get(i);
			//for (Couple<String, List<Couple<Observation, Integer>>> couple : lobs) {
			if (couple.getRight().size() > 0){
				if (!couple.getLeft().equals(abord)){
					list.add(couple.getRight().get(0).getLeft().getName());
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
				for (String otherAgent: listchasseur) {
					ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
					msg.setSender(this.myAgent.getAID());
					msg.setProtocol(chasseProto);
					msg.setContent(pos);
					System.out.println(((AbstractDedaleAgent)myAgent).getName() + " send to " + otherAgent + " with protocol : "+ msg.getProtocol());
					msg.addReceiver(new AID(otherAgent, AID.ISLOCALNAME));
					((AbstractDedaleAgent)myAgent).sendMessage(msg);
				}
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
	
	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return finished;
	}
	
	
	List<String> get_agent(Agent agent, String service){
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription () ;
        sd.setType(service); // name of the service
        dfd.addServices(sd) ;
        DFAgentDescription[] result = null;
        try {
            result = DFService.search( agent , dfd);
        } catch (FIPAException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //You get the list of all the agents (AID) offering this service
        System.out.println(result.length + "results");
        if (result.length>0)
            System.out.println(result[0].getName());
        List<String> res = new ArrayList<String>();
        for (DFAgentDescription a : result) {
            res.add(a.getName().getName());
        }
        return res;
    }

}
