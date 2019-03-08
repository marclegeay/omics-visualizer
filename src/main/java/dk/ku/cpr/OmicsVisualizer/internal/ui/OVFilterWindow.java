package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVFilter;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVFilter.OVFilterType;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVFilterCriteria;
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

	private List<FilterCriteriaPanel> critPanels;
	private List<JButton> critButtons;
	private JButton addButton;
	private JComboBox<OVFilter.OVFilterType> selectType;

	private JButton okButton;
	private JButton cancelButton;
	private JButton removeButton;

	public OVFilterWindow(OVManager ovManager) {
		super();
		this.ovManager=ovManager;
	}
	
	private void init() {
		this.critPanels = new ArrayList<>();
		this.critPanels.add(new FilterCriteriaPanel(this));
		
		this.critButtons = new ArrayList<>();
		JButton del = new JButton("del");
		del.addActionListener(this);
		this.critButtons.add(del);
		
		this.addButton = new JButton("add");
		this.addButton.addActionListener(this);
		
		this.selectType = new JComboBox<>(OVFilter.OVFilterType.values());

		this.okButton = new JButton("OK");
		this.okButton.addActionListener(this);

		this.cancelButton = new JButton("Cancel");
		this.cancelButton.addActionListener(this);

		this.removeButton = new JButton("Remove");
		this.removeButton.setForeground(Color.RED);
		this.removeButton.addActionListener(this);
	}

	private void update() {
		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		mainPanel.setLayout(new BorderLayout());
		
		JPanel typePanel = new JPanel();
		typePanel.setLayout(new FlowLayout());
		typePanel.add(new JLabel("Matches:"));
		typePanel.add(this.selectType);
		
		JPanel selectPanel = new JPanel();
		selectPanel.setLayout(new GridBagLayout());
		MyGridBagConstraints c = new MyGridBagConstraints();
		c.expandHorizontal().setInsets(2, 0, 0, 2);

		JPanel selectSubPanel = new JPanel();
		selectSubPanel.setLayout(new GridBagLayout());
		MyGridBagConstraints subC = new MyGridBagConstraints();
		subC.expandHorizontal();
		
		if(this.critPanels.size()>1) {
			selectPanel.add(new JSeparator(), c);
			c.nextRow();
		}
		
		selectSubPanel.add(this.critPanels.get(0), subC);
		selectSubPanel.add(this.critButtons.get(0), subC.nextCol());
		
		selectPanel.add(selectSubPanel, c);
		selectPanel.add(new JSeparator(), c.nextRow());
		for(int i=1; i<this.critPanels.size(); ++i) {
			selectSubPanel = new JPanel();
			selectSubPanel.setLayout(new GridBagLayout());
			subC = new MyGridBagConstraints();
			subC.expandHorizontal();
			
			selectSubPanel.add(this.critPanels.get(i), subC);
			selectSubPanel.add(this.critButtons.get(i), subC.nextCol());
			
			selectPanel.add(selectSubPanel, c.nextRow());
			selectPanel.add(new JSeparator(), c.nextRow());
		}
		
		if(this.critButtons.size() == 1) {
			this.critButtons.get(0).setEnabled(false);
		} else {
			this.critButtons.get(0).setEnabled(true);
		}

		c.noExpand().setAnchor("E");
		selectPanel.add(this.addButton, c.nextRow());

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(this.okButton);
		buttonPanel.add(this.cancelButton);
		buttonPanel.add(this.removeButton);

		if(this.critPanels.size()>1) {
			mainPanel.add(typePanel, BorderLayout.NORTH);
		}
		mainPanel.add(selectPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		this.setContentPane(mainPanel);
		
		this.pack();
	}

	@Override
	public void setVisible(boolean b) {
		if(b) {
			if(this.ovManager.getActiveOVTable() != null) {
				this.ovTable = this.ovManager.getActiveOVTable();
			}

			this.setTitle("Filter " + this.ovTable.getTitle());
			
			this.init();

			OVFilter filter = this.ovTable.getFilter();
			if(filter != null) {
				this.selectType.setSelectedItem(filter.getType());
				
				this.critPanels = new ArrayList<>();
				this.critButtons = new ArrayList<>();
				for(OVFilterCriteria crit : filter.getCriterias()) {
					this.critPanels.add(new FilterCriteriaPanel(this, crit));
					JButton del = new JButton("del");
					del.addActionListener(this);
					this.critButtons.add(del);
				}
			}
//			if(filter != null) {
//				String filterParts[] = DataUtils.getCSV(filter);
//
//				this.selectColumn.setSelectedItem(filterParts[0]);
//				this.selectOperator.setSelectedItem(Operator.valueOf(filterParts[1]));
//				if(filterParts.length==3) {
//					this.fieldValue.setText(filterParts[2]);
//					this.selectValue.setSelectedItem(filterParts[2]);
//				}
//			}

			this.removeButton.setEnabled(filter!=null);

			this.update();

			OVCytoPanel ovPanel = this.ovManager.getOVCytoPanel();
			if(ovPanel != null) {
				this.setLocationRelativeTo(ovPanel.getTopLevelAncestor());
			}
		}

		super.setVisible(b);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.addButton) {
			this.critPanels.add(new FilterCriteriaPanel(this));
			JButton del = new JButton("del");
			del.addActionListener(this);
			this.critButtons.add(del);
			
			this.update();
		} else if(e.getSource() == this.okButton) {
			OVFilter filter = new OVFilter();
			
			filter.setType((OVFilterType) this.selectType.getSelectedItem());
			for(FilterCriteriaPanel fcp : this.critPanels) {
				filter.addCriteria(fcp.getCriteria());
			}
			
			this.ovTable.setFilter(filter);
			
			FilterTaskFactory factory = new FilterTaskFactory(this.ovManager);
			this.ovManager.executeTask(factory.createTaskIterator(this.ovTable));

			this.setVisible(false);
		} else if(e.getSource() == this.cancelButton) {
			this.setVisible(false);
		} else if(e.getSource() == this.removeButton) {
			RemoveFilterTaskFactory factory = new RemoveFilterTaskFactory(this.ovManager);
			this.ovManager.executeTask(factory.createTaskIterator(this.ovTable));

			this.setVisible(false);
		} else {
			// We look for the del button
			int i=0;
			while(i<this.critButtons.size() && e.getSource() != this.critButtons.get(i)) {
				++i;
			}
			
			if(i<this.critButtons.size()) {
				this.critButtons.remove(i);
				this.critPanels.remove(i);
				
				this.update();
			}
		}
	}
	
	private class FilterCriteriaPanel extends JPanel implements ActionListener {
		private static final long serialVersionUID = -6252891714271043948L;
		
		private OVFilterWindow parent;
		private boolean isInitialized;

		private JComboBox<String> selectColumn;
		private JComboBox<Operator> selectOperator;
		private JTextField fieldValue;
		private JComboBox<String> selectValue;

		private boolean isBool;
		
		public FilterCriteriaPanel(OVFilterWindow parent) {
			this.parent=parent;
			this.isInitialized=false; // We do not want to trigger updates before it is fully initialized
			
			this.selectColumn = new JComboBox<>();
			this.selectColumn.addActionListener(this);

			this.selectOperator = new JComboBox<>();
			this.selectOperator.addActionListener(this);

			this.fieldValue = new JTextField(10);

			this.selectValue = new JComboBox<>();
			this.selectValue.addItem("true");
			this.selectValue.addItem("false");

			for(String colName : ovTable.getColNames()) {
				if(!OVShared.isOVCol(colName) && ovTable.getColType(colName) != List.class) {
					this.selectColumn.addItem(colName);
				}
			}
			
			this.isInitialized=true;
		}
		
		public FilterCriteriaPanel(OVFilterWindow parent, OVFilterCriteria crit) {
			this.parent=parent;
			this.isInitialized=false; // We do not want to trigger updates before it is fully initialized
			
			this.selectColumn = new JComboBox<>();
			this.selectColumn.addActionListener(this);

			this.selectOperator = new JComboBox<>();
			this.selectOperator.addActionListener(this);

			this.fieldValue = new JTextField(10);

			this.selectValue = new JComboBox<>();
			this.selectValue.addItem("true");
			this.selectValue.addItem("false");

			for(String colName : ovTable.getColNames()) {
				if(!OVShared.isOVCol(colName) && ovTable.getColType(colName) != List.class) {
					this.selectColumn.addItem(colName);
				}
			}
			
			// Customize part
			this.selectColumn.setSelectedItem(crit.getColName());
			this.selectOperator.setSelectedItem(crit.getOperator());
			this.selectValue.setSelectedItem(crit.getReference());
			this.fieldValue.setText(crit.getReference());
			
			this.isInitialized=true;
		}

		private void update() {
			this.setLayout(new GridBagLayout());
			MyGridBagConstraints c = new MyGridBagConstraints();
			c.expandHorizontal();

			this.add(this.selectColumn, c);
			this.add(this.selectOperator, c.nextCol());

			if(!((Operator)this.selectOperator.getSelectedItem()).isUnary()) {
				c.nextRow().useNCols(2);
				if(this.isBool) {
					this.selectValue.setVisible(true);
					this.fieldValue.setVisible(false);
					this.add(this.selectValue, c);
				} else {
					this.selectValue.setVisible(false);
					this.fieldValue.setVisible(true);
					this.add(this.fieldValue, c);
				}
			} else {
				this.selectValue.setVisible(false);
				this.fieldValue.setVisible(false);
			}
			
			if(this.isInitialized) {
				this.parent.update();
			}
		}
		
		public OVFilterCriteria getCriteria() {
			String colName = (String)this.selectColumn.getSelectedItem();
			Operator operator = (Operator)this.selectOperator.getSelectedItem();
			String reference;
			
			if(this.isBool) {
				reference = (String)this.selectValue.getSelectedItem();
			} else {
				reference = this.fieldValue.getText();
			}
			
			return new OVFilterCriteria(colName, operator, reference);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == this.selectColumn) {
				// We display only operators that are compatible with the column type
				String colName = (String) this.selectColumn.getSelectedItem();
				if(colName == null) {
					return;
				}
				
				Operator previousSelected = (Operator)this.selectOperator.getSelectedItem();

				Class<?> colType = ovTable.getColType(colName);
				boolean isNumeric = (colType == Integer.class) ||
						(colType == Long.class) ||
						(colType == Double.class);
				boolean isString = (colType == String.class);
				this.isBool = (colType == Boolean.class);

				this.selectOperator.removeAllItems();
				for(Operator op : Operator.values()) {
					if(isNumeric && op.isNumeric()) {
						this.selectOperator.addItem(op);
					} else if(isString && op.isString()) {
						this.selectOperator.addItem(op);
					} else if(this.isBool && op.isBool()) {
						this.selectOperator.addItem(op);
					}
				}
				
				if(previousSelected != null) {
					this.selectOperator.setSelectedItem(previousSelected);
				}
				
				this.update();
			} else if(e.getSource() == this.selectOperator) {
				Operator selectedOperator = (Operator)this.selectOperator.getSelectedItem();
				if(selectedOperator == null) {
					return;
				}

				this.update();
			}
		}
	}
}
