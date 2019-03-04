package dk.ku.cpr.OmicsVisualizer.internal.model.operators;

public enum Operator {
	EQUALS("==", new OperatorE()),
	NOT_EQUALS("!=", new OperatorNE()),
	LOWER("<", new OperatorL()),
	LOWER_EQUALS("<=", new OperatorLE()),
	GREATER(">", new OperatorG()),
	GREATER_EQUALS(">=", new OperatorGE()),
	NULL("is null", new OperatorN()),
	NOT_NULL("is not null", new OperatorNN());
	
	private String name;
	private AbstractOperator operator;
	
	Operator(String name, AbstractOperator operator) {
		this.name=name;
		this.operator=operator;
	}
	
	public String getName() {
		return this.name;
	}
	
	public AbstractOperator getOperator() {
		return this.operator;
	}
	
	public String toString() {
		return this.name;
	}
}
