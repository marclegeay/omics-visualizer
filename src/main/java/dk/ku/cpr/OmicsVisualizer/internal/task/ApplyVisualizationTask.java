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

public class ApplyVisualizationTask extends AbstractTask {

	private OVManager ovManager;
	private OVConnection ovCon;
	private boolean onlyFiltered;

	public ApplyVisualizationTask(OVManager ovManager, OVConnection ovCon, boolean onlyFiltered) {
		super();
		this.ovManager = ovManager;
		this.ovCon = ovCon;
		this.onlyFiltered=onlyFiltered;
	}

	private void createOVListColumn(CyTable cyTable, String colName, Class<?> valueType) {
		if(cyTable.getColumn(colName) == null) {
			cyTable.createListColumn(colName, valueType, false);
		}
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Apply visualization to Network");

		// First we erase all previous charts
		taskMonitor.setStatusMessage("Cleaning previous data");
		CyTable nodeTable = this.ovCon.getBaseNetwork().getDefaultNodeTable();
		OVShared.deleteOVColumns(nodeTable);
		nodeTable.createColumn(OVShared.CYNODETABLE_VIZCOL, String.class, false);

		OVVisualization ovViz = this.ovCon.getVisualization();
		if(ovViz == null) {
			return;
		}

		double progress = 0.0;
		taskMonitor.setProgress(progress);

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
				if(this.onlyFiltered && !this.ovCon.getOVTable().isFiltered(tableRow)) {
					continue;
				}
				
				for(String colName : ovViz.getValues()) {
					Object val = tableRow.get(colName, ovViz.getValuesType());
					
					// Missing values are treated as 0 (or "")
					if(val == null) {
						if(ovViz.getValuesType() == Integer.class) {
							val = new Integer(0);
						} else if(ovViz.getValuesType() == Long.class) {
							val = new Long(0);
						} else if(ovViz.getValuesType() == Double.class) {
							val = new Double(0.0);
						} else {
							val = "";
						}
					}
					
					// If we have a continuous mapping, we have to change the value to center them in rangeZero
					if(ovViz.getColors() instanceof OVColorContinuous) {
						double zero = ((OVColorContinuous) ovViz.getColors()).getRangeZero();
						Double newVal=null;
						
						// We subtract the zero value to center it
						if(ovViz.getValuesType() == Integer.class) {
							newVal = ((Integer)val) - zero;
						} else if(ovViz.getValuesType() == Long.class) {
							newVal = ((Long)val) - zero;
						} else if(ovViz.getValuesType() == Double.class) {
							newVal = ((Double)val) - zero;
						}
						
						// It should not be List or String with a continuous mapping...
						// But we make sure it is not null
						if(newVal == null) {
							// If it is we do not change the value
							nodeValues.add(val);
							continue;
						}
						
						// And then we cast back the value to its original type
						if(ovViz.getValuesType() == Integer.class) {
							val = newVal.intValue();
						} else if(ovViz.getValuesType() == Long.class) {
							val = newVal.longValue();
						} else { // Double, no cast needed
							val = newVal;
						}
					}
					
					nodeValues.add(val);
				}
				if(ovViz.getLabel() != null) {
					nodeLabels += tableRow.get(ovViz.getLabel(), this.ovCon.getOVTable().getColType(ovViz.getLabel()));
					nodeLabels += ",";
				}
			}

			if(nodeValues.isEmpty()) { // If the node is not connected to a row, we display nothing
				continue;
			}

			int ncol = ovViz.getValues().size();
			int nrow = nodeValues.size() / ncol;

			List<String> attributeList = new ArrayList<>();

			List<List<Object>> styleValues = new ArrayList<>();

			int ncolValues = (ovViz.isTranspose() ? nrow : ncol);
			int nrowValues = (ovViz.isTranspose() ? ncol : nrow);
			for(int c=0; c<ncolValues; ++c) {
				String colName = OVShared.CYNODETABLE_VIZCOL_VALUES+(c+1);
				attributeList.add(colName);
				// We create the column if this one does not exist yet
				createOVListColumn(nodeTable, colName, ovViz.getValuesType());

				List<Object> colValues = new ArrayList<>();
				for(int r=0; r<nrowValues; ++r) {
					int index = 0;

					if(ovViz.isTranspose()) {
						index = c*ncol+r;
					} else {
						index = r*ncol+c;
					}

					colValues.add(nodeValues.get(index));
				}
				nodeTable.getRow(node.getSUID()).set(colName, colValues);
				styleValues.add(colValues);
			}

			String nodeStyle = ovViz.toEnhancedGraphics(styleValues);
			if(ovViz.isContinuous()) {
				// Only continuous mapping needs attributes
				nodeStyle += " attributelist=\"" + String.join(",", attributeList) + "\"";
			}


			if(nodeLabels.length()>0) {
				if(ovViz.isTranspose()) {
					nodeLabels = "showlabels=\"false\" labelcircles=east circlelabels=\"" + nodeLabels.substring(0, nodeLabels.length()-1) + "\"";
				} else {
					nodeLabels = "labellist=\"" + nodeLabels.substring(0, nodeLabels.length()-1) + "\" showlabels=\"true\"";
				}
			} else {
				nodeLabels = "showlabels=\"false\"";
			}

			nodeStyle += " " + nodeLabels;

			nodeTable.getRow(node.getSUID()).set(OVShared.CYNODETABLE_VIZCOL, nodeStyle);
		}


		taskMonitor.setStatusMessage("Adding the VisualMappingFunction");
		// Finally we draw
		VisualMappingManager vmm = this.ovManager.getService(VisualMappingManager.class);
		// The connected network has been automatically selected when the window was opened
		CyNetworkView netView = this.ovManager.getService(CyApplicationManager.class).getCurrentNetworkView();
		if(netView != null) {
			VisualMappingFunctionFactory passthroughFactory = this.ovManager.getService(VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
			VisualLexicon lex = this.ovManager.getService(RenderingEngineManager.class).getDefaultVisualLexicon();
			VisualProperty<?> customGraphics = lex.lookup(CyNode.class, OVShared.MAPPING_VIZ_IDENTIFIER); 
			PassthroughMapping<?,?> pMapping = (PassthroughMapping<?,?>) passthroughFactory.createVisualMappingFunction(OVShared.CYNODETABLE_VIZCOL, String.class, customGraphics);
			vmm.getVisualStyle(netView).addVisualMappingFunction(pMapping);
			netView.updateView();
		}

	}

}
