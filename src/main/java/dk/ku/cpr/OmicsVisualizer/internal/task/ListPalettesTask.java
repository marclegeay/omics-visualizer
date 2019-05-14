package dk.ku.cpr.OmicsVisualizer.internal.task;

import org.cytoscape.util.color.PaletteProvider;
import org.cytoscape.util.color.PaletteProviderManager;
import org.cytoscape.util.color.PaletteType;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

public class ListPalettesTask extends AbstractTask implements ObservableTask {
	
	private OVManager ovManager;
	
	@Tunable(description="Display palettes that allow color blind safe colors (true) or not (false). Default: true.",
			exampleStringValue="true",
			gravity=1.0)
	public boolean colorBlindSafe = true;

	public ListPalettesTask(OVManager ovManager) {
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
		String str="";
		
		for(PaletteProvider provider : this.ovManager.getService(PaletteProviderManager.class).getPaletteProviders()) {
			for(PaletteType paletteType : provider.getPaletteTypes()) {
				for(String paletteName : provider.listPaletteNames(paletteType, this.colorBlindSafe)) {
					str += "'" + paletteName + "' (Provider: '" + provider.getProviderName() + "')\n";
				}
			}
		}
		
		return (R) str;
	}

}
