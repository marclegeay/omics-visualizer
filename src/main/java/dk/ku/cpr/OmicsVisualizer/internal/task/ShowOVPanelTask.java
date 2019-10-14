package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.ui.OVCytoPanel;

public class ShowOVPanelTask extends AbstractTask {
	
	private OVManager ovManager;
	
	public ShowOVPanelTask(OVManager ovManager) {
		super();
		this.ovManager=ovManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		OVCytoPanel panel = this.ovManager.getOVCytoPanel();

		if (panel == null) {
			panel = new OVCytoPanel(this.ovManager);
		} else {
			panel.initPanel(null);
		}
	}

}
