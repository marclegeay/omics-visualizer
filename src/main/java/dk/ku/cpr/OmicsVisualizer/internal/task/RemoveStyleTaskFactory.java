package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVConnection;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

public class RemoveStyleTaskFactory extends AbstractTaskFactory {
	private OVManager ovManager;
	private OVConnection ovCon;

	public RemoveStyleTaskFactory(OVManager ovManager, OVConnection ovCon) {
		super();
		this.ovManager = ovManager;
		this.ovCon = ovCon;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new RemoveStyleTask(ovManager, ovCon));
	}

}
