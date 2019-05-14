package dk.ku.cpr.OmicsVisualizer.internal.task;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVColorDiscrete;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVVisualization.ChartType;
import dk.ku.cpr.OmicsVisualizer.internal.utils.DataUtils;

public class VisualizationDiscreteTask extends VisualizationTask {
	
	@Tunable(description="Comma separated values of mappings value:color.",
			exampleStringValue="A:blue,B:#ffaa00,C:BLACK",
			gravity=1.0)
	public String colorMapping;

	public VisualizationDiscreteTask(OVManager ovManager, ChartType chartType) {
		super(ovManager, chartType);
		
		this.defaultPaletteProviderName="ColorBrewer";
		this.defaultPaletteName="Paired colors";
	}
	
	@Override
	protected void initColors(TaskMonitor taskMonitor) {
		// We init the palette
		super.initColors(taskMonitor);
		
		Map<String, Color> userMapping = new HashMap<>();
		if(this.colorMapping != null) {
			String data[] = DataUtils.getCSV(this.colorMapping);
			for(String map : data) {
				String map_parts[] = map.split("(?<!\\\\):");
				
				if(map_parts.length == 2) {
					Color col = parseColor(map_parts[1]);
					if(col == null) {
						taskMonitor.setStatusMessage("WARNING: Unknow color \""+map_parts[1]+"\", the palette will be used for the value \""+map_parts[0]+"\".");
					} else {
						userMapping.put(map_parts[0], col);
					}
				} else {
					taskMonitor.setStatusMessage("WARNING: Unknow mapping \""+map+"\", it will be ignored.");
				}
			}
		}
		
		
		Map<Object, Color> mapping = new HashMap<>();
		Color colors[] = this.palette.getColors(this.values.size());

		int i=0;
		for(Object val : this.values) {
			if(userMapping.containsKey(val.toString())) {
				mapping.put(val, userMapping.get(val));
			} else {
				mapping.put(val, colors[i]);
			}
			i++;
		}

		this.colors = new OVColorDiscrete(mapping);
	}

}
