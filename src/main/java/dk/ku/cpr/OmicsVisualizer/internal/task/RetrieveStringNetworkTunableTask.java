package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

public class RetrieveStringNetworkTunableTask extends RetrieveStringNetworkTask {
	@Tunable(description="Column name of the Omics Visualizer Table",
			required=true,
			tooltip="Put the name of the column that contains the identifiers that will be used to query STRING.",
			exampleStringValue="Uniprot",
			gravity=1.0)
	public String queryColumn;

	@Tunable(description="Only selected rows should be used to the query.",
			required=false,
			tooltip="If you want all the rows, put this at false (default value) or true if you only want selected rows to be part of the query.",
			gravity=1.0)
	public Boolean selectedOnly;
	
	@Tunable(description="Only filtered rows should be used to the query.",
			required=false,
			tooltip="If you want all the rows, put this at false or true (default value) if you only want filtered rows to be part of the query.",
			gravity=1.0)
	public Boolean filteredOnly;

	@Tunable(description="Identifier of the species to query.",
			required=false,
			tooltip="You can put here the taxon identifier of the species you want to query.",
			exampleStringValue="9606",
			gravity=1.0)
	public Integer taxonID;

	@Tunable(description="Name of the species to query.",
			required=false,
			tooltip="You can put here the name of the species you want to query.",
			exampleStringValue="Homo sapiens",
			gravity=1.0)
	public String species;

	@Tunable(description="Confidence (score) cutoff.",
			required=false,
			tooltip="Enter a value between 0.0 and 1.0 defining the confidence score the STRING network should have. Default: 0.40.",
			exampleStringValue="Homo sapiens",
			gravity=1.0)
	public String cutoff;

	public RetrieveStringNetworkTunableTask(OVManager ovManager) {
		super(ovManager);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.setQueryColumn(queryColumn);
		if(selectedOnly != null) {
			this.setSelectedOnly(selectedOnly);
		}
		if(filteredOnly != null) {
			this.setFilteredOnly(filteredOnly);
		}
		if(taxonID != null) {
			this.setTaxonID(taxonID);
		}
		if(species != null) {
			this.setSpecies(species);
		}
		if(cutoff != null) {
			this.setCutoff(cutoff);
		}
		
		super.run(taskMonitor);
	}

}
