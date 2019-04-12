package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;

/**
 * A filter that can be applied to a OVTable.
 * 
 * @see OVTable
 */
public abstract class OVFilter implements Serializable {
	private static final long serialVersionUID = -1392340255472045173L;
	
	/**
	 * Returns the serialized filter.
	 * @return the serialized filter
	 */
	public String save() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream out;
		
		try {
			out = new ObjectOutputStream(baos);
			
			out.writeObject(this);
			out.close();
		} catch (IOException e) {
			return "";
		}
		
		return Base64.getEncoder().encodeToString(baos.toByteArray());
	}
	
	/**
	 * Load a filter from a String representing a serialized filter.
	 * @param str The serialized filter
	 * @return The corresponding filter
	 */
	public static OVFilter load(String str) {
		if(str == null) {
			return null;
		}
		
		byte data[] = Base64.getDecoder().decode(str);
		ObjectInputStream in;
		Object o =null;
		
		try {
			in = new ObjectInputStream(new ByteArrayInputStream(data));
			
			o = in.readObject();
			in.close();
		} catch (IOException e) {
			return null;
		} catch (ClassNotFoundException e) {
			return null;
		}
		
		return (o instanceof OVFilter) ? (OVFilter)o : null;
	}
	
	@Override
	public abstract String toString();
	
	/**
	 * Returns the filter represented by the String.
	 * The String must respect the specific format of a filter.
	 * @param str The filter to parse
	 * @return The parsed filter
	 * 
	 * @see OVFilterCriteria#valueOf(String)
	 * @see OVFilterSet#valueOf(String)
	 */
	public static OVFilter valueOf(String str) {
//		System.out.println("OVFilter::valueOf("+str+")");
		if(str == null || str.isEmpty()) {
			return null;
		}
		
		if(str.startsWith("(")) {
			return OVFilterCriteria.valueOf(str);
		} else if (str.startsWith("{") || str.startsWith("[")) {
			return OVFilterSet.valueOf(str);
		}
		
		return null;
	}
}
