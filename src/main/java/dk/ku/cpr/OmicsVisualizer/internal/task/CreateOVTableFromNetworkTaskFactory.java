package dk.ku.cpr.OmicsVisualizer.internal.task;

import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

public class CreateOVTableFromNetworkTaskFactory extends AbstractTaskFactory {
	
	private OVManager ovManager;
	private CyNetwork cyNetwork;
	private String keyCyTableColName;
	private List<String> cyTableColNames;
	private String tableName;
	private String valuesColName;
	private String srcColName;
	
	public CreateOVTableFromNetworkTaskFactory(OVManager ovManager, CyNetwork cyNetwork, String keyCyTableColName,
			List<String> cyTableColNames, String tableName, String valuesColName, String srcColName) {
		super();
		this.ovManager = ovManager;
		this.cyNetwork = cyNetwork;
		this.keyCyTableColName = keyCyTableColName;
		this.cyTableColNames = cyTableColNames;
		this.tableName = tableName;
		this.valuesColName = valuesColName;
		this.srcColName = srcColName;
	}

	public CreateOVTableFromNetworkTaskFactory(OVManager ovManager) {
		this(ovManager, null, null, null, null, null, null);
	}

	@Override
	public TaskIterator createTaskIterator() {
		if(this.cyNetwork == null) {
			return new TaskIterator(new CreateOVTableFromNetworkTunableTask(ovManager));
		}
		return new TaskIterator(new CreateOVTableFromNetworkTask(ovManager, cyNetwork, keyCyTableColName, cyTableColNames, tableName, valuesColName, srcColName));
	}

}
