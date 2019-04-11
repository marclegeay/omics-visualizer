package dk.ku.cpr.OmicsVisualizer.internal.task;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVColorContinuous;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVConnection;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVVisualization;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVVisualization.ChartType;

public class ApplyVisualizationTask extends AbstractTask {

	private OVManager ovManager;
	private OVConnection ovCon;
	private OVVisualization ovViz;

	public ApplyVisualizationTask(OVManager ovManager, OVConnection ovCon, OVVisualization ovViz) {
		super();
		this.ovManager = ovManager;
		this.ovCon = ovCon;
		this.ovViz=ovViz;
	}

	private void createOVListColumn(CyTable cyTable, String colName, Class<?> valueType) {
		if(cyTable.getColumn(colName) == null) {
			cyTable.createListColumn(colName, valueType, false);
		}
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Apply visualization to Network");

		if(this.ovViz == null) {
			return;
		}

		// First we erase all previous charts
		taskMonitor.setStatusMessage("Cleaning previous data");
		CyTable nodeTable = this.ovCon.getBaseNetwork().getDefaultNodeTable();
		OVShared.deleteOVColumns(nodeTable, this.ovViz.getType());
		String vizCol;
		if(this.ovViz.getType().equals(ChartType.CIRCOS)) {
			vizCol = OVShared.CYNODETABLE_OUTERVIZCOL;
		} else {
			vizCol = OVShared.CYNODETABLE_INNERVIZCOL;
		}
		nodeTable.createColumn(vizCol, String.class, false);

		double progress = 0.0;
		taskMonitor.setProgress(progress);
		
		Class<?> outputValuesType = this.ovViz.getValuesType();
		// A continuous mapping should be Double
		if(this.ovViz.getColors() instanceof OVColorContinuous) {
			outputValuesType = Double.class;
		}

		double nbNodes = this.ovCon.getBaseNetwork().getNodeCount() * 1.0;
		int i=0;
		// Then we fill the columns
		taskMonitor.setStatusMessage("Computing visualization for each node");
		for(CyNode node : this.ovCon.getBaseNetwork().getNodeList()) {
			progress = (i++)/nbNodes;
			taskMonitor.setProgress(progress);
			
			ArrayList<Object> nodeValues = new ArrayList<>();
			String nodeLabels = "";
			for(CyRow tableRow : this.ovCon.getLinkedRows(nodeTable.getRow(node.getSUID()))) {
				if(this.ovViz.isOnlyFiltered() && !this.ovCon.getOVTable().isFiltered(tableRow)) {
					continue;
				}
				
				for(String colName : this.ovViz.getValues()) {
					Object val = tableRow.get(colName, this.ovViz.getValuesType());
					
					// If we have a continuous mapping, we have to change the value to center them in rangeZero
					if(this.ovViz.getColors() instanceof OVColorContinuous) {
						double zero = ((OVColorContinuous) this.ovViz.getColors()).getRangeZero();
						Double newVal=null;
						
						// We check if we have a missing value
						if(val == null) {
							newVal = Double.NaN;
						} else { // We subtract the zero value to center it
							newVal = (Double.parseDouble(val.toString())) - zero;
						}
						
						val = newVal;
					}
					
					nodeValues.add(val);
				}
				if(this.ovViz.getLabel() != null) {
					nodeLabels += tableRow.get(this.ovViz.getLabel(), this.ovCon.getOVTable().getColType(this.ovViz.getLabel()));
					nodeLabels += ",";
				}
			}

			if(nodeValues.isEmpty()) { // If the node is not connected to a row, we display nothing
				continue;
			}

			int ncol = this.ovViz.getValues().size();
			int nrow = nodeValues.size() / ncol;

			List<String> attributeList = new ArrayList<>();

			List<List<Object>> styleValues = new ArrayList<>();

			int ncolValues = (this.ovViz.isTranspose() ? nrow : ncol);
			int nrowValues = (this.ovViz.isTranspose() ? ncol : nrow);
			for(int c=0; c<ncolValues; ++c) {
				String colName;
				if(this.ovViz.getType().equals(ChartType.CIRCOS)) {
					colName = OVShared.CYNODETABLE_OUTERVIZCOL_VALUES+(c+1);
				} else {
					colName = OVShared.CYNODETABLE_INNERVIZCOL_VALUES+(c+1);
				}
				attributeList.add(colName);
				// We create the column if this one does not exist yet
				createOVListColumn(nodeTable, colName, outputValuesType);

				List<Object> colValues = new ArrayList<>();
				for(int r=0; r<nrowValues; ++r) {
					int index = 0;

					if(this.ovViz.isTranspose()) {
						index = c*ncol+r;
					} else {
						index = r*ncol+c;
					}

					colValues.add(nodeValues.get(index));
				}
				nodeTable.getRow(node.getSUID()).set(colName, colValues);
				styleValues.add(colValues);
			}

			String nodeStyle = this.ovViz.toEnhancedGraphics(styleValues);
			if(this.ovViz.isContinuous()) {
				// Only continuous mapping needs attributes
				nodeStyle += " attributelist=\"" + String.join(",", attributeList) + "\"";
			}


			if(nodeLabels.length()>0) {
				if(this.ovViz.isTranspose()) {
					nodeLabels = "showlabels=\"false\" labelcircles=east circlelabels=\"" + nodeLabels.substring(0, nodeLabels.length()-1) + "\"";
				} else {
					nodeLabels = "labellist=\"" + nodeLabels.substring(0, nodeLabels.length()-1) + "\" showlabels=\"true\"";
				}
			} else {
				nodeLabels = "showlabels=\"false\"";
			}

			nodeStyle += " " + nodeLabels;

			nodeTable.getRow(node.getSUID()).set(vizCol, nodeStyle);
		}


		taskMonitor.setStatusMessage("Adding the VisualMappingFunction");
		// Finally we draw
		VisualMappingManager vmm = this.ovManager.getService(VisualMappingManager.class);
		// The connected network has been automatically selected when the window was opened
		CyNetworkView netView = this.ovManager.getService(CyApplicationManager.class).getCurrentNetworkView();
		if(netView != null) {
			VisualMappingFunctionFactory passthroughFactory = this.ovManager.getService(VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
			VisualLexicon lex = this.ovManager.getService(RenderingEngineManager.class).getDefaultVisualLexicon();
			VisualProperty<?> customGraphics;
			PassthroughMapping<?,?> pMapping;
			if(this.ovViz.getType().equals(ChartType.CIRCOS)) {
				customGraphics= lex.lookup(CyNode.class, OVShared.MAPPING_OUTERVIZ_IDENTIFIER);
				pMapping = (PassthroughMapping<?,?>) passthroughFactory.createVisualMappingFunction(OVShared.CYNODETABLE_OUTERVIZCOL, String.class, customGraphics); 
			} else {
				customGraphics= lex.lookup(CyNode.class, OVShared.MAPPING_INNERVIZ_IDENTIFIER); 
				pMapping = (PassthroughMapping<?,?>) passthroughFactory.createVisualMappingFunction(OVShared.CYNODETABLE_INNERVIZCOL, String.class, customGraphics);
			}
			vmm.getVisualStyle(netView).addVisualMappingFunction(pMapping);
			netView.updateView();
		}

	}

}
