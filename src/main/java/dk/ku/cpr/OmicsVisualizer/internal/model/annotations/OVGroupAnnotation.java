package dk.ku.cpr.OmicsVisualizer.internal.model.annotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;

public class OVGroupAnnotation extends OVAbstractAnnotation<GroupAnnotation> {
	@SuppressWarnings("rawtypes")
	private List<OVAbstractAnnotation> annotList;
	
	public OVGroupAnnotation(AnnotationFactory<GroupAnnotation> factory, CyNetworkView networkView) {
		super(factory, networkView);
		
		this.annotList = new ArrayList<>();
	}
	
	public <T extends Annotation> void addAnnotation(OVAbstractAnnotation<T> annot) {
		this.annotList.add(annot);

		// ------------------------
		// We check the width and x
		// ------------------------
		double oldWidth = getWidth();
		double x = getX();
		
		// First the left part
		if(annot.getX() < x) {
			this.setWidth(oldWidth + x-annot.getX());
			this.setX(annot.getX());
		}
		// Then, the right part
		if(annot.getX()+annot.getWidth() > x+oldWidth) {
			this.setWidth(getWidth() + annot.getX()+annot.getWidth()-x-oldWidth);
		}

		// -------------------------
		// We check the height and y
		// -------------------------
		double oldHeight = getHeight();
		double y = getY();
		
		// First the upper part
		if(annot.getY() < y) {
			this.setHeight(oldHeight + y-annot.getY());
			this.setY(annot.getY());
		}
		// Then, the lower part
		if(annot.getY()+annot.getHeight() > y+oldHeight) {
			this.setHeight(getHeight() + annot.getY()+annot.getHeight()-y-oldHeight);
		}
	}
	
	@SuppressWarnings("unchecked")
	public GroupAnnotation getAnnotation() {
		Map<String, String> args = new HashMap<>();
		args.put(GroupAnnotation.X, String.valueOf(getX()));
		args.put(GroupAnnotation.Y, String.valueOf(getY()));
		args.put(GroupAnnotation.Z, String.valueOf(getZ()));
		this.annotation = this.factory.createAnnotation(GroupAnnotation.class, this.networkView, args);
		
		for(OVAbstractAnnotation<Annotation> a : this.annotList) {
			this.annotation.addMember(a.getAnnotation());
		}
		
		return super.getAnnotation();
	}
}
