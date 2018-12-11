package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.util.Properties;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import dk.ku.cpr.OmicsVisualizer.internal.model.OmicsVisualizerShared;

public class ShowOmicsVisualizerPanelTask extends AbstractTask {
	
	private CyServiceRegistrar serviceRegistrar;
	
	public ShowOmicsVisualizerPanelTask(CyServiceRegistrar serviceRegistrar) {
		super();
		this.serviceRegistrar=serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		CySwingApplication swingApplication = this.serviceRegistrar.getService(CySwingApplication.class);
		CytoPanel cytoPanel = swingApplication.getCytoPanel(CytoPanelName.SOUTH);

		if (cytoPanel.indexOfComponent(OmicsVisualizerShared.CYTOPANEL_NAME) < 0) {
			CytoPanelComponent2 panel = new OmicsVisualizerCytoPanel(this.serviceRegistrar);

			// Register it
			this.serviceRegistrar.registerService(panel, CytoPanelComponent.class, new Properties());
			this.serviceRegistrar.registerService(panel, RowsSetListener.class, new Properties());

			if (cytoPanel.getState() == CytoPanelState.HIDE)
				cytoPanel.setState(CytoPanelState.DOCK);

			cytoPanel.setSelectedIndex(
					cytoPanel.indexOfComponent(OmicsVisualizerShared.CYTOPANEL_NAME));

		} else {
			OmicsVisualizerCytoPanel panel = (OmicsVisualizerCytoPanel) cytoPanel.getComponentAt(
					cytoPanel.indexOfComponent(OmicsVisualizerShared.CYTOPANEL_NAME));
			panel.initPanel(null);
		}
	}

}
