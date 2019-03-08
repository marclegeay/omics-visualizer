package dk.ku.cpr.OmicsVisualizer.internal.model.operators;

import java.io.Serializable;

public enum Operator implements Serializable {
	EQUALS("==", new OperatorE(), true, true, true),
	NOT_EQUALS("!=", new OperatorNE(), true, true, true),
	CONTAINS("contains", new OperatorC(), false, true, false),
	NOT_CONTAIN("does not contain", new OperatorNC(), false, true, false),
	MATCHES("matches regex", new OperatorR(), false, true, false),
	LOWER("<", new OperatorL(), true, false, false),
	LOWER_EQUALS("<=", new OperatorLE(), true, false, false),
	GREATER(">", new OperatorG(), true, false, false),
	GREATER_EQUALS(">=", new OperatorGE(), true, false, false),
	NULL("is null", new OperatorN(), true, true, true),
	NOT_NULL("is not null", new OperatorNN(), true, true, true);
	
	private String name;
	private AbstractOperator operator;
	private boolean numeric;
	private boolean string;
	private boolean bool;
	
	Operator(String name, AbstractOperator operator, boolean numeric, boolean string, boolean bool) {
		this.name=name;
		this.operator=operator;
		this.numeric=numeric;
		this.string=string;
		this.bool=bool;
	}
	
	public String getName() {
		return this.name;
	}
	
	public AbstractOperator getOperator() {
		return this.operator;
	}
	
	public boolean isNumeric() {
		return this.numeric;
	}
	
	public boolean isString() {
		return this.string;
	}
	
	public boolean isBool() {
		return this.bool;
	}
	
	public boolean isUnary() {
		return this.operator instanceof OperatorNN ||
				this.operator instanceof OperatorN;
	}
	
	public String toString() {
		return this.name;
	}
}
