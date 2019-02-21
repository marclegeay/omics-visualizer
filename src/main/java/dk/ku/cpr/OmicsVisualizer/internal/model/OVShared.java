package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.awt.Color;
import java.util.Iterator;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;

public class OVShared {

	public static final String OV_PREFIX = "dk.ku.cpr.OmicsVisualizer.";
	
	public static final String CYTOPANEL_NAME = OV_PREFIX+"CytoPanel";
	
	public static final String OVTABLE_COLID_NAME = OV_PREFIX+"internalID";

	public static final String CYPROPERTY_NAME = "OmicsVisualizer";
	
//	public static final String PROPERTY_LINKED_NETWORK = OV_PREFIX+"linked_network";
//	public static final String PROPERTY_MAPPING_OV_CY = OV_PREFIX+"OV_to_CyNetwork";
//	public static final String PROPERTY_MAPPING_CY_OV = OV_PREFIX+"CyNetwork_to_OV";
	
	public static final String CYNETWORKTABLE_OVCOL = "OVTable";
	public static final String CYNETWORKTABLE_STYLECOL="OVStyle";

	public static final String CYNODETABLE_STYLECOL="OVStyle";
	public static final String CYNODETABLE_STYLECOL_VALUES="OVStyle Values Circle ";
	
	public static final String STRING_CMD_PROTEIN_QUERY = "protein query";
	public static final String STRING_CMD_LIST_SPECIES = "list species";
	
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

	
	/**
	 * Deletes the specific columns in a node table
	 * @param cyTable
	 */
	public static void deleteOVColumns(CyTable cyTable) {
		for(Iterator<CyColumn> cycolIt = cyTable.getColumns().iterator(); cycolIt.hasNext();) {
			CyColumn cycol = cycolIt.next();
			
			if(cycol.getName().startsWith(OVShared.CYNODETABLE_STYLECOL)) {
				cyTable.deleteColumn(cycol.getName());
			}
		}
	}
	
	public static String color2String(Color color) {
		return String.format("#%02x%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}
}
