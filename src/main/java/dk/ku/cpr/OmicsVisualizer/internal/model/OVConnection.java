package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

import dk.ku.cpr.OmicsVisualizer.internal.utils.DataUtils;

public class OVConnection {
	private OVManager ovManager;
	private OVTable ovTable;
	private CyNetwork network;
	/** Name of the column from the Cytoscape Network that links to the OVTable */
	private String mappingColCyto;
	/** Name of the column from the OVTable that links to the Cytoscape Network */
	private String mappingColOVTable;
	/** Map that associates the list of table rows with the network node */
	private Map<CyRow, List<CyRow>> node2table;
	private OVStyle ovStyle;

	public OVConnection(OVManager ovManager, OVTable ovTable, CyNetwork network, String mappingColCyto, String mappingColOVTable) {
		super();
		this.ovManager=ovManager;
		this.ovTable=ovTable;
		this.network = network;
		this.mappingColCyto = mappingColCyto;
		this.mappingColOVTable = mappingColOVTable;
		this.node2table = new HashMap<>();
		this.ovStyle=null;
		
		// We register all OVConnection to the OVManager
		this.ovManager.addConnection(this);
		
		this.updateLinks();
		
		this.ovTable.save();
	}
	
	public OVTable getOVTable() {
		return ovTable;
	}

	public CyNetwork getNetwork() {
		return network;
	}

	public String getMappingColOVTable() {
		return mappingColOVTable;
	}

	public String getMappingColCyto() {
		return mappingColCyto;
	}
	
	public List<CyRow> getLinkedRows(CyRow netRow) {
		List<CyRow> list = this.node2table.get(netRow); 
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
		
		CyTable networkTable = this.network.getDefaultNetworkTable();
		if(networkTable.getColumn(OVShared.CYNETWORKTABLE_STYLECOL) == null) {
			networkTable.createColumn(OVShared.CYNETWORKTABLE_STYLECOL, String.class, false);
		}
		networkTable.getRow(this.network.getSUID()).set(OVShared.CYNETWORKTABLE_STYLECOL, savedStyle);
	}
	
	public void updateLinks() {
		this.node2table = new HashMap<>();
		
		// FIRST STEP:
		// Make the link in the OVTable
		//
		CyTable nodeTable = this.network.getDefaultNodeTable();
		CyColumn keyCytoCol = nodeTable.getColumn(mappingColCyto);
		CyColumn keyOVCol = this.ovTable.getCyTable().getColumn(mappingColOVTable);
		
		if(keyCytoCol == null || keyOVCol == null) {
			return; // TODO message?
		}
		
		for(CyRow netRow : nodeTable.getAllRows()) {
			Object netKey = getKey(netRow, keyCytoCol);
			if(netKey == null) {
				continue;
			}
			
			for(CyRow tableRow : this.ovTable.getCyTable().getAllRows()) {
				Object tableKey = getKey(tableRow, keyOVCol);
				
				if(netKey.equals(tableKey)) {
					this.addLink(netRow, tableRow);
				}
			}
		}
		
		// SECOND STEP:
		// Make the link in the network table
		//
		CyTable networkTable = this.network.getDefaultNetworkTable();
		if(networkTable.getColumn(OVShared.CYNETWORKTABLE_OVCOL) == null) {
			networkTable.createColumn(OVShared.CYNETWORKTABLE_OVCOL, String.class, false);
		}
		networkTable.getRow(this.network.getSUID()).set(OVShared.CYNETWORKTABLE_OVCOL, DataUtils.escapeComma(this.ovTable.getTitle())+","+DataUtils.escapeComma(mappingColCyto)+","+DataUtils.escapeComma(mappingColOVTable));
	}

	/**
	 * Update the Connection by reinitializing it with new mapping columns
	 * @param mappingColCyto
	 * @param mappingColOVTable
	 * @return <code>true</code> if the connection has been updated, <code>false</code> if the mapping columns have not changed.
	 */
	public boolean update(String mappingColCyto, String mappingColOVTable) {
		// We update only if something has changed
		if(!mappingColOVTable.equals(this.mappingColOVTable) || !mappingColCyto.equals(this.mappingColCyto)) {
			this.mappingColOVTable=mappingColOVTable;
			this.mappingColCyto=mappingColCyto;

			this.updateLinks();

			return true;
		}

		return false;
	}

	public void addLink(CyRow networkNode, CyRow tableRow) {
		if(!this.node2table.containsKey(networkNode)) {
			this.node2table.put(networkNode, new ArrayList<>());
		}

		List<CyRow> tableRows = this.node2table.get(networkNode);
		tableRows.add(tableRow);
	}

	public void disconnect() {
		// We erase the link in the Network table
		CyTable networkTable = this.network.getDefaultNetworkTable();
		if(networkTable.getColumn(OVShared.CYNETWORKTABLE_OVCOL) != null) {
			networkTable.getRow(this.network.getSUID()).set(OVShared.CYNETWORKTABLE_OVCOL, "");
		}
		if(networkTable.getColumn(OVShared.CYNETWORKTABLE_STYLECOL) != null) {
			networkTable.getRow(this.network.getSUID()).set(OVShared.CYNETWORKTABLE_STYLECOL, "");
		}

		// We delete the style columns in the node table
		OVShared.deleteOVColumns(this.network.getDefaultNodeTable());
		
		// We forget about this connection
		this.ovManager.removeConnection(this);
		
		this.ovTable.save();
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
				con.network.equals(this.network);
	}
}
