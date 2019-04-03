package dk.ku.cpr.OmicsVisualizer.internal.ui;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;

public class OVFilterRootPanel extends OVFilterSetPanel {
	private static final long serialVersionUID = 1335657075915750533L;

	private OVFilterWindow parentWindow;
	
	public OVFilterRootPanel(OVFilterWindow parentWindow, OVTable ovTable, OVManager ovManager) {
		super(null, ovTable, ovManager);
		
		this.parentWindow = parentWindow;
	}
	
	@Override
	public void update(boolean up) {
		super.update(up);
		
		if(up && this.parentWindow != null) {
			this.parentWindow.update();
		}
	}
}
