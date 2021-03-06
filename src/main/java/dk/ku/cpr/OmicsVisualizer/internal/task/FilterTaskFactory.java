package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;
import dk.ku.cpr.OmicsVisualizer.internal.ui.OVCytoPanel;

public class FilterTaskFactory extends AbstractTaskFactory {
	private OVManager ovManager;
	private OVCytoPanel ovPanel;

	public FilterTaskFactory(OVManager ovManager) {
		this(ovManager, null);
	}

	public FilterTaskFactory(OVManager ovManager, OVCytoPanel ovPanel) {
		super();
		this.ovManager = ovManager;
		this.ovPanel = ovPanel;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new FilterTunableTask(this.ovManager, this.ovPanel));
	}

	public TaskIterator createTaskIterator(OVTable ovTable) {
		return new TaskIterator(new FilterTask(this.ovManager, this.ovPanel, ovTable));
	}
	
	@Override
	public boolean isReady() {
		return super.isReady() && (this.ovManager.getActiveOVTable()!=null);
	}
}
