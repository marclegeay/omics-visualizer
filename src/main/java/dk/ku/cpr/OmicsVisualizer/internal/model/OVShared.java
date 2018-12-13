package dk.ku.cpr.OmicsVisualizer.internal.model;

import org.cytoscape.model.CyTable;

public class OVShared {
	
	public static final String CYTOPANEL_NAME = "dk.ku.cpr.OmicsVisualizer.CytoPanel";
	
	public static final String OVTABLE_COLID_NAME = "dk.ku.cpr.OmicsVisualizer.internalID";
	
	public static final String MAPPING_NODE_CUSTOM_COL = "mapping_node_custom";
	public static final String MAPPING_CUSTOM_NODE_COL = "mapping_custom_node";
	public static final String CUSTOM_SUID_COL = "custom_suid";

	public static final String CYPROPERTY_NAME = "OmicsVisualizer";
	
	// We forbid the class to have instances
	private OVShared() {
	}
	
	public static boolean isOVTable(CyTable table) {
		return !table.isPublic() && (table.getColumn(OVTABLE_COLID_NAME) != null);
	}
}
