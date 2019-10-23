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

/**
 * A legend of visualizations applied to a network.
 */
public class OVLegend implements Serializable {
	private static final long serialVersionUID = 7710730183176886983L;
	
	/** Default title given to a legend. */
	public static final String DEFAULT_TITLE = "Legend";
	/** Default font size. */
	public static final int DEFAULT_FONT_SIZE = 22;
	/** The default font.
	 * The font family is {@link Font#SANS_SERIF}.
	 * The font style is {@link Font#PLAIN}.
	 * The font size is {@link #DEFAULT_FONT_SIZE}. */
	public static final Font DEFAULT_FONT = Font.decode(Font.SANS_SERIF).deriveFont(Font.PLAIN, DEFAULT_FONT_SIZE);
	
	private OVVisualization innerViz;
	private OVVisualization outerViz;
	private String title;
	private Font font;
	private int fontSize;
	private OVLegendPosition position;
	private OVLegendOrientation orientation;
	
	/**
	 * Creates a legend.
	 * @param innerViz The inner visualization to put in the legend. <code>null</code> if the visualization should not be in the legend.
	 * @param outerViz The outer visualization to put in the legend. <code>null</code> if the visualization should not be in the legend.
	 * @param title The title of the legend. An empty title will not be displayed.
	 * @param fontName The font family for the legend texts.
	 * @param fontSize The font size.
	 * @param position The position of the legend.
	 * @param orientation The orientation of the legend.
	 */
	public OVLegend(OVVisualization innerViz, OVVisualization outerViz, String title, String fontName, int fontSize, OVLegendPosition position, OVLegendOrientation orientation) {
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
	
	/**
	 * Sets the inner visualization. If <code>null</code> then the visualization will not be in the legend.
	 * @param innerViz The inner visualization, or <code>null</code>.
	 */
	public void setInnerVisualization(OVVisualization innerViz) {
		this.innerViz=innerViz;
	}
	
	/**
	 * Sets the outer visualization. If <code>null</code> then the visualization will not be in the legend.
	 * @param outerViz The outer visualization, or <code>null</code>.
	 */
	public void setOuterVisualization(OVVisualization outerViz) {
		this.outerViz=outerViz;
	}

	/**
	 * Returns if the legend includes an inner visualization.
	 * @return If the legend includes an inner visualization.
	 */
	public boolean isDrawInner() {
		return this.innerViz != null;
	}

	/**
	 * Returns if the legend includes an outer visualization.
	 * @return If the legend includes an outer visualization.
	 */
	public boolean isDrawOuter() {
		return this.outerViz != null;
	}

	/**
	 * Returns the title of the legend.
	 * @return The title.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Returns the font for the legend texts.
	 * @return The font.
	 */
	public Font getFont() {
		return font;
	}

	/**
	 * Returns the font size.
	 * @return The font size.
	 */
	public int getFontSize() {
		return fontSize;
	}
	
	/**
	 * Returns the position of the legend according to the network.
	 * @return The position.
	 */
	public OVLegendPosition getPosition() {
		return position;
	}
	
	/**
	 * Returns the orientation of the legend.
	 * @return The orientation.
	 */
	public OVLegendOrientation getOrientation() {
		return orientation;
	}
	
	/**
	 * Returns the inner visualization.
	 * @return The visualization, or <code>null</code>.
	 */
	public OVVisualization getInnerVisualization() {
		return innerViz;
	}
	
	/**
	 * Returns the outer visualization.
	 * @return The visualization, or <code>null</code>.
	 */
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
	
	/**
	 * The position of the legend according to the network.
	 */
	public enum OVLegendPosition implements Serializable {
		/** The legend is on top of the network, aligned left. */
		NORTH_LEFT,
		/** The legend is on top of the network, aligned center. */
		NORTH,
		/** The legend is on top of the network, aligned right. */
		NORTH_RIGHT,
		/** The legend is on the right of the network, aligned top. */
		EAST_TOP,
		/** The legend is on the right of the network, aligned middle. */
		EAST,
		/** The legend is on the right of the network, aligned bottom. */
		EAST_BOTTOM,
		/** The legend is on bottom of the network, aligned left. */
		SOUTH_LEFT,
		/** The legend is on bottom of the network, aligned center. */
		SOUTH,
		/** The legend is on bottom of the network, aligned right. */
		SOUTH_RIGHT,
		/** The legend is on the left of the network, aligned top. */
		WEST_TOP,
		/** The legend is on the left of the network, aligned middle. */
		WEST,
		/** The legend is on the left of the network, aligned bottom. */
		WEST_BOTTOM
	}
	
	/**
	 * The orientation of the legend elements.
	 */
	public enum OVLegendOrientation implements Serializable {
		/** The legend elements are drawn vertically. */
		VERTICAL,
		/** The legend elements are drawn horizontally. */
		HORIZONTAL
	}
}
