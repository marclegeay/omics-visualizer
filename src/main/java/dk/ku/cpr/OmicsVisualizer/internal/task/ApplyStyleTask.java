package dk.ku.cpr.OmicsVisualizer.internal.task;

import java.util.ArrayList;
import java.util.Collections;
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

public class ApplyStyleTask extends AbstractTask {

	private OVManager ovManager;
	private OVConnection ovCon;

	public ApplyStyleTask(OVManager ovManager, OVConnection ovCon) {
		super();
		this.ovManager = ovManager;
		this.ovCon = ovCon;
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
		CyTable nodeTable = this.ovCon.getNetwork().getDefaultNodeTable();
		OVShared.deleteOVColumns(nodeTable);
		nodeTable.createColumn(OVShared.CYNODETABLE_STYLECOL, String.class, false);

		double progress = 0.0;
		taskMonitor.setProgress(progress);
		
		double nbNodes = this.ovCon.getNetwork().getNodeCount() * 1.0;
		int i=0;
		// Then we fill the columns
		for(CyNode node : this.ovCon.getNetwork().getNodeList()) {
			progress = (i++)/nbNodes;
			taskMonitor.setProgress(progress);
			
			ArrayList<Object> nodeValues = new ArrayList<>();
			String nodeLabels = "";
			for(CyRow tableRow : this.ovCon.getLinkedRows(nodeTable.getRow(node.getSUID()))) {
				for(String colName : this.ovCon.getStyle().getValues()) {
					Object val = tableRow.get(colName, this.ovCon.getStyle().getValuesType());
					if(val != null) {
						nodeValues.add(tableRow.get(colName, this.ovCon.getStyle().getValuesType()));
					} else {
						if(this.ovCon.getStyle().getValuesType() == Integer.class) {
							nodeValues.add(new Integer(0));
						} else if(this.ovCon.getStyle().getValuesType() == Long.class) {
							nodeValues.add(new Long(0));
						} else if(this.ovCon.getStyle().getValuesType() == Double.class) {
							nodeValues.add(new Double(0.0));
						} else {
							nodeValues.add("");
						}
					}
				}
				if(this.ovCon.getStyle().getLabel() != null) {
					nodeLabels += tableRow.get(this.ovCon.getStyle().getLabel(), this.ovCon.getOVTable().getColType(this.ovCon.getStyle().getLabel()));
					nodeLabels += ",";
				}
			}

			if(nodeValues.isEmpty()) { // If the node is not connected to a row, we display nothing
				continue;
			}

			int ncol = this.ovCon.getStyle().getValues().size();
			int nrow = nodeValues.size() / ncol;

			List<String> attributeList = new ArrayList<>();

			int ncolValues = (this.ovCon.getStyle().isTranspose() ? nrow : ncol);
			int nrowValues = (this.ovCon.getStyle().isTranspose() ? ncol : nrow);
			for(int c=0; c<ncolValues; ++c) {
				String colName = OVShared.CYNODETABLE_STYLECOL_VALUES+(c+1);
				attributeList.add(colName);
				// We create the column if this one does not exist yet
				createOVListColumn(nodeTable, colName, this.ovCon.getStyle().getValuesType());

				List<Object> colValues = new ArrayList<>();
				for(int r=0; r<nrowValues; ++r) {
					int index = 0;

					if(this.ovCon.getStyle().isTranspose()) {
						index = c*ncol+r;
					} else {
						index = r*ncol+c;
					}

					colValues.add(nodeValues.get(index));
				}
				nodeTable.getRow(node.getSUID()).set(colName, colValues);
			}

			String nodeStyle = this.ovCon.getStyle().toEnhancedGraphics(nodeValues);
			nodeStyle += " valuelist=\"" + String.join(",", Collections.nCopies(nrowValues, "1")) + "\"";
			if(this.ovCon.getStyle().isContinuous()) {
				nodeStyle += " attributelist=\"" + String.join(",", attributeList) + "\"";
			} else {
				// Discrete mapping have only 1 circle
				nodeStyle += " attributelist=\"1\"";
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
