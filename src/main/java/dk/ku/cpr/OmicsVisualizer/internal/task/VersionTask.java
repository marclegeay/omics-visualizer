package dk.ku.cpr.OmicsVisualizer.internal.task;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;

public class VersionTask extends AbstractTask implements ObservableTask {
	private String version;

	public VersionTask(String version) {
		super();
		this.version = version;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// Do nothing
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> R getResults(Class<? extends R> type) {
		return (R)this.version;
	}
	
	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(String.class);
	}

}
