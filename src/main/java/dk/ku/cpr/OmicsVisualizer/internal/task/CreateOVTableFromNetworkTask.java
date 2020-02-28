package dk.ku.cpr.OmicsVisualizer.internal.task;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;

public class CreateOVTableFromNetworkTask extends AbstractTask implements ObservableTask {
	protected OVManager ovManager;
	protected CyNetwork cyNetwork;
	protected String keyCyTableColName;
	protected List<String> cyTableColNames;
	protected String tableName;
	protected String valuesColName;
	protected String srcColName;
	protected boolean displayNamespaces;
	
	private CyTable newCyTable;

	public CreateOVTableFromNetworkTask(OVManager ovManager, CyNetwork cyNetwork, String keyCyTableColName, List<String> cyTableColNames, String tableName,
			String valuesColName, String srcColName, boolean displayNamespaces) {
		super();
		this.ovManager = ovManager;
		this.cyNetwork = cyNetwork;
		this.keyCyTableColName = keyCyTableColName;
		this.cyTableColNames = cyTableColNames;
		this.tableName = tableName;
		this.valuesColName = valuesColName;
		this.srcColName = srcColName;
		this.displayNamespaces = displayNamespaces;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Creating OVTable from Cytoscape table");
		
		if(this.cyNetwork == null) {
			taskMonitor.showMessage(Level.ERROR, "ERROR: The network cannot be found.");
			return;
		}
		
		CyTable cyTable = this.cyNetwork.getDefaultNodeTable();
		if(cyTable == null) {
			taskMonitor.showMessage(Level.ERROR, "ERROR: The network's node table cannot be found.");
			return;
		}
		
		// First we check if everything is OK
		if(cyTable.getColumn(keyCyTableColName) == null) {
			taskMonitor.showMessage(Level.ERROR, "ERROR: The key column \"" + keyCyTableColName + "\" is not in the table \"" + cyTable.getTitle() + "\".");
			return;
		}
		
		Class<?> keyCyTableType = cyTable.getColumn(keyCyTableColName).getType();
		if(keyCyTableType == List.class) {
			taskMonitor.showMessage(Level.ERROR, "ERROR: The key column can not be a List.");
			return;
		}
		
		if(this.cyTableColNames.size() < 1) {
			taskMonitor.showMessage(Level.ERROR, "ERROR: At least one column value must be given.");
			return;
		}
		
		Class<?> valuesType = cyTable.getColumn(this.cyTableColNames.get(0)).getType(); 
		
		boolean error=false;
		boolean sameType=true;
		for(String col : this.cyTableColNames) {
			if(cyTable.getColumn(col) == null) {
				error=true;
				taskMonitor.showMessage(Level.ERROR, "ERROR: The column \"" + col + "\" is not in the table \"" + cyTable.getTitle() + "\".");
			} else if(valuesType != cyTable.getColumn(col).getType()) {
				sameType=false;
			}
		}
		if(error) {
			return;
		}
		if(!sameType) {
			taskMonitor.showMessage(Level.ERROR, "ERROR: All the 'value' columns should have the same type.");
			return;
		}
		
		// We must create the CyTable that will be wrapped into the OVTable
		if(this.tableName == null || this.tableName.trim().isEmpty()) {
			this.tableName = this.ovManager.getNextTableName();
		}
		
		for(CyTable existingTable : this.ovManager.getService(CyTableManager.class).getAllTables(true)) {
			if(existingTable.getTitle().equals(this.tableName)) {
				taskMonitor.showMessage(Level.ERROR, "ERROR: The table named \"" + this.tableName + "\" already exist.");
				return;
			}
		}
		
		// We check for the colnames
		if(this.valuesColName == null) {
			this.valuesColName = OVShared.OV_DEFAULT_VALUES_COLNAME;
		}
		if(this.srcColName == null) {
			this.srcColName = OVShared.OV_DEFAULT_VALUES_SOURCE_COLNAME;
		}
		
		newCyTable = this.ovManager.getService(CyTableFactory.class).createTable(this.tableName, OVShared.OVTABLE_COLID_NAME, OVShared.OVTABLE_COLID_TYPE, false, true);
		
		// We create the "key" column
		newCyTable.createColumn(keyCyTableColName, cyTable.getColumn(keyCyTableColName).getType(), false);
		
		// We create the "value" and "value source" column
		newCyTable.createColumn(this.valuesColName, valuesType, false);
		newCyTable.createColumn(this.srcColName, String.class, false);
		
		// Now we create the rows
		Integer key = 0;
		for(CyRow srcRow : cyTable.getAllRows()) {
			Object keyCyTableValue = srcRow.get(this.keyCyTableColName, keyCyTableType);
			
			if(keyCyTableValue == null) {
				continue;
			}
			
			for(String colName : this.cyTableColNames) {
				Object newValue = srcRow.get(colName, valuesType);
				
				CyRow newRow = newCyTable.getRow(key);
				
				// First we copy the key value from the source CyTable
				newRow.set(this.keyCyTableColName, keyCyTableValue);
				
				// Then we copy the specific column
				newRow.set(this.valuesColName, newValue);
				
				// Finally we set the source
				if(!displayNamespaces) {
					// We don't display the namespaces, so we ask Cytoscape for the "name only" of the column
					newRow.set(this.srcColName, cyTable.getColumn(colName).getNameOnly());
				} else {
					// We display the namespaces, so it's the fullname of the column, the one we already use
					newRow.set(this.srcColName, colName);
				}
				
				// Each column is now a row
				key++;
			}
		}
		
		// At the end, we create the OVTable
		this.ovManager.getService(CyTableManager.class).addTable(newCyTable);
		OVTable newOVTable = new OVTable(this.ovManager, newCyTable);
		this.ovManager.addOVTable(newOVTable);
		this.ovManager.showPanel();
		
		// We connect the new table with the network
		ConnectTaskFactory factory = new ConnectTaskFactory(this.ovManager, newOVTable, this.cyNetwork, this.keyCyTableColName, this.keyCyTableColName);
		this.ovManager.executeSynchronousTask(factory.createTaskIterator());
	}

	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(CyTable.class, String.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(type.equals(CyTable.class)) {
			return (R) newCyTable;
		} else if(type.equals(String.class)) {
			return (R) newCyTable.getTitle();
		}

		return null;
	}
}
