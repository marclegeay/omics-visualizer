package dk.ku.cpr.OmicsVisualizer.internal.model;

import javax.swing.Icon;

import org.cytoscape.application.swing.CyColumnPresentation;

public class OVColumnPresentation implements CyColumnPresentation {

	public static final int ICON_SIZE=16;
	
	private OVManager ovManager;
	
	public OVColumnPresentation(OVManager ovManager) {
		this.ovManager = ovManager;
	}
	
	//*
	@Override
	public Icon getNamespaceIcon() {
		return this.ovManager.getLogoIcon(ICON_SIZE);
	}
	//*/
	
	/*
	@Override
	public Icon getNamespaceIcon() {
		return new ImageIcon((new ImageIcon(this.getClass().getResource("/images/ov_logo.png"))).getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_AREA_AVERAGING));
		
		//return new TextIcon(texts, this.ovManager.getIconFont().deriveFont(15.0f), colors, ICON_SIZE, ICON_SIZE);
	}
	//*/

	@Override
	public String getNamespaceDescription() {
		return OVShared.OV_COLUMN_NAMESPACE;
	}

}
