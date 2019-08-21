package dk.ku.cpr.OmicsVisualizer.internal.model.annotations;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.TextAnnotation;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVLegend;

public class OVTextAnnotation extends OVAbstractAnnotation<TextAnnotation> {
	private String text;
	private Color color;
	private Font font;

	public OVTextAnnotation(AnnotationFactory<TextAnnotation> factory, CyNetworkView networkView) {
		super(factory, networkView);
		
		this.text = "";
		this.color = null;
		this.font = OVLegend.DEFAULT_FONT;
	}
	
	private void updateAnnotation() {
		// We make sure we have a font
		if(this.font == null) {
			this.font = OVLegend.DEFAULT_FONT;
		}
		
		Map<String, String> args = new HashMap<>();
		args.put(TextAnnotation.X, String.valueOf(this.getX()));
		args.put(TextAnnotation.Y, String.valueOf(this.getY()));
		args.put(TextAnnotation.Z, String.valueOf(this.getZ()));
		args.put(TextAnnotation.TEXT, (this.text != null ? this.text : ""));
		args.put(TextAnnotation.FONTSIZE, String.valueOf(this.font.getSize()));
		args.put(TextAnnotation.FONTFAMILY, this.font.getFamily());
		args.put(TextAnnotation.FONTSTYLE, String.valueOf(this.font.getStyle()));
		if(this.color != null) {
			args.put(TextAnnotation.COLOR, String.valueOf(this.color.getRGB())); // TODO
		}
		
		this.annotation = factory.createAnnotation(TextAnnotation.class, networkView, args);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		
		// We compute the dimensions
		Dimension dim = this.getTextDimension(this.text, this.font);
		this.setWidth(dim.getWidth());
		this.setHeight(dim.getHeight());
		
		this.updateAnnotation();
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
		
		this.updateAnnotation();
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
		
		this.updateAnnotation();
	}
	
	private Dimension getTextDimension(String text, Font font) {
		if(text == null) {
			text = "";
		}
		
		Rectangle2D bounds = font.getStringBounds(text, new FontRenderContext(null, false, false));
		
		return new Dimension((int) bounds.getWidth(), (int) bounds.getHeight());
	}
}
