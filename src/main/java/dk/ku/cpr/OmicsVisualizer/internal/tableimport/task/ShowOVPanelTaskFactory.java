package dk.ku.cpr.OmicsVisualizer.internal.ui;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

public class ShowOVPanelTaskFactory extends AbstractTaskFactory {
	
	private OVManager ovManager;
	
	public ShowOVPanelTaskFactory(OVManager ovManager) {
		super();
		this.ovManager=ovManager;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ShowOVPanelTask(this.ovManager));
	}

}
