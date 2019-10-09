package dk.ku.cpr.OmicsVisualizer.internal.task;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.util.color.Palette;
import org.cytoscape.util.color.PaletteProvider;
import org.cytoscape.util.color.PaletteProviderManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import dk.ku.cpr.OmicsVisualizer.internal.model.EGSettings;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVColor;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVConnection;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVVisualization;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVVisualization.ChartType;
import dk.ku.cpr.OmicsVisualizer.internal.utils.DataUtils;

public class VisualizationTask extends AbstractTask {
	
	protected OVManager ovManager;
	protected OVConnection ovCon;
	protected ChartType chartType;
	protected EGSettings egSettings;

	protected String defaultPaletteProviderName;
	protected String defaultPaletteName;
	protected Palette palette;
	protected OVColor colors=null;
	
	protected SortedSet<Object> values;
	
	@Tunable(description="Comma separated value list of attributes from the table you want to visualize the data. Special characters in the attributes must be escaped.",
			exampleStringValue="experiment A,experiment B,experiment\\,C",
			gravity=1.0,
			required=true)
	public String attributes;
	protected List<String> listAttributes;
	protected Class<?> attributesType;

	@Tunable(description="Name of the palette to use as default colors. (See ov palette list)",
			exampleStringValue="Red-Blue",
			gravity=1.0)
	public String paletteName;
	
	@Tunable(description="Name of the palette provider of the palette. (See ov palette list)",
			exampleStringValue="ColorBrewer",
			gravity=1.0)
	public String paletteProviderName;

	@Tunable(description="Column name of the table that should be used to label the data. By default no label is displayed.",
			exampleStringValue="Site name",
			gravity=1.0)
	public String labels=null;

	@Tunable(description="Use all the data (false) or only the filtered one (true)? Default:false.",
			exampleStringValue="false",
			gravity=1.0)
	public boolean filteredOnly=false;

	@Tunable(description="(Outer Visualization only) Should the ring represents a column (false) or a row (true)? Default: ring is a column.",
			exampleStringValue="false",
			gravity=1.0)
	public boolean transpose=false;
	
	@Tunable(description="Comma separated list of enhancedGraphics settings. Here is how the string should be formatted: \"setting1:value1,setting2:value2\"",
			exampleStringValue="bordercolor:white,arcstart:0",
			required=false,
			gravity=1.0)
	public String chartSettings="";
	
	public VisualizationTask(OVManager ovManager, ChartType chartType) {
		this.ovManager=ovManager;
		this.chartType=chartType;
		
		this.ovCon = null;
	}
	
	protected Color parseColor(String colorString)  {
		Color color = null;

		if (colorString == null) {
			return null;
		}
		
		colorString = colorString.trim();
		if (colorString.matches("^#([A-Fa-f0-9]{8}|[A-Fa-f0-9]{6})$")) {
			// We have a hex value with either 6 (rgb) or 8 (rgba) digits
			int r = Integer.parseInt(colorString.substring(1,3), 16);
			int g = Integer.parseInt(colorString.substring(3,5), 16);
			int b = Integer.parseInt(colorString.substring(5,7), 16);
			if (colorString.length() > 7) {
				int a = Integer.parseInt(colorString.substring(7,9), 16);
				color = new Color(r,g,b,a);
			} else {
				color = new Color(r,g,b);
			}
		} else {
			try {
				return Color.decode(colorString);
			} catch (NumberFormatException e1) {
				try {
					color = (Color) Color.class.getField(colorString).get(null);
				} catch (Exception e2) {
					return null;
				}
			}
		}
			
		return color;
	}
	
	protected boolean init(TaskMonitor taskMonitor) {
		CyNetwork currentNetwork = this.ovManager.getService(CyApplicationManager.class).getCurrentNetwork();
		if(currentNetwork == null) {
			taskMonitor.setStatusMessage("ERROR: No current network.");
			return false;
		}
		
		OVTable currentTable = this.ovManager.getActiveOVTable();
		if(currentTable == null) {
			taskMonitor.setStatusMessage("ERROR: No current OVTable.");
			return false;
		}
		
		this.ovCon = currentTable.getConnection(currentNetwork);
		if(this.ovCon == null) {
			taskMonitor.setStatusMessage("ERROR: No connection between the current table ("+currentTable.getTitle()+") and the current network ("+currentNetwork.toString()+").");
			return false;
		}

		// We check if all attributes exist, and if they have the same type
		List<String> tableColNames = currentTable.getColNames();
		this.listAttributes = new ArrayList<>();
		this.attributesType = null;
		boolean attOK=true;
		for(String att : DataUtils.getCSV(this.attributes)) {
			if(tableColNames.contains(att)) {
				if(this.attributesType==null) {
					this.attributesType = currentTable.getColType(att);
					this.listAttributes.add(att);
				} else if(this.attributesType != currentTable.getColType(att)) {
					taskMonitor.setStatusMessage("ERROR: Attributes do not have the same type.");
					attOK=false;
				} else {
					this.listAttributes.add(att);
				}
			} else {
				taskMonitor.setStatusMessage("ERROR: Unknown table attribute \""+att+"\".");
				attOK=false;
			}
		}
		
		if(this.chartType == ChartType.PIE && this.listAttributes.size() > 1) {
			taskMonitor.setStatusMessage("ERROR: Inner visualization must only have one attribute.");
			attOK=false;
		}
		
		if(!attOK) {
			return false;
		}
		// We only look at values of rows connected to the network
		List<CyRow> valueRows = new ArrayList<>();
		for(CyNode netNode : this.ovCon.getRootNetwork().getNodeList()) {
			valueRows.addAll(this.ovCon.getLinkedRows(netNode));
		}

		// We look for the values in the data
		this.values = new TreeSet<>();
		for(CyRow row : valueRows) {
			if(this.filteredOnly && !currentTable.isFiltered(row)) {
				continue;
			}

			for(String colName : this.listAttributes) {
				Object val = row.get(colName, this.attributesType);
				if(val != null ) {
					values.add(val);
				} else {
					if(this.attributesType == Integer.class) {
						values.add(new Integer(0));
					} else if(this.attributesType == Long.class) {
						values.add(new Long(0));
					} else if(this.attributesType == Double.class) {
						values.add(new Double(0.0));
					} else {
						values.add("");
					}
				}
			}
		}
		
		if(values.isEmpty()) {
			taskMonitor.setStatusMessage("WARNING: No row.");
			return false;
		}
		
		this.egSettings = new EGSettings();
		for(String setting : this.chartSettings.split(",")) {
			String key_value[] = setting.split(":");
			if(key_value.length != 2) {
				taskMonitor.setStatusMessage("WARNING: Cannot parse this enhancedGraphics settings \"" + setting + "\".");
			} else {
				this.egSettings.set(key_value[0], key_value[1]);
			}
		}

		return true;
	}
	
	protected void initColors(TaskMonitor taskMonitor) {
		Palette p=null;
		
		if(this.paletteName == null) {
			this.paletteName = "";
			this.paletteProviderName = "";
		} else if(this.paletteProviderName == null) {
			for(PaletteProvider paletteProvider : this.ovManager.getService(PaletteProviderManager.class).getPaletteProviders()) {
				if(p == null) {
					p=paletteProvider.getPalette(this.paletteName);
				}
			}
		} else {
			PaletteProvider paletteProvider = this.ovManager.getService(PaletteProviderManager.class).getPaletteProvider(this.paletteProviderName);
			if(paletteProvider != null) {
				p = paletteProvider.getPalette(this.paletteName);
			}
		}
		
		if(p == null) {
			this.paletteName = this.defaultPaletteName;
			this.paletteProviderName = this.defaultPaletteProviderName;
			
			 p = this.ovManager.getService(PaletteProviderManager.class).getPaletteProvider(this.paletteProviderName).getPalette(this.paletteName);
		}
		
		this.palette = p;
		
		taskMonitor.setStatusMessage("Setting palette \""+this.paletteName+"\" from palette provider \""+this.paletteProviderName+"\".");
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Visualization Task");
		
		if(this.init(taskMonitor)) {
			this.initColors(taskMonitor);
			
			if(this.chartType==ChartType.PIE) {
				this.transpose=false;
			}
			
			OVVisualization ovViz = new OVVisualization(this.chartType,
					this.egSettings,
					this.listAttributes,
					this.attributesType,
					this.filteredOnly,
					this.colors,
					this.paletteName,
					this.labels,
					this.transpose);

			if(this.chartType.equals(ChartType.CIRCOS)) {
				this.ovCon.setOuterVisualization(ovViz);
			} else {
				this.ovCon.setInnerVisualization(ovViz);
			}

			ApplyVisualizationTaskFactory factory = new ApplyVisualizationTaskFactory(this.ovManager, this.ovCon, ovViz);
			this.ovManager.executeTask(factory.createTaskIterator());
		}
	}
}
