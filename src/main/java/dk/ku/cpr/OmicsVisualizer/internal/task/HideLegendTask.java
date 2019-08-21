package dk.ku.cpr.OmicsVisualizer.internal.task;

import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;

public class HideLegendTask extends AbstractTask {
	
	private OVManager ovManager;

	public HideLegendTask(OVManager ovManager) {
		super();
		this.ovManager = ovManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Hide Legend");
		
		AnnotationManager annotManager = this.ovManager.getService(AnnotationManager.class);
		if(annotManager == null) {
			taskMonitor.showMessage(Level.ERROR, "AnnotationManager is null");
			return;
		}
		
		CyNetworkView networkView = this.ovManager.getService(CyApplicationManager.class).getCurrentNetworkView();
		if(networkView == null) {
			taskMonitor.showMessage(Level.ERROR, "networkView is null");
			return;
		}
		
		taskMonitor.setStatusMessage("Removing legend... ");
		
		List<Annotation> previousAnnotations = annotManager.getAnnotations(networkView);
		for(Annotation annot : previousAnnotations) {
			if(annot.getName() != null && annot.getName().startsWith(OVShared.OVLEGEND_ANNOTATION_NAME)) {
				annotManager.removeAnnotation(annot);
			}
		}
	}

}
