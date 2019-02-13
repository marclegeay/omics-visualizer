package dk.ku.cpr.OmicsVisualizer.internal.task;

import java.util.Map;

import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;

public class StringCommandTaskFactory extends AbstractTaskFactory {
	
	private final CommandExecutorTaskFactory commandExecutorTaskFactory;
	private final String command;
	private final Map<String, Object> args;
	private final TaskObserver taskObserver;

	public StringCommandTaskFactory(CommandExecutorTaskFactory commandExecutorTaskFactory, String command, Map<String, Object> args, TaskObserver taskObserver) {
		super();
		this.commandExecutorTaskFactory = commandExecutorTaskFactory;
		this.command = command;
		this.args=args;
		this.taskObserver=taskObserver;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new StringCommandTask(commandExecutorTaskFactory, command, args, taskObserver));
	}
}
