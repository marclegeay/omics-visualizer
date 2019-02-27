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

import dk.ku.cpr.OmicsVisualizer.internal.model.OVConnection;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVStyle;

public class ApplyStyleTask extends AbstractTask {

	private OVManager ovManager;
	private OVConnection ovCon;
	private boolean onlyFiltered;

	public ApplyStyleTask(OVManager ovManager, OVConnection ovCon, boolean onlyFiltered) {
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
		taskMonitor.setTitle("Apply style to Network");

		// First we erase all previous charts
		taskMonitor.setStatusMessage("Cleaning previous data");
		CyTable nodeTable = this.ovCon.getNetwork().getDefaultNodeTable();
		OVShared.deleteOVColumns(nodeTable);
		nodeTable.createColumn(OVShared.CYNODETABLE_STYLECOL, String.class, false);

		OVStyle ovStyle = this.ovCon.getStyle();
		if(ovStyle == null) {
			return;
		}

		double progress = 0.0;
		taskMonitor.setProgress(progress);

		double nbNodes = this.ovCon.getNetwork().getNodeCount() * 1.0;
		int i=0;
		// Then we fill the columns
		taskMonitor.setStatusMessage("Computing style for each node");
		for(CyNode node : this.ovCon.getNetwork().getNodeList()) {
			progress = (i++)/nbNodes;
			taskMonitor.setProgress(progress);

			ArrayList<Object> nodeValues = new ArrayList<>();
			String nodeLabels = "";
			for(CyRow tableRow : this.ovCon.getLinkedRows(nodeTable.getRow(node.getSUID()))) {
				if(this.onlyFiltered && !this.ovCon.getOVTable().isFiltered(tableRow)) {
					continue;
				}
				
				for(String colName : ovStyle.getValues()) {
					Object val = tableRow.get(colName, ovStyle.getValuesType());
					if(val != null) {
						nodeValues.add(tableRow.get(colName, ovStyle.getValuesType()));
					} else {
						if(ovStyle.getValuesType() == Integer.class) {
							nodeValues.add(new Integer(0));
						} else if(ovStyle.getValuesType() == Long.class) {
							nodeValues.add(new Long(0));
						} else if(ovStyle.getValuesType() == Double.class) {
							nodeValues.add(new Double(0.0));
						} else {
							nodeValues.add("");
						}
					}
				}
				if(ovStyle.getLabel() != null) {
					nodeLabels += tableRow.get(ovStyle.getLabel(), this.ovCon.getOVTable().getColType(ovStyle.getLabel()));
					nodeLabels += ",";
				}
			}

			if(nodeValues.isEmpty()) { // If the node is not connected to a row, we display nothing
				continue;
			}

			int ncol = ovStyle.getValues().size();
			int nrow = nodeValues.size() / ncol;

			List<String> attributeList = new ArrayList<>();

			List<List<Object>> styleValues = new ArrayList<>();

			int ncolValues = (ovStyle.isTranspose() ? nrow : ncol);
			int nrowValues = (ovStyle.isTranspose() ? ncol : nrow);
			for(int c=0; c<ncolValues; ++c) {
				String colName = OVShared.CYNODETABLE_STYLECOL_VALUES+(c+1);
				attributeList.add(colName);
				// We create the column if this one does not exist yet
				createOVListColumn(nodeTable, colName, ovStyle.getValuesType());

				List<Object> colValues = new ArrayList<>();
				for(int r=0; r<nrowValues; ++r) {
					int index = 0;

					if(ovStyle.isTranspose()) {
						index = c*ncol+r;
					} else {
						index = r*ncol+c;
					}

					colValues.add(nodeValues.get(index));
				}
				nodeTable.getRow(node.getSUID()).set(colName, colValues);
				styleValues.add(colValues);
			}

			String nodeStyle = ovStyle.toEnhancedGraphics(styleValues);
			if(ovStyle.isContinuous()) {
				// Only continuous mapping needs attributes
				nodeStyle += " attributelist=\"" + String.join(",", attributeList) + "\"";
			}


			if(nodeLabels.length()>0) {
				nodeLabels = "labellist=\"" + nodeLabels.substring(0, nodeLabels.length()-1) + "\" showlabels=\"true\"";
			} else {
				nodeLabels = "showlabels=\"false\"";
			}

			nodeStyle += " " + nodeLabels;

			nodeTable.getRow(node.getSUID()).set(OVShared.CYNODETABLE_STYLECOL, nodeStyle);
		}


		// Finally we draw
		VisualMappingManager vmm = this.ovManager.getService(VisualMappingManager.class);
		// The connected network has been automatically selected when the window was opened
		CyNetworkView netView = this.ovManager.getService(CyApplicationManager.class).getCurrentNetworkView();
		if(netView != null) {
			VisualMappingFunctionFactory passthroughFactory = this.ovManager.getService(VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
			VisualLexicon lex = this.ovManager.getService(RenderingEngineManager.class).getDefaultVisualLexicon();
			// Set up the passthrough mapping for the label
			//				if (show) {
			VisualProperty<?> customGraphics = lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_4"); // Same CUSTOMGRAPHICS as stringApp displays enrichment
			PassthroughMapping<?,?> pMapping = (PassthroughMapping<?,?>) passthroughFactory.createVisualMappingFunction(OVShared.CYNODETABLE_STYLECOL, String.class, customGraphics);
			vmm.getVisualStyle(netView).addVisualMappingFunction(pMapping);
			//				} else {
			//					stringStyle
			//							.removeVisualMappingFunction(lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_4"));
			//				}
			netView.updateView();
		}
		//*/

	}

}
