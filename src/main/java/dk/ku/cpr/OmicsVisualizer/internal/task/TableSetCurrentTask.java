package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;

public class TableSetCurrentTask extends AbstractTask {
	
	private OVManager ovManager;
	
	@Tunable(description="Name of the Omics Visualizer table to set as current.",
			required=true,
			exampleStringValue="Omics Visualizer Table 1",
			gravity=1.0)
	public String tableName;
	
	public TableSetCurrentTask(OVManager ovManager) {
		super();
		this.ovManager = ovManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if(this.tableName == null) {
			return;
		}
		
		OVTable newCurrent = null;
		
		for(OVTable table : this.ovManager.getOVTables()) {
			if(table.getTitle().equals(this.tableName)) {
				newCurrent = table;
				break;
			}
		}
		
		if(newCurrent == null) {
			taskMonitor.setStatusMessage("Error: Unknown table \"" + this.tableName + "\".");
			return;
		}
		
		this.ovManager.getOVCytoPanel().initPanel(newCurrent);
		
		taskMonitor.setStatusMessage("New current OV Table: " + this.ovManager.getActiveOVTable().getTitle());
	}

}
