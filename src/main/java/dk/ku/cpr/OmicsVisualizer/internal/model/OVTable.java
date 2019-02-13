package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.table.TableColumn;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.CyProperty.SavePolicy;
import org.cytoscape.property.SimpleCyProperty;

import dk.ku.cpr.OmicsVisualizer.internal.ui.OVTableColumnModel;
import dk.ku.cpr.OmicsVisualizer.internal.ui.OVTableModel;

public class OVTable {
	private OVManager ovManager;
	
	private CyTable cyTable;
	private JTable jTable;
	private OVTableModel tableModel;
	private OVTableColumnModel tableColumnModel;
	
	private CyProperty<Properties> cyProperty;
	
	public OVTable(OVManager ovManager, CyTable cyTable) {
		this.ovManager=ovManager;
		this.cyTable=cyTable;
		this.jTable=null;
		this.cyProperty=null;
		
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
	
	public List<OVConnection> getConnections() {
		return this.ovManager.getConnections(this);
	}
	
	public OVConnection getConnection(CyNetwork network) {
		for(OVConnection con : this.getConnections()) {
			if(con.getNetwork() == network) {
				return con;
			}
		}
		return null;
	}
	
	public boolean isConnected() {
		return this.getConnections().size()>0;
	}
	
	public boolean isConnectedTo(CyNetwork net) {
		return this.getConnection(net) != null;
	}
	
	/*
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
	*/
	
	public void connect(String netName, String mappingColCyto, String mappingColOVTable) {
		CyNetwork linkedNetwork=null;
		
		for(CyNetwork net : this.ovManager.getNetworkManager().getNetworkSet()) {
			if(net.toString().equals(netName)) {
				linkedNetwork = net;
			}
		}
		
		if(linkedNetwork == null) {
			return;
		}
		
		OVConnection con = this.getConnection(linkedNetwork);
		
		if(con == null || con.update(mappingColCyto, mappingColOVTable)) {
			if(con == null) { // Connection to a new network
				con = new OVConnection(this.ovManager, this, linkedNetwork, mappingColCyto, mappingColOVTable);
			}
		}
	}
	
	public void disconnectAll() {
		for(OVConnection con : this.getConnections()) {
			con.disconnect();
		}
	}
	
	public void disconnect(CyNetwork network) {
		OVConnection con = this.getConnection(network);
		
		if(con != null) {
			con.disconnect();
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
		
		if(this.isConnected()) {
			String linkedNames="";
			String mappingsCyOV="";
			String mappingsOVCy="";
			
			for(OVConnection con : this.getConnections()) {
				linkedNames += "," + con.getNetwork().toString();
				mappingsCyOV += "," + con.getMappingColCyto();
				mappingsOVCy += "," + con.getMappingColOVTable();
			}
			
			// We get rid of the first comma
			linkedNames = linkedNames.substring(1);
			mappingsCyOV = mappingsCyOV.substring(1);
			mappingsOVCy = mappingsOVCy.substring(1);

			this.setTableProperty(OVShared.PROPERTY_LINKED_NETWORK, linkedNames);
			this.setTableProperty(OVShared.PROPERTY_MAPPING_CY_OV, mappingsCyOV);
			this.setTableProperty(OVShared.PROPERTY_MAPPING_OV_CY, mappingsOVCy);
		}
	}
	
	public void load() {
		String linkedName = this.getTableProperty(OVShared.PROPERTY_LINKED_NETWORK, "");
		String mappingCyOV = this.getTableProperty(OVShared.PROPERTY_MAPPING_CY_OV, "");
		String mappingOVCy = this.getTableProperty(OVShared.PROPERTY_MAPPING_OV_CY, "");
		
		String linkedNames[] = linkedName.split(",");
		String mappingsCyOV[] = mappingCyOV.split(",");
		String mappingsOVCy[] = mappingOVCy.split(",");
		
		for(int i=0; i<linkedNames.length; ++i) {
			this.connect(linkedNames[i], mappingsCyOV[i], mappingsOVCy[i]);
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
