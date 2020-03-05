package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.awt.Color;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVVisualization.ChartType;

/**
 * Discrete mapping of a Visualization.
 */
public class OVColorDiscrete implements OVColor, Serializable {
	private static final long serialVersionUID = 482988419184312110L;
	
	private Map<Object, Color> colors;
	private Color missingColor;
	
	/**
	 * Creates a Discrete mapping.
	 * @param colors Mapping of the Color used for each value
	 * @param missingColor Color of the missing values. <code>null</code> if there is no missing values.
	 */
	public OVColorDiscrete(Map<Object, Color> colors, Color missingColor) {
		this.colors = colors;
		this.missingColor = missingColor;
	}
	
	/**
	 * Returns the Color associated to the value.
	 * @param value The value to retrieve the Color
	 * @return The Color associated to the value, or <code>null</code> if the value is not in the mapping
	 */
	public Color getColor(Object value) {
		return this.colors.get(value);
	}
	
	/**
	 * Returns the set of values present in the mapping.
	 * @return The set of values from the mapping
	 */
	public Set<Object> getValues() {
		return this.colors.keySet();
	}
	
	/**
	 * Returns the mapping.
	 * @return The mapping
	 */
	public Map<Object, Color> getMapping() {
		return this.colors;
	}
	
	/**
	 * Returns the color of missing values.
	 * @return The missing value color
	 */
	public Color getMissingColor() {
		return this.missingColor;
	}

	@Override
	public boolean isMissingUsed() {
		return this.missingColor != null;
	}

	@Override
	public String toEnhancedGraphics(List<List<Object>> values, ChartType chartType) {
		if(values.isEmpty()) {
			return "";
		}
		
		String colorlist = "";
		String valuelist = "";
		
		for(List<Object> vals : values) {
			colorlist += ",[";
			valuelist += ",[";
			String colorsublist = "";
			for(Object val : vals) {
				if(val == null || this.colors.get(val) == null) {
					colorsublist += "," + OVShared.color2String(this.missingColor);
				} else {
					colorsublist += "," + OVShared.color2String(this.colors.get(val));
				}
			}
			colorlist += colorsublist.substring(1) + "]";
			valuelist += String.join(",", Collections.nCopies(vals.size(), "1")) + "]";
		}

		// We get rid of the first comma
		colorlist = colorlist.substring(1);
		valuelist = valuelist.substring(1);
		
		if(!chartType.equals(ChartType.CIRCOS)) { // Only CIRCOS can have several layers
			// In that case there is only one layer, we do not use [ ]
			colorlist = colorlist.substring(1, colorlist.length()-1);
			valuelist = valuelist.substring(1, valuelist.length()-1);
		}
		
		colorlist = "colorlist=\"" + colorlist + "\"";
		valuelist = "valuelist=\"" + valuelist + "\"";
		
		return colorlist + " " + valuelist;
	}
}
