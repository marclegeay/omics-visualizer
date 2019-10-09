package dk.ku.cpr.OmicsVisualizer.internal.task;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVColorContinuous;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVColorDiscrete;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVLegend;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVLegend.OVLegendOrientation;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVVisualization;
import dk.ku.cpr.OmicsVisualizer.internal.model.annotations.OVAbstractAnnotation;
import dk.ku.cpr.OmicsVisualizer.internal.model.annotations.OVGroupAnnotation;
import dk.ku.cpr.OmicsVisualizer.internal.model.annotations.OVShapeAnnotation;
import dk.ku.cpr.OmicsVisualizer.internal.model.annotations.OVTextAnnotation;

public class DrawLegendTask extends AbstractTask {
	
	protected OVManager ovManager;
	protected OVLegend ovLegend;
	
	private TaskMonitor taskMonitor;
	
	private CyNetworkView networkView;

	private AnnotationFactory<ShapeAnnotation> shapeFactory;
	private AnnotationFactory<TextAnnotation> textFactory;
	private AnnotationFactory<GroupAnnotation> groupFactory;
	
	private OVGroupAnnotation root;

	private static final float TITLE_RATIO = 1.5f;
	private static final int VERTICAL_BOX_RATIO = 5;
	private static final int HORIZONTAL_BOX_RATIO = 10;
	private static final float MARGIN_RATIO = 0.3f;
	private static final float TICK_RATIO = 0.2f;
	
	public DrawLegendTask(OVManager ovManager, OVLegend ovLegend) {
		this.ovManager=ovManager;
		this.ovLegend=ovLegend;
	}
	
	private double getMargin() {
		return this.ovLegend.getFontSize() * MARGIN_RATIO;
	}
	
	private OVShapeAnnotation createBorder(double x, double y, double width, double height) {
		OVShapeAnnotation border = new OVShapeAnnotation(shapeFactory, networkView);
		
		border.setX(x);
		border.setY(y);
		border.setWidth(width);
		border.setHeight(height);
		border.setShapeType(ShapeType.RECTANGLE);
		
		return border;
	}
	
	/**
	 * Create ticks from the origin point (x,y) to the point (x,y)+length.
	 * Can be vertical or horizontal.
	 * If vertical, the origin point is consider to be the upper right point of the gradient box.
	 * If horizontal, the origin point is consider to be the lower left point of the gradient box.
	 * @param x
	 * @param y
	 * @param length
	 * @param fractions
	 * @param tickValues
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private List<OVAbstractAnnotation> createTicks(double x, double y, double length, float[] fractions, double[] tickValues, double tickThickness) {
		ArrayList<OVAbstractAnnotation> tickAnnotations = new ArrayList<>();
		
		if(fractions.length != tickValues.length) {
			taskMonitor.showMessage(Level.WARN, "Impossible to draw ticks because the number of ticks does not match the number of tick labels.");
			return tickAnnotations;
		}
		
		double lastPosition=-Double.MAX_VALUE;
		double lastWidth=0; // in case of VERTICAL overlap
		for(int f=0; f<fractions.length; ++f) {
			double tickShift = fractions[f]*length;
			
			OVShapeAnnotation tick = null;
			if(this.ovLegend.getOrientation().equals(OVLegendOrientation.HORIZONTAL)) {
				tick = createBorder(x + tickShift, y, 0, tickThickness);
			} else { // VERTICAL
				tick = createBorder(x, y + tickShift, tickThickness, 0);
			}
			tick.setFillColor(Color.BLACK);
			tick.setName("tick " + f);
			
			double legendSpacing = tickThickness + getMargin();
			double positionShift = 0; // if two legends are too close
			
			OVTextAnnotation tickLegend = new OVTextAnnotation(textFactory, networkView);
			NumberFormat decimalFormatter = new DecimalFormat("#0.00");
			tickLegend.setFont(this.ovLegend.getFont());
			tickLegend.setText(decimalFormatter.format(tickValues[f]));
			tickLegend.setName(decimalFormatter.format(tickValues[f]));
			
			if(this.ovLegend.getOrientation().equals(OVLegendOrientation.HORIZONTAL)) {
				// The center of the legend should be align with the tick
				double newX = x + tickShift - (tickLegend.getWidth()/2.0);
				
				if(newX < lastPosition) { // There will be an overlap, we shift the text
					// We shift the text from 1 line + margin
					positionShift = this.ovLegend.getFontSize() * (1.0+MARGIN_RATIO);
				}
				
				tickLegend.setPosition(newX, y + legendSpacing + positionShift);
				
				lastPosition = tickLegend.getX()+tickLegend.getWidth();
			} else { // VERTICAL
				// The center of the legend should be align with the tick
				double newY = y + tickShift - (tickLegend.getHeight()/2.0);
				
				if(newY < lastPosition) { // There will be an overlap, we shift the text
					// We shift the text from the size of previous text width + margin
					positionShift = lastWidth + getMargin();
				}
				
				tickLegend.setPosition(x + legendSpacing + positionShift, newY);
				
				lastPosition = tickLegend.getY()+tickLegend.getHeight();
				lastWidth = tickLegend.getWidth();
			}

			tickAnnotations.add(tick);
			tickAnnotations.add(tickLegend);
		}
		
		return tickAnnotations;
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Annotation> OVGroupAnnotation createContinuousLegend(OVColorContinuous ovColor) {
		OVGroupAnnotation legendGroup = new OVGroupAnnotation(groupFactory, networkView);
		
		float middlePosition = (float) ((ovColor.getRangeZero()-ovColor.getRangeMin()) / (ovColor.getRangeMax()-ovColor.getRangeMin()));
		
		int fontSize = this.ovLegend.getFontSize();
		
		// We initialize with the HORIZONTAL setting
		float fractions[] = {0.0f, middlePosition, 1.0f};
		double tickValues[] = {ovColor.getRangeMin(), ovColor.getRangeZero(), ovColor.getRangeMax()};
		Color colors[] = { ovColor.getDown(), ovColor.getZero(), ovColor.getUp() };
		
		float endX=0.0f;
		float endY=0.0f;
		double widthRatio=1.0;
		double heightRatio=1.0;
		
		if(this.ovLegend.getOrientation().equals(OVLegendOrientation.HORIZONTAL)) {
			endX=1.0f;
			endY=0.0f;
			widthRatio=HORIZONTAL_BOX_RATIO;
			heightRatio=1.0;
		} else { // VERTICAL
			// The HORIZONTAL is drown left to right, so Down to Up
			// The VERTICAL is drown from top to bottom, so Up to Down
			// We have to reverse everything
			fractions[1] = 1.0f-fractions[1];
			
			tickValues[0]=ovColor.getRangeMax();
			tickValues[2]=ovColor.getRangeMin();
			
			colors[0] = ovColor.getUp();
			colors[2] = ovColor.getDown();
			
			endX=0.0f;
			endY=1.0f;
			widthRatio=1.0;
			heightRatio=VERTICAL_BOX_RATIO;
		}
		LinearGradientPaint paint = new LinearGradientPaint(0, 0, endX, endY, fractions, colors);
		
		OVShapeAnnotation gradientBox = createBorder(0, 0, fontSize*widthRatio, fontSize*heightRatio);
		gradientBox.setFillColor(paint);
		gradientBox.setZ(10); // We put the gradient box on top of others (not working?)
		gradientBox.setName("gradient");
		
		double length = gradientBox.getWidth(); // HORIZONTAL setting
		if(this.ovLegend.getOrientation().equals(OVLegendOrientation.VERTICAL)) {
			length = gradientBox.getHeight();
		}
		
		// (x,y) should be (fontSize,0) if vertical
		// or (0,fontSize) if horizontal
		// we use endX and endY to 'generalize' the process here
		for(OVAbstractAnnotation<T> a : createTicks(fontSize*endY, fontSize*endX, length, fractions, tickValues, fontSize*TICK_RATIO)) {
			legendGroup.addAnnotation(a);
		}
		
		// We add the gradientBox in the end to have more chances that the box has a greater Z-order
		legendGroup.addAnnotation(gradientBox);
		
		return legendGroup;
	}
	
	private OVGroupAnnotation createDiscreteLegend(OVColorDiscrete ovColor) {
		OVGroupAnnotation legend = new OVGroupAnnotation(groupFactory, networkView);
		
		double boxWidth=this.ovLegend.getFontSize();
		double boxHeight=this.ovLegend.getFontSize();
		double x=0;
		double y=0;
		
		// We sort the values
		SortedSet<Object> values = new TreeSet<>(ovColor.getValues());
		for(Object val : values) {
			OVShapeAnnotation box = createBorder(x, y, boxWidth, boxHeight);
			box.setFillColor(ovColor.getColor(val));
			box.setName("color box " + val.toString());
			
			OVTextAnnotation boxLegend = new OVTextAnnotation(textFactory, networkView);
			boxLegend.setFont(this.ovLegend.getFont());
			boxLegend.setText(val.toString());
			boxLegend.setName(val.toString());
			boxLegend.setPosition(x + box.getWidth() + getMargin(), y);

			legend.addAnnotation(box);
			legend.addAnnotation(boxLegend);
			
			if(this.ovLegend.getOrientation().equals(OVLegendOrientation.HORIZONTAL)) {
				x = legend.getWidth() + getMargin();
				y = 0;
			} else { // VERTICAL
				x = 0;
				y = legend.getHeight() + getMargin();
			}
		}
		
		return legend;
	}
	
	private OVGroupAnnotation createLegendText(List<String> texts) {
		OVGroupAnnotation legend = new OVGroupAnnotation(groupFactory, networkView);
		
		double y = 0;
		for(String text : texts) {
			OVTextAnnotation textAnnotation = new OVTextAnnotation(textFactory, networkView);
			textAnnotation.setFont(this.ovLegend.getFont());
			textAnnotation.setText(text);
			textAnnotation.setName(text);
			textAnnotation.setY(y);
			
			legend.addAnnotation(textAnnotation);
			
			y = legend.getHeight() + getMargin();
		}
		
		return legend;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Draw Legend");
		
		AnnotationManager annotManager = this.ovManager.getService(AnnotationManager.class);
		if(annotManager == null) {
			taskMonitor.showMessage(Level.ERROR, "AnnotationManager is null");
			return;
		}
		
		this.shapeFactory = (AnnotationFactory<ShapeAnnotation>) this.ovManager.getService(AnnotationFactory.class, "(type=ShapeAnnotation.class)");
		if(this.shapeFactory == null) {
			taskMonitor.showMessage(Level.ERROR, "shapeFactory is null");
			return;
		}
		
		this.textFactory = (AnnotationFactory<TextAnnotation>) this.ovManager.getService(AnnotationFactory.class, "(type=TextAnnotation.class)");
		if(this.textFactory == null) {
			taskMonitor.showMessage(Level.ERROR, "textFactory is null");
			return;
		}
		
		this.groupFactory = (AnnotationFactory<GroupAnnotation>) this.ovManager.getService(AnnotationFactory.class, "(type=GroupAnnotation.class)");
		if(this.groupFactory == null) {
			taskMonitor.showMessage(Level.ERROR, "groupFactory is null");
			return;
		}
		
		this.networkView = this.ovManager.getService(CyApplicationManager.class).getCurrentNetworkView();
		if(this.networkView == null) {
			taskMonitor.showMessage(Level.ERROR, "networkView is null");
			return;
		}
		
		this.taskMonitor = taskMonitor;

		// -----------------------------------
		// First we remove the previous legend
		// -----------------------------------
		
		taskMonitor.setStatusMessage("Removing previous legend... ");
		
		List<Annotation> previousAnnotations = annotManager.getAnnotations(networkView);
		for(Annotation annot : previousAnnotations) {
			if(annot.getName() != null && annot.getName().startsWith(OVShared.OVLEGEND_ANNOTATION_NAME)) {
				annotManager.removeAnnotation(annot);
			}
		}

		// ---------------------------------
		// We check if the Legend is visible
		// ---------------------------------
		
		if(!this.ovLegend.isVisible()) {
			taskMonitor.showMessage(Level.WARN, "The legend is not visible, it will not be drawn.");
			return;
		}
		
		// ----------------------------------
		// Then we create the specific panels
		// ----------------------------------
		
		taskMonitor.setStatusMessage("Creating legend...");
		
		root = new OVGroupAnnotation(groupFactory, networkView);
		// The coordinates (startX, startY) of the top-right corner of the space that each component should have
		double startX=0.0;
		double startY = 0.0;
		
		// Legend title
		if(!this.ovLegend.getTitle().isEmpty()) {
			OVTextAnnotation legendTitle = new OVTextAnnotation(textFactory, networkView);
			
			legendTitle.setPosition(0.0, 0.0);
			legendTitle.setFont(this.ovLegend.getFont().deriveFont(this.ovLegend.getFontSize() * TITLE_RATIO));
			legendTitle.setText(this.ovLegend.getTitle());
			legendTitle.setName(this.ovLegend.getTitle());
			
			root.addAnnotation(legendTitle);
			
			startX = 0.0;
			startY = root.getHeight() + getMargin();
		}
		
		OVVisualization innerViz = this.ovLegend.getInnerVisualization();
		if(this.ovLegend.isDrawInner() && innerViz != null) {
			OVGroupAnnotation innerLegend = new OVGroupAnnotation(groupFactory, networkView);
			
			// Legend text
			OVGroupAnnotation caption = createLegendText(innerViz.getValues());
			caption.setName("innerViz caption");
			innerLegend.addAnnotation(caption);
			
			if(innerViz.isContinuous()) {
				OVGroupAnnotation legendColors = createContinuousLegend((OVColorContinuous) innerViz.getColors());
				legendColors.setName("innerViz continuous");
				
				legendColors.setPosition(0, innerLegend.getHeight() + getMargin());
				innerLegend.addAnnotation(legendColors);
			} else {
				OVGroupAnnotation legend = createDiscreteLegend((OVColorDiscrete) innerViz.getColors());
				legend.setName("innerViz discrete");
				legend.setPosition(0, innerLegend.getHeight() + getMargin());
				innerLegend.addAnnotation(legend);
			}
			
			OVShapeAnnotation border = createBorder(-getMargin(), -getMargin(), innerLegend.getWidth() + 2*getMargin(), innerLegend.getHeight() + 2*getMargin());
			border.setName("innerViz border");
			innerLegend.addAnnotation(border);
			
			innerLegend.setPosition(startX, startY);
			innerLegend.setName("innerViz legend");
			root.addAnnotation(innerLegend);

			// We set the coordinates for the outerViz
			if(this.ovLegend.getOrientation().equals(OVLegendOrientation.HORIZONTAL)) {
				// If the legends have the horizontal orientation, then we stack the inner and outer viz vertically
				startY = root.getHeight() + getMargin()*2;
			} else { // VERTICAL
				startX = innerLegend.getWidth() + getMargin()*2;
			}
		}
		
		OVVisualization outerViz = this.ovLegend.getOuterVisualization();
		if(this.ovLegend.isDrawOuter() && outerViz != null) {
			OVGroupAnnotation outerLegend = new OVGroupAnnotation(groupFactory, networkView);
			
			// Legend text
			List<String> captionTexts = new ArrayList<>();
			
			// If we have more than 1 column, we announce the order of the rings/slices
			if(outerViz.getValues().size() > 1) {
				String captionSentence = "";
				if(outerViz.isTranspose()) { // columns are slices
					captionSentence = "The slices are orderred, from 3 o'clock then counterclockwise:";
				} else { // columns are rings
					captionSentence = "The rings are orderred, from inner to outer:";
				}

				captionTexts.add(captionSentence);
			}
			captionTexts.addAll(outerViz.getValues());
			
			OVGroupAnnotation caption = createLegendText(captionTexts);
			caption.setName("outerViz caption");
			outerLegend.addAnnotation(caption);
			
			if(outerViz.isContinuous()) {
				OVGroupAnnotation legend = createContinuousLegend((OVColorContinuous) outerViz.getColors());
				legend.setName("outerViz continuous");
				legend.setPosition(0, outerLegend.getHeight() + getMargin());
				outerLegend.addAnnotation(legend);
			} else {
				OVGroupAnnotation legend = createDiscreteLegend((OVColorDiscrete) outerViz.getColors());
				legend.setName("outerViz discrete");
				legend.setPosition(0, outerLegend.getHeight() + getMargin());
				outerLegend.addAnnotation(legend);
			}
			
			OVShapeAnnotation border = createBorder(-getMargin(), -getMargin(), outerLegend.getWidth() + 2*getMargin(), outerLegend.getHeight() + 2*getMargin());
			border.setName("outerViz border");
			outerLegend.addAnnotation(border);
			
			outerLegend.setPosition(startX, startY);
			outerLegend.setName("outerViz legend");
			root.addAnnotation(outerLegend);
		}


		// ------------------------
		// Finally we position the legend with respect to the network
		// ------------------------

		// First we identify the dimensions of the network
		double networkMinX=Double.MAX_VALUE;
		double networkMinY=Double.MAX_VALUE;
		double networkMaxX=-Double.MAX_VALUE;
		double networkMaxY=-Double.MAX_VALUE;
		
		for(View<CyNode> nodeView : this.networkView.getNodeViews()) {
			double nodeX = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
			double nodeY = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
			double nodeWidth = nodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH);
			double nodeHeight = nodeView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT);

			networkMinX = Math.min(networkMinX, nodeX-nodeWidth);
			networkMinY = Math.min(networkMinY, nodeY-nodeHeight);
			networkMaxX = Math.max(networkMaxX, nodeX+nodeWidth);
			networkMaxY = Math.max(networkMaxY, nodeY+nodeHeight);
		}
		
		double networkCenterX=(networkMinX + networkMaxX)/2;
		double networkCenterY=(networkMinY + networkMaxY)/2;
		
		double rootX=0;
		double rootY=0;
		double rootWidth=root.getWidth();
		double rootHeight=root.getHeight();
		
		switch(this.ovLegend.getPosition()) {
		case EAST:
			rootX = networkMaxX;
			rootY = networkCenterY - rootHeight/2;
			break;
		case EAST_BOTTOM:
			rootX = networkMaxX;
			rootY = networkMaxY - rootHeight;
			break;
		case EAST_TOP:
			rootX = networkMaxX;
			rootY = networkMinY;
			break;
		case NORTH:
			rootX = networkCenterX - rootWidth/2;
			rootY = networkMinY - rootHeight;
			break;
		case NORTH_LEFT:
			rootX = networkMinX;
			rootY = networkMinY - rootHeight;
			break;
		case NORTH_RIGHT:
			rootX = networkMaxX - rootWidth;
			rootY = networkMinY - rootHeight;
			break;
		case SOUTH:
			rootX = networkCenterX - rootWidth/2;
			rootY = networkMaxY;
			break;
		case SOUTH_LEFT:
			rootX = networkMinX;
			rootY = networkMaxY;
			break;
		case SOUTH_RIGHT:
			rootX = networkMaxX - rootWidth;
			rootY = networkMaxY;
			break;
		case WEST:
			rootX = networkMinX - rootWidth;
			rootY = networkCenterY - rootHeight/2;
			break;
		case WEST_BOTTOM:
			rootX = networkMinX - rootWidth;
			rootY = networkMaxY - rootHeight;
			break;
		case WEST_TOP:
			rootX = networkMinX - rootWidth;
			rootY = networkMinY;
			break;
		}
		
		root.setName(OVShared.OVLEGEND_ANNOTATION_NAME);
		root.setPosition(rootX, rootY);
		
		// We add the group to the Cytoscape Annotations
		annotManager.addAnnotation(root.getAnnotation());
	}

}
