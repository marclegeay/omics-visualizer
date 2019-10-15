package dk.ku.cpr.OmicsVisualizer.internal.task;

import java.util.Arrays;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;
import org.cytoscape.work.Tunable;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVConnection;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVLegend;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVLegend.OVLegendOrientation;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVLegend.OVLegendPosition;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVVisualization;

public class DrawLegendTunableTask extends DrawLegendTask {

	@Tunable(description="Should the inner visualization included in the legend? Default: true.",
			required=false,
			gravity=1.0)
	public boolean includeInner=true;
	
	@Tunable(description="Should the outer visualization included in the legend? Default: true.",
			required=false,
			gravity=1.0)
	public boolean includeOuter=true;

	@Tunable(description="The title of the legend. If null, the name of the network will be used.",
			required=false,
			gravity=1.0)
	public String title=null;
	
	@Tunable(description="The font family.",
			required=false,
			gravity=1.0)
	public String fontName = OVLegend.DEFAULT_FONT.getFamily();
	
	@Tunable(description="The font size (integer).",
	required=false,
	gravity=1.0)
	public int fontSize = OVLegend.DEFAULT_FONT_SIZE;
	
	@Tunable(description="The position of the legend. Must be one of: 'NORTH', 'NORTH_LEFT', 'NORTH_RIGHT' (same with SOUTH) or 'EAST', 'EAST_TOP', 'EAST_BOTTOM' (same with WEST). Default: NORTH_LEFT.",
			required=false,
			gravity=1.0)
	public String position = OVLegendPosition.NORTH_LEFT.name();
	
	@Tunable(description="The orientation of the legend. Must be one of: HORIZONTAL or VERTICAL. Default: HORIZONTAL.",
			required=false,
			gravity=1.0)
	public String orientation = OVLegendOrientation.HORIZONTAL.name();
	
	@Tunable(description="Should the view of the network be fitted to the content after the legend is added. Default: true.",
			required=false,
			gravity=1.0)
	public boolean fitView = true;

	public DrawLegendTunableTask(OVManager ovManager) {
		super(ovManager, null, true);
	}

	public void run(TaskMonitor taskMonitor) throws Exception {
		CyNetwork currentNetwork = this.ovManager.getService(CyApplicationManager.class).getCurrentNetwork();
		if(currentNetwork == null) {
			taskMonitor.showMessage(Level.ERROR, "ERROR: There is no active network.");
			return;
		}
		
		OVConnection ovCon = this.ovManager.getConnection(this.ovManager.getService(CyRootNetworkManager.class).getRootNetwork(currentNetwork));
		if(ovCon == null) {
			taskMonitor.showMessage(Level.ERROR, "ERROR: The active network is not connected to any Omics Visualizer table.");
			return;
		}
		
		OVTable currentTable = this.ovManager.getActiveOVTable();
		if(currentTable == null) {
			taskMonitor.showMessage(Level.ERROR, "ERROR: There is no active Omics Visualizer table.");
			return;
		}
		
		if(!ovCon.getOVTable().equals(currentTable)) {
			taskMonitor.showMessage(Level.ERROR, "ERROR: The active network and active Omics Visualizer table are not connected.");
			return;
		}
		
		if(!Arrays.asList(OVShared.getAvailableFontNames()).contains(fontName)) {
			taskMonitor.showMessage(Level.WARN, "WARNING: The font '"+fontName+"' is unknown, the default font '"+OVLegend.DEFAULT_FONT.getFamily()+"' will be used.");
			this.fontName = OVLegend.DEFAULT_FONT.getFamily();
		}
		
		if(title == null) {
			title = ovCon.getBaseNetwork().toString();
		}
		
		OVVisualization innerViz = ovCon.getInnerVisualization();
		OVVisualization outerViz = ovCon.getOuterVisualization();

		if(!includeInner) {
			innerViz=null;
		}
		if(!includeOuter) {
			outerViz=null;
		}
		
		this.ovLegend = new OVLegend(innerViz, outerViz, title, fontName, fontSize, OVLegendPosition.valueOf(position), OVLegendOrientation.valueOf(orientation));
		ovCon.setLegend(this.ovLegend);
		
		this.updateView = this.fitView;
		
		super.run(taskMonitor);
	}

}
