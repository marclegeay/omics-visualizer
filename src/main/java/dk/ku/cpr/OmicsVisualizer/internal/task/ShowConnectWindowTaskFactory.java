package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

public class ShowConnectWindowTaskFactory extends AbstractTaskFactory {
	
	private OVManager ovManager;

	public ShowConnectWindowTaskFactory(OVManager ovManager) {
		super();
		this.ovManager = ovManager;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ShowConnectWindowTask(ovManager));
	}

	@Override
	public boolean isReady() {
		return super.isReady()
				&& (this.ovManager.getActiveOVTable() != null)
				&& (this.ovManager.getNetworkManager().getNetworkSet().size() > 0);
	}
}
