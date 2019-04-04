package dk.ku.cpr.OmicsVisualizer.internal.task;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyRow;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVFilter;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVFilterSet.OVFilterSetType;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVFilterCriteria;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVFilterSet;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
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
	
	private List<CyRow> filter(TaskMonitor taskMonitor, OVFilter filter, List<CyRow> workingRows) {
		if(filter instanceof OVFilterCriteria) {
			OVFilterCriteria filterCrit = (OVFilterCriteria) filter;
			
			String colName = filterCrit.getColName();
			Operator operator = filterCrit.getOperator();
			String strReference = filterCrit.getReference();
			
			Class<?> colType = this.ovTable.getColType(colName);
			if(colType == null) {
				taskMonitor.setStatusMessage("Error: Column \"" + colName + "\" not found, the filter can not be applied.");
				return null;
			}
	
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
						return null;
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
			
			return critFilteredRows;
		} else if(filter instanceof OVFilterSet) {
			OVFilterSet filterSet = (OVFilterSet) filter;
			
			List<CyRow> filteredRows = null;
			if(filterSet.getType() == OVFilterSetType.ALL) {
				// We start with all the rows
				filteredRows = workingRows;
			} else if(filterSet.getType() == OVFilterSetType.ANY) {
				// We start with no rows
				filteredRows = new ArrayList<>();
			} else {
				taskMonitor.setStatusMessage("Unknow filter type : " + filterSet.getType());
				return null;
			}
			
			List<CyRow> subFilteredRows;
			for(OVFilter subFilter : filterSet.getFilters()) {
				subFilteredRows = filter(taskMonitor, subFilter, workingRows);
				
				if(subFilteredRows == null) {
					return null;
				}
				
				if(filterSet.getType() == OVFilterSetType.ALL) {
					filteredRows = subFilteredRows;
					workingRows = filteredRows;
				} else if(filterSet.getType() == OVFilterSetType.ANY) {
					filteredRows.addAll(subFilteredRows);
				}
			}
			
			return filteredRows;
		}
		
		return null;
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
		
		// We apply the filter
		List<CyRow> filteredRows = filter(taskMonitor, this.ovFilter, this.ovTable.getCyTable().getAllRows());
		if(filteredRows == null) {
			return;
		}
		
		// We convert CyRow in keys:
		List<Object> filteredRowKeys = new ArrayList<>();
		for(CyRow row : filteredRows) {
			filteredRowKeys.add(row.get(OVShared.OVTABLE_COLID_NAME, OVShared.OVTABLE_COLID_TYPE));
		}
		filteredRowKeys.sort(new OVShared.OVTableIDComparator());
		
		this.ovTable.filter(filteredRowKeys);

		// The filter can come from the command, so we assign the filter to the table
		this.ovTable.setFilter(this.ovFilter);
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
