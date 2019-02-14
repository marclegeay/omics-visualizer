package dk.ku.cpr.OmicsVisualizer.internal.task;

import java.util.Map;

import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;

public class StringCommandTask extends AbstractTask {
	
	private final OVManager ovManager;
	private final String command;
	private final Map<String, Object> args;
	private final TaskObserver taskObserver;

	public StringCommandTask(OVManager ovManager, String command, Map<String, Object> args, TaskObserver taskObserver) {
		super();
		this.ovManager = ovManager;
		this.command = command;
		this.args=args;
		this.taskObserver=taskObserver;
	}
	
	public String toString() {
		return "STRING \""+this.command+"\" command";
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
		
		CommandExecutorTaskFactory commandExecutorTaskFactory = this.ovManager.getService(CommandExecutorTaskFactory.class);
		TaskIterator task = commandExecutorTaskFactory.createTaskIterator("string", this.command, this.args, this.taskObserver);
		insertTasksAfterCurrentTask(task);
	}
}
