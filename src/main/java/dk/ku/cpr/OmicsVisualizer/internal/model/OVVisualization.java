package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.List;

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

	public ChartType getType() {
		return type;
	}
	
	public List<String> getValues() {
		return this.values;
	}
	
	public Class<?> getValuesType() {
		return this.valuesType;
	}
	
	public String getPaletteName() {
		return this.paletteName;
	}
	
	public boolean isOnlyFiltered() {
		return this.onlyFiltered;
	}

	public OVColor getColors() {
		return colors;
	}
	
	public boolean isContinuous() {
		return colors instanceof OVColorContinuous;
	}

	public String getLabel() {
		return label;
	}

	public boolean isTranspose() {
		return transpose;
	}
	
	public String toEnhancedGraphics(List<List<Object>> values) {
		String style = this.type.getStyle();
		
		style += " ";
		style += this.colors.toEnhancedGraphics(values, this.type);
		
		return style;
	}
	
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

	public enum ChartType implements Serializable {
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
			return this.name;
		}

		public String getStyle() {
			return this.style;
		}
	}
}
