package dk.ku.cpr.OmicsVisualizer.internal.model.annotations;

import java.awt.geom.Point2D;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;

public abstract class OVAbstractAnnotation<T extends Annotation> {
	protected AnnotationFactory<T> factory;
	protected CyNetworkView networkView;
	
	protected T annotation;
	
	private double x;
	private double y;
	private double z;
	private double width;
	private double height;
	private String name;
	
	public OVAbstractAnnotation(AnnotationFactory<T> factory, CyNetworkView networkView) {
		this.factory=factory;
		this.networkView=networkView;
		
		this.annotation=null;
		this.x=0;
		this.y=0;
		this.z=-1;
		this.width=0;
		this.height=0;
		this.name=null;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
		
		if(this.annotation != null) {
			this.annotation.moveAnnotation(new Point2D.Double(this.x, this.y));
		}
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
		
		if(this.annotation != null) {
			this.annotation.moveAnnotation(new Point2D.Double(this.x, this.y));
		}
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}
	
	public String getName() {
		if(name == null) {
			name = "";
		}
		
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public T getAnnotation() {
		if(this.annotation != null) {
			this.annotation.setName(getName());
		}
		
		// We make sure that the annotation is at the right position
		this.annotation.moveAnnotation(new Point2D.Double(x, y));
		
//		System.out.println("\n\ngetAnnotation '"+getName()+"' x=" + x +" y="+y +" width="+width +" height="+height);
//		for(String key : this.annotation.getArgMap().keySet()) {
//			System.out.println(key+"="+this.annotation.getArgMap().get(key));
//		}
		
		return annotation;
	}
	
	public void setPosition(double x, double y) {
		this.x=x;
		this.y=y;
		
		if(this.annotation != null) {
			this.annotation.moveAnnotation(new Point2D.Double(x, y));
		}
	}
}
