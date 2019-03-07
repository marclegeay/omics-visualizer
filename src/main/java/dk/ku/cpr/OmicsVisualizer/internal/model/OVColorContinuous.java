package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.awt.Color;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVStyle.ChartType;

public class OVColorContinuous implements OVColor, Serializable {
	private static final long serialVersionUID = -5770168065992750255L;
	
	private Color down;
	private Color up;
	private Color zero;
	private double rangeMin;
	private double rangeZero;
	private double rangeMax;

	public OVColorContinuous(Color down, Color up, Color zero, double rangeMin, double rangeMid, double rangeMax) {
		super();
		this.down = down;
		this.up = up;
		this.zero = zero;
		this.rangeMin = rangeMin;
		this.rangeZero = rangeMid;
		this.rangeMax = rangeMax;
	}

	public Color getDown() {
		return down;
	}

	public void setDown(Color down) {
		this.down = down;
	}

	public Color getUp() {
		return up;
	}

	public void setUp(Color up) {
		this.up = up;
	}

	public Color getZero() {
		return zero;
	}

	public void setZero(Color zero) {
		this.zero = zero;
	}

	public double getRangeMin() {
		return this.rangeMin;
	}

	public void setRangeMin(double rangeMin) {
		this.rangeMin = rangeMin;
	}

	public double getRangeZero() {
		return this.rangeZero;
	}

	public void setRangeZero(double rangeZero) {
		this.rangeZero = rangeZero;
	}

	public double getRangeMax() {
		return this.rangeMax;
	}

	public void setRangeMax(double rangeMax) {
		this.rangeMax = rangeMax;
	}

	@Override
	public String toEnhancedGraphics(List<List<Object>> values, ChartType chartType) {
		String style = "colorlist=\"";
		style += "down:" + OVShared.color2String(this.down);
		style += ",";
		style += "up:" + OVShared.color2String(this.up);
		style += ",";
		style += "zero:" + OVShared.color2String(this.zero);
		style += "\"";

		style += " ";

		// We always center the values so that the rangeZero is 0
		style += "range=\"" + (this.rangeMin-this.rangeZero) + "," + (this.rangeMax-this.rangeZero) + "\"";
		
		if(!values.isEmpty()) {
			style += " valuelist=\"" + String.join(",", Collections.nCopies(values.get(0).size(), "1")) + "\"";
		}

		return style;
	}
}
