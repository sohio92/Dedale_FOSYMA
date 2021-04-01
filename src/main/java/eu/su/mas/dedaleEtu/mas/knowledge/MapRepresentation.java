package eu.su.mas.dedaleEtu.mas.knowledge;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.Viewer.CloseFramePolicy;

import dataStructures.serializableGraph.*;
import javafx.application.Platform;

/**
 * <pre>
 * This simple topology representation only deals with the graph, not its content.
 * The knowledge representation is not well written (at all), it is just given as a minimal example.
 * The viewer methods are not independent of the data structure, and the dijkstra is recomputed every-time.
 * </pre>
 * @author hc
 */
public class MapRepresentation implements Serializable {

	/**
	 * A node is open, closed, or agent
	 * @author hc
	 *
	 */

	public enum MapAttribute {
		agent,open,closed
	}

	private static final long serialVersionUID = -1333959882640838272L;

	/*********************************
	 * Parameters for graph rendering
	 ********************************/

	private String defaultNodeStyle= "node {"+"fill-color: black;"+" size-mode:fit;text-alignment:under; text-size:14;text-color:white;text-background-mode:rounded-box;text-background-color:black;}";
	private String nodeStyle_open = "node.agent {"+"fill-color: forestgreen;"+"}";
	private String nodeStyle_agent = "node.open {"+"fill-color: blue;"+"}";
	private String nodeStyle=defaultNodeStyle+nodeStyle_agent+nodeStyle_open;

	private Graph g; //data structure non serializable
	private Viewer viewer; //ref to the display,  non serializable
	private Integer nbEdges;//used to generate the edges ids

	private SerializableSimpleGraph<String, MapAttribute> sg;//used as a temporary dataStructure during migration
	private Boolean isMigrating = false;
		
	// Owner of the map
	private String ownerName;
	// Last known position of the owner
	private String currentPosition;
	// How different the map is to that of "me"
	private int diffEdge = 0;
	private int diffNodes = 0;

	public MapRepresentation(String ownerName) {
		System.setProperty("org.graphstream.ui", "javafx");
		this.g= new SingleGraph("My world vision");
		this.g.setAttribute("ui.stylesheet",nodeStyle);
		this.sg = new SerializableSimpleGraph<String,MapAttribute>();
		this.nbEdges=0;
		this.ownerName = ownerName;
	}

	/**
	 * Add or replace a node and its attribute 
	 * @param id Id of the node
	 * @param mapAttribute associated state of the node
	 */
	public void addNode(String id,MapAttribute mapAttribute){
		Node n;
		if (this.g.getNode(id)==null){
			n=this.g.addNode(id);
		}else{
			n=this.g.getNode(id);
		}
		n.clearAttributes();
		n.setAttribute("ui.class", mapAttribute.toString());
		n.setAttribute("ui.label",id);
		
		this.sg.addNode(n.getId(), mapAttribute);
	}
	
	public void updateNode(String id,MapAttribute mapAttribute){
		Node n;
		if (this.g.getNode(id)==null){
			this.addNode(id, mapAttribute);
		}else{
			n=this.g.getNode(id);

			if (mapAttribute != null) {
				if (mapAttribute.toString() == MapAttribute.closed.toString() || mapAttribute.toString() == MapAttribute.agent.toString()) {
					n.clearAttributes();
					n.setAttribute("ui.class", mapAttribute.toString());
					this.sg.addNode(n.getId(), mapAttribute);
					n.setAttribute("ui.label",id);
				}
			}
		}
	}

	/**
	 * Add the edge if not already existing.
	 * @param idNode1 one side of the edge
	 * @param idNode2 the other side of the edge
	 */
	public void addEdge(String idNode1,String idNode2){
		try {
			this.nbEdges++;
			this.g.addEdge(this.nbEdges.toString(), idNode1, idNode2);
			this.sg.addEdge(this.nbEdges.toString(), idNode1, idNode2);
		}catch (IdAlreadyInUseException e1) {
			System.err.println("ID existing");
			System.exit(1);
		}catch (EdgeRejectedException e2) {
			//System.err.println("ajout arrete echoué "+e);
			this.nbEdges--;
		} catch(ElementNotFoundException e3){
			
		}
	}	
	/**
	 * Fuse two maps together
	 */
	public void fuseMap(SerializableSimpleGraph<String, MapAttribute> sg2) {

		for (SerializableNode<String, MapAttribute> n: sg2.getAllNodes()){
			this.updateNode(n.getNodeId(), n.getNodeContent());	
		}

		for (SerializableNode<String, MapAttribute> n: sg2.getAllNodes()){
			for(String s:sg2.getEdges(n.getNodeId())){
				this.addEdge(n.getNodeId(),s);
			}
		}
		
		// The fused map is now identical to "me" map
		this.diffEdge = 0;
		this.diffNodes = 0;
	}
	
	/*
	 * Gives a serializable version of the graph
	 */
	public SerializableSimpleGraph<String, MapAttribute> getSg(){
		this.sg= new SerializableSimpleGraph<String,MapAttribute>();
        Iterator<Node> iter=this.g.iterator();
        while(iter.hasNext()){
            Node n=iter.next();
            sg.addNode(n.getId(),MapAttribute.valueOf((String)n.getAttribute("ui.class")));
        }
        Iterator<Edge> iterE=this.g.edges().iterator();
        while (iterE.hasNext()){
            Edge e=iterE.next();
            Node sn=e.getSourceNode();
            Node tn=e.getTargetNode();
            sg.addEdge(e.getId(), sn.getId(), tn.getId());
        } 
		return this.sg;
	}
	/**
	 * Compute the shortest Path from idFrom to IdTo. The computation is currently not very efficient
	 * 
	 * @param idFrom id of the origin node
	 * @param idTo id of the destination node
	 * @return the list of nodes to follow
	 */
	public List<String> getShortestPath(String idFrom,String idTo){
		List<String> shortestPath=new ArrayList<String>();

		Dijkstra dijkstra = new Dijkstra();//number of edge
		dijkstra.init(g);
		dijkstra.setSource(g.getNode(idFrom));
		dijkstra.compute();//compute the distance to all nodes from idFrom
		List<Node> path=dijkstra.getPath(g.getNode(idTo)).getNodePath(); //the shortest path from idFrom to idTo
		Iterator<Node> iter=path.iterator();
		while (iter.hasNext()){
			shortestPath.add(iter.next().getId());
		}
		dijkstra.clear();
		shortestPath.remove(0);//remove the current position
		return shortestPath;
	}

	/**
	 * Before the migration we kill all non serializable components and store their data in a serializable form
	 */
	public void prepareMigration(){
		this.sg= new SerializableSimpleGraph<String,MapAttribute>();
		Iterator<Node> iter=this.g.iterator();
		while(iter.hasNext()){
			Node n=iter.next();
			sg.addNode(n.getId(),(MapAttribute)n.getAttribute("ui.class"));
		}
		Iterator<Edge> iterE=this.g.edges().iterator();
		while (iterE.hasNext()){
			Edge e=iterE.next();
			Node sn=e.getSourceNode();
			Node tn=e.getTargetNode();
			sg.addEdge(e.getId(), sn.getId(), tn.getId());
		}

		closeGui();

		this.g=null;
		this.isMigrating = true;

	}

	/**
	 * After migration we load the serialized data and recreate the non serializable components (Gui,..)
	 */
	public void loadSavedData(){

		this.g= new SingleGraph("My world vision");
		this.g.setAttribute("ui.stylesheet",nodeStyle);

		openGui();

		Integer nbEd=0;
		for (SerializableNode<String, MapAttribute> n: this.sg.getAllNodes()){
			this.g.addNode(n.getNodeId()).setAttribute("ui.class", n.getNodeContent().toString());
			for(String s:this.sg.getEdges(n.getNodeId())){
				this.g.addEdge(nbEd.toString(),n.getNodeId(),s);
				nbEd++;
			}
		}
		System.out.println("Loading done");
		this.isMigrating = false;
	}

	/**
	 * Method called before migration to kill all non serializable graphStream components
	 */
	private void closeGui() {
		//once the graph is saved, clear non serializable components
		if (this.viewer!=null){
			try{
				this.viewer.close();
			}catch(NullPointerException e){
				System.err.println("Bug graphstream viewer.close() work-around - https://github.com/graphstream/gs-core/issues/150");
			}
			this.viewer=null;
		}
	}

	/**
	 * Method called after a migration to reopen GUI components
	 */
	public void testGui() {
		if (this.viewer==null && this.ownerName == "me")
			openGui();
	}
	private void openGui() {
		this.viewer =new FxViewer(this.g, FxViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);////GRAPH_IN_GUI_THREAD);
		viewer.enableAutoLayout();
		viewer.setCloseFramePolicy(FxViewer.CloseFramePolicy.CLOSE_VIEWER);
		viewer.addDefaultView(true);
		g.display();
	}
	
	public Boolean getMigration() {
		return this.isMigrating;
	}
	
	public String getOwner() {
		return this.ownerName;
	}
	public String getCurrentPosition() {
		return this.currentPosition;
	}
	public void setCurrentPosition(String newPosition) {
		this.currentPosition = newPosition;
	}
	
	public int getDiffEdges(){
		return this.diffEdge;
	}
	public int getDiffNodes(){
		return this.diffNodes;
	}
	public void addDiffEdges(int increment) {
		this.diffEdge += increment;
	}
	public void addDiffNodes(int increment) {
		this.diffNodes += increment;
	}
	public SerializableSimpleGraph<String, MapAttribute> getMissingFromMap(MapRepresentation otherMap){
		// Returns the nodes and edges that the current map lacks
		SerializableSimpleGraph<String,MapAttribute> missingSg = new SerializableSimpleGraph<String,MapAttribute>();
		SerializableSimpleGraph<String, MapAttribute> otherSg = otherMap.getSg();
		this.sg = this.getSg();
		
		// Adding the missing nodes
		Set<SerializableNode<String, MapAttribute>> otherNodes = otherSg.getAllNodes();
		for (SerializableNode<String, MapAttribute> n: this.sg.getAllNodes()){
			if (!otherNodes.contains(n)) {
				missingSg.addNode(n.getNodeId(),n.getNodeContent());
			}
		}
		
		// Adding the missing edges
		Integer nbEd = 0;
		Set<SerializableNode<String, MapAttribute>> missingNodes = missingSg.getAllNodes();
		//	Iterating over all known nodes
		for (SerializableNode<String, MapAttribute> n: this.sg.getAllNodes()){
			Set<String> otherEdge = otherSg.getEdges(n.getNodeId());
			//	Iterating over all known edges
			for(String s: this.sg.getEdges(n.getNodeId())){
				// If the edge is missing
				if (!otherEdge.contains(s)) {
					// If the node from the edge isn't missing but its edges are incomplete we add the node to missingSg
					if (!missingNodes.contains(n)) {
						missingSg.addNode(n.getNodeId());
					}
					// We add the edge
					missingSg.addEdge(nbEd.toString(),n.getNodeId() ,s);
					nbEd ++;
				}
				
			}
		}
		return missingSg;
	}
	
	public int getNbNodes() {
		return this.g.getNodeCount();
	}
	
	public int getNbEdges() {
		return this.g.getEdgeCount();
	}
	
	public void updateIgnorance(MapRepresentation otherMap) {
		// Updates the information about the owner's supposed ignorance
		this.diffNodes = otherMap.getNbNodes() - this.g.getNodeCount();
		this.diffEdge = otherMap.getNbEdges() - this.g.getEdgeCount();
	}
}