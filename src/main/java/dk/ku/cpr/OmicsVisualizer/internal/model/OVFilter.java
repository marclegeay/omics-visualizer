package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OVFilter implements Serializable {
	private static final long serialVersionUID = -1392340255472045173L;
	
	private OVFilterType type;
	private List<OVFilterCriteria> criterias;
	
	public OVFilter() {
		type=OVFilterType.ALL;
		criterias=new ArrayList<>();
	}
	
	public OVFilterType getType() {
		return type;
	}

	public void setType(OVFilterType type) {
		this.type = type;
	}

	public List<OVFilterCriteria> getCriterias() {
		return criterias;
	}
	
	public void addCriteria(OVFilterCriteria criteria) {
		this.criterias.add(criteria);
	}
	
	public void removeCriteria(OVFilterCriteria criteria) {
		this.criterias.remove(criteria);
	}
	
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
	public String toString() {
		String str = "[" + this.type.name() + "]";
		
		for(OVFilterCriteria c : this.criterias) {
			str += "(" +  c + ")";
		}
		
		return str;
	}
	
	public static OVFilter valueOf(String str) {
		if(str == null) {
			return null;
		}
		
		Pattern p_type = Pattern.compile("^\\[(.+)\\](.+)$");
		Matcher m_type = p_type.matcher(str);
		
		if(!m_type.matches()) {
			return null;
		}
		
		OVFilterType type = OVFilterType.valueOf(m_type.group(1));
		String crits = m_type.group(2);
		
		Pattern p_crit = Pattern.compile("\\((.+?)\\)");
		Matcher m_crit = p_crit.matcher(crits);
		
		if(!m_crit.find()) {
			return null;
		}
		
		OVFilter filter = new OVFilter();
		filter.setType(type);
		
		int start = m_crit.start();
		int end;
		while(m_crit.find(start)) {
			end = m_crit.end();
			
			// 'start' and 'end' catches the string "(filter)" with the parenthesis
			// But OVFilterCriteria.valueOf does not want the parenthesis 
			filter.addCriteria(OVFilterCriteria.valueOf(crits.substring(start+1, end-1)));
			
			start = end;
		}
		
		return filter;
	}

	public enum OVFilterType {
		ALL("All (AND)"),
		ANY("Any (OR)");
		
		private String display;
		
		OVFilterType(String display) {
			this.display=display;
		}
		
		@Override
		public String toString() {
			return this.display;
		}
	}
}
