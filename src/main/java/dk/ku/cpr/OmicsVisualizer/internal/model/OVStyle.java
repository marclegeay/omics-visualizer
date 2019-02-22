package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class OVStyle implements Serializable {
	private static final long serialVersionUID = 3816461719599891386L;
	
	private ChartType type;
	private List<String> values;
	private Class<?> valuesType;
	private OVColor colors;
	private String label;
	private boolean transpose;
	
	public OVStyle(ChartType type, List<String> values, Class<?> valuesType, OVColor colors, String label, boolean transpose) {
		super();
		this.type=type;
		this.values=values;
		this.valuesType=valuesType;
		this.colors=colors;
		this.label=label;
		this.transpose=transpose;
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

	public Object copy() {
		List<String> valuesCopy = new ArrayList<>();
		
		for(String val : this.values) {
			valuesCopy.add(val);
		}
		
		return new OVStyle(this.type,
				valuesCopy,
				this.valuesType,
				this.colors.copy(),
				this.label,
				this.transpose);
	}
	
	public String toEnhancedGraphics(List<List<Object>> values) {
		String style = this.type.getStyle();
		
		style += " ";
		style += this.colors.toEnhancedGraphics(values);
		
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
	
	public static OVStyle load(String str) {
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
		
		return (o instanceof OVStyle) ? (OVStyle)o : null;
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
			return this.name();
		}

		public String getStyle() {
			return this.style;
		}
	}
}
