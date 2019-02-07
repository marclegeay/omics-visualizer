package dk.ku.cpr.OmicsVisualizer.internal.ui;

public enum ChartType {
	BAR("Bar Chart", "barchart: position:south"),
	CIRCOS("Donut Chart", "circoschart: firstarc=1.0 arcwidth=0.4"),
	LINE("Line Chart", "linechart:"),
	PIE("Pie Chart", "piechart:");

	String name;
	String style;
	ChartType(String name, String style) {
		this.name = name;
		this.style = style;
	}

	public String toString() {
		return this.name;
	}
	
	public String getName() {
		return this.name();
	}
	
	public String getStyle() {
		return this.style;
	}
}
