package dk.ku.cpr.OmicsVisualizer.internal.task;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;

public class TableListTask extends AbstractTask implements ObservableTask {
	
	private OVManager ovManager;

	public TableListTask(OVManager ovManager) {
		super();
		this.ovManager = ovManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// Do nothing
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(type == List.class) {
			return (R) ovManager.getOVTables();
		} else if(type == String.class) {
			String str="";
			
			for(OVTable table : this.ovManager.getOVTables()) {
				str += table.getTitle() + "\n";
			}
			
			return (R) str;
		}
		
		return null;
	}
	
	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(String.class, List.class);
	}

}
