package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class VersionTaskFactory extends AbstractTaskFactory {
	private String version;

	public VersionTaskFactory(String version) {
		super();
		this.version = version;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new VersionTask(version));
	}

}
