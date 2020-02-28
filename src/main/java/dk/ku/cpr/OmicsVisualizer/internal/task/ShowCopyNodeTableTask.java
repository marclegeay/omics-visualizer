package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.ui.CopyNodeTableWindow;
import dk.ku.cpr.OmicsVisualizer.internal.utils.ViewUtil;

public class ShowCopyNodeTableTask extends AbstractTask {
	
	private OVManager ovManager;

	public ShowCopyNodeTableTask(OVManager ovManager) {
		super();
		this.ovManager = ovManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		CopyNodeTableWindow window = new CopyNodeTableWindow(this.ovManager);
		
		// JFrame.setVisible blocks the current thread
		// So we invoke this in an other thread
		ViewUtil.invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				window.setVisible(true);
			}
		});
	}

}
