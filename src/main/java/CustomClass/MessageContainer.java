package CustomClass;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

@SuppressWarnings("serial")
public class MessageContainer implements Serializable{
	
	private static final long serialVersionUID = -1333959882640851272L;
	
	
	private SerializableSimpleGraph<String, MapAttribute> sg;
	private ArrayList<String> receivedOpen;
	private HashSet<String> receivedClosed;
	private String intention;
	
	public MessageContainer(SerializableSimpleGraph<String, MapAttribute> sg, ArrayList<String> receivedOpen,
							HashSet<String> receivedClosed, String intention) {
		this.sg = sg;
		this.receivedOpen = receivedOpen;
		this.receivedClosed = receivedClosed;
		this.intention = intention;
	}
	
	public SerializableSimpleGraph<String, MapAttribute> getGraph(){
		return this.sg;
	}
	public ArrayList<String> getOpen(){
		return this.receivedOpen;
	}
	public HashSet<String> getClosed(){
		return this.receivedClosed;
	}
	public String getIntention(){
		return this.intention;
	}
}
