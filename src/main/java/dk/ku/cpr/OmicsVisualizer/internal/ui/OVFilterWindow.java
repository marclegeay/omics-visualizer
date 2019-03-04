package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;
import dk.ku.cpr.OmicsVisualizer.internal.model.operators.Operator;
import dk.ku.cpr.OmicsVisualizer.internal.task.FilterTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.task.RemoveFilterTaskFactory;

public class OVFilterWindow extends JFrame implements ActionListener {
	private static final long serialVersionUID = -7306443854568361953L;

	private OVManager ovManager;
	
	private OVTable ovTable;
	
	private JComboBox<String> selectColumn;
	private JComboBox<Operator> selectOperator;
	private JTextField fieldValue;
	
	private JButton okButton;
	private JButton cancelButton;
	private JButton removeButton;
	
	public OVFilterWindow(OVManager ovManager) {
		super();
		this.ovManager=ovManager;
		
		this.selectColumn = new JComboBox<>();
		this.selectColumn.addActionListener(this);
		
		this.selectOperator = new JComboBox<>();
		this.selectOperator.addActionListener(this);
		
		this.fieldValue = new JTextField();
		
		this.okButton = new JButton("OK");
		this.okButton.addActionListener(this);
		
		this.cancelButton = new JButton("Cancel");
		this.cancelButton.addActionListener(this);
		
		this.removeButton = new JButton("Remove");
		this.removeButton.setForeground(Color.RED);
		this.removeButton.addActionListener(this);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		JPanel selectPanel = new JPanel();
		selectPanel.setLayout(new GridBagLayout());
		MyGridBagConstraints c = new MyGridBagConstraints();
		c.expandHorizontal();

		selectPanel.add(new JLabel("Select column:"), c);
		selectPanel.add(this.selectColumn, c.nextCol());
		
		selectPanel.add(new JLabel("Select operator:"), c.nextRow());
		selectPanel.add(this.selectOperator, c.nextCol());
		
		selectPanel.add(new JLabel("Value:"), c.nextRow());
		selectPanel.add(this.fieldValue, c.nextCol());
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		buttonPanel.add(removeButton);
		
		mainPanel.add(selectPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		this.setContentPane(mainPanel);
	}
	
	@Override
	public void setVisible(boolean b) {
		if(b) {
			if(this.ovManager.getActiveOVTable() != null) {
				this.ovTable = this.ovManager.getActiveOVTable();
			}
			
			this.selectColumn.removeAllItems();
			for(String colName : this.ovTable.getColNames()) {
				if(!OVShared.isOVCol(colName)) {
					this.selectColumn.addItem(colName);
				}
			}
			
			String filter = this.ovTable.getFilter();
			if(filter != null) {
				String filterParts[] = filter.split(",");
				
				this.selectColumn.setSelectedItem(filterParts[0]);
				this.selectOperator.setSelectedItem(Operator.valueOf(filterParts[1]));
				this.fieldValue.setText(filterParts[2]);
			}
			
			this.removeButton.setEnabled(filter!=null);
			
			this.pack();

			OVCytoPanel ovPanel = this.ovManager.getOVCytoPanel();
			if(ovPanel != null) {
				this.setLocationRelativeTo(ovPanel.getTopLevelAncestor());
			}
		}
		
		super.setVisible(b);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.selectColumn) {
			// We display only operators that are compatible with the column type
			String colName = (String) this.selectColumn.getSelectedItem();
			if(colName == null) {
				return;
			}
			
			Class<?> colType = this.ovTable.getColType(colName);
			boolean isNumeric = (colType == Integer.class) ||
					(colType == Long.class) ||
					(colType == Double.class);
			
			this.selectOperator.removeAllItems();
			for(Operator op : Operator.values()) {
				// A numeric column can have all the operators
				if(isNumeric || !op.isNumeric()) {
					this.selectOperator.addItem(op);
				}
			}
		} else if(e.getSource() == this.selectOperator) {
			Operator selectedOperator = (Operator)this.selectOperator.getSelectedItem();
			if(selectedOperator == null) {
				return;
			}
			
			this.fieldValue.setEnabled(!selectedOperator.isUnary());
		} else if(e.getSource() == this.okButton) {
			String colName = (String) this.selectColumn.getSelectedItem();
			Operator operator = (Operator) this.selectOperator.getSelectedItem();
			String strReference = this.fieldValue.getText();
			
			FilterTaskFactory factory = new FilterTaskFactory(this.ovManager);
			this.ovManager.executeTask(factory.createTaskIterator(colName, operator, strReference));
			
			this.setVisible(false);
		} else if(e.getSource() == this.cancelButton) {
			this.setVisible(false);
		} else if(e.getSource() == this.removeButton) {
			RemoveFilterTaskFactory factory = new RemoveFilterTaskFactory(this.ovManager);
			this.ovManager.executeTask(factory.createTaskIterator(this.ovTable));
			
			this.setVisible(false);
		}
	}
}
