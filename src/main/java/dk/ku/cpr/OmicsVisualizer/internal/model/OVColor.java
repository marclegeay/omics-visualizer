package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.util.List;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVVisualization.ChartType;

/**
 * Interface used to store the Mapping of a Visualization.
 * Classes that implement the interface should be able to give a String that enhancedGraphics can parse.
 */
public interface OVColor {
	/**
	 * Transform the OVColor object into a String that enhancedGraphics can parse.
	 * @param values Matrix of the values
	 * @param chartType Type of enhancedGraphics chart
	 * @return an enhancedGraphics String
	 * 
	 * @see ChartType
	 */
	public String toEnhancedGraphics(List<List<Object>> values, ChartType chartType);
}
