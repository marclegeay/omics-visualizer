package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.stream.Collectors;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.ServiceProperties;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVVisualization.ChartType;

/**
 * Class used to store shared properties.
 * The class also provides some useful functions.
 */
public class OVShared {
	/** The prefix form hidden attributes. */
	public static final String OV_PREFIX = "dk.ku.cpr.OmicsVisualizer.";
	
	/** The menu where store the Omics Visualizer actions. */
	public static final String OV_PREFERRED_MENU = ServiceProperties.APPS_MENU+".Omics Visualizer";
	/** The namespace of the Omics Visualizer commands. */
	public static final String OV_COMMAND_NAMESPACE = "ov";
	
	/** The prefix of a table name if no name was given by the user. */
	public static final String OVTABLE_DEFAULT_NAME = "Omics Visualizer Table ";
	
	/** Identifier of the Omics Visualizer panel. */
	public static final String CYTOPANEL_NAME = OV_PREFIX+"CytoPanel";
	
	/** Identifier of the Omics Visualizer legend Annotation */
	public static final String OVLEGEND_ANNOTATION_NAME = "OmicsVisualizerLegend";
	
	/** Name of the table column where the id of each row is stored. */
	public static final String OVTABLE_COLID_NAME = OV_PREFIX+"internalID";
	/** Type of the id column.
	 * Linked with {@link OVShared#OVTableIDComparator}. */
	public static final Class<Integer> OVTABLE_COLID_TYPE = Integer.class;

	/** Name of the column from the properties table where the property key is stored. */
	public static final String OVPROPERTY_KEY = "key";
	/** Name of the column from the properties table where the property value is stored. */
	public static final String OVPROPERTY_VALUE = "value";
	/** Prefix of the properties specific to Omics Visualizer. */
	public static final String OVPROPERTY_NAME = "OmicsVisualizer";
	
	/** Default column name of the 'values' column while copying a node table. */
	public static final String OV_DEFAULT_VALUES_COLNAME = "value";
	/** Default column name of the 'values source' column while copying a node table. */
	public static final String OV_DEFAULT_VALUES_SOURCE_COLNAME = "source";
	
	/** Name of the property storing the filter of a table */
	public static final String PROPERTY_FILTER = OV_PREFIX+"filter";
	public static final String PROPERTY_IMPORTED_COLNAME = OV_PREFIX+"imported_colname";

	/** Name of the visual property where to store the inner visualization. */
	public static final String MAPPING_INNERVIZ_IDENTIFIER="NODE_CUSTOMGRAPHICS_7";
	/** Name of the visual property where to store the outer visualization. */
	public static final String MAPPING_OUTERVIZ_IDENTIFIER="NODE_CUSTOMGRAPHICS_8";
	
	/** Name of the column namespaces */
	public static final String OV_COLUMN_NAMESPACE = "Omics Visualizer";
	
	/** Name of the column from the network's network table where the connection information is stored. */
	public static final String CYNETWORKTABLE_OVCOL = "OVTable";
	/** Name of the column from the network's network table where the inner visualization is stored. */
	public static final String CYNETWORKTABLE_INNERVIZCOL="Viz PieChart";
	/** Name of the column from the network's network table where the outer visualization is stored. */
	public static final String CYNETWORKTABLE_OUTERVIZCOL="Viz DonutChart";
	/** Name of the column from the network's network table where the legend is stored. */
	public static final String CYNETWORKTABLE_LEGEND="Legend";

	/** Name of the column from the networks's node table where the number of connected rows is stored. */
	public static final String CYNODETABLE_CONNECTEDCOUNT = "Connected rows";
	/** Prefix of the name of the columns from the networks's node table where the visualizations are stored. */
	public static final String CYNODETABLE_VIZCOL="Viz";
	/** Name of the column from the networks's node table where the inner visualization is stored. */
	public static final String CYNODETABLE_INNERVIZCOL=CYNODETABLE_VIZCOL+" PieChart";
	/** Name of the column from the networks's node table where the outer visualization is stored. */
	public static final String CYNODETABLE_OUTERVIZCOL=CYNODETABLE_VIZCOL+" DonutChart";
	/** Prefix of the name of the columns from the networks's node table where the inner visualization's values are stored. */
	public static final String CYNODETABLE_INNERVIZCOL_VALUES=CYNODETABLE_INNERVIZCOL + " Values ";
	/** Prefix of the name of the columns from the networks's node table where the outer visualization's values are stored. */
	public static final String CYNODETABLE_OUTERVIZCOL_VALUES=CYNODETABLE_OUTERVIZCOL + " Values ";
	
	/** Name of the stringApp command to query proteins. This is used to retrieve a STRING network. */
	public static final String STRING_CMD_PROTEIN_QUERY = "protein query";
	/** Name of the stringApp command to list species. */
	public static final String STRING_CMD_LIST_SPECIES = "list species";

	
	/** Number of rows from which a warning should be raised because too many rows are connected to a node */
	public static final int MAXIMUM_ROWS_CONNECTED_TO_NODE = 100;
	
	// We forbid the class to have instances
	private OVShared() {
	}
	
	/**
	 * Indicates if a Cytoscape table is an Omics Visualizer table.
	 * @param table The table to test.
	 * @return If the table is an Omics Visualizer table.
	 */
	public static boolean isOVTable(CyTable table) {
		return !table.isPublic() && (table.getColumn(OVTABLE_COLID_NAME) != null);
	}
	
	/**
	 * Indicates if the column is specific to Omics Visualizer tables.
	 * @param colName The name of the column to test.
	 * @return If the column name is specific to Omics Visualizer.
	 */
	public static boolean isOVCol(String colName) {
		return OV_PREFIX.regionMatches(0, colName, 0, OV_PREFIX.length());
	}

	
	/**
	 * Deletes the specific Omics Visualizer columns from a Cytoscape node table.
	 * Only tables from a node table will be deleted.
	 * @param cyTable The Cytoscape node table.
	 */
	public static void deleteOVColumns(CyTable cyTable) {
		for(Iterator<CyColumn> cyColIt = cyTable.getColumns(OV_COLUMN_NAMESPACE).iterator(); cyColIt.hasNext() ; ) {
			CyColumn cyCol = cyColIt.next();
			
			if(cyCol != null) {
				cyTable.deleteColumn(cyCol.getName());
			}
		}
	}

	/**
	 * Deletes the specific Omics Visualizer columns, related to the given type of visualization, from a Cytoscape node table.
	 * @param cyTable The Cytoscape node table.
	 * @param vizType The type of visualization to delete.
	 */
	public static void deleteOVColumns(CyTable cyTable, ChartType vizType) {
		String prefix;
		if(vizType.equals(ChartType.CIRCOS)) {
			prefix = OVShared.CYNODETABLE_OUTERVIZCOL;
		} else {
			prefix = OVShared.CYNODETABLE_INNERVIZCOL;
		}
		
		for(Iterator<CyColumn> cyColIt = cyTable.getColumns(OV_COLUMN_NAMESPACE).iterator(); cyColIt.hasNext() ; ) {
			CyColumn cyCol = cyColIt.next();
			
			if(cyCol != null && cyCol.getNameOnly().startsWith(prefix)) {
				cyTable.deleteColumn(cyCol.getName());
			}
		}
	}
	
	/**
	 * Transforms a Color into a hexadecimal representation.
	 * @param color The color to transform.
	 * @return The hexadecimal representation.
	 */
	public static String color2String(Color color) {
		return String.format("#%02x%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}
	
	/**
	 * Returns a String of the list of elements from a given collection, separated by a given delimiter.
	 * The elements are represented by the String given by the toString method.
	 * @param collection The list to join.
	 * @param delimiter The delimiter used to delimits the elements of the list.
	 * @return The String representing the list.
	 * 
	 * @see Object#toString()
	 */
	public static String join(Collection<?> collection, String delimiter) {
		return collection.stream().map(Object::toString).collect(Collectors.joining(delimiter));
	}
	
	/**
	 * Returns the list of the font families available on the system.
	 * @return The list of font families.
	 */
	public static String[] getAvailableFontNames() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
	}
	
	/**
	 * Class used to compare the OVTable identifiers.
	 * This class is used to sort the rows after being filtered.<br>
	 * <b style="color:red">/!\ It should use the same type as defined by {@link OVShared#OVTABLE_COLID_TYPE} /!\</b>
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
