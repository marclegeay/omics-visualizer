package dk.ku.cpr.OmicsVisualizer.internal.model.annotations;

import java.awt.Paint;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;

public class OVShapeAnnotation extends OVAbstractAnnotation<ShapeAnnotation> {
	private ShapeType shapeType;
	private Paint borderColor;
	private double borderWidth;
	private Paint fillColor;

	public OVShapeAnnotation(AnnotationFactory<ShapeAnnotation> factory, CyNetworkView networkView) {
		super(factory, networkView);
		
		this.shapeType=ShapeType.RECTANGLE;
		this.borderColor=null;
		this.borderWidth=-1.0;
		this.fillColor=null;
	}

	public ShapeAnnotation.ShapeType getShapeType() {
		return shapeType;
	}

	public void setShapeType(ShapeAnnotation.ShapeType shapeType) {
		this.shapeType = shapeType;
	}

	public Paint getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(Paint borderColor) {
		this.borderColor = borderColor;
	}

	public double getBorderWidth() {
		return borderWidth;
	}

	public void setBorderWidth(double borderWidth) {
		this.borderWidth = borderWidth;
	}

	public Paint getFillColor() {
		return fillColor;
	}

	public void setFillColor(Paint fillColor) {
		this.fillColor = fillColor;
	}

	public ShapeAnnotation getAnnotation() {
		Map<String, String> args = new HashMap<>();
		args.put(ShapeAnnotation.X, String.valueOf(this.getX()));
		args.put(ShapeAnnotation.Y, String.valueOf(this.getY()));
		args.put(ShapeAnnotation.Z, String.valueOf(this.getZ()));
		args.put(ShapeAnnotation.WIDTH, String.valueOf(this.getWidth()));
		args.put(ShapeAnnotation.HEIGHT, String.valueOf(this.getHeight()));
		args.put(ShapeAnnotation.SHAPETYPE, this.shapeType.shapeName());
		
		this.annotation = factory.createAnnotation(ShapeAnnotation.class, this.networkView, args);
		
		if(this.borderColor != null) {
			this.annotation.setBorderColor(borderColor);
		}
		
		if(this.borderWidth >= 0) {
			this.annotation.setBorderWidth(this.borderWidth);
		}
		
		if(this.fillColor != null) {
			this.annotation.setFillColor(fillColor);
		}
		
		return super.getAnnotation();
	}
}
