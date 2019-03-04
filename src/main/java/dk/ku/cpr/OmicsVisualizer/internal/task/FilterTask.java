package dk.ku.cpr.OmicsVisualizer.internal.task;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyRow;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;
import dk.ku.cpr.OmicsVisualizer.internal.model.operators.*;
import dk.ku.cpr.OmicsVisualizer.internal.ui.OVCytoPanel;

public class FilterTask extends AbstractTask {
	protected OVManager ovManager;
	protected OVCytoPanel ovPanel;

	protected OVTable ovTable;
	
	protected boolean removeFilter;

	protected String colName;
	protected Operator operator;
	protected String strReference;
	
	public FilterTask(OVManager ovManager, OVCytoPanel ovPanel, boolean removeFilter) {
		this.ovManager=ovManager;
		this.ovPanel=ovPanel;
		
		this.removeFilter=removeFilter;
		
		if(this.ovPanel != null) {
			this.ovTable = this.ovPanel.getDisplayedTable();
		}
	}
	
	public FilterTask(OVManager ovManager, OVCytoPanel ovPanel, String colName, Operator operator, String strReference) {
		this.ovManager=ovManager;
		this.ovPanel=ovPanel;
		
		this.removeFilter=false;
		
		this.colName=colName;
		this.operator=operator;
		this.strReference=strReference;
		
		if(this.ovPanel != null) {
			this.ovTable = this.ovPanel.getDisplayedTable();
		}
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Filtering Omics Visualizer rows");

		if(this.ovPanel == null) {
			CySwingApplication swingApplication = this.ovManager.getService(CySwingApplication.class);
			CytoPanel cytoPanel = swingApplication.getCytoPanel(CytoPanelName.SOUTH);
			try {
				this.ovPanel = (OVCytoPanel) cytoPanel.getComponentAt(cytoPanel.indexOfComponent(OVShared.CYTOPANEL_NAME));
			} catch(IndexOutOfBoundsException e) {
				return;
			}

			if(this.ovPanel == null) {
				return;
			}
		}
		
		this.ovTable = this.ovPanel.getDisplayedTable();

		if(this.removeFilter) {
			this.ovTable.removeFilter();
			this.ovTable.setTableProperty(OVShared.PROPERTY_FILTER, "");
		} else {
			Class<?> colType = this.ovTable.getColType(colName);

			Object reference = null;
			if(colType == String.class) {
				reference = strReference;
			} else {
				try {
					if(colType == Integer.class) {
						reference=Integer.parseInt(strReference);
					} else if(colType == Long.class) {
						reference=Long.parseLong(strReference);
					} else if(colType == Double.class) {
						reference=Double.parseDouble(strReference);
					}
				} catch(NumberFormatException e) {
					reference = null;
				}
	
				if(reference ==null) {
					taskMonitor.setStatusMessage("Error: Impossible to parse the value \""+strReference+"\" as a number.");
					return;
				}
			}

			String savedFilter = colName+","+this.operator.name()+","+this.strReference;
			this.ovTable.setTableProperty(OVShared.PROPERTY_FILTER, savedFilter);
			
			List<Object> filteredRowKeys = new ArrayList<>();
			List<CyRow> allRows = this.ovTable.getCyTable().getAllRows();
			int i=0;
			for(CyRow row : allRows) {
				taskMonitor.setProgress((i++)/allRows.size());
				try { 
					if(operator.getOperator().filter(row.get(colName, colType), reference)) {
						filteredRowKeys.add(row.getRaw(this.ovTable.getCyTable().getPrimaryKey().getName()));
					}
				} catch(ClassCastException e) {
					taskMonitor.setStatusMessage("Warning: Could not cast \""+colName+"\".");
				}
			}

			this.ovTable.filter(filteredRowKeys);
		}

		this.ovTable.save();
		this.ovPanel.update();
	}
}
