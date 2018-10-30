package cpr.loadsitespecific.internal.tableimport.task;

import static cpr.loadsitespecific.internal.tableimport.reader.TextDelimiter.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import cpr.loadsitespecific.internal.tableimport.reader.TextDelimiter;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSelection;

public class DelimitersTunable {

	@Tunable(description = "Text delimiters", 
	         longDescription = "Select the delimiters to use to separate columns in the table, "+
					           "from the list '``,``',' ','``TAB``', or '``;``'.  ``TAB`` and '``,``' are used by default",
	         exampleStringValue = ";,\\,",
	         context = "both")
	public ListMultipleSelection<String> delimiters;
	
	
	public DelimitersTunable() {
		List<String> values = Arrays.asList(
			COMMA.getDelimiter(),
			SEMICOLON.getDelimiter(),
			SPACE.getDelimiter(),
			TAB.getDelimiter(),
			"\t" // to support escape sequences used in command arguments
		);
		delimiters = new ListMultipleSelection<>(values);
	}
	
	public void setSelectedValues(List<TextDelimiter> values) {
		List<String> strVaues = values.stream().map(TextDelimiter::getDelimiter).collect(Collectors.toList());
		delimiters.setSelectedValues(strVaues);
	}
	
	public List<String> getSelectedValues() {
		Set<String> values = new HashSet<>(delimiters.getSelectedValues());
		if(values.remove("\t")) {
			values.add(TAB.getDelimiter());
		}
		return new ArrayList<>(values	);
	}
	
	ListSelection<String> getTunable() {
		return delimiters;
	}
}
