package dk.ku.cpr.OmicsVisualizer.internal.model.operators;

public class OperatorE extends AbstractOperator {
	@Override
	public boolean filter(Object tableValue, Object reference) {
		if(tableValue == null) {
			return false;
		}
		
		return tableValue.equals(reference);
	}
}
