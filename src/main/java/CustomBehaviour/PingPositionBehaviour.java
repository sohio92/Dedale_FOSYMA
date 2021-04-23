package CustomBehaviour;

import java.io.IOException;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
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
	private List<String> receivers;

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
		}
		msg.setProtocol("PingProtocol");

		if (myPosition!=""){
			// Sending my position
			msg.setContent(myPosition);

			//Mandatory to use this method (it takes into account the environment to decide if someone is reachable or not)
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		}
	}
	
}
