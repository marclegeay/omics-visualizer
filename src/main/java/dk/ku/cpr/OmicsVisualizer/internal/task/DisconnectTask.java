package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVConnection;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;

public class DisconnectTask extends AbstractTask {
	private OVManager ovManager;

	public DisconnectTask(OVManager ovManager) {
		super();
		this.ovManager = ovManager;
	}
	
	@ProvidesTitle
	public String getName() {
		return "Disconnect table";
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle(this.getName());
		
		OVTable ovTable = this.ovManager.getActiveOVTable();
		
		if(ovTable == null) {
			taskMonitor.setStatusMessage("No active Omics Visualizer table. The task stops here.");
			return;
		}
		
		CyNetwork network = this.ovManager.getService(CyApplicationManager.class).getCurrentNetwork();
		
		if(network == null) {
			taskMonitor.setStatusMessage("There is no current network. The task stops here.");
			return;
		}
		
		OVConnection ovCon = ovTable.getConnection(network);
		
		if(ovCon != null) {
			taskMonitor.setStatusMessage("Disconnecting " + ovTable.getTitle() + " table and " + network.toString() + ".");
			ovTable.disconnect(network);
		} else {
			taskMonitor.setStatusMessage(ovTable.getTitle() + " table and " + network.toString() + " are not connected.");
		}
	}

}
