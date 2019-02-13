package dk.ku.cpr.OmicsVisualizer.internal.ui;

public enum ChartType {
	CIRCOS("Donut Chart", "circoschart: firstarc=1.0 arcwidth=0.4"),
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
