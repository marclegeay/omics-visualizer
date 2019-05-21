package dk.ku.cpr.OmicsVisualizer.internal.task;

import java.awt.Color;

import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVColorContinuous;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVVisualization.ChartType;
import dk.ku.cpr.OmicsVisualizer.internal.ui.OVVisualizationWindow;

public class VisualizationContinuousTask extends VisualizationTask {

	private static final int SEQ_NEG = 1;
	private static final int SEQ_POS = 2;
	private static final int DIV = 3;
	
	private int colorType=0;

	@Tunable(description="Minimum value. Below this value, the same color will be applied.",
			exampleStringValue="-1.0",
			gravity=1.0)
	public Double rangeMin=null;
	
	@Tunable(description="Middle value. The colors will be a gradient from min to mid, and from mid to max.",
			exampleStringValue="0.0",
			gravity=1.0)
	public Double rangeMid=null;
	
	@Tunable(description="Maximum value. Above this value, the same color will be applied.",
			exampleStringValue="1.0",
			gravity=1.0)
	public Double rangeMax=null;
	
	@Tunable(description="Color used in the gradient as the lowest value.",
			exampleStringValue="blue",
			gravity=1.0)
	public String colorMin = null;
	
	@Tunable(description="Color used in the gradient as the middle value.",
			exampleStringValue="#0000ff",
			gravity=1.0)
	public String colorMid = null;
	
	@Tunable(description="Color used in the gradient as the highest value.",
			exampleStringValue="BLUE",
			gravity=1.0)
	public String colorMax = null;
	
	@Tunable(description="Color used for missing values.",
			exampleStringValue="#bebebe",
			gravity=1.0)
	public String colorMissing = OVShared.color2String(OVVisualizationWindow.DEFAULT_MISSING_COLOR);

	public VisualizationContinuousTask(OVManager ovManager, ChartType chartType) {
		super(ovManager, chartType);
	}
	
	@Override
	protected boolean init(TaskMonitor taskMonitor) {
		if(super.init(taskMonitor)) {
			Double defaultRangeMin=Double.MAX_VALUE, defaultRangeMid=0.0, defaultRangeMax=Double.MIN_VALUE;
			
			if(this.attributesType == Double.class) {
				for(Object o : this.values) {
					Double d = (Double) o;
					
					if(d<defaultRangeMin) {
						defaultRangeMin=d;
					}
					if(d>defaultRangeMax) {
						defaultRangeMax=d;
					}
				}
			} else if(this.attributesType == Integer.class) {
				Integer min=Integer.MAX_VALUE, max=Integer.MIN_VALUE;
				for(Object o : this.values) {
					Integer i = (Integer) o;
					
					if(i<min) {
						min=i;
					}
					if(i>max) {
						max=i;
					}
				}
				
				defaultRangeMin = min*1.0;
				defaultRangeMax = max*1.0;
			} else if(this.attributesType == Long.class) {
				Long min=Long.MAX_VALUE, max=Long.MIN_VALUE;
				for(Object o : this.values) {
					Long l = (Long) o;
					
					if(l<min) {
						min=l;
					}
					if(l>max) {
						max=l;
					}
				}
				
				defaultRangeMin = min*1.0;
				defaultRangeMax = max*1.0;
			} else {
				taskMonitor.setStatusMessage("ERROR: Continuous mapping is only for Double, Long and Integer values.");
				return false;
			}
			
			if((defaultRangeMax <= 0) || (defaultRangeMin >= 0)) {
				// The values have the same sign
				defaultRangeMid = (defaultRangeMax + defaultRangeMin) / 2.0;

				if(defaultRangeMax <= 0) { // Values all negatives
					this.colorType = SEQ_NEG;
				} else { // values all positives
					this.colorType = SEQ_POS;
				}
			} else {
				// We detect the highest absolute value for the range
				defaultRangeMax = (defaultRangeMax >= -defaultRangeMin ? defaultRangeMax : -defaultRangeMin);
				defaultRangeMin = -defaultRangeMax;
				defaultRangeMid = 0.0;

				this.colorType = DIV;
			}

			if(this.rangeMin == null) {
				this.rangeMin = defaultRangeMin;
			}
			if(this.rangeMid == null) {
				this.rangeMid = defaultRangeMid;
			}
			if(this.rangeMax == null) {
				this.rangeMax = defaultRangeMax;
			}

			if((rangeMin > rangeMid) || (rangeMid > rangeMax)) {
				taskMonitor.setStatusMessage("ERROR: The minimum limit should be lower than the middle value, that should be lower than the maximum limit.");
				return false;
			}
		} else {
			return false;
		}
		
		switch(this.colorType) {
		case SEQ_POS:
		case SEQ_NEG:
			this.defaultPaletteProviderName="Viridis";
			this.defaultPaletteName="Viridis";
			break;
		case DIV:
		default:
			this.defaultPaletteProviderName="ColorBrewer";
			this.defaultPaletteName="Red-Blue";
			break;
		}
		
		return true;
	}
	
	@Override
	protected void initColors(TaskMonitor taskMonitor) {
		// We init the palette
		super.initColors(taskMonitor);
		
		if(this.colorMin != null && this.colorMid != null && this.colorMax != null) {
			taskMonitor.setStatusMessage("Using user-specified colors");
		}
		
		Color colMin = parseColor(this.colorMin),
				colZero = parseColor(this.colorMid),
				colMax = parseColor(this.colorMax),
				colMissing = parseColor(this.colorMissing);
		
		Color colors[] = this.palette.getColors(9);
		if(this.colorType == SEQ_POS) {
			// Values are all positives
			// We revert the color range
			if(colMax == null) {
				if(this.colorMax != null) {
					taskMonitor.setStatusMessage("WARNING: Unknown color \""+this.colorMax+"\", using palette instead.");
				}
				colMax = colors[8];
			}
			if(colZero == null) {
				if(this.colorMid != null) {
					taskMonitor.setStatusMessage("WARNING: Unknown color \""+this.colorMid+"\", using palette instead.");
				}
				colZero = colors[4];
			}
			if(colMin == null) {
				if(this.colorMin != null) {
					taskMonitor.setStatusMessage("WARNING: Unknown color \""+this.colorMin+"\", using palette instead.");
				}
				colMin = colors[0];
			}
		} else {
			if(colMax == null) {
				if(this.colorMax != null) {
					taskMonitor.setStatusMessage("WARNING: Unknown color \""+this.colorMax+"\", using palette instead.");
				}
				colMax = colors[0];
			}
			if(colZero == null) {
				if(this.colorMid != null) {
					taskMonitor.setStatusMessage("WARNING: Unknown color \""+this.colorMid+"\", using palette instead.");
				}
				colZero = colors[4];
			}
			if(colMin == null) {
				if(this.colorMin != null) {
					taskMonitor.setStatusMessage("WARNING: Unknown color \""+this.colorMin+"\", using palette instead.");
				}
				colMin = colors[8];
			}
		}

		this.colors = new OVColorContinuous(colMin, // min
				colMax, // max
				colZero, // zero
				colMissing, // missing
				rangeMin.doubleValue(),
				rangeMid.doubleValue(),
				rangeMax.doubleValue());
	}

}
