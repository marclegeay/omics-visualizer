package dk.ku.cpr.OmicsVisualizer.internal.task;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;

import dk.ku.cpr.OmicsVisualizer.internal.model.operators.Operator;

public class OperatorListTask extends AbstractTask implements ObservableTask {

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// Do nothing, it the getResults that does everything
	}
	
	@ProvidesTitle
	public String getTitle() {
		return "List filter operators";
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(type == String.class) {
			String result = "";
			
			for(Operator o : Operator.values()) {
				result += o.name() + "\n";
			}
			
			return (R) result;
		} else if(type == List.class) {
			return (R) Arrays.asList(Operator.values());
		}
		return null;
	}
	
	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(List.class, String.class);
	}
}
