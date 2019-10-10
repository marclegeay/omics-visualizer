package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * enhancedGraphics settings
 */
public class EGSettings implements Serializable {
	private static final long serialVersionUID = -7356849626176436599L;
	
	public static final String BORDER_WIDTH = "borderwidth";
	public static final String BORDER_COLOR = "bordercolor";
	public static final String LABEL_FONT = "labelfont";
	public static final String LABEL_COLOR = "labelcolor";
	public static final String LABEL_SIZE = "labelsize";
	public static final String ARC_START = "arcstart";
	public static final String ARC_WIDTH = "arcwidth";
	public static final String ARC_DIRECTION = "arcdirection";

	public static final String BORDER_WIDTH_DEFAULT = "0.1";
	public static final String BORDER_COLOR_DEFAULT = "black";
	public static final String LABEL_FONT_DEFAULT = "SansSerif";
	public static final String LABEL_COLOR_DEFAULT = "black";
	public static final String LABEL_SIZE_DEFAULT = "10";
	public static final String ARC_START_DEFAULT = ArcStartValues.TWELVE.toEnhancedGraphics();
	public static final String ARC_WIDTH_DEFAULT = "0.4";
	public static final String ARC_DIRECTION_DEFAULT = ArcDirectionValues.CLOCKWISE.toString();
	
	private Map<String, String> settings;
	
	public EGSettings() {
		this.settings = new HashMap<>();

		// Set the default values
		this.settings.put(BORDER_WIDTH, BORDER_WIDTH_DEFAULT);
		this.settings.put(BORDER_COLOR, BORDER_COLOR_DEFAULT);
		this.settings.put(LABEL_FONT, LABEL_FONT_DEFAULT);
		this.settings.put(LABEL_COLOR, LABEL_COLOR_DEFAULT);
		this.settings.put(LABEL_SIZE, LABEL_SIZE_DEFAULT);
		this.settings.put(ARC_START, ARC_START_DEFAULT);
		this.settings.put(ARC_WIDTH, ARC_WIDTH_DEFAULT);
		this.settings.put(ARC_DIRECTION, ARC_DIRECTION_DEFAULT);
	}
	
	public void set(String setting, String value) {
		this.settings.put(setting, value);
	}
	
	public String get(String setting) {
		return this.settings.get(setting);
	}
	
	public Set<String> getKeys() {
		return this.settings.keySet();
	}
	
	public enum ArcDirectionValues {
		CLOCKWISE("clockwise"),
		COUNTERCLOCKWISE("counterclockwise");
		
		private String str;
		
		private ArcDirectionValues(String str) {
			this.str=str;
		}
		
		public String toString() {
			return this.str;
		}
		
		public static ArcDirectionValues valueOfStr(String str) {
			for(ArcDirectionValues adv : ArcDirectionValues.values()) {
				if(adv.str.equals(str)) {
					return adv;
				}
			}
			
			return ArcDirectionValues.CLOCKWISE;
		}
	}
	
	public enum ArcStartValues {
		TWELVE("12 o'clock", "90"),
		THREE("3 o'clock", "0"),
		SIX("6 o'clock", "270"),
		NINE("9 o'clock", "180");
		
		private String displayValue;
		private String egValue;
		
		private ArcStartValues(String displayValue, String egValue) {
			this.displayValue=displayValue;
			this.egValue=egValue;
		}
		
		public String toString() {
			return this.displayValue;
		}
		
		public String toEnhancedGraphics() {
			return this.egValue;
		}
		
		/**
		 * Returns the ArcStartValues corresponding to the given enhancedGraphics value.
		 * @param egValue value to parse into ArcStartValues
		 * @return the ArcStartValues corresponding, TWELVE if the value cannot be parsed.
		 */
		public static ArcStartValues valueOfEG(String egValue) {
			for(ArcStartValues asv : ArcStartValues.values()) {
				if(asv.egValue.equals(egValue)) {
					return asv;
				}
			}
			
			return TWELVE;
		}
	}
}
