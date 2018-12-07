package dk.ku.cpr.OmicsVisualizer.internal.ui;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ShowOmicsVisualizerPanelTaskFactory extends AbstractTaskFactory {
	
	private CyServiceRegistrar serviceRegistrar;
	
	public ShowOmicsVisualizerPanelTaskFactory(CyServiceRegistrar serviceRegistrar) {
		super();
		this.serviceRegistrar=serviceRegistrar;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ShowOmicsVisualizerPanelTask(this.serviceRegistrar));
	}

}
