package dk.ku.cpr.OmicsVisualizer.internal.task;

import java.util.ArrayList;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TaskMonitor.Level;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.utils.DataUtils;

public class CreateOVTableFromNetworkTunableTask extends CreateOVTableFromNetworkTask {
	
	@Tunable(description="The network containing the node table.",
			required=true,
			gravity=1.0)
	public CyNetwork srcNetwork=null;

	@Tunable(description="Name of the column that identifies the row in the node table.",
			required=true,
			gravity=1.0)
	public String keyColName=null;

	@Tunable(description="Comma-separated list of column names to import into the new Omics Visualizer table."
			+ " Commas in the column names should be escaped.",
			required=false,
			gravity=1.0)
	public String copiedColNames=null;

	@Tunable(description="Comma-separated list of column namespaces to import into the new Omics Visualizer table."
			+ " All the columns from those namespaces will be imported."
			+ " Commas in the column namespaces should be escaped.",
			required=false,
			gravity=1.0)
	public String copiedNamespaces=null;

	@Tunable(description="The name of the new Omics Visualizer table."
			+ " A default generated name will be given if omitted.",
			required=false,
			gravity=1.0)
	public String newTableName=null;

	@Tunable(description="The name of the column containing the values. Default: "+OVShared.OV_DEFAULT_VALUES_COLNAME+".",
			required=false,
			gravity=1.0)
	public String valuesName;

	@Tunable(description="The name of the column containing the values source. Default: "+OVShared.OV_DEFAULT_VALUES_SOURCE_COLNAME+".",
			required=false,
			gravity=1.0)
	public String srcName;
	
	@Tunable(description="Should the namespace of the columns be included in the source column? Default: true.",
			required=false,
			gravity=1.0)
	public Boolean includeNamespaces = Boolean.TRUE;

	public CreateOVTableFromNetworkTunableTask(OVManager ovManager) {
		super(ovManager, null, null, null, null, null, null, true);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.cyNetwork = this.srcNetwork;
		this.keyCyTableColName = this.keyColName;
		this.cyTableColNames = new ArrayList<>();
		this.tableName = this.newTableName;
		this.valuesColName = this.valuesName;
		this.srcColName = this.srcName;
		this.displayNamespaces = this.includeNamespaces;
		
		if(this.copiedColNames == null && this.copiedNamespaces == null) {
			taskMonitor.showMessage(Level.ERROR, "ERROR: You should at least give a list of columns (copiedColNames) or a list of namespaces (copiedNamespaces).");
			return;
		}
		
		// We add the colnames:
		if(this.copiedColNames != null) {
			String colNames[] = DataUtils.getCSV(this.copiedColNames);
			for(String colName : colNames) {
				this.cyTableColNames.add(colName);
			}
		}
		
		// We add the columns from the namespace
		if(this.copiedNamespaces != null) {
			if(this.cyNetwork == null) {
				taskMonitor.showMessage(Level.ERROR, "ERROR: The node table cannot be found.");
				return;
			}
			
			String namespaces[] = DataUtils.getCSV(this.copiedNamespaces);
			for(String namespace : namespaces) {
				for(CyColumn col : this.cyNetwork.getDefaultNodeTable().getColumns(namespace)) {
					this.cyTableColNames.add(col.getName());
				}
			}
		}
		
		super.run(taskMonitor);
	}

}
