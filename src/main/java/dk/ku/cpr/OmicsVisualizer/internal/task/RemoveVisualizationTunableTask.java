package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.work.TaskMonitor;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

public class RemoveVisualizationTunableTask extends RemoveVisualizationTask {
	
	public RemoveVisualizationTunableTask(OVManager ovManager, String type) {
		super(ovManager, null, type);
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// First we retrieve the current Network
		CyNetwork currentNetwork = this.ovManager.getService(CyApplicationManager.class).getCurrentNetwork();
		
		if(currentNetwork == null) {
			return;
		}
		
		// Then we identify the connection
		CyRootNetworkManager rootNetManager = this.ovManager.getService(CyRootNetworkManager.class);
		this.ovCon = this.ovManager.getConnection(rootNetManager.getRootNetwork(currentNetwork));
		
		// Finally we can run the Task
		super.run(taskMonitor);
	}
}
