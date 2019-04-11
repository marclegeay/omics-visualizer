package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVConnection;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVVisualization;

public class ApplyVisualizationTaskFactory extends AbstractTaskFactory {
	private OVManager ovManager;
	private OVConnection ovCon;
	private OVVisualization ovViz;

	public ApplyVisualizationTaskFactory(OVManager ovManager, OVConnection ovCon, OVVisualization ovViz) {
		super();
		this.ovManager = ovManager;
		this.ovCon = ovCon;
		this.ovViz=ovViz;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ApplyVisualizationTask(ovManager, ovCon, ovViz));
	}

}
