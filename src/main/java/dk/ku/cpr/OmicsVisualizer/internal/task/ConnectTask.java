package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;

public class ConnectTask extends AbstractTask {
	private OVManager ovManager;
	
	@Tunable(description="Name of the Network Collection to connect the table with.",
			required=true,
			tooltip="Usually the Network Collection's name is the same as the main Network in the collection, but the name of the collection can be changed separately.",
			exampleStringValue="String Network",
			gravity=1.0)
	public String rootNetName;
	
	@Tunable(description="Key column from the Network node table used to map the network with the table.",
			required=true,
			exampleStringValue="query term",
			gravity=1.0)
	public String mappingColNet;
	
	@Tunable(description="Key column from the Omics Visualizer table used to map the table with the network.",
			required=true,
			exampleStringValue="Uniprot",
			gravity=1.0)
	public String mappingColTable;

	public ConnectTask(OVManager ovManager) {
		super();
		this.ovManager = ovManager;
	}
	
	@ProvidesTitle
	public String getName() {
		return "Connect Omics Visualizer Table";
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle(this.getName());
		
		OVTable ovTable = this.ovManager.getActiveOVTable();
		
		if(ovTable == null) {
			taskMonitor.setStatusMessage("No active Omics Visualizer table. The task stops here.");
			return;
		}

		taskMonitor.setStatusMessage("Connecting " + ovTable.getTitle() + " table with " + rootNetName + ".");
		taskMonitor.setStatusMessage("Network key column: " + mappingColNet);
		taskMonitor.setStatusMessage("Table key column: " + mappingColTable);
		
		ovTable.connect(rootNetName, mappingColNet, mappingColTable);
		
		if(this.ovManager.getOVCytoPanel() != null) {
			this.ovManager.getOVCytoPanel().update();
		}
	}

}
