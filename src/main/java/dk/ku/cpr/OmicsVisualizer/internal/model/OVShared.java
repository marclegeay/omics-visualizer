package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.awt.Color;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.stream.Collectors;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVVisualization.ChartType;

public class OVShared {
	public static final String OV_PREFIX = "dk.ku.cpr.OmicsVisualizer.";
	
	public static final String OV_PREFERRED_MENU = "Apps.Omics Visualizer";
	public static final String OV_COMMAND_NAMESPACE = "ov";
	
	public static final String OVTABLE_DEFAULT_NAME = "Omics Visualizer Table ";
	
	public static final String CYTOPANEL_NAME = OV_PREFIX+"CytoPanel";
	
	public static final String OVTABLE_COLID_NAME = OV_PREFIX+"internalID";
	public static final Class<Integer> OVTABLE_COLID_TYPE = Integer.class;

	public static final String OVPROPERTY_KEY = "key";
	public static final String OVPROPERTY_VALUE = "value";
	public static final String OVPROPERTY_NAME = "OmicsVisualizer";
	
//	public static final String PROPERTY_LINKED_NETWORK = OV_PREFIX+"linked_network";
//	public static final String PROPERTY_MAPPING_OV_CY = OV_PREFIX+"OV_to_CyNetwork";
//	public static final String PROPERTY_MAPPING_CY_OV = OV_PREFIX+"CyNetwork_to_OV";
	public static final String PROPERTY_FILTER = OV_PREFIX+"filter";

	public static final String MAPPING_INNERVIZ_IDENTIFIER="NODE_CUSTOMGRAPHICS_7";
	public static final String MAPPING_OUTERVIZ_IDENTIFIER="NODE_CUSTOMGRAPHICS_8";
	
	public static final String CYNETWORKTABLE_OVCOL = "OVTable";
	public static final String CYNETWORKTABLE_INNERVIZCOL="OVViz PieChart";
	public static final String CYNETWORKTABLE_OUTERVIZCOL="OVViz DonutChart";

	public static final String CYNODETABLE_CONNECTEDCOUNT = "OV Connected rows";
	public static final String CYNODETABLE_VIZCOL="OVViz";
	public static final String CYNODETABLE_INNERVIZCOL=CYNODETABLE_VIZCOL+"Inner";
	public static final String CYNODETABLE_OUTERVIZCOL=CYNODETABLE_VIZCOL+"Outer";
	public static final String CYNODETABLE_INNERVIZCOL_VALUES=CYNODETABLE_INNERVIZCOL + " Values ";
	public static final String CYNODETABLE_OUTERVIZCOL_VALUES=CYNODETABLE_OUTERVIZCOL + " Values ";
	
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
			
			if(cycol.getName().startsWith(OVShared.CYNODETABLE_VIZCOL)) {
				cyTable.deleteColumn(cycol.getName());
			}
		}
	}

	/**
	 * Delete the specific columns in a node table related to a type of visualization
	 * @param cyTable
	 * @param vizType
	 */
	public static void deleteOVColumns(CyTable cyTable, ChartType vizType) {
		String prefix;
		if(vizType.equals(ChartType.CIRCOS)) {
			prefix = OVShared.CYNODETABLE_OUTERVIZCOL;
		} else {
			prefix = OVShared.CYNODETABLE_INNERVIZCOL;
		}
		
		for(Iterator<CyColumn> cycolIt = cyTable.getColumns().iterator(); cycolIt.hasNext();) {
			CyColumn cycol = cycolIt.next();
			
			if(cycol.getName().startsWith(prefix)) {
				cyTable.deleteColumn(cycol.getName());
			}
		}
	}
	
	public static String color2String(Color color) {
		return String.format("#%02x%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}
	
	public static String join(Collection<?> collection, String delimiter) {
		return collection.stream().map(Object::toString).collect(Collectors.joining(delimiter));
	}
	
	/**
	 * Class used to compare the OVTable identifiers.
	 * This class is used to sort the rows after being filtered.
	 * /!\ It should use the same type as defined by OVShared.OVTABLE_COLID_TYPE /!\
	 * @author marc
	 */
	public static class OVTableIDComparator implements Comparator<Object> {
		@Override
		public int compare(Object o1, Object o2) {
			Integer x = (Integer)o1;
			Integer y = (Integer)o2;
			
			return Integer.compare(x.intValue(), y.intValue());
		}
	}
}
