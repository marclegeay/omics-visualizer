package dk.ku.cpr.OmicsVisualizer.internal.model.operators;

public abstract class NumericOperator extends AbstractOperator {
	public NumberComparable cast(Object tableValue) {
		if((tableValue == null) || !(tableValue instanceof Number)) {
			return null;
		}
		
		return new NumberComparable((Number)tableValue);
	}
	
	protected class NumberComparable implements Comparable<Number> {
		private Number n;

		public NumberComparable(Number n) {
			this.n=n;
		}

		@Override
		public int compareTo(Number o) {
			if(n instanceof Integer) {
				Integer i = (Integer)n;
				return i.compareTo((Integer)o);
			} else if(n instanceof Long) {
				Long l = (Long)n;
				return l.compareTo((Long)o);
			} else if(n instanceof Double) {
				Double d = (Double)n;
				return d.compareTo((Double)o);
			}

			return 0;
		}
	}
}
