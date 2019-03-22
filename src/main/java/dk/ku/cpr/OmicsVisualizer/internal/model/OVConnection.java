package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;

import dk.ku.cpr.OmicsVisualizer.internal.task.RemoveStyleTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.utils.DataUtils;

public class OVConnection {
	public static final double MINIMUM_CONNECTED_ROWS = 0.5;
	
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
	private OVStyle ovStyle;

	public OVConnection(OVManager ovManager, OVTable ovTable, CyRootNetwork rootNetwork, String mappingColCyto, String mappingColOVTable) {
		super();
		this.ovManager=ovManager;
		this.ovTable=ovTable;
		this.rootNetwork = rootNetwork;
		this.mappingColCyto = mappingColCyto;
		this.mappingColOVTable = mappingColOVTable;
		this.node2table = new HashMap<>();
		this.ovStyle=null;
		
		// We register all OVConnection to the OVManager
		this.ovManager.addConnection(this);
		
		this.nbConnectedTableRows = this.updateLinks();
		
		this.ovTable.save();
	}
	
	public OVTable getOVTable() {
		return ovTable;
	}
	
	public CyRootNetwork getRootNetwork() {
		return rootNetwork;
	}
	
	public String getCollectionNetworkName() {
		return this.rootNetwork.toString();
	}

	public CySubNetwork getBaseNetwork() {
		return this.rootNetwork.getBaseNetwork();
	}

	public String getMappingColOVTable() {
		return mappingColOVTable;
	}

	public String getMappingColCyto() {
		return mappingColCyto;
	}
	
	public int getNbConnectedTableRows() {
		return this.nbConnectedTableRows;
	}
	
	private String getSavedConnection() {
		return DataUtils.escapeComma(this.ovTable.getTitle())
				+ ","
				+ DataUtils.escapeComma(mappingColCyto)
				+ ","
				+ DataUtils.escapeComma(mappingColOVTable);
	}
	
	public List<CyRow> getLinkedRows(CyRow netRow) {
		Long suid = netRow.get(CyNetwork.SUID, Long.class);
		List<CyRow> list = this.node2table.get(suid); 
		return (list == null ? new ArrayList<>() : list);
	}
	
	public void setStyle(OVStyle ovStyle) {
		this.ovStyle=ovStyle;
		this.updateStyle();
	}
	
	public OVStyle getStyle() {
		return this.ovStyle;
	}
	
	private Object getKey(CyRow row, CyColumn keyCol) {
		if(keyCol.getListElementType() == null) { // this is not a List element
			return row.get(keyCol.getName(), keyCol.getType());
		} else { // this is a list
			return row.getList(keyCol.getName(), keyCol.getListElementType());
		}
	}
	
	public void updateStyle() {
		String savedStyle = "";
		if(this.ovStyle != null) {
			savedStyle = this.ovStyle.save();
		}
		
		for(CyNetwork net : this.rootNetwork.getSubNetworkList()) {
			CyTable networkTable = net.getDefaultNetworkTable();
			if(networkTable.getColumn(OVShared.CYNETWORKTABLE_STYLECOL) == null) {
				networkTable.createColumn(OVShared.CYNETWORKTABLE_STYLECOL, String.class, false);
			}
			networkTable.getRow(net.getSUID()).set(OVShared.CYNETWORKTABLE_STYLECOL, savedStyle);
		}
	}
	
	/**
	 * Update the mapping between the Table and the Network
	 * @return the number of rows from the table that are connected to the network
	 */
	public int updateLinks() {
		this.node2table = new HashMap<>();
		int nbConnectedRows=0;
		
		// FIRST STEP:
		// Make the link in the OVTable
		//
		CyTable nodeTable = this.rootNetwork.getBaseNetwork().getDefaultNodeTable();
		CyColumn keyCytoCol = nodeTable.getColumn(mappingColCyto);
		CyColumn keyOVCol = this.ovTable.getCyTable().getColumn(mappingColOVTable);
		
		if(keyCytoCol == null || keyOVCol == null) {
			return 0; // TODO message?
		}
		
		for(CyRow netRow : nodeTable.getAllRows()) {
			Object netKey = getKey(netRow, keyCytoCol);
			if(netKey == null) {
				continue;
			}
			
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
					++nbConnectedRows;
				}
			}
		}
		
		// SECOND STEP:
		// Make the link in the network tables
		//
		for(CyNetwork net : this.rootNetwork.getSubNetworkList()) {
			CyTable networkTable = net.getDefaultNetworkTable();
			if(networkTable.getColumn(OVShared.CYNETWORKTABLE_OVCOL) == null) {
				networkTable.createColumn(OVShared.CYNETWORKTABLE_OVCOL, String.class, false);
			}
			networkTable.getRow(net.getSUID()).set(OVShared.CYNETWORKTABLE_OVCOL, this.getSavedConnection());
		}
		
		return nbConnectedRows;
	}

	/**
	 * Update the Connection by reinitializing it with new mapping columns
	 * @param mappingColCyto
	 * @param mappingColOVTable
	 * @return the number of connected rows from the table
	 */
	public int update(String mappingColCyto, String mappingColOVTable) {
		// We update only if something has changed
		if(!mappingColOVTable.equals(this.mappingColOVTable) || !mappingColCyto.equals(this.mappingColCyto)) {
			this.mappingColOVTable=mappingColOVTable;
			this.mappingColCyto=mappingColCyto;

			this.nbConnectedTableRows = this.updateLinks();
			
			return this.nbConnectedTableRows;
		}

		return this.nbConnectedTableRows;
	}

	public void addLink(CyRow networkNode, CyRow tableRow) {
		Long suid = networkNode.get(CyNetwork.SUID, Long.class);
		if(!this.node2table.containsKey(suid)) {
			this.node2table.put(suid, new ArrayList<>());
		}

		List<CyRow> tableRows = this.node2table.get(suid);
		tableRows.add(tableRow);
	}
	
	private List<CyNetwork> getConnectedNetworks() {
		List<CyNetwork> connectedNetwork = new ArrayList<>();
		
		for(CyNetwork net : this.rootNetwork.getSubNetworkList()) {
			CyTable netTable = net.getDefaultNetworkTable();
			if(netTable != null
					&& netTable.getColumn(OVShared.CYNETWORKTABLE_OVCOL) != null
					&& !netTable.getRow(net.getSUID()).get(OVShared.CYNETWORKTABLE_OVCOL, String.class).isEmpty()) {
				connectedNetwork.add(net);
			}
		}
		
		return connectedNetwork;
	}
	
	public void connectNetwork(CyNetwork network) {
		if(!this.rootNetwork.containsNetwork(network)) {
			return;
		}
		
		CyTable networkTable = network.getDefaultNetworkTable();
		
		// Link :
		if(networkTable.getColumn(OVShared.CYNETWORKTABLE_OVCOL) == null) {
			networkTable.createColumn(OVShared.CYNETWORKTABLE_OVCOL, String.class, false);
		}
		networkTable.getRow(network.getSUID()).set(OVShared.CYNETWORKTABLE_OVCOL, this.getSavedConnection());
		
		
		// Style :
		if(networkTable.getColumn(OVShared.CYNETWORKTABLE_STYLECOL) == null) {
			networkTable.createColumn(OVShared.CYNETWORKTABLE_STYLECOL, String.class, false);
		}
		String savedStyle = "";
		if(this.ovStyle != null) {
			savedStyle = this.ovStyle.save();
		}
		networkTable.getRow(network.getSUID()).set(OVShared.CYNETWORKTABLE_STYLECOL, savedStyle);
	}

	public void disconnect() {
		for(CyNetwork net : this.rootNetwork.getSubNetworkList()) {
			this.disconnectNetwork(net);
		}
	}
	
	public void disconnectNetwork(CyNetwork network) {
		// We make sure that the network is in the collection of the OVConnection
		if(!this.rootNetwork.containsNetwork(network)) {
			return;
		}
		
		// We erase the link in the Network table
		CyTable networkTable = network.getDefaultNetworkTable();
		if(networkTable.getColumn(OVShared.CYNETWORKTABLE_OVCOL) != null) {
			networkTable.getRow(network.getSUID()).set(OVShared.CYNETWORKTABLE_OVCOL, "");
		}
		if(networkTable.getColumn(OVShared.CYNETWORKTABLE_STYLECOL) != null) {
			networkTable.getRow(network.getSUID()).set(OVShared.CYNETWORKTABLE_STYLECOL, "");
		}
		
		// We erase the Style
		if(this.getStyle() != null) {
			RemoveStyleTaskFactory factory = new RemoveStyleTaskFactory(ovManager, this);
			this.ovManager.executeTask(factory.createTaskIterator());
		}
		
		// If it was the last network of the collection ...
		if(this.getConnectedNetworks().size() == 0) {
			// We delete the style columns in the node table
			OVShared.deleteOVColumns(this.getBaseNetwork().getDefaultNodeTable());
			
			// We forget about this connection
			this.ovManager.removeConnection(this);
			
			this.ovTable.save();
		}
		
		if(this.ovManager.getOVCytoPanel() != null) {
			this.ovManager.getOVCytoPanel().update();
		}
	}

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
