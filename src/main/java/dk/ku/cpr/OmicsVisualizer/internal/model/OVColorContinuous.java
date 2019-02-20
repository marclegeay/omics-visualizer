package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.awt.Color;
import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OVColorContinuous implements OVColor, Serializable {
	private static final long serialVersionUID = -5770168065992750255L;
	
	private Color down;
	private Color up;
	private Color zero;
	private double rangeMin;
	private double rangeMax;

	public OVColorContinuous(Color down, Color up, Color zero, double rangeMin, double rangeMax) {
		super();
		this.down = down;
		this.up = up;
		this.zero = zero;
		this.rangeMin = rangeMin;
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

	public double getRangeMax() {
		return this.rangeMax;
	}

	public void setRangeMax(double rangeMax) {
		this.rangeMax = rangeMax;
	}

	@Override
	public String toEnhancedGraphics(List<Object> values) {
		String colorlist = "colorlist=\"";
		colorlist += "down:" + OVShared.color2String(this.down);
		colorlist += ",";
		colorlist += "up:" + OVShared.color2String(this.up);
		colorlist += ",";
		colorlist += "zero:" + OVShared.color2String(this.zero);
		colorlist += "\"";

		colorlist += " ";

		colorlist += "range=\"" + this.rangeMin + "," + this.rangeMax + "\"";;

		return colorlist;
	}

	@Override
	public String save() {
		return "OVColorContinuous("+
				OVShared.color2String(down)+
				","+
				OVShared.color2String(up)+
				","+
				OVShared.color2String(zero)+
				","+
				rangeMin+
				","+
				rangeMax+
				")";
	}

	@Override
	public OVColor copy() {
		return new OVColorContinuous(down, up, zero, rangeMin, rangeMax);
	}

	public static OVColor load(String str) {
		if(str.isEmpty()) {
			return null;
		}

		Color down, up, zero;
		double rangeMin, rangeMax;

		Pattern pattern = Pattern.compile("OVColorContinuous\\((.*),(.*),(.*),(.*),(.*)\\)");
		Matcher matcher = pattern.matcher(str);

		if(matcher.find()) {
			down = Color.decode(matcher.group(0));
			up = Color.decode(matcher.group(1));
			zero = Color.decode(matcher.group(2));
			rangeMin = Double.parseDouble(matcher.group(3));
			rangeMax = Double.parseDouble(matcher.group(4));
		} else {
			return null;
		}

		return new OVColorContinuous(down, up, zero, rangeMin, rangeMax);
	}
}
