package dk.ku.cpr.OmicsVisualizer.external.tableimport.task;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;


public class LoadOVTableFileTaskFactory extends AbstractTaskFactory {

	private final OVManager ovManager;

	public LoadOVTableFileTaskFactory(final OVManager ovManager) {
		this.ovManager = ovManager;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(2, new LoadOVTableFileTask(ovManager));
	}
}
