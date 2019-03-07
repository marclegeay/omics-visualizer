package dk.ku.cpr.OmicsVisualizer.internal.model.operators;

public class OperatorR extends AbstractOperator {

	@Override
	public boolean filter(Object tableValue, Object reference) {
		if((tableValue == null) || !(tableValue instanceof String)) {
			return false;
		}
		if(reference == null) {
			return true; // null is in every String
		}
		if(!(reference instanceof String)) {
			return false;
		}
		
		return ((String)tableValue).matches((String) reference);
	}

}
