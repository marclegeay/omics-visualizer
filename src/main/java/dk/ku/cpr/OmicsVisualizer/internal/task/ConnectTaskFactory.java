package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;

public class ConnectTaskFactory extends AbstractTaskFactory {
	
	private OVManager ovManager;
	private OVTable ovTable;
	private CyNetwork cyNetwork;
	private String keyColNet;
	private String keyColTable;

	public ConnectTaskFactory(OVManager ovManager, OVTable ovTable, CyNetwork cyNetwork, String keyColNet,
			String keyColTable) {
		super();
		this.ovManager = ovManager;
		this.ovTable = ovTable;
		this.cyNetwork = cyNetwork;
		this.keyColNet = keyColNet;
		this.keyColTable = keyColTable;
	}

	public ConnectTaskFactory(OVManager ovManager) {
		this(ovManager, null, null, null, null);
	}

	@Override
	public TaskIterator createTaskIterator() {
		if(this.ovTable == null) {
			return new TaskIterator(new ConnectTunableTask(ovManager));
		}
		
		return new TaskIterator(new ConnectTask(ovManager, ovTable, cyNetwork, keyColNet, keyColTable));
	}

}
