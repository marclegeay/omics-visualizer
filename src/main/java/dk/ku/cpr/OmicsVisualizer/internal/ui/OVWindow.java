package dk.ku.cpr.OmicsVisualizer.internal.ui;

import javax.swing.JDialog;

import org.cytoscape.application.swing.CySwingApplication;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

public abstract class OVWindow extends JDialog {
	private static final long serialVersionUID = 3893945165182884004L;
	
	protected OVManager ovManager;
	
	public OVWindow(OVManager ovManager, String title) {
		super(ovManager.getService(CySwingApplication.class).getJFrame(), title);
		
		this.ovManager=ovManager;
		
		this.setModal(true);
		this.setResizable(false);
	}
	
	public OVWindow(OVManager ovManager) {
		this(ovManager, "");
	}
}
