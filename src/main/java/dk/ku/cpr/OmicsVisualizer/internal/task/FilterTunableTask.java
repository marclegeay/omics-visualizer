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
import dk.ku.cpr.OmicsVisualizer.internal.model.operators.Operator;
import dk.ku.cpr.OmicsVisualizer.internal.ui.OVCytoPanel;

public class FilterTunableTask extends FilterTask {

	@Tunable(description="Select column",
			tooltip="Select the column you want to filter",
			gravity=1.0)
	public ListSingleSelection<String> selectColName;

	@Tunable(description="Operator",
			tooltip="Select the way the values should be filtered",
			gravity=1.0)
	public ListSingleSelection<Operator> selectOperator;

	@Tunable(description="Value",
			tooltip="Select the value to compare with",
			gravity=1.0)
	public String strTunableReference;

	public FilterTunableTask(OVManager ovManager) {
		this(ovManager, null);
	}

	public FilterTunableTask(OVManager ovManager, OVCytoPanel ovPanel) {
		super(ovManager, ovPanel);

		List<String> colNames = new ArrayList<>();
		for(String colname : this.ovTable.getColNames()) {
			if(!OVShared.isOVCol(colname) && 
					this.ovTable.getColType(colname) != List.class) {
				colNames.add(colname);
			}
		}
		this.selectColName = new ListSingleSelection<>(colNames);

		this.selectOperator = new ListSingleSelection<>(Arrays.asList(Operator.values()));


		this.strTunableReference="0";

		String oldFilter = this.ovTable.getFilter();
		if(oldFilter != null) {
			String oldFilterParts[] = oldFilter.split(",");

			this.selectColName.setSelectedValue(oldFilterParts[0]);
			this.selectOperator.setSelectedValue(Operator.valueOf(oldFilterParts[1]));
			this.strTunableReference = oldFilterParts[2];
		}
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.colName = this.selectColName.getSelectedValue();
		this.operator = this.selectOperator.getSelectedValue();
		this.strReference = this.strTunableReference;

		super.run(taskMonitor);
	}

	@ProvidesTitle
	public String getTitle() {
		return "Filter Omics Visualizer Table";
	}
}
