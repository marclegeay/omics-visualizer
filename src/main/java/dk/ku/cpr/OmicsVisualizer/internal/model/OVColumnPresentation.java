package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.awt.Color;

import javax.swing.Icon;

import org.cytoscape.application.swing.CyColumnPresentation;

import dk.ku.cpr.OmicsVisualizer.internal.utils.TextIcon;

public class OVColumnPresentation implements CyColumnPresentation {

	public static final int ICON_SIZE=16;
	
	private OVManager ovManager;
	
	public OVColumnPresentation(OVManager ovManager) {
		this.ovManager = ovManager;
	}
	
	@Override
	public Icon getNamespaceIcon() {
		char chart = 'e'; // pie
		
		String texts[] = {
				Character.toString((char) (chart+1)),
				Character.toString((char) (chart+2)),
				Character.toString((char) (chart+3))
		};
		Color colors[] = {
//				// blue - white - red
//				new Color(33,102,172),
//				new Color(247,247,247),
//				new Color(178,24,43)
				// Viridis colors
				new Color(68,1,84),
				new Color(33,145,140),
				new Color(253,231,37)
		};
		
		return new TextIcon(texts, this.ovManager.getIconFont().deriveFont(15.0f), colors, ICON_SIZE, ICON_SIZE);
	}

	@Override
	public String getNamespaceDescription() {
		return OVShared.OV_COLUMN_NAMESPACE;
	}

}
