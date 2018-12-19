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
	/** Name of the column from the OVTable that links to the CyNetwork */
	private String mappingColOVTable;
	/** Name of the column from the Network that links to the OVTable */
	private String mappingColCyNetwork;
	
	private CyProperty<Properties> cyProperty;
	
	public OVTable(OVManager ovManager, CyTable cyTable) {
		this.ovManager=ovManager;
		this.cyTable=cyTable;
		this.jTable=null;
		this.cyProperty=null;
		
		this.linkedNetwork=null;
		this.mappingColOVTable="";
		this.mappingColCyNetwork="";
		
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
	
	public String getMappingColCyNetwork() {
		return this.mappingColCyNetwork;
	}
	
	public void connect(String netName, String mappingColNetwork, String mappingColOVTable) {
		for(CyNetwork net : this.ovManager.getNetworkManager().getNetworkSet()) {
			if(net.toString().equals(netName)) {
				this.linkedNetwork = net;
			}
		}
		this.mappingColCyNetwork=mappingColNetwork;
		this.mappingColOVTable=mappingColOVTable;
		
		//TODO
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
				String propValues[] = propValue.split(",");
				
				int i = Integer.parseInt(propValues[0]);
				colNames.set(i, col.getName());
				
				boolean visible = Boolean.parseBoolean(propValues[1]);
				if(visible) {
					visibleCols.add(col.getName());
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
			this.setTableProperty(OVShared.PROPERTY_LINKED_NETWORK, this.linkedNetwork.getSUID().toString());
			this.setTableProperty(OVShared.PROPERTY_MAPPING_CY_OV, this.mappingColCyNetwork);
			this.setTableProperty(OVShared.PROPERTY_MAPPING_OV_CY, this.mappingColOVTable);
		}
	}
	
	public void load() {
		String sNetSUID = this.getTableProperty(OVShared.PROPERTY_LINKED_NETWORK, ""); 
		if(!sNetSUID.equals("")) {
			long netSUID = Long.parseLong(sNetSUID);
			
			this.linkedNetwork = this.ovManager.getNetworkManager().getNetwork(netSUID);
			this.mappingColCyNetwork = this.getTableProperty(OVShared.PROPERTY_MAPPING_CY_OV);
			this.mappingColOVTable = this.getTableProperty(OVShared.PROPERTY_MAPPING_OV_CY);
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
