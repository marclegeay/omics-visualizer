package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVFilter;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
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
			longDescription="The filter is defined by the following non-contextual grammar:<br>"
					+ "filter = and | or | criteria<br>"
					+ "and = {filter_list}<br>"
					+ "or = [filter_list]<br>"
					+ "filter_list = filter,filter_list | filter<br>"
					+ "criteria = (colName,operator,value)<br>"
					+ "where 'colName' is the name of the column (commas in colName should be escaped by being preceeded a backslash)<br>"
					+ "'operator' is the name of the operation applied, the list of operators is available with the command \"" + OVShared.OV_COMMAND_NAMESPACE + " filter list operators\"<br>"
					+ "'value' if necessary, is the value to compare with. Careful with the regex and escaped characters.",
			tooltip="Define the filter that should be applied",
			required=true,
			exampleStringValue="{(name,MATCHES,\\\\\\\\w+)(pvalue,LOWER,0.05)}",
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
