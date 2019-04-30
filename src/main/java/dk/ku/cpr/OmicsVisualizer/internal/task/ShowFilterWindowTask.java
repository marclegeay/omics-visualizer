package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.ui.OVCytoPanel;
import dk.ku.cpr.OmicsVisualizer.internal.utils.ViewUtil;

public class ShowFilterWindowTask extends AbstractTask {
	private OVManager ovManager;

	public ShowFilterWindowTask(OVManager ovManager) {
		super();
		this.ovManager = ovManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		OVCytoPanel ovPanel = this.ovManager.getOVCytoPanel();

		if(ovPanel != null) {
			// JDialog.setVisible blocks the current thread
			// So we invoke this in an other thread
			ViewUtil.invokeOnEDT(new Runnable() {
				@Override
				public void run() {
					ovPanel.getFilterWindow().setVisible(true);
				}
			});
		}
	}

}
