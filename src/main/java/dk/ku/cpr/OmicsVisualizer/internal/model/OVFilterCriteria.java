package dk.ku.cpr.OmicsVisualizer.internal.model;

import dk.ku.cpr.OmicsVisualizer.internal.model.operators.Operator;
import dk.ku.cpr.OmicsVisualizer.internal.utils.DataUtils;

/**
 * A filter criteria.
 * This is a basic comparison, a triplet (column name, operator, value to compare with).
 */
public class OVFilterCriteria extends OVFilter {
	private static final long serialVersionUID = -8009257438567922908L;
	
	private String colName;
	private Operator operator;
	private String reference;
	
	/**
	 * Creates a filter criteria.
	 * @param colName Name of the column to compare
	 * @param operator Operator used for the comparison
	 * @param reference String value used to compare
	 */
	public OVFilterCriteria(String colName, Operator operator, String reference) {
		super();
		this.colName = colName;
		this.operator = operator;
		this.reference = reference;
	}

	/**
	 * Returns the name of the column to compare.
	 * @return The name of the column
	 */
	public String getColName() {
		return colName;
	}

	/**
	 * Sets the name of the column to compare.
	 * @param colName Name of the column to compare
	 */
	public void setColName(String colName) {
		this.colName = colName;
	}

	/**
	 * Returns the operator used for the comparison
	 * @return The operator
	 */
	public Operator getOperator() {
		return operator;
	}

	/**
	 * Sets the operator used for the comparison
	 * @param operator The operator
	 */
	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	/**
	 * Returns the String value of the reference used to compare
	 * @return The String value
	 */
	public String getReference() {
		return reference;
	}

	/**
	 * Sets the String value of the reference used to compare
	 * @param reference String value of the reference to compare with
	 */
	public void setReference(String reference) {
		this.reference = reference;
	}
	
	@Override
	public String toString() {
		return "("
				+ DataUtils.escapeComma(this.colName)
				+ ","
				+ this.operator.name()
				+ ","
				+ DataUtils.escapeComma(DataUtils.escapeBackslash(this.reference))
				+ ")";
	}

	@Override
	public void renameColumn(String oldName, String newName) {
		if(this.colName.equals(oldName)) {
			this.colName = newName;
		}
	}
	
	/**
	 * Returns the filter represented by the String.
	 * The String must respect the following format:<br>
	 * (<code>colName</code>,<code>operator</code>,<code>reference</code>)<br>
	 * Or just:<br>
	 * (<code>colName</code>,<code>operator</code>)<br>
	 * Where:
	 * <ul>
	 * <li><code>colName</code> is the name of the column used to compare</li>
	 * <li><code>operator</code> is the name operator</li>
	 * <li><code>reference</code> is the value used to compare with</li>
	 * </ul>
	 * 
	 * Any comma in the <code>colName</code> or in the <code>reference</code> should be escaped by a backslash.<br>
	 * Please note that there should not have any space before nor after the commas.
	 * 
	 * @param str The filter to parse
	 * @return The parsed filter
	 * 
	 * @see Operator
	 */
	public static OVFilterCriteria valueOf(String str) {
//		System.out.println("OVFilterCriteria::valueOf("+str+")");
		if(str == null || str.length() < 2) {
			return null;
		}
		
		String critParts[] = DataUtils.getCSV(str.substring(1, str.length()-1));
		
		if((critParts.length < 2) || (critParts.length > 3)) {
			return null;
		}
		
		String colName = critParts[0];
		Operator operator = Operator.valueOf(critParts[1]);
		String reference="";
		
		if(critParts.length == 3) {
			reference = critParts[2];
		}
		
		return new OVFilterCriteria(colName, operator, reference);
	}
}
