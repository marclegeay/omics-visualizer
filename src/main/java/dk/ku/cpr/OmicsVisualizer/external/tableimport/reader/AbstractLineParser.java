package dk.ku.cpr.OmicsVisualizer.external.tableimport.reader;

import static dk.ku.cpr.OmicsVisualizer.external.tableimport.util.AttributeDataType.TYPE_BOOLEAN_LIST;
import static dk.ku.cpr.OmicsVisualizer.external.tableimport.util.AttributeDataType.TYPE_FLOATING_LIST;
import static dk.ku.cpr.OmicsVisualizer.external.tableimport.util.AttributeDataType.TYPE_INTEGER_LIST;
import static dk.ku.cpr.OmicsVisualizer.external.tableimport.util.AttributeDataType.TYPE_LONG_LIST;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.cytoscape.equations.Equation;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.service.util.CyServiceRegistrar;

import dk.ku.cpr.OmicsVisualizer.external.tableimport.util.AttributeDataType;

public abstract class AbstractLineParser {

	protected CyServiceRegistrar serviceRegistrar;
	private EquationCompiler compiler;

	protected AbstractLineParser(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object parse(final String s, final AttributeDataType type, final String delimiter) {
		Object value = null;
		
		if (s != null && !s.isEmpty() && !"null".equals(s)) {
			try {
				switch (type) {
					case TYPE_BOOLEAN:  return Boolean.valueOf(s.trim());
					case TYPE_INTEGER:  return Integer.valueOf(s.trim());
					case TYPE_LONG:     return Long.valueOf(s.trim());
					case TYPE_FLOATING: 
						// Modification ML: French decimals (with a comma)
						try {
							return Double.valueOf(s.trim());
						} catch(NumberFormatException nfe) {
							try {
								NumberFormat nf = NumberFormat.getInstance(Locale.FRANCE);
								return nf.parse(s.trim()).doubleValue();
							} catch (ParseException pe) {
								value = createInvalidNumberEquation(s.trim(), type);
							}
						}
					case TYPE_STRING:   return s.trim();
	
					case TYPE_BOOLEAN_LIST:
					case TYPE_INTEGER_LIST:
					case TYPE_LONG_LIST:
					case TYPE_FLOATING_LIST:
					case TYPE_STRING_LIST:
						value = parseList(s, type, delimiter);
						
						if (value instanceof List)
							value = new ArrayList<>((List)value);
						
						break;
				}
			} catch (NumberFormatException e) {
				value = createInvalidNumberEquation(s.trim(), type);
			}
		}
		
		return value;
	}
	
	private Object parseList(final String s, final AttributeDataType type, String delimiter) {
		if (s == null)
			return null;

		final List<Object> list = new ArrayList<>();
		final String[] parts = (s.replace("\"", "")).split(delimiter);

		for (String listItem : parts) {
			try {
				if (type == TYPE_BOOLEAN_LIST)
					list.add(Boolean.valueOf(listItem.trim()));
				else if (type == TYPE_INTEGER_LIST)
					list.add(Integer.valueOf(listItem.trim()));
				else if (type == TYPE_LONG_LIST)
					list.add(Long.valueOf(listItem.trim()));
				else if (type == TYPE_FLOATING_LIST) {
					// Modification ML: Taking into account comma as decimal separator
					try {
						Double d = new Double(listItem.trim());
						list.add(d);
					} catch(NumberFormatException nfe) {
						try {
							NumberFormat nf = NumberFormat.getInstance(Locale.FRANCE);
							Double d = (Double) nf.parse(listItem.trim());
							list.add(d);
						} catch(ParseException pe) {
							return createInvalidListEquation(s, listItem.trim(), type);
						}
					}
				} else // TYPE_STRING or unknown
					list.add(listItem.trim());				
			} catch (NumberFormatException e) {
				return createInvalidListEquation(s, listItem.trim(), type);
			}
		}

		return list;
	}
	
	private Equation createInvalidNumberEquation(final String value, final AttributeDataType type) {
		final String text = "=\"" + value + "\"";
		final String msg = "Invalid value: " + value;
		
		return getEquationCompiler().getErrorEquation(text, type.getType(), msg);
	}
	
	private Equation createInvalidListEquation(final String list, final String listItem,
			final AttributeDataType type) {
		final String text = "=\"" + list + "\"";
		final String msg = "Invalid list item: " + listItem;
		
		return getEquationCompiler().getErrorEquation(text, type.getType(), msg);
	}
	
	private EquationCompiler getEquationCompiler() {
		if (compiler == null)
			compiler = serviceRegistrar.getService(EquationCompiler.class);
		
		return compiler;
	}
}
