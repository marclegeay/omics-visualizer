package dk.ku.cpr.OmicsVisualizer.internal.ui;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ShowSiteSpecificPanelTaskFactory extends AbstractTaskFactory {
	
	private CyServiceRegistrar serviceRegistrar;
	
	public ShowSiteSpecificPanelTaskFactory(CyServiceRegistrar serviceRegistrar) {
		super();
		this.serviceRegistrar=serviceRegistrar;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ShowSiteSpecificPanelTask(this.serviceRegistrar));
	}

}
