package dk.ku.cpr.OmicsVisualizer.internal.model.operators;

public class OperatorN extends AbstractOperator {
	@Override
	public boolean filter(Object tableValue, Object reference) {
		// Here we do not look at reference
		return (tableValue==null);
	}
}
