package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVVisualization.ChartType;

public class VisualizationTaskFactory extends AbstractTaskFactory {
	
	private OVManager ovManager;
	private boolean isContinuous;
	private ChartType chartType;

	public VisualizationTaskFactory(OVManager ovManager, boolean isContinuous, ChartType chartType) {
		super();
		this.ovManager = ovManager;
		this.isContinuous = isContinuous;
		this.chartType = chartType;
	}

	@Override
	public TaskIterator createTaskIterator() {
		Task t;
		
		if(isContinuous) {
			t = new VisualizationContinuousTask(ovManager, chartType);
		} else {
			t = new VisualizationDiscreteTask(ovManager, chartType);
		}
		
		return new TaskIterator(t);
	}

}
