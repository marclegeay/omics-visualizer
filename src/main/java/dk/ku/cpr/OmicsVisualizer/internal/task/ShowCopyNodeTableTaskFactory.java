package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

public class ShowCopyNodeTableTaskFactory extends AbstractTaskFactory {
	
	private OVManager ovManager;

	public ShowCopyNodeTableTaskFactory(OVManager ovManager) {
		super();
		this.ovManager = ovManager;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ShowCopyNodeTableTask(this.ovManager));
	}
	
	@Override
	public boolean isReady() {
		return !this.ovManager.getNetworkManager().getNetworkSet().isEmpty();
	}

}
