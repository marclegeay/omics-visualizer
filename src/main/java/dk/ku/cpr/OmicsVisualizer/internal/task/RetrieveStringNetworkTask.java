package dk.ku.cpr.OmicsVisualizer.internal.task;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;

public class RetrieveStringNetworkTask extends AbstractTask implements TaskObserver {
	protected OVManager ovManager;
	
	protected String protected_queryColumn;
	protected boolean protected_selectedOnly;
	protected boolean protected_filteredOnly;
	protected Integer protected_taxonID;
	protected String protected_species;
	protected String protected_cutoff;

	public RetrieveStringNetworkTask(OVManager ovManager) {
		super();
		this.ovManager = ovManager;
		
		this.protected_queryColumn="";
		this.protected_selectedOnly=false;
		this.protected_filteredOnly=true;
		this.protected_taxonID=null;
		this.protected_species=null;
		this.protected_cutoff="0.4";
	}

	public void setQueryColumn(String queryColumn) {
		this.protected_queryColumn = queryColumn;
	}

	public void setSelectedOnly(boolean selectedOnly) {
		this.protected_selectedOnly = selectedOnly;
	}

	public void setFilteredOnly(boolean filteredOnly) {
		this.protected_filteredOnly = filteredOnly;
	}

	public void setTaxonID(Integer taxonID) {
		this.protected_taxonID = taxonID;
	}

	public void setSpecies(String species) {
		this.protected_species = species;
	}

	public void setCutoff(String cutoff) {
		this.protected_cutoff = cutoff;
	}
	
	@ProvidesTitle
	public String getName() {
		return "Retrieve STRING Network";
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle(this.getName());
		
		OVTable ovTable = this.ovManager.getActiveOVTable();
		
		if(ovTable == null) {
			taskMonitor.setStatusMessage("No active Omics Visualizer Table. Task will stop here.");
			return;
		}
		
		taskMonitor.setStatusMessage("Source table: " + ovTable.getTitle());
		taskMonitor.setStatusMessage("Query column: " + this.protected_queryColumn);
		taskMonitor.setStatusMessage("Selected rows only: " + this.protected_selectedOnly);
		taskMonitor.setStatusMessage("Filtered rows only: " + this.protected_filteredOnly);
		taskMonitor.setStatusMessage("Taxon ID: " + this.protected_taxonID);
		taskMonitor.setStatusMessage("Species: " + this.protected_species);
		taskMonitor.setStatusMessage("Cut-off: " + this.protected_cutoff);
		
		if((this.protected_taxonID == null) && (this.protected_species == null)) {
			taskMonitor.setStatusMessage("You have to give either the Taxon ID or the Species name.");
			return;
		}
		
		// We identify the query column
		Class<?> colType = ovTable.getColType(this.protected_queryColumn);

		// We retrieve the list for the query
		Set<String> queryTerms = new HashSet<>();
		List<CyRow> tableRows;
		if(this.protected_selectedOnly) {
			tableRows = ovTable.getSelectedRows();
		} else {
			tableRows = ovTable.getAllRows(this.protected_filteredOnly);
		}
		for(CyRow row : tableRows) {
			queryTerms.add(row.get(this.protected_queryColumn, colType).toString());
		}

		// We set the arguments for the STRING command
		String query = String.join(",", queryTerms);
		Map<String, Object> args = new HashMap<>();
		args.put("query", query);
		if(this.protected_taxonID != null) {
			args.put("taxonID", this.protected_taxonID);
		}
		if(this.protected_species != null) {
			args.put("species", this.protected_species);
		}
		args.put("cutoff", this.protected_cutoff);
		args.put("limit", "0");

		// We call the STRING command
		StringCommandTaskFactory factory = new StringCommandTaskFactory(this.ovManager, OVShared.STRING_CMD_PROTEIN_QUERY, args, this);
		TaskIterator ti = factory.createTaskIterator();
		this.ovManager.executeTask(ti, this);
	}

	@Override
	public void taskFinished(ObservableTask task) {
		if(task.getClass().getSimpleName().equals("ProteinQueryTask")) {
			CyNetwork retrievedNetwork = task.getResults(CyNetwork.class);
			OVTable ovTable = this.ovManager.getActiveOVTable();
			
			if(ovTable == null) {
				return;
			}
			
			ovTable.connect(retrievedNetwork, "query term", this.protected_queryColumn);
			
			if(this.ovManager.getOVCytoPanel() != null) {
				this.ovManager.getOVCytoPanel().update();
			}
		}
	}

	@Override
	public void allFinished(FinishStatus finishStatus) {
		//Do nothing
	}
}
