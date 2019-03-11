package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class OperatorListTaskFactory extends AbstractTaskFactory {

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new OperatorListTask());
	}

}
