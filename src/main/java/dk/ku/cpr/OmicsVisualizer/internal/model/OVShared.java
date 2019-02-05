package dk.ku.cpr.OmicsVisualizer.internal.model;

import org.cytoscape.model.CyTable;

public class OVShared {

	public static final String OV_PREFIX = "dk.ku.cpr.OmicsVisualizer.";
	
	public static final String CYTOPANEL_NAME = OV_PREFIX+"CytoPanel";
	
	public static final String OVTABLE_COLID_NAME = OV_PREFIX+"internalID";
	public static final String OVTABLE_COL_NODE_SUID = OV_PREFIX+"node_suid";

	public static final String CYPROPERTY_NAME = "OmicsVisualizer";
	
	public static final String PROPERTY_LINKED_NETWORK = OV_PREFIX+"linked_network";
	public static final String PROPERTY_MAPPING_OV_CY = OV_PREFIX+"OV_to_CyNetwork";
	public static final String PROPERTY_MAPPING_CY_OV = OV_PREFIX+"CyNetwork_to_OV";
	
	public static final String CYNETWORK_OVCOL = "Connected OVTables";
	
	// We forbid the class to have instances
	private OVShared() {
	}
	
	/**
	 * Indicates if the CyTable is an OVTable.
	 * @param table
	 * @return
	 */
	public static boolean isOVTable(CyTable table) {
		return !table.isPublic() && (table.getColumn(OVTABLE_COLID_NAME) != null);
	}
	
	/**
	 * Indicates if the column is specific to OVTables.
	 * @param colName
	 * @return
	 */
	public static boolean isOVCol(String colName) {
		return OV_PREFIX.regionMatches(0, colName, 0, OV_PREFIX.length());
	}
}
