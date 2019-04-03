package dk.ku.cpr.OmicsVisualizer.internal.model;

import dk.ku.cpr.OmicsVisualizer.internal.model.operators.Operator;
import dk.ku.cpr.OmicsVisualizer.internal.utils.DataUtils;

public class OVFilterCriteria extends OVFilter {
	private static final long serialVersionUID = -8009257438567922908L;
	
	private String colName;
	private Operator operator;
	private String reference;
	
	public OVFilterCriteria(String colName, Operator operator, String reference) {
		super();
		this.colName = colName;
		this.operator = operator;
		this.reference = reference;
	}

	public String getColName() {
		return colName;
	}

	public void setColName(String colName) {
		this.colName = colName;
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	public String getReference() {
		return reference;
	}

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
