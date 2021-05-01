package customBehaviours;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.PingContainer;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class PingPositionBehaviour extends CyclicBehaviour{
	
	/**
	 * An agent lets his surroundings know that it is there
	 */
	private static final long serialVersionUID = -5033886006595412971L;
	private HashSet<String> receivers;

	public PingPositionBehaviour(final Agent myagent) {
		this.myAgent = myagent;
		this.receivers = ((ExploreMultiAgent)this.myAgent).getAgentsNames();
	}

	@Override
	public void action() {
		
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		//A message is defined by : a performative, a sender, a set of receivers, (a protocol),(a content (and/or contentOBject))
		ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
		msg.setSender(this.myAgent.getAID());
		
		for (String agentName : this.receivers) {
			msg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
			msg.setProtocol("PingProtocol");
			
			if (myPosition!=""){
				try {
					// Sending a ping container
					PingContainer myPingContainer = new PingContainer((ExploreMultiAgent)this.myAgent, agentName);
					msg.setContentObject(myPingContainer);
					
					//Mandatory to use this method (it takes into account the environment to decide if someone is reachable or not)
					((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				} catch (IOException e) {e.printStackTrace();}
			}
		}
	}
	
}
