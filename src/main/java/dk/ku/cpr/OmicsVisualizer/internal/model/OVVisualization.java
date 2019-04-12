package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.List;

/**
 * Represents a visualization of table data.
 */
public class OVVisualization implements Serializable {
	private static final long serialVersionUID = 3816461719599891386L;
	
	private ChartType type;
	private List<String> values;
	private Class<?> valuesType;
	private String paletteName;
	private boolean onlyFiltered;
	private OVColor colors;
	private String label;
	private boolean transpose;
	
	/**
	 * Creates a visualization.
	 * @param type The type of chart.
	 * @param values The list of the table column names used to fetch the data.
	 * @param valuesType The type of the values.
	 * @param onlyFiltered Should the visualization apply to the filtered rows (<code>true</code>) or all the rows (<code>false</code>)?
	 * @param colors The color mapping.
	 * @param paletteName The name of the palette used for the color mapping.
	 * @param label Name of the table column that should be used to label data. <code>null</code> if no label should be used.
	 * @param transpose Should the data matrix be transposed before visualization?
	 */
	public OVVisualization(ChartType type, List<String> values, Class<?> valuesType, boolean onlyFiltered, OVColor colors, String paletteName, String label, boolean transpose) {
		super();
		this.type=type;
		this.values=values;
		this.valuesType=valuesType;
		this.onlyFiltered=onlyFiltered;
		this.colors=colors;
		this.paletteName=paletteName;
		this.label=label;
		// Only CIRCOS can transpose data
		if(type.equals(ChartType.CIRCOS)) {
			this.transpose=transpose;
		} else {
			this.transpose=false;
		}
	}

	/**
	 * Returns the type of chart.
	 * @return The type of chart.
	 */
	public ChartType getType() {
		return type;
	}
	
	/**
	 * Returns the list of the table column names used to fetch the data.
	 * @return The list of the column names.
	 */
	public List<String> getValues() {
		return this.values;
	}
	
	/**
	 * Returns the type of the values.
	 * @return The type of the values.
	 */
	public Class<?> getValuesType() {
		return this.valuesType;
	}
	
	/**
	 * Returns the name of the palette.
	 * @return The name of the palette.
	 */
	public String getPaletteName() {
		return this.paletteName;
	}
	
	/**
	 * Is the visualization applied to filtered rows only?
	 * @return <code>true</code> if the visualization should be applied to filtered rows only, <code>false</code> otherwise.
	 */
	public boolean isOnlyFiltered() {
		return this.onlyFiltered;
	}

	/**
	 * Returns the color mapping.
	 * @return The color mapping.
	 */
	public OVColor getColors() {
		return colors;
	}
	
	/**
	 * Is the color mapping a continuous mapping?
	 * @return <code>true</code> if the color mapping is a continuous mapping, <code>false</code> otherwise.
	 */
	public boolean isContinuous() {
		return colors instanceof OVColorContinuous;
	}

	/**
	 * Returns the name of the table column used to label the data.
	 * @return the name of the label column. <code>null</code> if no label should be used.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Should the matrix data be transposed before visualization?
	 * @return <code>true</code> is the matrix should be transposed, <code>false</code> otherwise.
	 */
	public boolean isTranspose() {
		return transpose;
	}
	
	/**
	 * Returns the enhancedGrahpics String to visualize the data.
	 * @param values The matrix of values.
	 * @return The enhancedGraphics String.
	 */
	public String toEnhancedGraphics(List<List<Object>> values) {
		String style = this.type.getStyle();
		
		style += " ";
		style += this.colors.toEnhancedGraphics(values, this.type);
		
		return style;
	}
	
	/**
	 * Returns the serialized String of the visualization.
	 * @return The serialized String.
	 */
	public String save() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream out;
		
		try {
			out = new ObjectOutputStream(baos);
			
			out.writeObject(this);
			out.close();
		} catch (IOException e) {
			return "";
		}
		
		return Base64.getEncoder().encodeToString(baos.toByteArray());
	}
	
	/**
	 * Creates a visualization from the given serialized String.
	 * @param str The serialized String.
	 * @return The visualization corresponding to the serialized String.
	 */
	public static OVVisualization load(String str) {
		if(str == null) {
			return null;
		}
		
		byte data[] = Base64.getDecoder().decode(str);
		ObjectInputStream in;
		Object o =null;
		
		try {
			in = new ObjectInputStream(new ByteArrayInputStream(data));
			
			o = in.readObject();
			in.close();
		} catch (IOException e) {
			return null;
		} catch (ClassNotFoundException e) {
			return null;
		}
		
		return (o instanceof OVVisualization) ? (OVVisualization)o : null;
	}

	/**
	 * Types of supported enhancedGraphics charts.
	 */
	public enum ChartType implements Serializable {
		/** Donut chart. */
		CIRCOS("Donut Chart", "circoschart: firstarc=1.0 arcwidth=0.4"),
		/** Pie chart. */
		PIE("Pie Chart", "piechart:");

		String name;
		String style;
		ChartType(String name, String style) {
			this.name = name;
			this.style = style;
		}

		@Override
		public String toString() {
			return this.name;
		}

		/**
		 * Returns the name of the type.
		 * @return The name of the type.
		 */
		public String getName() {
			return this.name;
		}

		/**
		 * Returns the beginning of the enhancedGraphics String corresponding to the chart.
		 * @return The enhancedGraphics String.
		 */
		public String getStyle() {
			return this.style;
		}
	}
}
