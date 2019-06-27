package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;

import dk.ku.cpr.OmicsVisualizer.internal.task.RemoveVisualizationTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.utils.DataUtils;

/**
 * Connection between a CyRootNetwork and a OVTable.
 * The connection holds the mapping between the table and the network collection, and the inner and outer visualizations of the network collection.
 * 
 * @see CyRootNetwork
 * @see OVTable
 * @see OVVisualization
 */
public class OVConnection {
	private OVManager ovManager;
	private OVTable ovTable;
	private CyRootNetwork rootNetwork;
	/** Name of the column from the Cytoscape Network that links to the OVTable */
	private String mappingColCyto;
	/** Name of the column from the OVTable that links to the Cytoscape Network */
	private String mappingColOVTable;
	/** Map that associates the list of table rows with the network node SUID */
	private Map<Long, List<CyRow>> node2table;
	private int nbConnectedTableRows;
	private OVVisualization ovInnerViz;
	private OVVisualization ovOuterViz;

	/**
	 * Creates a connection between a CyRootNetwork and a OVTable.
	 * @param ovManager Omics Visualizer Manager
	 * @param ovTable Table to connect
	 * @param rootNetwork Network collection to connect
	 * @param mappingColCyto Name of the column from the network node table that should be used for the mapping
	 * @param mappingColOVTable Name of the column from the table that should be used for the mapping
	 */
	public OVConnection(OVManager ovManager, OVTable ovTable, CyRootNetwork rootNetwork, String mappingColCyto, String mappingColOVTable) {
		super();
		this.ovManager=ovManager;
		this.ovTable=ovTable;
		this.rootNetwork = rootNetwork;
		this.mappingColCyto = mappingColCyto;
		this.mappingColOVTable = mappingColOVTable;
		this.node2table = new HashMap<>();
		this.ovInnerViz=null;
		this.ovOuterViz=null;
		
		// We register all OVConnection to the OVManager
		this.ovManager.addConnection(this);
		
		this.nbConnectedTableRows = this.updateLinks();
		
		this.ovTable.save();
	}
	
	/**
	 * Returns the connected table.
	 * @return The connected table
	 */
	public OVTable getOVTable() {
		return ovTable;
	}
	
	/**
	 * Returns the connected network collection.
	 * @return The connected network collection
	 */
	public CyRootNetwork getRootNetwork() {
		return rootNetwork;
	}
	
	/**
	 * Returns the network collection's name.
	 * @return The network collection's name
	 */
	public String getCollectionNetworkName() {
		return this.rootNetwork.toString();
	}

	/**
	 * Returns the base network from the connected network collection.
	 * @return The base network
	 */
	public CySubNetwork getBaseNetwork() {
		return this.rootNetwork.getBaseNetwork();
	}

	/**
	 * Returns the column name of the node table from the connected network collection.
	 * This column is used in the mapping.
	 * @return The column name
	 */
	public String getMappingColOVTable() {
		return mappingColOVTable;
	}

	/**
	 * Returns the column name of the connected table used in the mapping.
	 * @return The column name
	 */
	public String getMappingColCyto() {
		return mappingColCyto;
	}
	
	/**
	 * Returns the number of mapped rows from the connected table.
	 * @return The number of mapped rows
	 */
	public int getNbConnectedTableRows() {
		return this.nbConnectedTableRows;
	}
	
	/**
	 * Returns the String used to be stored in the network table.
	 * @return The String representing the connection
	 */
	private String getSavedConnection() {
		return DataUtils.escapeComma(this.ovTable.getTitle())
				+ ","
				+ DataUtils.escapeComma(mappingColCyto)
				+ ","
				+ DataUtils.escapeComma(mappingColOVTable);
	}
	
	/**
	 * Returns the table rows that are mapped to a given network's node table row.
	 * @param netRow The row from the network's node table
	 * @return The table rows mapped to the network node
	 */
	public List<CyRow> getLinkedRows(CyRow netRow) {
		Long suid = netRow.get(CyNetwork.SUID, Long.class);
		List<CyRow> list = this.node2table.get(suid); 
		return (list == null ? new ArrayList<>() : list);
	}
	
	/**
	 * Sets the inner Visualization, then save the visualization in the network table.
	 * @param ovViz The inner Visualization
	 * @param update Should the OVConnection be updated after that
	 */
	public void setInnerVisualization(OVVisualization ovViz, boolean update) {
		this.ovInnerViz=ovViz;
		if(update) {
			this.updateVisualization();
		}
	}
	
	/**
	 * Sets the inner Visualization, then save the visualization in the network table.
	 * @param ovViz The inner Visualization
	 */
	public void setInnerVisualization(OVVisualization ovViz) {
		this.setInnerVisualization(ovViz, true);
	}
	
	/**
	 * Returns the saved inner Visualization.
	 * @return The inner Visualization
	 */
	public OVVisualization getInnerVisualization() {
		return this.ovInnerViz;
	}
	
	/**
	 * Sets the outer Visualization, then save the visualization in the network table.
	 * @param ovViz The outer Visualization
	 * @param update Should the OVConnection be updated after that
	 */
	public void setOuterVisualization(OVVisualization ovViz, boolean update) {
		this.ovOuterViz=ovViz;
		if(update) {
			this.updateVisualization();
		}
	}
	
	/**
	 * Sets the outer Visualization, then save the visualization in the network table.
	 * @param ovViz The outer Visualization
	 */
	public void setOuterVisualization(OVVisualization ovViz) {
		this.setOuterVisualization(ovViz, true);
	}
	
	/**
	 * Returns the saved outer Visualization.
	 * @return The outer Visualization
	 */
	public OVVisualization getOuterVisualization() {
		return this.ovOuterViz;
	}
	
	/**
	 * Returns the key value of a given row.
	 * @param row Row containing the key value
	 * @param keyCol Column containing key values
	 * @return The key value
	 */
	private Object getKey(CyRow row, CyColumn keyCol) {
		if(keyCol.getListElementType() == null) { // this is not a List element
			return row.get(keyCol.getName(), keyCol.getType());
		} else { // this is a list
			return row.getList(keyCol.getName(), keyCol.getListElementType());
		}
	}
	
	/**
	 * Saves inner and outer Visualization.
	 * @see OVVisualization#save()
	 */
	public void updateVisualization() {
		String savedInnerViz = "";
		String savedOuterViz = "";

		if(this.ovInnerViz != null) {
			savedInnerViz = this.ovInnerViz.save();
		}
		if(this.ovOuterViz != null) {
			savedOuterViz = this.ovOuterViz.save();
		}
		
		for(CyNetwork net : this.rootNetwork.getSubNetworkList()) {
			CyTable networkTable = net.getDefaultNetworkTable();
			if(networkTable.getColumn(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_INNERVIZCOL) == null) {
				networkTable.createColumn(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_INNERVIZCOL, String.class, false);
			}
			if(networkTable.getColumn(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_OUTERVIZCOL) == null) {
				networkTable.createColumn(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_OUTERVIZCOL, String.class, false);
			}
			networkTable.getRow(net.getSUID()).set(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_INNERVIZCOL, savedInnerViz);
			networkTable.getRow(net.getSUID()).set(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_OUTERVIZCOL, savedOuterViz);
		}
	}
	
	/**
	 * Update the mapping between the table and the network.
	 * Creates a column in the node table indicating the number of table rows connected to a node.
	 * @return the number of rows from the table that are connected to the network
	 */
	public int updateLinks() {
		this.node2table = new HashMap<>();
		int totalConnectedRows=0;
		
		// FIRST STEP:
		// Make the link in the OVTable
		//
		CyTable nodeTable = this.rootNetwork.getBaseNetwork().getDefaultNodeTable();
		CyColumn keyCytoCol = nodeTable.getColumn(mappingColCyto);
		CyColumn keyOVCol = this.ovTable.getCyTable().getColumn(mappingColOVTable);
		
		if(keyCytoCol == null || keyOVCol == null) {
			return 0; // TODO message?
		}
		
		if(nodeTable.getColumn(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNODETABLE_CONNECTEDCOUNT) == null) {
			nodeTable.createColumn(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNODETABLE_CONNECTEDCOUNT, Integer.class, false);
		}
		
		//for(CyRow netRow : nodeTable.getAllRows()) {
		for(CyNode netNode : this.rootNetwork.getNodeList()) {
			CyRow netRow = this.rootNetwork.getRow(netNode);
			Object netKey = getKey(netRow, keyCytoCol);

			if(netKey == null) {
				continue;
			}
			
			Integer nodeConnectedRows=0;
			for(CyRow tableRow : this.ovTable.getCyTable().getAllRows()) {
				Object tableKey = getKey(tableRow, keyOVCol);
				
				// If the two keys are not the same type, we compare their toString
				boolean equals = false;
				if(keyCytoCol.getType() == keyOVCol.getType()) {
					equals = netKey.equals(tableKey);
				} else {
					equals = netKey.toString().equals(tableKey.toString());
				}
				
				if(equals) {
					this.addLink(netRow, tableRow);
					++totalConnectedRows;
					++nodeConnectedRows;
				}
			}
			
			// We store the number of connected rows to the node in the Node Table so that users can use it in a Cytoscape style
			netRow.set(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNODETABLE_CONNECTEDCOUNT, nodeConnectedRows);
		}
		
		// SECOND STEP:
		// Make the link in the network tables
		//
		for(CyNetwork net : this.rootNetwork.getSubNetworkList()) {
			CyTable networkTable = net.getDefaultNetworkTable();
			if(networkTable.getColumn(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_OVCOL) == null) {
				networkTable.createColumn(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_OVCOL, String.class, false);
			}
			networkTable.getRow(net.getSUID()).set(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_OVCOL, this.getSavedConnection());
		}
		
		return totalConnectedRows;
	}

	/**
	 * Update the Connection by reinitializing it with new mapping columns.
	 * @param mappingColCyto
	 * @param mappingColOVTable
	 * @return the number of connected rows from the table
	 */
	public int update(String mappingColCyto, String mappingColOVTable) {
//		// We update only if something has changed
//		if(!mappingColOVTable.equals(this.mappingColOVTable) || !mappingColCyto.equals(this.mappingColCyto)) {
			this.mappingColOVTable=mappingColOVTable;
			this.mappingColCyto=mappingColCyto;

			this.nbConnectedTableRows = this.updateLinks();
			
			return this.nbConnectedTableRows;
//		}
//
//		return this.nbConnectedTableRows;
	}

	/**
	 * Adds a link between two rows: one from network's node table and one from the data table.
	 * @param networkNode Node from the network's node table
	 * @param tableRow Node from the data table
	 */
	public void addLink(CyRow networkNode, CyRow tableRow) {
		Long suid = networkNode.get(CyNetwork.SUID, Long.class);
		if(!this.node2table.containsKey(suid)) {
			this.node2table.put(suid, new ArrayList<>());
		}

		List<CyRow> tableRows = this.node2table.get(suid);
		tableRows.add(tableRow);
	}
	
	/**
	 * Returns the list of connected network collections.
	 * @return The list of connected network collections
	 */
	private List<CyNetwork> getConnectedNetworks() {
		List<CyNetwork> connectedNetwork = new ArrayList<>();
		
		for(CyNetwork net : this.rootNetwork.getSubNetworkList()) {
			CyTable netTable = net.getDefaultNetworkTable();
			if(netTable != null
					&& netTable.getColumn(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_OVCOL) != null
					&& !netTable.getRow(net.getSUID()).get(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_OVCOL, String.class).isEmpty()) {
				connectedNetwork.add(net);
			}
		}
		
		return connectedNetwork;
	}
	
	/**
	 * Saves the connection information into the network table.
	 * @param network The network where to save the information
	 */
	public void connectNetwork(CyNetwork network) {
		if(!this.rootNetwork.containsNetwork(network)) {
			return;
		}
		
		CyTable networkTable = network.getDefaultNetworkTable();
		
		// Link :
		if(networkTable.getColumn(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_OVCOL) == null) {
			networkTable.createColumn(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_OVCOL, String.class, false);
		}
		networkTable.getRow(network.getSUID()).set(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_OVCOL, this.getSavedConnection());
		
		
		// Visualization :
		if(networkTable.getColumn(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_INNERVIZCOL) == null) {
			networkTable.createColumn(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_INNERVIZCOL, String.class, false);
		}
		if(networkTable.getColumn(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_OUTERVIZCOL) == null) {
			networkTable.createColumn(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_OUTERVIZCOL, String.class, false);
		}
		String savedInnerViz = "";
		String savedOuterViz = "";
		if(this.ovInnerViz != null) {
			savedInnerViz = this.ovInnerViz.save();
		}
		if(this.ovOuterViz != null) {
			savedOuterViz = this.ovOuterViz.save();
		}
		networkTable.getRow(network.getSUID()).set(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_INNERVIZCOL, savedInnerViz);
		networkTable.getRow(network.getSUID()).set(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_OUTERVIZCOL, savedOuterViz);
	}

	/**
	 * Erase the connection between the connected table and all the networks from the connected network collection.
	 */
	public void disconnect() {
		for(CyNetwork net : this.rootNetwork.getSubNetworkList()) {
			this.disconnectNetwork(net);
		}
	}
	
	/**
	 * Disconnect a specific network from the connected network collection.
	 * It erases the visualizations and all Omics Visualizer specific columns from the different network tables.
	 * @param network The network to be disconnected
	 */
	public void disconnectNetwork(CyNetwork network) {
		// We make sure that the network is in the collection of the OVConnection
		if(!this.rootNetwork.containsNetwork(network)) {
			return;
		}
		
		// We erase the link in the Network table
		CyTable networkTable = network.getDefaultNetworkTable();
		if(networkTable.getColumn(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_OVCOL) != null) {
			networkTable.getRow(network.getSUID()).set(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_OVCOL, "");
		}
		if(networkTable.getColumn(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_INNERVIZCOL) != null) {
			networkTable.getRow(network.getSUID()).set(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_INNERVIZCOL, "");
		}
		if(networkTable.getColumn(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_OUTERVIZCOL) != null) {
			networkTable.getRow(network.getSUID()).set(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_OUTERVIZCOL, "");
		}
		
		// We erase the Visualization
		if(this.getInnerVisualization() != null) {
			RemoveVisualizationTaskFactory factory = new RemoveVisualizationTaskFactory(ovManager, this);
			this.ovManager.executeTask(factory.createTaskIterator());
		}
		
		// If it was the last network of the collection ...
		if(this.getConnectedNetworks().size() == 0) {
			// We delete the visualization columns in the node table
			OVShared.deleteOVColumns(this.getBaseNetwork().getDefaultNodeTable());
			
			// We forget about this connection
			this.ovManager.removeConnection(this);
			
			this.ovTable.save();
		}
		
		if(this.ovManager.getOVCytoPanel() != null) {
			this.ovManager.getOVCytoPanel().update();
		}
	}

	@Override
	public boolean equals(Object o) {
		if(o == null) {
			return false;
		}

		if(!(o instanceof OVConnection)) {
			return false;
		}

		OVConnection con = (OVConnection)o;

		return con.mappingColCyto.equals(this.mappingColCyto) &&
				con.mappingColOVTable.equals(this.mappingColOVTable) &&
				con.rootNetwork.equals(this.rootNetwork);
	}
}
