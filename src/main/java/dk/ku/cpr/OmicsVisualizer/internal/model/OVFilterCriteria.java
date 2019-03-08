package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.io.Serializable;

import dk.ku.cpr.OmicsVisualizer.internal.model.operators.Operator;
import dk.ku.cpr.OmicsVisualizer.internal.utils.DataUtils;

public class OVFilterCriteria implements Serializable {
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
				+ this.operator
				+ ","
				+ DataUtils.escapeComma(DataUtils.escapeBackslash(this.reference))
				+ ")";
	}
}
