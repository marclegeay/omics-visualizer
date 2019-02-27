package dk.ku.cpr.OmicsVisualizer.internal.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.ui.OVCytoPanel;

public class FilterTunableTask extends FilterTask {

	@Tunable(description="Select column",
			tooltip="Select the column you want to filter",
			gravity=1.0)
	public ListSingleSelection<String> selectColName;

	@Tunable(description="Operator",
			tooltip="Select the way the values should be filtered",
			gravity=1.0)
	public ListSingleSelection<String> selectOperator;

	@Tunable(description="Value",
			tooltip="Select the value to compare with",
			gravity=1.0)
	public String strTunableReference;

	@Tunable(description="Remove filter",
			tooltip="Remove the current filter and display all the rows",
			gravity=1.0)
	public boolean tunableRemoveFilter;

	public FilterTunableTask(OVManager ovManager) {
		this(ovManager, null);
	}

	public FilterTunableTask(OVManager ovManager, OVCytoPanel ovPanel) {
		super(ovManager, ovPanel, false);

		List<String> colNames = new ArrayList<>();
		for(String colname : this.ovTable.getColNames()) {
			if(!OVShared.isOVCol(colname) && 
					this.ovTable.getColType(colname) != String.class &&
					this.ovTable.getColType(colname) != List.class) {
				colNames.add(colname);
			}
		}
		this.selectColName = new ListSingleSelection<>(colNames);

		this.selectOperator = new ListSingleSelection<String>(Arrays.asList("==",
				"!=",
				"<",
				"<=",
				">",
				">=",
				"null",
				"not null"));

		this.strTunableReference="0";

		this.tunableRemoveFilter=false;
		
		String oldFilter = this.ovTable.getFilter();
		if(oldFilter != null) {
			String oldFilterParts[] = oldFilter.split(",");
			
			this.selectColName.setSelectedValue(oldFilterParts[0]);
			this.selectOperator.setSelectedValue(oldFilterParts[1]);
			this.strTunableReference = oldFilterParts[2];
		}
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if(this.tunableRemoveFilter) {
			this.removeFilter=true;
		} else {
			this.colName = this.selectColName.getSelectedValue();
			this.strOperator = this.selectOperator.getSelectedValue();
			this.strReference = this.strTunableReference;
		}
		
		super.run(taskMonitor);
	}

	@ProvidesTitle
	public String getTitle() {
		return "Filter Omics Visualizer Table";
	}
}
