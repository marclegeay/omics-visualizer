package dk.ku.cpr.OmicsVisualizer.internal.task;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;

import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVConnection;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVVisualization.ChartType;

public class RemoveVisualizationTask extends AbstractTask {
	protected OVManager ovManager;
	protected OVConnection ovCon;
	protected String type;

	public RemoveVisualizationTask(OVManager ovManager, OVConnection ovCon, String type) {
		super();
		this.ovManager = ovManager;
		this.ovCon = ovCon;
		this.type = type;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle(this.getTitle());
		
		// We do some checks before doing anything
		List<String> availableTypes = Arrays.asList("inner", "outer", "all");
		if(!availableTypes.contains(this.type)) {
			taskMonitor.setStatusMessage("Error: The type \"" + this.type + "\" is unknown.\nAvailable types are: " + OVShared.join(availableTypes, ", "));
			JOptionPane.showMessageDialog(null, "Error: The type of Visualization is unknown.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if(this.ovCon == null) {
			taskMonitor.setStatusMessage("Error: Impossible to identify the Network.");
			JOptionPane.showMessageDialog(null, "Error: Impossible to identify the Network.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		taskMonitor.setStatusMessage("Network Collection: " + this.ovCon.getCollectionNetworkName());
		taskMonitor.setStatusMessage("Type: " + this.type);
		
		taskMonitor.setStatusMessage("Removing the VisualMappingFunction");
		VisualMappingManager vmm = this.ovManager.getService(VisualMappingManager.class);
		VisualLexicon lex = this.ovManager.getService(RenderingEngineManager.class).getDefaultVisualLexicon();
		Collection<CyNetworkView> netViews = this.ovManager.getService(CyNetworkViewManager.class).getNetworkViews(this.ovCon.getBaseNetwork());
		for(CyNetworkView netView : netViews) {
			if(this.type.equals("inner") || this.type.equals("all")) {
				vmm.getVisualStyle(netView).removeVisualMappingFunction(lex.lookup(CyNode.class, OVShared.MAPPING_INNERVIZ_IDENTIFIER));	
			}
			if(this.type.equals("outer") || this.type.equals("all")) {
				vmm.getVisualStyle(netView).removeVisualMappingFunction(lex.lookup(CyNode.class, OVShared.MAPPING_OUTERVIZ_IDENTIFIER));	
			}
			netView.updateView();
		}
		
		// We erase all NodeTable columns
		taskMonitor.setStatusMessage("Cleaning node table data");
		CyTable nodeTable = this.ovCon.getBaseNetwork().getDefaultNodeTable();
		if(this.type.equals("inner") || this.type.equals("all")) {
			OVShared.deleteOVColumns(nodeTable, ChartType.PIE);
		}
		if(this.type.equals("outer") || this.type.equals("all")) {
			OVShared.deleteOVColumns(nodeTable, ChartType.CIRCOS);
		}
		
		
		// We erase all NetworkTable columns
		taskMonitor.setStatusMessage("Cleaning network table data");
		if(this.type.equals("inner") || this.type.equals("all")) {
			this.ovCon.setInnerVisualization(null);	
		}
		if(this.type.equals("outer") || this.type.equals("all")) {
			this.ovCon.setOuterVisualization(null);	
		}
		
		this.ovManager.getOVCytoPanel().update();
	}
	
	@ProvidesTitle
	public String getTitle() {
		return "Remove visualization";
	}

}
