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
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;

public class ChassB extends SimpleBehaviour{

	private MapRepresentation mymap;
	private boolean finished;

	public ChassB(Agent myAgent, MapRepresentation myMap) {
		// TODO Auto-generated constructor stub
		super(myAgent);
		this.mymap = myMap;
		this.finished = false;
	}

	@Override
	public void action() {
		
		//0) init des variable 
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		String lastPosition = myPosition;
		String nextNode=null;
		int time_sleep = 0;
		
		//deplacement aleatoire
		if (this.myAgent.getLocalName() == "Golem"){
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
			Random r= new Random();
			int moveId=1+r.nextInt(lobs.size()-1);//removing the current position from the list of target, not necessary as to stay is an action but allow quicker random move
	
			//The move action (if any) should be the last action of your behaviour
			((AbstractDedaleAgent)this.myAgent).moveTo(lobs.get(moveId).getLeft());
			try {
				Thread.sleep(time_sleep);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
			Random r= new Random();
			int moveId=1+r.nextInt(lobs.size()-1);//removing the current position from the list of target, not necessary as to stay is an action but allow quicker random move
	
			//The move action (if any) should be the last action of your behaviour
			System.out.println(this.myAgent.getLocalName() + " voit " +  lobs + " depui " + ((AbstractDedaleAgent)this.myAgent).getCurrentPosition());
			
			List<String> list = new ArrayList<String>();
			for (int i = 1; i < lobs.size(); i++) {
				Couple<String, List<Couple<Observation, Integer>>> couple = lobs.get(i);
			//for (Couple<String, List<Couple<Observation, Integer>>> couple : lobs) {
				if (couple.getRight().size() >0){
					list.add(couple.getRight().get(0).getLeft().getName());
				}
				else {
					list.add(null);
				}
			}
			int index = list.indexOf("Stench");
			//ne sent rien autour de lui
			if (index==-1) {
				((AbstractDedaleAgent)this.myAgent).moveTo(lobs.get(moveId).getLeft());
			}
			else {
				System.out.println("JE SENS!!!!!!!!!!!!!!!!!!");
				((AbstractDedaleAgent)this.myAgent).moveTo(lobs.get(index+1).getLeft());
			}
			try {
				Thread.sleep(time_sleep);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return finished;
	}

}
