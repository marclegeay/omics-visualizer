package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.awt.GridBagConstraints;

public class MyGridBagConstraints extends GridBagConstraints {
	private static final long serialVersionUID = 8741125344034643901L;
	
	public MyGridBagConstraints() {
		reset();
	}
	
	public MyGridBagConstraints reset() {
		gridx=gridy=0;
		gridwidth=gridheight=1;
		fill=GridBagConstraints.NONE;
		ipadx=ipady=0;
		insets.set(0, 0, 0, 0);
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
	
	public MyGridBagConstraints expandVertial() {
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
}
