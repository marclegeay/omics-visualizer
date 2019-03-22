package dk.ku.cpr.OmicsVisualizer.internal.task;

import java.util.Collection;

import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVConnection;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;

public class RemoveVisualizationTask extends AbstractTask {
	private OVManager ovManager;
	private OVConnection ovCon;

	public RemoveVisualizationTask(OVManager ovManager, OVConnection ovCon) {
		super();
		this.ovManager = ovManager;
		this.ovCon = ovCon;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Remove visualization");
		taskMonitor.setStatusMessage("Network Collection: " + this.ovCon.getCollectionNetworkName());
		
		taskMonitor.setStatusMessage("Removing the VisualMappingFunction");
		VisualMappingManager vmm = this.ovManager.getService(VisualMappingManager.class);
		VisualLexicon lex = this.ovManager.getService(RenderingEngineManager.class).getDefaultVisualLexicon();
		Collection<CyNetworkView> netViews = this.ovManager.getService(CyNetworkViewManager.class).getNetworkViews(this.ovCon.getBaseNetwork());
		for(CyNetworkView netView : netViews) {
			vmm.getVisualStyle(netView).removeVisualMappingFunction(lex.lookup(CyNode.class, OVShared.MAPPING_VIZ_IDENTIFIER));
			netView.updateView();
		}
		
		// We erase all NodeTable columns
		taskMonitor.setStatusMessage("Cleaning node table data");
		CyTable nodeTable = this.ovCon.getBaseNetwork().getDefaultNodeTable();
		OVShared.deleteOVColumns(nodeTable);
		
		// We erase all NetworkTable columns
		taskMonitor.setStatusMessage("Cleaning network table data");
		this.ovCon.setVisualization(null);
	}

}
