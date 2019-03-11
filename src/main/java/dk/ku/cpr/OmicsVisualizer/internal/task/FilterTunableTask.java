package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVFilter;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;
import dk.ku.cpr.OmicsVisualizer.internal.ui.OVCytoPanel;

public class FilterTunableTask extends FilterTask {

	@Tunable(description="Omics Visualizer table name",
			required=false,
			tooltip="By default the active table is filtered, but you can specify the name of the table you wish to filter here.",
			exampleStringValue="My table",
			gravity=1.0)
	public String tableName="";

	@Tunable(description="Filter",
			longDescription="The filter must have the following format:<br>"
					+ "[filter_type](filter_criteria_1)(filter_criteria_2)...(filter_criteria_n)<br>"
					+ "'filter_type' must be either 'ALL' or 'ANY'<br>"
					+ "'filter_criteria_x' must be formatted as:<br>"
					+ "colName,operator,value<br>"
					+ "where 'colName' is the name of the column, where commas are escaped by being preceeded by two backslashes<br>"
					+ "'operator' is the name of the operation applied, the list of operators is available with the command \"ov operators\"<br>"
					+ "'value' if necessary, is the value to compare with. If the value is a regex, the backslash must be repeated four-times to be escaped \"\\\\\\\\w\" to represent the regex \"\\w\".",
			tooltip="Define the filter that should be applied",
			required=true,
			exampleStringValue="[ALL](name,MATCHES,\\\\w+)(pvalue,LOWER,0.05)",
			gravity=1.0)
	public String filter;

	public FilterTunableTask(OVManager ovManager, OVCytoPanel ovPanel) {
		super(ovManager, ovPanel);
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if(!this.tableName.isEmpty()) {
			for(OVTable table : this.ovManager.getOVTables()) {
				if(table.getTitle().equals(this.tableName)) {
					this.ovTable=table;
					break;
				}
			}
		}
		
		this.ovFilter = OVFilter.valueOf(this.filter);
		
		super.run(taskMonitor);
	}
}
