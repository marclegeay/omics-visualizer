package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.ui.OVCytoPanel;

public class ShowVisualizationWindowTaskFactory extends AbstractTaskFactory {
	
	private OVManager ovManager;

	public ShowVisualizationWindowTaskFactory(OVManager ovManager) {
		super();
		this.ovManager = ovManager;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ShowVisualizationWindowTask(ovManager));
	}

	@Override
	public boolean isReady() {
		OVCytoPanel ovPanel = this.ovManager.getOVCytoPanel();
		if(ovPanel == null) {
			return false;
		}
		
		return super.isReady() 
				&& ovPanel.getDisplayedTable() != null
				&& ovPanel.getDisplayedTable().isConnected();
	}
}
