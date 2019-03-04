package dk.ku.cpr.OmicsVisualizer.internal.model.operators;

public class OperatorLE extends NumericOperator {
	@Override
	public boolean filter(Object tableValue, Object reference) {
		NumberComparable value = this.cast(tableValue);
		
		if(value == null) {
			return false;
		}
		return value.compareTo((Number)reference) <= 0;
	}
}