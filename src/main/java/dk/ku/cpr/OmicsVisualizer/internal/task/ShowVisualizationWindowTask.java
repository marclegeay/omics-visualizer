package dk.ku.cpr.OmicsVisualizer.internal.task;

import javax.swing.JOptionPane;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVVisualization.ChartType;
import dk.ku.cpr.OmicsVisualizer.internal.ui.OVCytoPanel;
import dk.ku.cpr.OmicsVisualizer.internal.ui.OVVisualizationWindow;
import dk.ku.cpr.OmicsVisualizer.internal.utils.ViewUtil;

public class ShowVisualizationWindowTask extends AbstractTask {
	
	private OVManager ovManager;
	private ChartType type;

	public ShowVisualizationWindowTask(OVManager ovManager, ChartType type) {
		super();
		this.ovManager = ovManager;
		this.type = type;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// First we make sure that enhancedGraphics is installed and enabled
		AvailableCommands availableCommands = (AvailableCommands) this.ovManager.getService(AvailableCommands.class);
		if (!availableCommands.getNamespaces().contains("enhancedGraphics")) {
			JOptionPane.showMessageDialog(this.ovManager.getService(CySwingApplication.class).getJFrame(),
					"You need to install enhancedGraphics from the App Manager or Cytoscape App Store.",
					"Dependency error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		OVCytoPanel ovPanel = this.ovManager.getOVCytoPanel();
		if(ovPanel != null) {
			OVVisualizationWindow ovVizWindow;
			
			if(this.type.equals(ChartType.PIE)) { // inner
				ovVizWindow = ovPanel.getVizInnerWindow();
			} else { // outer
				ovVizWindow = ovPanel.getVizOuterWindow();
			}
			
			ovVizWindow.setTable(ovPanel.getDisplayedTable());
			// JDialog.setVisible blocks the current thread
			// So we invoke this in an other thread
			ViewUtil.invokeOnEDT(new Runnable() {
				@Override
				public void run() {
					ovVizWindow.setVisible(true);
				}
			});
		}
	}

}
