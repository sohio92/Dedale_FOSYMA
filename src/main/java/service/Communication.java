package service;

import java.util.ArrayList;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class Communication {
	
	// inscription a un service
	void log_service(Agent agent, String service) {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd .setName(agent.getAID()); // The agent AID
		ServiceDescription sd = new ServiceDescription () ;
		sd.setType(service); // You have to give a name to each service your agent offers
		sd.setName(agent.getLocalName());//(local)name of the agent
		dfd.addServices(sd) ;
		//Register the service
		try {
			//DFService.deregister(agent, dfd);
			DFService.register(agent, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace(); 
		}
	}
	
	// partie recuperation d'agent
	// resuperer la liste des agent lier a un service
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
		List<String> res = new ArrayList<String>();
		for (DFAgentDescription a : result) {
			res.add(a.getName().getLocalName());
		}
		return res;
	}
	
	// resuperer la liste de tout les agent
	public List <String> getAgentsList(Agent agent){
		// Get the list of all the agents through the yellow pages
		AMSAgentDescription[] agentsDescriptionCatalog = null;
		List <String> agentsNames= new ArrayList<String>();
		try {
			SearchConstraints c = new SearchConstraints();
			c.setMaxResults(new Long(-1));
			agentsDescriptionCatalog = AMSService.search(agent, new
					AMSAgentDescription(), c);
		}
		catch (Exception e) {
			System.err.println("Problem searching AMS: " + e );
			e.printStackTrace();
		}
		for (int i=0; i<agentsDescriptionCatalog.length; i++){
			AID agentID = agentsDescriptionCatalog[i].getName();
			agentsNames.add(agentID.getLocalName());
		}
		return agentsNames;
	}
	
	
	// partie desinscription
	// cas de desinscription d'un service
	public final void logout_service(Agent agent, String service) {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd .setName(agent.getAID()); // The agent AID
		ServiceDescription sd = new ServiceDescription ();
		sd.setType(service); // You have to give a name to each service your agent offers
		sd.setName(agent.getLocalName());//(local)name of the agent
		dfd.addServices(sd);
		//Register the service
		try {
			DFService.deregister(agent, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace(); 
		}
	}
	
	// cas de desinscription de tout les services
	public final void log_out(Agent agent) 
    {
       try { DFService.deregister(agent); }
       catch (Exception e) {}
    }
	
}
