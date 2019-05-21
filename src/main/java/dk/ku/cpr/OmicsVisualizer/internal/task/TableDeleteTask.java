package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;

public class TableDeleteTask extends AbstractTask {
	
	private OVManager ovManager;

	public TableDeleteTask(OVManager ovManager) {
		super();
		this.ovManager = ovManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Delete Omics Visualizer Table");
		
		OVTable ovTable = this.ovManager.getActiveOVTable();
		if(ovTable == null) {
			taskMonitor.setStatusMessage("No active table.");
			return;
		}
		
		this.ovManager.getOVCytoPanel().removeTable(ovTable);
	}

}
