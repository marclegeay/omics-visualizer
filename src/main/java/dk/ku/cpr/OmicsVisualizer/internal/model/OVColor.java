package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.util.List;

public interface OVColor {
	public String toEnhancedGraphics(List<List<Object>> values);
	public String save();
	public OVColor copy();
}
