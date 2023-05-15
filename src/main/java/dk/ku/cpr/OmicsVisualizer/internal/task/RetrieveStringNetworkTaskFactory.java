package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

public class RetrieveStringNetworkTaskFactory extends AbstractTaskFactory {
	private OVManager ovManager;

	public RetrieveStringNetworkTaskFactory(OVManager ovManager) {
		super();
		this.ovManager = ovManager;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new RetrieveStringNetworkTunableTask(this.ovManager));
	}
	
	public TaskIterator createTaskIterator(String queryColumn, boolean selectedOnly, boolean filteredOnly, Integer taxonID, String species, double cutoff, String netType, boolean isGUI) {
		RetrieveStringNetworkTask task = new RetrieveStringNetworkTask(this.ovManager);
		
		task.setQueryColumn(queryColumn);
		task.setSelectedOnly(selectedOnly);
		task.setFilteredOnly(filteredOnly);
		task.setTaxonID(taxonID);
		task.setSpecies(species);
		task.setCutoff(cutoff);
		task.setNetType(netType);
		task.setIsGUI(isGUI);
		
		return new TaskIterator(task);
	}

}
