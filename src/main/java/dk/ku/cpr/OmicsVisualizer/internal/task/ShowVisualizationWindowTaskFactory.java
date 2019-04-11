package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVVisualization.ChartType;
import dk.ku.cpr.OmicsVisualizer.internal.ui.OVCytoPanel;

public class ShowVisualizationWindowTaskFactory extends AbstractTaskFactory {
	
	private OVManager ovManager;
	private ChartType type;

	public ShowVisualizationWindowTaskFactory(OVManager ovManager, ChartType type) {
		super();
		this.ovManager = ovManager;
		this.type = type;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ShowVisualizationWindowTask(ovManager, type));
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
