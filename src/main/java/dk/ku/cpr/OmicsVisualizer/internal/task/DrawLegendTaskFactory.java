package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVLegend;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

public class DrawLegendTaskFactory extends AbstractTaskFactory {
	
	private OVManager ovManager;
	private OVLegend ovLegend;
	private boolean updateView;
	
	public DrawLegendTaskFactory(OVManager ovManager, OVLegend ovLegend, boolean updateView) {
		this.ovManager=ovManager;
		this.ovLegend=ovLegend;
		this.updateView=updateView;
	}
	
	public DrawLegendTaskFactory(OVManager ovManager) {
		this(ovManager, null, true);
	}

	@Override
	public TaskIterator createTaskIterator() {
		if(this.ovLegend != null) {
			return new TaskIterator(new DrawLegendTask(ovManager, ovLegend, updateView));
		}
		
		return new TaskIterator(new DrawLegendTunableTask(ovManager));
	}

}
