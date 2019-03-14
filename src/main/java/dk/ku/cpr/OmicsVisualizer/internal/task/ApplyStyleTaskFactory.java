package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVConnection;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

public class ApplyStyleTaskFactory extends AbstractTaskFactory {
	private OVManager ovManager;
	private OVConnection ovCon;
	private boolean onlyFiltered;

	public ApplyStyleTaskFactory(OVManager ovManager, OVConnection ovCon, boolean onlyFiltered) {
		super();
		this.ovManager = ovManager;
		this.ovCon = ovCon;
		this.onlyFiltered=onlyFiltered;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ApplyStyleTask(ovManager, ovCon, onlyFiltered));
	}

}
