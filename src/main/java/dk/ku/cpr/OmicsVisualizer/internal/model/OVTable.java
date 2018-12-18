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
import org.cytoscape.model.CyTable;
import org.cytoscape.property.AbstractConfigDirPropsReader;
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
	
	private CyProperty<Properties> cyProperty;
	
	public OVTable(OVManager ovManager, CyTable cyTable) {
		this.ovManager=ovManager;
		this.cyTable=cyTable;
		this.jTable=null;
		this.cyProperty=null;
		
		this.createJTable();
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

	private void createJTable() {
		List<String> colNames = new ArrayList<String>();
		Collection<CyColumn> cols = this.cyTable.getColumns();
		for(@SuppressWarnings("unused") CyColumn c : cols) {
			colNames.add("");
		}
		int custom_col_id=0;
		
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

				// We do not want to display our custom_col_id
				if(col.getName().equals(OVShared.OVTABLE_COLID_NAME)) {
					custom_col_id = i;
				}
			}
		} else {
			// We do not display the Custom column ID name
			Iterator<CyColumn> it = cols.iterator();
			int i=0;
			while(it.hasNext()) {
				CyColumn col = it.next();
				colNames.set(i, col.getName());
				// We do not want to display our custom_col_id
				if(col.getName().equals(OVShared.OVTABLE_COLID_NAME)) {
					custom_col_id = i;
				} else {
					visibleCols.add(col.getName());
				}
				i++;
			}
		}

		tableModel = new OVTableModel(this, colNames);
		JTable jTable = new JTable(tableModel);
		tableColumnModel = new OVTableColumnModel(this);
		
		for (int i1 = 0; i1 < tableModel.getColumnCount(); i1++) {
			TableColumn tableColumn = new TableColumn(i1);
			tableColumn.setHeaderValue(tableModel.getColumnName(i1));
			tableColumnModel.addColumn(tableColumn);
		}
		jTable.setColumnModel(tableColumnModel);

		// We remove the custom_col_id from the model because we do not want it to be displayed:
		tableColumnModel.removeColumn(tableColumnModel.getColumn(custom_col_id));
		
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
