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
		if(cyTable.getColumn(OVShared.OV_COLUMN_NAMESPACE, colName) == null) {
			cyTable.createListColumn(OVShared.OV_COLUMN_NAMESPACE, colName, valueType, false);
		}
	}
	
	private String escapeComma(Object str) {
		if(str == null) {
			return "";
		}
		
		return escapeComma(str.toString());
	}
	
	private String escapeComma(String str) {
		StringBuilder sb = new StringBuilder();
		
		for(int i=0; i<str.length(); ++i) {
			char curChar = str.charAt(i);
			
			// If the current char is the escape character or the comma, we escape it
			if(curChar == '\\' || curChar == ',') {
				// The character must be escaped twice
				sb.append('\\');
				sb.append('\\');
				sb.append('\\');
			}
			sb.append(curChar);
		}
		
		return sb.toString();
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Apply visualization to Network");
//		System.out.println("[ApplyVisualizationTask::run] Apply visualization to Network");

		if(this.ovViz == null) {
			return;
		}

		// First we erase all previous charts
		taskMonitor.setStatusMessage("Cleaning previous data");
//		System.out.println("[ApplyVisualizationTask::run] Cleaning previous data");
		CyTable nodeTable = this.ovCon.getRootNetwork().getSharedNodeTable();
		OVShared.deleteOVColumns(nodeTable, this.ovViz.getType());
		String vizCol;
		if(this.ovViz.getType().equals(ChartType.CIRCOS)) {
			vizCol = OVShared.CYNODETABLE_OUTERVIZCOL;
		} else {
			vizCol = OVShared.CYNODETABLE_INNERVIZCOL;
		}
		nodeTable.createColumn(OVShared.OV_COLUMN_NAMESPACE, vizCol, String.class, false);

		double progress = 0.0;
		taskMonitor.setProgress(progress);
		
		Class<?> outputValuesType = this.ovViz.getValuesType();
		// A continuous mapping should be Double
		if(this.ovViz.getColors() instanceof OVColorContinuous) {
			outputValuesType = Double.class;
		}

		double nbNodes = this.ovCon.getRootNetwork().getNodeCount() * 1.0;
		int i=0;
		// Then we fill the columns
		taskMonitor.setStatusMessage("Computing visualization for each node");
//		System.out.println("[ApplyVisualizationTask::run] Computing visualization for each node " + this.ovCon.getRootNetwork().getNodeList().size());
		for(CyNode node : this.ovCon.getRootNetwork().getNodeList()) {
			progress = (i++)/nbNodes;
//			System.out.println("progress=" + progress);
			taskMonitor.setProgress(progress);
			
			ArrayList<Object> nodeValues = new ArrayList<>();
			String nodeLabels = "";
			for(CyRow tableRow : this.ovCon.getLinkedRows(node)) {
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
					
					if(val != null ) {
						nodeValues.add(val);
					} else {
//						if(this.ovViz.getValuesType() == Integer.class) {
//							nodeValues.add(Integer.valueOf(0));
//						} else if(this.ovViz.getValuesType() == Long.class) {
//							nodeValues.add(Long.valueOf(0));
//						} else if(this.ovViz.getValuesType() == Double.class) {
//							nodeValues.add(Double.valueOf(0.0));
//						} else {
//							nodeValues.add("");
//						}
						nodeValues.add(null);
					}
					
				}
				if(this.ovViz.getLabel() != null) {
					nodeLabels +=  escapeComma(tableRow.get(this.ovViz.getLabel(), this.ovCon.getOVTable().getColType(this.ovViz.getLabel())));
					nodeLabels += ",";
				}
			}

			if(nodeValues.isEmpty()) { // If the node is not connected to a row, we display nothing
				continue;
			}
			
			// If we want to skip "overflawing" nodes, we check if this node is overflawing and skip if needed
			if(this.ovViz.skipOverflaw() && nodeValues.size() > OVShared.MAXIMUM_ROWS_CONNECTED_TO_NODE) {
				continue;
			}

			int ncol = this.ovViz.getValues().size();
			int nrow = nodeValues.size() / ncol;

			List<String> attributeList = new ArrayList<>();

			List<List<Object>> styleValues = new ArrayList<>();

			int ncolValues = (this.ovViz.isTranspose() ? nrow : ncol);
			int nrowValues = (this.ovViz.isTranspose() ? ncol : nrow);
			for(int c=0; c<ncolValues; ++c) {
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
				styleValues.add(colValues);
				
				if(this.ovViz.isContinuous()) {
					String colName;
					if(this.ovViz.getType().equals(ChartType.CIRCOS)) {
						colName = OVShared.CYNODETABLE_OUTERVIZCOL_VALUES+(c+1);
					} else {
						colName = OVShared.CYNODETABLE_INNERVIZCOL_VALUES+(c+1);
					}
					attributeList.add(OVShared.OV_COLUMN_NAMESPACE+"::"+colName);
					// We create the column if this one does not exist yet
					createOVListColumn(nodeTable, colName, outputValuesType);

					nodeTable.getRow(node.getSUID()).set(OVShared.OV_COLUMN_NAMESPACE, colName, colValues);
				}
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

			nodeTable.getRow(node.getSUID()).set(OVShared.OV_COLUMN_NAMESPACE, vizCol, nodeStyle);
		}


		taskMonitor.setStatusMessage("Adding the VisualMappingFunction");
//		System.out.println("[ApplyVisualizationTask::run] Adding the VisualMappingFunction");
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
				pMapping = (PassthroughMapping<?,?>) passthroughFactory.createVisualMappingFunction(OVShared.OV_COLUMN_NAMESPACE+"::"+OVShared.CYNODETABLE_OUTERVIZCOL, String.class, customGraphics); 
			} else {
				customGraphics= lex.lookup(CyNode.class, OVShared.MAPPING_INNERVIZ_IDENTIFIER); 
				pMapping = (PassthroughMapping<?,?>) passthroughFactory.createVisualMappingFunction(OVShared.OV_COLUMN_NAMESPACE+"::"+OVShared.CYNODETABLE_INNERVIZCOL, String.class, customGraphics);
			}
			vmm.getVisualStyle(netView).addVisualMappingFunction(pMapping);
			netView.updateView();
		}
		
		taskMonitor.setStatusMessage("Updating the Omics Visualizer panel");
//		System.out.println("[ApplyVisualizationTask::run] Updating the Omics Visualizer panel");
		this.ovManager.getOVCytoPanel().update();

//		System.out.println("[ApplyVisualizationTask::run] done");
	}

}
