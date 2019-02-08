package dk.ku.cpr.OmicsVisualizer.internal.tableimport.task;

import java.util.Properties;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.ui.OVCytoPanel;

public class ShowOVPanelTask extends AbstractTask {
	
	private OVManager ovManager;
	
	public ShowOVPanelTask(OVManager ovManager) {
		super();
		this.ovManager=ovManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		CySwingApplication swingApplication = this.ovManager.getService(CySwingApplication.class);
		CytoPanel cytoPanel = swingApplication.getCytoPanel(CytoPanelName.SOUTH);

		if (cytoPanel.indexOfComponent(OVShared.CYTOPANEL_NAME) < 0) {
			CytoPanelComponent2 panel = new OVCytoPanel(this.ovManager);

			// Register it
			this.ovManager.registerService(panel, CytoPanelComponent.class, new Properties());
			this.ovManager.registerService(panel, RowsSetListener.class, new Properties());

			if (cytoPanel.getState() == CytoPanelState.HIDE)
				cytoPanel.setState(CytoPanelState.DOCK);

			cytoPanel.setSelectedIndex(
					cytoPanel.indexOfComponent(OVShared.CYTOPANEL_NAME));

		} else {
			OVCytoPanel panel = (OVCytoPanel) cytoPanel.getComponentAt(
					cytoPanel.indexOfComponent(OVShared.CYTOPANEL_NAME));
			panel.initPanel(null);
		}
	}

}
