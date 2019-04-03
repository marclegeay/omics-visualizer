package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;

public abstract class OVFilter implements Serializable {
	private static final long serialVersionUID = -1392340255472045173L;
	
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
