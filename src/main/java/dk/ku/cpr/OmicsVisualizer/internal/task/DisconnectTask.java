package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;

public class DisconnectTask extends AbstractTask {
	private OVManager ovManager;
	
	@Tunable(description="Name of the Network Collection to disconnect.",
			required=true,
			tooltip="Usually the Network Collection's name is the same as the main Network in the collection, but the name of the collection can be changed separately.",
			exampleStringValue="String Network",
			gravity=1.0)
	public String rootNetName;
	
	@Tunable(description="Name of the table to disconnect. (if not used, the active table will be used)",
			exampleStringValue="Omics Visualizer Table 1",
			gravity=1.0)
	public String tableName;

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
		
		OVTable ovTable = null;
		
		if(tableName != null) {
			for(OVTable tab : this.ovManager.getOVTables()) {
				if(tab.getTitle().equals(tableName)) {
					ovTable = tab;
				}
			}
		} else {
			ovTable = this.ovManager.getActiveOVTable();
		}
		
		if(ovTable == null) {
			taskMonitor.setStatusMessage("No active Omics Visualizer table. The task stops here.");
			return;
		}
		CyRootNetwork linkedRootNetwork=null;
		CyRootNetworkManager manager = this.ovManager.getService(CyRootNetworkManager.class);
		
		for(CyNetwork net : this.ovManager.getNetworkManager().getNetworkSet()) {
			CyRootNetwork rootNet = manager.getRootNetwork(net);
			if(rootNet.toString().equals(rootNetName)) {
				linkedRootNetwork = rootNet;
			}
		}
		
		if(linkedRootNetwork == null) {
			taskMonitor.setStatusMessage("Unknown network collection " + rootNetName + ". The task stops here.");
			return;
		}
		
		taskMonitor.setStatusMessage("Disconnecting " + ovTable.getTitle() + " table and " + rootNetName + ".");
		ovTable.disconnect(linkedRootNetwork);
	}

}
