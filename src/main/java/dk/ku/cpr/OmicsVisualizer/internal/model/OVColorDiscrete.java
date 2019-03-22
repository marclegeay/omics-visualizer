package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.awt.Color;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVVisualization.ChartType;

public class OVColorDiscrete implements OVColor, Serializable {
	private static final long serialVersionUID = 482988419184312110L;
	
	private Map<Object, Color> colors;
	
	public OVColorDiscrete(Map<Object, Color> colors) {
		this.colors = colors;
	}
	
	public Color getColor(Object value) {
		return this.colors.get(value);
	}
	
	public Set<Object> getValues() {
		return this.colors.keySet();
	}
	
	public Map<Object, Color> getMapping() {
		return this.colors;
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
				colorsublist += "," + OVShared.color2String(this.colors.get(val));
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
