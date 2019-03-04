package dk.ku.cpr.OmicsVisualizer.internal.model.operators;

public class OperatorNN extends AbstractOperator {
	@Override
	public boolean filter(Object tableValue, Object reference) {
		// Here we do not look at reference
		return (tableValue!=null);
	}
}
