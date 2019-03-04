package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;
import dk.ku.cpr.OmicsVisualizer.internal.ui.OVCytoPanel;

public class RemoveFilterTask extends AbstractTask {
	protected OVManager ovManager;
	protected OVTable ovTable;
	
	protected OVCytoPanel ovPanel;
	
	public RemoveFilterTask(OVManager ovManager, OVTable ovTable, OVCytoPanel ovPanel) {
		super();
		this.ovManager=ovManager;
		this.ovTable=ovTable;
		this.ovPanel=ovPanel;
		
		if(this.ovTable==null) {
			this.ovTable=this.ovManager.getActiveOVTable();
		}
		
		if(this.ovPanel==null) {
			this.ovPanel = this.ovManager.getOVCytoPanel();
		}
	}
	
	public RemoveFilterTask(OVManager ovManager, OVTable ovTable) {
		this(ovManager, ovTable, null);
	}
	
	public RemoveFilterTask(OVManager ovManager) {
		this(ovManager, null, null);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if(this.ovTable==null) {
			return;
		}
		
		taskMonitor.setTitle("Removing the filter of '"+this.ovTable.getTitle()+"' Omics Visualizer table");
		
		// We remove the filter from the table
		this.ovTable.removeFilter();
		// And from the TableProperty
		this.ovTable.setTableProperty(OVShared.PROPERTY_FILTER, "");
		
		// We update the panel, if we know it
		if(this.ovPanel != null) {
			this.ovPanel.update();
		}
	}

}
