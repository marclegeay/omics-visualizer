package dk.ku.cpr.OmicsVisualizer.internal.task;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyRow;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
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

	protected String colName;
	protected Operator operator;
	protected String strReference;

	public FilterTask(OVManager ovManager, OVCytoPanel ovPanel) {
		this.ovManager=ovManager;
		this.ovPanel=ovPanel;

		if(this.ovPanel == null) {
			this.ovPanel=this.ovManager.getOVCytoPanel();
		}

		this.ovTable = this.ovManager.getActiveOVTable();
	}

	public FilterTask(OVManager ovManager, OVCytoPanel ovPanel, String colName, Operator operator, String strReference) {
		this.ovManager=ovManager;
		this.ovPanel=ovPanel;

		this.colName=colName;
		this.operator=operator;
		this.strReference=strReference;

		if(this.ovPanel == null) {
			CySwingApplication swingApplication = this.ovManager.getService(CySwingApplication.class);
			CytoPanel cytoPanel = swingApplication.getCytoPanel(CytoPanelName.SOUTH);
			try {
				this.ovPanel = (OVCytoPanel) cytoPanel.getComponentAt(cytoPanel.indexOfComponent(OVShared.CYTOPANEL_NAME));
			} catch(IndexOutOfBoundsException e) {
			}
		}

		this.ovTable = this.ovManager.getActiveOVTable();
	}
	
	public FilterTask(OVManager ovManager, OVCytoPanel ovPanel, OVTable ovTable, String colName, Operator operator, String strReference) {
		this.ovManager=ovManager;
		this.ovPanel=ovPanel;

		this.colName=colName;
		this.operator=operator;
		this.strReference=strReference;

		if(this.ovPanel == null) {
			CySwingApplication swingApplication = this.ovManager.getService(CySwingApplication.class);
			CytoPanel cytoPanel = swingApplication.getCytoPanel(CytoPanelName.SOUTH);
			try {
				this.ovPanel = (OVCytoPanel) cytoPanel.getComponentAt(cytoPanel.indexOfComponent(OVShared.CYTOPANEL_NAME));
			} catch(IndexOutOfBoundsException e) {
			}
		}

		this.ovTable = ovTable;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		System.out.println("[OV - FilterTask::run]");
		if(this.ovTable == null) {
			return;
		}
		
		taskMonitor.setTitle("Filtering the rows of '"+this.ovTable.getTitle()+"' Omics Visualizer table");

		Class<?> colType = this.ovTable.getColType(colName);

		Object reference = null;
		if(!this.operator.isUnary()) {
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
		}

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

		String savedFilter = colName+","+this.operator.name()+","+this.strReference;
		this.ovTable.setTableProperty(OVShared.PROPERTY_FILTER, savedFilter);
		
		this.ovTable.save();
		
		if(this.ovPanel != null) {
			this.ovPanel.update();
		}
		
		taskMonitor.setStatusMessage("Applied filter : " + savedFilter);
	}

	@ProvidesTitle
	public String getTitle() {
		return "Filter Omics Visualizer Table";
	}
}
