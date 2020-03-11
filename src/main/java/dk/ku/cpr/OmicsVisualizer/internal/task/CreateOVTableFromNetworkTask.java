package dk.ku.cpr.OmicsVisualizer.internal.task;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;

public class CreateOVTableFromNetworkTask extends AbstractTask implements ObservableTask {
	protected OVManager ovManager;
	protected CyNetwork cyNetwork;
	protected String keyCyTableColName;
	protected List<String> cyTableColNames;
	protected String tableName;
	protected String valuesColName;
	protected String srcColName;
	
	private CyTable newCyTable;

	public CreateOVTableFromNetworkTask(OVManager ovManager, CyNetwork cyNetwork, String keyCyTableColName, List<String> cyTableColNames, String tableName,
			String valuesColName, String srcColName) {
		super();
		this.ovManager = ovManager;
		this.cyNetwork = cyNetwork;
		this.keyCyTableColName = keyCyTableColName;
		this.cyTableColNames = cyTableColNames;
		this.tableName = tableName;
		this.valuesColName = valuesColName;
		this.srcColName = srcColName;
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
			} else {
				if(valuesType != cyTable.getColumn(col).getType()) {
					sameType=false;
				}
			}
		}
		if(error) {
			return;
		}
		if(!sameType) {
			taskMonitor.showMessage(Level.ERROR, "ERROR: All the 'value' columns should have the same type.");
			return;
		}
		
		if(valuesType == List.class) {
			taskMonitor.showMessage(Level.ERROR, "ERROR: The 'value' columns can not be List.");
			return;
		}
		
		// We check for the name
		if(this.tableName == null || this.tableName.trim().isEmpty()) {
			this.tableName = this.ovManager.getNextTableName();
		}
		for(CyTable existingTable : this.ovManager.getService(CyTableManager.class).getAllTables(true)) {
			if(existingTable.getTitle().equals(this.tableName)) {
				taskMonitor.showMessage(Level.ERROR, "ERROR: The table named \"" + this.tableName + "\" already exist.");
				return;
			}
		}
		
		newCyTable = this.ovManager.createCyTableFromNetwork(
				cyNetwork,
				keyCyTableColName,
				cyTableColNames,
				tableName,
				valuesColName,
				srcColName,
				0
		);
		
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
