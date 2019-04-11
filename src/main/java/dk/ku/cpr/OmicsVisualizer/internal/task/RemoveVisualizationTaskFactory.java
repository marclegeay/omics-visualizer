package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVConnection;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

public class RemoveVisualizationTaskFactory extends AbstractTaskFactory {
	private OVManager ovManager;
	private OVConnection ovCon;
	private String type;

	public RemoveVisualizationTaskFactory(OVManager ovManager, OVConnection ovCon, String type) {
		super();
		this.ovManager = ovManager;
		this.ovCon = ovCon;
		this.type = type;
	}

	public RemoveVisualizationTaskFactory(OVManager ovManager, OVConnection ovCon) {
		this(ovManager, ovCon, "all"); // by default we remove all visualizations
	}
	
	public RemoveVisualizationTaskFactory(OVManager ovManager, String type) {
		this(ovManager, null, type);
	}

	@Override
	public TaskIterator createTaskIterator() {
		if(ovCon == null) {
			return new TaskIterator(new RemoveVisualizationTunableTask(ovManager, type));
		}
		return new TaskIterator(new RemoveVisualizationTask(ovManager, ovCon, type));
	}

}
