package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A set of filters.
 * It is a list of filters, and a logical operator (AND or OR).
 */
public class OVFilterSet extends OVFilter {
	private static final long serialVersionUID = -3110109155399467175L;
	
	private OVFilterSetType type;
	private List<OVFilter> filters;
	
	/**
	 * Creates an empty set of filters.
	 */
	public OVFilterSet() {
		type=OVFilterSetType.ALL;
		filters=new ArrayList<>();
	}
	
	/**
	 * Returns the logical operator.
	 * @return the type of set
	 */
	public OVFilterSetType getType() {
		return type;
	}

	/**
	 * Sets the type of set, i.e. the logical operator.
	 * @param type The type of set
	 */
	public void setType(OVFilterSetType type) {
		this.type = type;
	}

	/**
	 * Returns the set of filters.
	 * @return The set of filters
	 */
	public List<OVFilter> getFilters() {
		return filters;
	}
	
	/**
	 * Adds a filter to the set.
	 * @param filter The filter to add
	 */
	public void addFilter(OVFilter filter) {
		this.filters.add(filter);
	}
	
	/**
	 * Remove a filter from the set
	 * @param filter The filter to remove
	 */
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

	@Override
	public void renameColumn(String oldName, String newName) {
		// We propagate the information
		for(OVFilter filter : this.filters) {
			filter.renameColumn(oldName, newName);
		}
	}
	
	/**
	 * Returns the filter represented by the String.
	 * The String must respect the following format:<br>
	 * AND set: 
	 * {<code>filter_1</code>,<code>filter_2</code>,...,<code>filter_n</code>}<br>
	 * OR set: 
	 * [<code>filter_1</code>,<code>filter_2</code>,...,<code>filter_n</code>]<br>
	 * Please note that there should not have any space before nor after the commas.
	 * 
	 * @param str The filter to parse
	 * @return The parsed filter
	 * 
	 * @see OVFilter#valueOf(String)
	 * @see OVFilterCriteria#valueOf(String)
	 */
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

	/**
	 * Type of filter set.
	 * It represents a logical operator (AND or OR).
	 */
	public enum OVFilterSetType {
		/**
		 * Value representing a logicial AND.
		 * All the filters of the set should return <code>true</code> so that a row is filtered.
		 */
		ALL("All (AND)"),
		/**
		 * Value representing a logicial OR.
		 * At least one filter from the set should return <code>true</code> so that a row is filtered.
		 */
		ANY("Any (OR)");
		
		private String display;
		
		private OVFilterSetType(String display) {
			this.display=display;
		}
		
		@Override
		public String toString() {
			return this.display;
		}
	}
}
