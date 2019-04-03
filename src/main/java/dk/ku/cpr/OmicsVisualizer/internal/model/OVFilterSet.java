package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OVFilterSet extends OVFilter {
	private static final long serialVersionUID = -3110109155399467175L;
	
	private OVFilterSetType type;
	private List<OVFilter> filters;
	
	public OVFilterSet() {
		type=OVFilterSetType.ALL;
		filters=new ArrayList<>();
	}
	
	public OVFilterSetType getType() {
		return type;
	}

	public void setType(OVFilterSetType type) {
		this.type = type;
	}

	public List<OVFilter> getFilters() {
		return filters;
	}
	
	public void addFilter(OVFilter filter) {
		this.filters.add(filter);
	}
	
	public void removeFilter(OVFilter filter) {
		this.filters.remove(filter);
	}
	
	@Override
	public String toString() {
		String str =  OVShared.join(this.filters, ",");
		
		if(this.filters.size() > 1) {
			if(this.type.equals(OVFilterSetType.ALL)) { // (AND)
				str = "{" + str + "}";
			} else { // ANY (OR)
				str = "[" + str + "]";
			}
		}
		
		return str;
	}
	
	public static OVFilterSet valueOf(String str) {
//		System.out.println("OVFilterSet::valueOf("+str+")");
		if(str == null) {
			return null;
		}

		Pattern p_any = Pattern.compile("^\\[(.+)\\]$");
		Pattern p_all = Pattern.compile("^\\{(.+)\\}$");
		Matcher m_any = p_any.matcher(str);
		Matcher m_all = p_all.matcher(str);
		Matcher matcher = null;
		
		OVFilterSet filter = new OVFilterSet();
		
		if(m_any.matches()) {
			matcher = m_any;
			filter.setType(OVFilterSetType.ANY);
		} else if(m_all.matches()) {
			matcher = m_all;
			filter.setType(OVFilterSetType.ALL);
		} else {
			return null;
		}
		
		String filters = matcher.group(1);
		
		int start = 0;
		int end = -1;
		char startingChar=' ', endingChar=' ';
		
		while(start < (filters.length()-1)) {
			// We first identify the starting/ending character, and the position of the ending character
			if(filters.startsWith("(", start)) {
				startingChar = '(';
				endingChar = ')';
			} else if(filters.startsWith("[", start)) {
				startingChar = '[';
				endingChar = ']';
			} else if(filters.startsWith("{", start)) {
				startingChar = '{';
				endingChar = '}';
			} else {
//				System.out.println("[OV - OVFilterSet::valueOf] not a valid starting character : '" + filters + "' ; start=" + start);
				return null;
			}
			end = filters.indexOf(endingChar, start);

			// If there is no ending character, the expression is not well formated
			if(end == -1) {
//				System.out.println("[OV - OVFilterSet::valueOf] no end character : '" + filters + "' ; start=" + start + " ; endingChar=" + endingChar);
				return null;
			}
			
			// We verify that the endingChar is indeed the last one
			int inside = filters.indexOf(startingChar, start+1);
			// If we find a startingChar between start and end, it means that it is not the good ending character
			while(inside != -1 && inside < end) {
				end = filters.indexOf(endingChar, end+1);
				
				inside = filters.indexOf(startingChar, inside+1);
			}

			// If there is no ending character, the expression is not well formated
			if(end == -1) {
//				System.out.println("[OV - OVFilterSet::valueOf] no end character (after inside) : '" + filters + "' ; start=" + start + " ; endingChar=" + endingChar);
				return null;
			}
			
			// substring exclude the end-th character, we want to include it
			++end;
			
			String str_filter = filters.substring(start, end);
			filter.addFilter(OVFilter.valueOf(str_filter));
			
			start = end;
			
			if(filters.startsWith(",", start)) {
				++start;
			}
		}
		
		return filter;
	}

	public enum OVFilterSetType {
		ALL("All (AND)"),
		ANY("Any (OR)");
		
		private String display;
		
		OVFilterSetType(String display) {
			this.display=display;
		}
		
		@Override
		public String toString() {
			return this.display;
		}
	}
}
