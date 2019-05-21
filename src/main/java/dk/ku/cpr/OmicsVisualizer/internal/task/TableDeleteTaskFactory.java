package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

public class TableDeleteTaskFactory extends AbstractTaskFactory {
	
	private OVManager ovManager;

	public TableDeleteTaskFactory(OVManager ovManager) {
		super();
		this.ovManager = ovManager;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new TableDeleteTask(ovManager));
	}

}
