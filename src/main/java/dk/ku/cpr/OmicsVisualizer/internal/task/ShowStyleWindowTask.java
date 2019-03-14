package dk.ku.cpr.OmicsVisualizer.internal.task;

import javax.swing.JOptionPane;

import org.cytoscape.command.AvailableCommands;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.ui.OVCytoPanel;

public class ShowStyleWindowTask extends AbstractTask {
	
	private OVManager ovManager;

	public ShowStyleWindowTask(OVManager ovManager) {
		super();
		this.ovManager = ovManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// First we make sure that enhancedGraphics is installed and enabled
		AvailableCommands availableCommands = (AvailableCommands) this.ovManager.getService(AvailableCommands.class);
		if (!availableCommands.getNamespaces().contains("enhancedGraphics")) {
			JOptionPane.showMessageDialog(null,
					"You need to install enhancedGraphics from the App Manager or Cytoscape App Store.",
					"Dependency error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		OVCytoPanel ovPanel = this.ovManager.getOVCytoPanel();
		if(ovPanel != null) {
			ovPanel.getStyleWindow().setTable(ovPanel.getDisplayedTable());
			ovPanel.getStyleWindow().setVisible(true);
		}
	}

}
