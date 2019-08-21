package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVLegend;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

public class DrawLegendTaskFactory extends AbstractTaskFactory {
	
	private OVManager ovManager;
	private OVLegend ovLegend;
	
	public DrawLegendTaskFactory(OVManager ovManager, OVLegend ovLegend) {
		this.ovManager=ovManager;
		this.ovLegend=ovLegend;
	}
	
	public DrawLegendTaskFactory(OVManager ovManager) {
		this(ovManager, null);
	}

	@Override
	public TaskIterator createTaskIterator() {
		if(this.ovLegend != null) {
			return new TaskIterator(new DrawLegendTask(ovManager, ovLegend));
		}
		// TODO Tunable
		return null;
	}

}
