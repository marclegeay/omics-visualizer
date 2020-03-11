package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

public class ConnectTunableTask extends ConnectTask {
	@Tunable(description="Key column from the Network node table used to map the network with the table. The column should be a shared column.",
			required=true,
			exampleStringValue="query term",
			gravity=1.0)
	public String mappingColNet;
	
	@Tunable(description="Key column from the Omics Visualizer table used to map the table with the network.",
			required=true,
			exampleStringValue="Uniprot",
			gravity=1.0)
	public String mappingColTable;

	public ConnectTunableTask(OVManager ovManager) {
		super(ovManager, null, null, null, null);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.ovTable = this.ovManager.getActiveOVTable();
		this.cyNetwork = this.ovManager.getService(CyApplicationManager.class).getCurrentNetwork();
		this.keyColNet = this.mappingColNet;
		this.keyColTable = this.mappingColTable;

		super.run(taskMonitor);
	}

}
