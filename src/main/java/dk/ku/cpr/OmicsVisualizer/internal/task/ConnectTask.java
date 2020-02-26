package dk.ku.cpr.OmicsVisualizer.internal.task;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVConnection;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;

public class ConnectTask extends AbstractTask implements ObservableTask {
	
	protected OVManager ovManager;
	protected OVTable ovTable;
	protected CyNetwork cyNetwork;
	protected String keyColNet;
	protected String keyColTable;
	
	private OVConnection resultCon;
	
	public ConnectTask(OVManager ovManager, OVTable ovTable, CyNetwork cyNetwork, String keyColNet,
			String keyColTable) {
		super();
		this.ovManager = ovManager;
		this.ovTable = ovTable;
		this.cyNetwork = cyNetwork;
		this.keyColNet = keyColNet;
		this.keyColTable = keyColTable;
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
		
		CyNetwork network = this.ovManager.getService(CyApplicationManager.class).getCurrentNetwork();
		if(network == null) {
			taskMonitor.setStatusMessage("No current network. The task stops here.");
			return;
		}

		taskMonitor.setStatusMessage("Connecting " + ovTable.getTitle() + " table with " + network.toString() + ".");
		taskMonitor.setStatusMessage("Network key column: " + keyColNet);
		taskMonitor.setStatusMessage("Table key column: " + keyColTable);
		
		this.resultCon = ovTable.connect(network, keyColNet, keyColTable);
		
		if(this.ovManager.getOVCytoPanel() != null) {
			this.ovManager.getOVCytoPanel().update();
		}
		
		taskMonitor.setStatusMessage(this.resultCon.getNbConnectedTableRows()+" rows from the table are connected.");
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(type.equals(String.class)) {
			return (R) String.valueOf(this.resultCon.getNbConnectedTableRows());
		} else if(type.equals(OVConnection.class)) {
			return (R) this.resultCon;
		}
		return null;
	}
	
	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(OVConnection.class, String.class);
	}

}
