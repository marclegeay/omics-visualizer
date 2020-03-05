package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.awt.Color;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVVisualization.ChartType;

/**
 * Continuous mapping of a Visualization.
 */
public class OVColorContinuous implements OVColor, Serializable {
	private static final long serialVersionUID = -5770168065992750255L;
	
	private Color down;
	private Color up;
	private Color zero;
	private Color missing;
	private double rangeMin;
	private double rangeZero;
	private double rangeMax;
	private boolean missingUsed;

	/**
	 * Creates a Continuous mapping.
	 * @param down Color of the lower limit
	 * @param up Color of the upper limit
	 * @param zero Color of the values equal to zero
	 * @param missing Color of missing values
	 * @param rangeMin Value of the lower limit
	 * @param rangeMid Value of the zero value
	 * @param rangeMax Value of the upper limit
	 */
	public OVColorContinuous(Color down, Color up, Color zero, Color missing, double rangeMin, double rangeMid, double rangeMax) {
		super();
		this.down = down;
		this.up = up;
		this.zero = zero;
		this.missing = missing;
		this.rangeMin = rangeMin;
		this.rangeZero = rangeMid;
		this.rangeMax = rangeMax;
		this.missingUsed = false;
	}

	/**
	 * Returns the Color used for the lower limit
	 * @return The Color used for the lower limit, or <code>null</code> if there is no Color associated with the lower limit
	 */
	public Color getDown() {
		return down;
	}

	/**
	 * Sets the Color used for the lower limit
	 * @param down The Color that should be used for the lower limit
	 */
	public void setDown(Color down) {
		this.down = down;
	}

	/**
	 * Returns the Color used for the upper limit
	 * @return The Color used for the upper limit, or <code>null</code> if there is no Color associated with the upper limit
	 */
	public Color getUp() {
		return up;
	}

	/**
	 * Sets the Color used for the upper limit
	 * @param down The Color that should be used for the upper limit
	 */
	public void setUp(Color up) {
		this.up = up;
	}

	/**
	 * Returns the Color used for the zero value
	 * @return The Color used for the zero value, or <code>null</code> if there is no Color associated with this value
	 */
	public Color getZero() {
		return zero;
	}

	/**
	 * Sets the Color used for the zero value
	 * @param zero The Color used for the zero value
	 */
	public void setZero(Color zero) {
		this.zero = zero;
	}

	/**
	 * Returns the Color for missing values
	 * @return The Color for missing values, or <code>null</code> if there is no Color associated with missing values
	 */
	public Color getMissing() {
		return missing;
	}

	/**
	 * Sets the Color used for missing values
	 * @param missing The Color to use for missing values
	 */
	public void setMissing(Color missing) {
		this.missing = missing;
	}

	/**
	 * Returns the value of the lower limit
	 * @return The value of the lower limit
	 */
	public double getRangeMin() {
		return this.rangeMin;
	}

	/**
	 * Sets the lower limit
	 * @param rangeMin Value of the lower limit
	 */
	public void setRangeMin(double rangeMin) {
		this.rangeMin = rangeMin;
	}

	/**
	 * Returns the zero value
	 * @return The zero value
	 */
	public double getRangeZero() {
		return this.rangeZero;
	}

	/**
	 * Sets the zero value
	 * @param rangeZero Value of the zero value
	 */
	public void setRangeZero(double rangeZero) {
		this.rangeZero = rangeZero;
	}

	/**
	 * Returns the upper limit
	 * @return The upper limit
	 */
	public double getRangeMax() {
		return this.rangeMax;
	}

	/**
	 * Sets the upper limit
	 * @param rangeMax Value of the upper limit
	 */
	public void setRangeMax(double rangeMax) {
		this.rangeMax = rangeMax;
	}
	
	@Override
	public boolean isMissingUsed() {
		return this.missingUsed;
	}

	@Override
	public String toEnhancedGraphics(List<List<Object>> values, ChartType chartType) {
		String style = "colorlist=\"";
		style += "down:" + OVShared.color2String(this.down);
		style += ",";
		style += "up:" + OVShared.color2String(this.up);
		style += ",";
		style += "zero:" + OVShared.color2String(this.zero);
		style += ",";
		style += "missing:" + OVShared.color2String(this.missing);
		style += "\"";

		style += " ";

		// We always center the values so that the rangeZero is 0
		style += "range=\"" + (this.rangeMin-this.rangeZero) + "," + (this.rangeMax-this.rangeZero) + "\"";
		
		if(!values.isEmpty()) {
			style += " valuelist=\"" + String.join(",", Collections.nCopies(values.get(0).size(), "1")) + "\"";
		}
		
		// We look at missing values
		for(List<Object> vals : values) {
			this.missingUsed |= vals.contains(null);
			this.missingUsed |= vals.contains(Double.NaN);
		}

		return style;
	}
}
