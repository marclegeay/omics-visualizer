package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Base64;

public class OVLegend implements Serializable {
	private static final long serialVersionUID = 7710730183176886983L;
	
	public static final String DEFAULT_TITLE = "Legend";
	public static final int DEFAULT_FONT_SIZE = 22;
	public static final Font DEFAULT_FONT = Font.decode(Font.SANS_SERIF).deriveFont(Font.PLAIN, DEFAULT_FONT_SIZE);
	
	private boolean visible;
	private OVVisualization innerViz;
	private OVVisualization outerViz;
	private String title;
	private Font font;
	private int fontSize;
	private OVLegendPosition position;
	private OVLegendOrientation orientation;
	
	public OVLegend(OVVisualization innerViz, OVVisualization outerViz, String title, String fontName, int fontSize, OVLegendPosition position, OVLegendOrientation orientation) {
		this.visible=true;
		this.innerViz=innerViz;
		this.outerViz=outerViz;
		this.title=title;
		this.font=DEFAULT_FONT;
		this.fontSize=fontSize;
		this.position=position;
		this.orientation=orientation;
		
		if(Arrays.asList(OVShared.getAvailableFontNames()).contains(fontName)) {
			this.font = Font.decode(fontName).deriveFont(Font.PLAIN, this.fontSize);
		}
	}

	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isDrawInner() {
		return this.innerViz != null;
	}

	public boolean isDrawOuter() {
		return this.outerViz != null;
	}

	public String getTitle() {
		return title;
	}

	public Font getFont() {
		return font;
	}

	public int getFontSize() {
		return fontSize;
	}
	
	public OVLegendPosition getPosition() {
		return position;
	}
	
	public OVLegendOrientation getOrientation() {
		return orientation;
	}
	
	public OVVisualization getInnerVisualization() {
		return innerViz;
	}
	
	public OVVisualization getOuterVisualization() {
		return outerViz;
	}
	
	/**
	 * Returns the serialized String of the legend.
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
	 * Creates a legend from the given serialized String.
	 * @param str The serialized String.
	 * @return The legend corresponding to the serialized String.
	 */
	public static OVLegend load(String str) {
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
		
		return (o instanceof OVLegend) ? (OVLegend)o : null;
	}
	
	public enum OVLegendPosition implements Serializable {
		NORTH_LEFT,
		NORTH,
		NORTH_RIGHT,
		EAST_TOP,
		EAST,
		EAST_BOTTOM,
		SOUTH_LEFT,
		SOUTH,
		SOUTH_RIGHT,
		WEST_TOP,
		WEST,
		WEST_BOTTOM
	}
	
	public enum OVLegendOrientation implements Serializable {
		VERTICAL,
		HORIZONTAL
	}
}
