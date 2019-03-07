package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.util.List;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVStyle.ChartType;

public interface OVColor {
	public String toEnhancedGraphics(List<List<Object>> values, ChartType chartType);
}
