package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.table.TableColumn;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.property.CyProperty.SavePolicy;

import dk.ku.cpr.OmicsVisualizer.internal.ui.OVTableColumnModel;
import dk.ku.cpr.OmicsVisualizer.internal.ui.OVTableModel;

public class OVTable {
	private OVManager ovManager;
	
	private CyTable cyTable;
	private JTable jTable;
	private OVTableModel tableModel;
	private OVTableColumnModel tableColumnModel;
	
	private CyNetwork linkedNetwork;
	/** Name of the column from the OVTable that links to the Cytoscape Network */
	private String mappingColOVTable;
	/** Name of the column from the Cytoscape Network that links to the OVTable */
	private String mappingColCyto;
	
	/** Map that associates the list of table rows with the network node */
	private Map<CyRow, List<CyRow>> node2table;
	
	private CyProperty<Properties> cyProperty;
	
	public OVTable(OVManager ovManager, CyTable cyTable) {
		this.ovManager=ovManager;
		this.cyTable=cyTable;
		this.jTable=null;
		this.cyProperty=null;
		
		this.linkedNetwork=null;
		this.mappingColOVTable="";
		this.mappingColCyto="";
		
		this.node2table=new HashMap<>();
		
		this.createJTable();
		this.load();
		this.save();
	}
	
	public CyTable getCyTable() {
		return this.cyTable;
	}
	
	public JTable getJTable() {
		if(this.jTable == null) {
			this.createJTable();
		}
		
		return this.jTable;
	}
	
	public CyNetwork getLinkedNetwork() {
		return this.linkedNetwork;
	}
	public String getLinkedNetworkName() {
		return (this.linkedNetwork == null ? "" : this.linkedNetwork.toString());
	}
	
	public String getMappingColOVTable() {
		return this.mappingColOVTable;
	}
	
	public String getMappingColCyto() {
		return this.mappingColCyto;
	}
	
	public boolean isConnected() {
		return this.linkedNetwork!=null;
	}
	
	private Object getKey(CyRow row, CyColumn keyCol) {
		if(keyCol.getListElementType() == null) { // this is not a List element
			return row.get(keyCol.getName(), keyCol.getType());
		} else { // this is a list
			return row.getList(keyCol.getName(), keyCol.getListElementType());
		}
	}
	
	private void addLink(CyRow networkNode, CyRow tableRow) {
		if(!this.node2table.containsKey(networkNode)) {
			this.node2table.put(networkNode, new ArrayList<>());
		}
		
		List<CyRow> tableRows = this.node2table.get(networkNode);
		tableRows.add(tableRow);
	}
	
	public List<CyRow> getLinkedRows(CyRow networkNode) {
		return this.node2table.get(networkNode);
	}
	
	public void connect(String netName, String mappingColCyto, String mappingColOVTable) {
		CyNetwork oldLinkedNetwork = this.linkedNetwork;
		String oldColCyto = this.mappingColCyto;
		String oldColOVTable = this.mappingColOVTable;
		
		for(CyNetwork net : this.ovManager.getNetworkManager().getNetworkSet()) {
			if(net.toString().equals(netName)) {
				this.linkedNetwork = net;
			}
		}
		this.mappingColCyto=mappingColCyto;
		this.mappingColOVTable=mappingColOVTable;
		
		if(oldLinkedNetwork == null
				|| !oldLinkedNetwork.equals(this.linkedNetwork)
				|| !oldColCyto.equals(this.mappingColCyto)
				|| !oldColOVTable.equals(this.mappingColOVTable)) {
			// Something has changed, we delete all previous records and we save the new ones
			
			// FIRST STEP:
			// Make the link in the OVTable
			//
			this.node2table = new HashMap<>();
			this.cyTable.deleteColumn(OVShared.OVTABLE_COL_NODE_SUID);
			
			this.cyTable.createColumn(OVShared.OVTABLE_COL_NODE_SUID, Long.class, false);
			
			
			CyTable nodeTable = this.linkedNetwork.getDefaultNodeTable();
			CyColumn keyCytoCol = nodeTable.getColumn(this.mappingColCyto);
			CyColumn keyOVCol = this.cyTable.getColumn(this.mappingColOVTable);
			
			if(keyCytoCol == null || keyOVCol == null) {
				return; // TODO message?
			}
			
			for(CyRow netRow : nodeTable.getAllRows()) {
				Object netKey = getKey(netRow, keyCytoCol);
				
				for(CyRow tableRow : this.cyTable.getAllRows()) {
					Object tableKey = getKey(tableRow, keyOVCol);
					
					if(netKey.equals(tableKey)) {
						System.out.println("[OV] key match "+netKey+" ("+netRow.get("SUID", Long.class)+")");
						tableRow.set(OVShared.OVTABLE_COL_NODE_SUID, netRow.get("SUID", Long.class));
						this.addLink(netRow, tableRow);
					}
				}
			}
			
			// SECOND STEP:
			// Make the link in the network table
			//
			CyTable networkTable = this.linkedNetwork.getDefaultNetworkTable();
			if(networkTable.getColumn(OVShared.CYNETWORKTABLE_OVCOL) == null) {
				networkTable.createColumn(OVShared.CYNETWORKTABLE_OVCOL, String.class, false);
			}
			networkTable.getRow(this.linkedNetwork.getSUID()).set(OVShared.CYNETWORKTABLE_OVCOL, this.getTitle()+","+this.getMappingColCyto()+","+this.getMappingColOVTable());
		}
	}
	
	public void disconnect() {
		if(isConnected()) {
			// We erase the link in the Network table
			CyTable networkTable = this.linkedNetwork.getDefaultNetworkTable();
			if(networkTable.getColumn(OVShared.CYNETWORKTABLE_OVCOL) != null) {
				networkTable.getRow(this.linkedNetwork.getSUID()).set(OVShared.CYNETWORKTABLE_OVCOL, "");
			}
	
			// We delete the style columns in the node table
			CyTable nodeTable = this.linkedNetwork.getDefaultNodeTable();
			if(nodeTable.getColumn(OVShared.CYNODETABLE_STYLECOL) != null) {
				nodeTable.deleteColumn(OVShared.CYNODETABLE_STYLECOL);
			}
			if(nodeTable.getColumn(OVShared.CYNODETABLE_STYLECOL_VALUES) != null) {
				nodeTable.deleteColumn(OVShared.CYNODETABLE_STYLECOL_VALUES);
			}
			
			// We "forget" everything
			this.linkedNetwork=null;
			this.mappingColCyto="";
			this.mappingColOVTable="";
		}
	}

	private void createJTable() {
		List<String> colNames = new ArrayList<String>();
		Collection<CyColumn> cols = this.cyTable.getColumns();
		for(@SuppressWarnings("unused") CyColumn c : cols) {
			colNames.add("");
		}
		
		Set<String> visibleCols = new HashSet<String>();
		
		if(this.getTableProperty(OVShared.OVTABLE_COLID_NAME, "") != "") {
			// We load the columns from the Properties
			// Cytoscape store CyTable with columns sorted alphabetically, so we stored the order of each columns in Properties
			Iterator<CyColumn> it = cols.iterator();
			String propValue;
			while(it.hasNext()) {
				CyColumn col = it.next();
				
				propValue = this.getTableProperty(col.getName());
				if(propValue != null) { // Some CyColumn are not stored in property (OVColumns)
					String propValues[] = propValue.split(",");
					
					int i = Integer.parseInt(propValues[0]);
					colNames.set(i, col.getName());
					
					boolean visible = Boolean.parseBoolean(propValues[1]);
					if(visible) {
						visibleCols.add(col.getName());
					}
				}
			}
		} else {
			// We do not display the Custom column ID name
			Iterator<CyColumn> it = cols.iterator();
			int i=0;
			while(it.hasNext()) {
				CyColumn col = it.next();
				colNames.set(i, col.getName());
				
				// We do not want to display our special OVTable columns
				if(!OVShared.isOVCol(col.getName())) {
					visibleCols.add(col.getName());
				}
				
				i++;
			}
		}

		tableModel = new OVTableModel(this, colNames);
		JTable jTable = new JTable(tableModel);
		tableColumnModel = new OVTableColumnModel(this);
		
		for (int i = 0; i < tableModel.getColumnCount(); i++) {
			TableColumn tableColumn = new TableColumn(i);
			tableColumn.setHeaderValue(tableModel.getColumnName(i));
			tableColumnModel.addColumn(tableColumn);
		}
		jTable.setColumnModel(tableColumnModel);
		
		this.setVisibleColumns(visibleCols);

		jTable.setAutoCreateRowSorter(true);
		jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//		jTable.getColumnModel().addColumnModelListener(this);
		
		this.jTable = jTable;
	}
	
	public List<String> getColNames() {
		return this.tableColumnModel.getColumnNames(false);
	}
	
	public Class<?> getColType(String colName) {
		CyColumn col = this.cyTable.getColumn(colName);
		
		if(col != null) {
			return col.getType();
		}
		
		return null;
	}
	
	public Class<?> getColListType(String colName) {
		CyColumn col = this.cyTable.getColumn(colName);
		
		if(col != null) {
			return col.getListElementType();
		}
		
		return null;
	}
	
	public Collection<CyColumn> getColumns() {
		Collection<CyColumn> cols = this.cyTable.getColumns();
		
		CyColumn keyCol = null;
		for(CyColumn col : cols) {
			if(col.isPrimaryKey()) {
				keyCol = col;
			}
		}
		
		cols.remove(keyCol);
		
		return cols;
	}
	
	public Collection<CyColumn> getColumnsInOrder() {
		Collection<CyColumn> cols = new ArrayList<CyColumn>();
		
		for(String colName : this.tableColumnModel.getColumnNames(false)) {
			if(!colName.equals(OVShared.OVTABLE_COLID_NAME)) {
				cols.add(this.cyTable.getColumn(colName));
			}
		}
		
		return cols;
	}

	public void setVisibleColumns(Set<String> visibleAttributes) {
		for (final String name : tableModel.getAllColumnNames()) {
			int col = tableModel.mapColumnNameToColumnIndex(name);
			TableColumn column = tableColumnModel.getColumnByModelIndex(col);
			tableColumnModel.setColumnVisible(column, visibleAttributes.contains(name));
		}
		
		this.save();
	}
	
	public Collection<String> getVisibleColumns() {
		return this.tableColumnModel.getColumnNames(true);
	}
	
	public void addColumn(String colName, Class<?> type) {
		this.cyTable.createColumn(colName, type, false);
		
		this.tableModel.addColumnName(colName);
		
		int modelIndex = this.tableColumnModel.getColumnCount();
		TableColumn tableColumn = new TableColumn(modelIndex);
		tableColumn.setHeaderValue(this.tableModel.getColumnName(modelIndex));
		tableColumnModel.addColumn(tableColumn);
	}
	
	public String getTitle() {
		return this.cyTable.getTitle();
	}
	
	public void save() {
		Collection<String> visibleCols = this.getVisibleColumns();
		Collection<String> cols = this.getColNames();
		Integer index = new Integer(0);
		
		for(String col : cols) {
			String savedValue = index.toString()+","+visibleCols.contains(col);
			this.setTableProperty(col, savedValue);
			index += 1;
		}
		
		if(this.linkedNetwork != null) {
			this.setTableProperty(OVShared.PROPERTY_LINKED_NETWORK, this.linkedNetwork.toString());
			this.setTableProperty(OVShared.PROPERTY_MAPPING_CY_OV, this.mappingColCyto);
			this.setTableProperty(OVShared.PROPERTY_MAPPING_OV_CY, this.mappingColOVTable);
		}
	}
	
	public void load() {
		String linkedName = this.getTableProperty(OVShared.PROPERTY_LINKED_NETWORK, "");
		if(!linkedName.equals("")) {
			this.connect(linkedName, this.getTableProperty(OVShared.PROPERTY_MAPPING_CY_OV), this.getTableProperty(OVShared.PROPERTY_MAPPING_OV_CY));
		}
	}
	
	@SuppressWarnings("unchecked")
	public CyProperty<Properties> getTableCyProperty() {
		String propName = OVShared.CYPROPERTY_NAME+"-"+this.cyTable.getTitle();
		
		try { // If the service is not registered yet, it throws an Exception
			this.cyProperty = this.ovManager.getService(CyProperty.class, "(cyPropertyName="+propName+")");
		} catch(Exception e ) {
			// Now we store those Properties into the Session File
			// We use the SimpleCyProperty class to do so
			this.cyProperty = new SimpleCyProperty<Properties>(propName, new Properties(), Properties.class, SavePolicy.SESSION_FILE_AND_CONFIG_DIR);
			Properties cyPropServiceProps = new Properties(); // The SimpleCyProperty service must be registered with a name, so we have Properties for this service also
			cyPropServiceProps.setProperty("cyPropertyName", this.cyProperty.getName());
			this.ovManager.registerAllServices(this.cyProperty, cyPropServiceProps);
		}
		
		return this.cyProperty;
	}
	public String getTableProperty(String propName) {
		return this.getTableCyProperty().getProperties().getProperty(propName);
	}
	public String getTableProperty(String propName, String propDefaultValue) {
		return this.getTableCyProperty().getProperties().getProperty(propName, propDefaultValue);
	}
	public void setTableProperty(String propName, String propValue) {
		this.getTableCyProperty().getProperties().put(propName, propValue);
	}
	
	public void deleteProperties() {
		if(this.cyProperty != null) {
			this.ovManager.unregisterAllServices(this.cyProperty);
		}
	}
}
