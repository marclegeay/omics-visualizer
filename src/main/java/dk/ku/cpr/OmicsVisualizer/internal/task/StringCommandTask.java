package dk.ku.cpr.OmicsVisualizer.internal.task;

import java.util.Map;

import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;

public class StringCommandTask extends AbstractTask {
	
	private final CommandExecutorTaskFactory commandExecutorTaskFactory;
	private final String command;
	private final Map<String, Object> args;
	private final TaskObserver taskObserver;

	public StringCommandTask(CommandExecutorTaskFactory commandExecutorTaskFactory, String command, Map<String, Object> args, TaskObserver taskObserver) {
		super();
		this.commandExecutorTaskFactory = commandExecutorTaskFactory;
		this.command = command;
		this.args=args;
		this.taskObserver=taskObserver;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		String title = this.command;
		if(title.equals(OVShared.STRING_CMD_LIST_SPECIES)) {
			title = "Retrieving list species from STRING";
		} else if(title.equals(OVShared.STRING_CMD_PROTEIN_QUERY)) {
			title = "Retrieving STRING Network";
		}
		taskMonitor.setTitle(title);
		
		TaskIterator task = commandExecutorTaskFactory.createTaskIterator("string", command, args, this.taskObserver);
		insertTasksAfterCurrentTask(task);
	}
}
