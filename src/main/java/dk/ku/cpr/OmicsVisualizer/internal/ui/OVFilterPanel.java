package dk.ku.cpr.OmicsVisualizer.internal.ui;

import javax.swing.JPanel;

import org.cytoscape.util.swing.LookAndFeelUtil;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVFilter;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;

public abstract class OVFilterPanel extends JPanel {
	private static final long serialVersionUID = 9019444185386294472L;
	
	protected OVTable ovTable;
	protected OVFilterPanel parent;
	
	public OVFilterPanel(OVFilterPanel parent, OVTable ovTable) {
		this.parent = parent;
		this.ovTable = ovTable;
		
		this.setOpaque(!LookAndFeelUtil.isAquaLAF());
	}

	public abstract OVFilter getFilter();

	public abstract void setFilter(OVFilter ovFilter);
	
	/**
	 * Check if the filter is valid.
	 * If the filter is not valid, the bad formatted components will be changed to highlight the error.
	 * @return <code>true</code> if the filter is valid, <code>false</code> otherwise.
	 */
	public abstract boolean isFilterValid();

	/**
	 * Update the panel.
	 * @param up : should the update message be transmitted to the parent (<code>true</code>) or to the children (<code>false</code>).
	 */
	public abstract void update(boolean up);
}
