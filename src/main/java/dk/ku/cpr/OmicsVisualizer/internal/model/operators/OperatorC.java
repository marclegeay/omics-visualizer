package dk.ku.cpr.OmicsVisualizer.internal.model.operators;

public class OperatorC extends AbstractOperator {

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
		
		String strValue = (String)tableValue;
		
		return strValue.contains((String) reference);
	}
}
