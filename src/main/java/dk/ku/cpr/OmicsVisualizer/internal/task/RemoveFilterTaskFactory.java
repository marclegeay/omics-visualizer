package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;
import dk.ku.cpr.OmicsVisualizer.internal.ui.OVCytoPanel;

public class RemoveFilterTaskFactory extends AbstractTaskFactory {
	private OVManager ovManager;
	
	public RemoveFilterTaskFactory(OVManager ovManager) {
		super();
		this.ovManager=ovManager;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new RemoveFilterTask(this.ovManager));
	}

	public TaskIterator createTaskIterator(OVTable ovTable) {
		return new TaskIterator(new RemoveFilterTask(this.ovManager, ovTable));
	}

	public TaskIterator createTaskIterator(OVTable ovTable, OVCytoPanel ovPanel) {
		return new TaskIterator(new RemoveFilterTask(this.ovManager, ovTable, ovPanel));
	}
	
	@Override
	public boolean isReady() {
		OVTable activeOVTable = this.ovManager.getActiveOVTable();
		
		if(activeOVTable == null) {
			return false;
		}
		
		return super.isReady() && (activeOVTable.getFilter() != null);
	}
}
