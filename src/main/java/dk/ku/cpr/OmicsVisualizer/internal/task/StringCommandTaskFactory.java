package dk.ku.cpr.OmicsVisualizer.internal.task;

import java.util.Map;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

public class StringCommandTaskFactory extends AbstractTaskFactory {
	
	private final OVManager ovManager;
	private final String command;
	private final Map<String, Object> args;
	private final TaskObserver taskObserver;

	public StringCommandTaskFactory(OVManager ovManager, String command, Map<String, Object> args, TaskObserver taskObserver) {
		super();
		this.ovManager = ovManager;
		this.command = command;
		this.args=args;
		this.taskObserver=taskObserver;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new StringCommandTask(ovManager, command, args, taskObserver));
	}
}
