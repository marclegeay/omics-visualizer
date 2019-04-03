package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.awt.GridBagConstraints;
import java.util.HashMap;
import java.util.Map;

public class MyGridBagConstraints extends GridBagConstraints {
	private static final long serialVersionUID = 8741125344034643901L;
	
	static Map<String, Integer> anchors = new HashMap<>();
	static {
//		anchors.put("NW",	FIRST_LINE_START);
//		anchors.put("N",	PAGE_START);
//		anchors.put("NE",	FIRST_LINE_END);
//		anchors.put("W",	LINE_START);
//		anchors.put("C",	CENTER);
//		anchors.put("E",	LINE_END);
//		anchors.put("SW",	LAST_LINE_START);
//		anchors.put("S",	PAGE_END);
//		anchors.put("W",	LAST_LINE_END);
		anchors.put("NW",	NORTHWEST);
		anchors.put("N",	NORTH);
		anchors.put("NE",	NORTHEAST);
		anchors.put("W",	WEST);
		anchors.put("C",	CENTER);
		anchors.put("E",	EAST);
		anchors.put("SW",	SOUTHEAST);
		anchors.put("S",	SOUTH);
		anchors.put("W",	WEST);
	}
	
	static int DEFAULT_INSET=5; // By default we have 5px of margins
	
	public MyGridBagConstraints() {
		reset();
	}
	
	public MyGridBagConstraints reset() {
		gridx=gridy=0;
		gridwidth=gridheight=1;
		fill=GridBagConstraints.NONE;
		ipadx=ipady=0;
		insets.set(DEFAULT_INSET, DEFAULT_INSET, DEFAULT_INSET, DEFAULT_INSET);
		anchor=GridBagConstraints.CENTER;
		weightx=weighty=0.0;
		
		return this;
	}
	
	public MyGridBagConstraints nextCol() {
		gridx++;
		
		return this;
	}
	
	public MyGridBagConstraints nextRow() {
		gridx=0;
		gridy++;
		
		return this;
	}
	
	public MyGridBagConstraints noExpand() {
		weightx=weighty=0.0;
		fill=GridBagConstraints.NONE;
		
		return this;
	}
	
	public MyGridBagConstraints expandVertical() {
		weightx=0.0;
		weighty=1.0;
		fill=GridBagConstraints.VERTICAL;
		
		return this;
	}
	
	public MyGridBagConstraints expandHorizontal() {
		weightx=1.0;
		weighty=0.0;
		fill=GridBagConstraints.HORIZONTAL;
		
		return this;
	}
	
	public MyGridBagConstraints expandBoth() {
		weightx=weighty=1.0;
		fill=GridBagConstraints.BOTH;
		
		return this;
	}
	
	public MyGridBagConstraints useNCols(int n) {
		gridwidth=n;
		
		return this;
	}
	
	public MyGridBagConstraints setAnchor(String direction) {
		Integer anc = anchors.get(direction);
		
		anchor = (anc==null ? GridBagConstraints.CENTER : anc);
		
		return this;
	}
	
	public MyGridBagConstraints setAnchor(int a) {
		anchor = a;
		
		return this;
	}
	
	public MyGridBagConstraints setInsets(int top, int left, int bottom, int right) {
		insets.set(top, left, bottom, right);
		
		return this;
	}
}
