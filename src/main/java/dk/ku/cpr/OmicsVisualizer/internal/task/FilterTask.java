package dk.ku.cpr.OmicsVisualizer.internal.task;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyRow;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVFilter;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVFilter.OVFilterType;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVFilterCriteria;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;
import dk.ku.cpr.OmicsVisualizer.internal.model.operators.Operator;
import dk.ku.cpr.OmicsVisualizer.internal.ui.OVCytoPanel;

public class FilterTask extends AbstractTask {
	protected OVManager ovManager;
	protected OVCytoPanel ovPanel;

	protected OVTable ovTable;

	protected OVFilter ovFilter;

	public FilterTask(OVManager ovManager, OVCytoPanel ovPanel) {
		this.ovManager=ovManager;
		this.ovPanel=ovPanel;

		if(this.ovPanel == null) {
			this.ovPanel=this.ovManager.getOVCytoPanel();
		}

		this.ovTable = this.ovManager.getActiveOVTable();
	}
	
	public FilterTask(OVManager ovManager, OVCytoPanel ovPanel, OVTable ovTable) {
		this.ovManager=ovManager;
		this.ovPanel=ovPanel;

		if(this.ovPanel == null) {
			this.ovPanel=this.ovManager.getOVCytoPanel();
		}

		this.ovTable = ovTable;
		this.ovFilter = this.ovTable.getFilter();
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if(this.ovTable == null) {
			return;
		}
		
		taskMonitor.setTitle("Filtering the rows of '"+this.ovTable.getTitle()+"' Omics Visualizer table");
		taskMonitor.setStatusMessage("Apply filter : " + this.ovFilter);
		
		if(this.ovFilter == null) {
			return;
		}


		List<CyRow> filteredRows=null;
		List<CyRow> workingRows=this.ovTable.getCyTable().getAllRows(); // rows used to apply the criteria
		if(this.ovFilter.getType() == OVFilterType.ALL) {
			// We start with all the rows
			filteredRows = this.ovTable.getCyTable().getAllRows();
		} else if(this.ovFilter.getType() == OVFilterType.ANY) {
			// We start with no rows
			filteredRows = new ArrayList<>();
		} else {
			taskMonitor.setStatusMessage("Unknow filter type : " + this.ovFilter.getType());
			return;
		}
		for(OVFilterCriteria crit : this.ovFilter.getCriterias()) {
			String colName = crit.getColName();
			Operator operator = crit.getOperator();
			String strReference = crit.getReference();
			
			Class<?> colType = this.ovTable.getColType(colName);
	
			Object reference = null;
			if(!operator.isUnary()) {
				if(colType == String.class) {
					reference = strReference;
				} else if(colType == Boolean.class) {
					reference = Boolean.valueOf(strReference);
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
	
			List<CyRow> critFilteredRows = new ArrayList<>();
			for(CyRow row : workingRows) {
				try { 
					if(operator.getOperator().filter(row.get(colName, colType), reference)) {
						critFilteredRows.add(row);
					}
				} catch(ClassCastException e) {
					taskMonitor.setStatusMessage("Warning: Could not cast \""+colName+"\".");
				}
			}
			
			if(this.ovFilter.getType() == OVFilterType.ALL) {
				filteredRows = critFilteredRows;
				workingRows = filteredRows;
			} else if(this.ovFilter.getType() == OVFilterType.ANY) {
				filteredRows.addAll(critFilteredRows);
			}
		}
		
		// We convert CyRow in keys:
		List<Object> filteredRowKeys = new ArrayList<>();
		for(CyRow row : filteredRows) {
			filteredRowKeys.add(row.getRaw(this.ovTable.getCyTable().getPrimaryKey().getName()));
		}
		this.ovTable.filter(filteredRowKeys);
		
		this.ovTable.save();
		
		if(this.ovPanel != null) {
			this.ovPanel.update();
		}
	}

	@ProvidesTitle
	public String getTitle() {
		return "Filter Omics Visualizer Table";
	}
}
