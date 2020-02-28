package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.application.swing.CyColumnSelector;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.task.CreateOVTableFromNetworkTaskFactory;

public class CopyNodeTableWindow extends OVWindow implements ActionListener {
	private static final long serialVersionUID = -8874802023098233849L;
	private static final String KEY_COL_TOOLTIP = "Column used from the node table to identify rows in the new table. This column will be used to link the new table with the network.";
	private static final String VALUES_NAME_TOOLTIP = "Name of the column of the new table containing the values of the imported columns.";
	private static final String SRC_NAME_TOOLTIP = "Name of the column of the new table containing the name of the imported columns where the values come from.";
	
	private JComboBox<CyNetwork> selectNetwork;
	private JComboBox<String> selectKeyCol;
	private CyColumnSelector columnsSelector;
	private JTextField tableName;
	private JTextField valuesName;
	private JTextField srcName;
	private JCheckBox includeNamespaces;
	
	private JButton createButton;
	private JButton cancelButton;

	public CopyNodeTableWindow(OVManager ovManager) {
		super(ovManager, "Create from a node table");
		this.setResizable(true);
		
		init();
		draw();
	}
	
	private void init() {
		this.selectNetwork = new JComboBox<>();
		this.selectNetwork.addActionListener(this);
		
		this.selectKeyCol = new JComboBox<>();
		this.selectKeyCol.setToolTipText(KEY_COL_TOOLTIP);
		
		IconManager iconManager = this.ovManager.getService(IconManager.class);
		CyColumnPresentationManager presentationManager = this.ovManager.getService(CyColumnPresentationManager.class);
		this.columnsSelector = new CyColumnSelector(iconManager, presentationManager);
		
		this.tableName = new JTextField();
		
		this.valuesName = new JTextField(OVShared.OV_DEFAULT_VALUES_COLNAME);
		this.valuesName.setToolTipText(VALUES_NAME_TOOLTIP);
		
		this.srcName = new JTextField(OVShared.OV_DEFAULT_VALUES_SOURCE_COLNAME);
		this.srcName.setToolTipText(SRC_NAME_TOOLTIP);
		
		this.includeNamespaces = new JCheckBox("Include namespaces in the source", true);
		this.includeNamespaces.setToolTipText("Should the namespaces be included in the name of the imported source columns?");
		
		this.createButton = new JButton("Create");
		this.createButton.addActionListener(this);
		
		this.cancelButton = new JButton("Cancel");
		this.cancelButton.addActionListener(this);
		
		LookAndFeelUtil.equalizeSize(this.createButton, this.cancelButton);
		
		// Now we put the network list so that it will init all the other combo boxes
		CyNetworkManager netManager = this.ovManager.getService(CyNetworkManager.class);
		for(CyNetwork net : netManager.getNetworkSet()) {
			this.selectNetwork.addItem(net);
		}
	}
	
	private void draw() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	
		JPanel configPanel = new JPanel();
		configPanel.setOpaque(!LookAndFeelUtil.isAquaLAF());
		configPanel.setBorder(LookAndFeelUtil.createTitledBorder("Create a new Omics Visualizer table from a node table"));
		configPanel.setLayout(new GridBagLayout());
		
		MyGridBagConstraints c = new MyGridBagConstraints();
		c.expandHorizontal();
		
		configPanel.add(new JLabel("Network:"), c);
		configPanel.add(this.selectNetwork, c.nextCol());
		
		JLabel keyColLabel = new JLabel("Network key column:");
		keyColLabel.setToolTipText(KEY_COL_TOOLTIP);
		configPanel.add(keyColLabel, c.nextRow());
		configPanel.add(this.selectKeyCol, c.nextCol());

		// This JLabel must be displayed in the top-left corner
		configPanel.add(new JLabel("Columns to import:"), c.nextRow().setAnchor("NW"));
		// reset constraint
		c.setAnchor("C");
		configPanel.add(this.columnsSelector, c.nextCol());
		
		configPanel.add(new JLabel("New table name:"), c.nextRow());
		configPanel.add(this.tableName, c.nextCol());
		
		JLabel valuesLabel = new JLabel("Values column name:");
		valuesLabel.setToolTipText(VALUES_NAME_TOOLTIP);
		configPanel.add(valuesLabel, c.nextRow());
		configPanel.add(this.valuesName, c.nextCol());

		JLabel srcLabel = new JLabel("Sources column name:");
		srcLabel.setToolTipText(SRC_NAME_TOOLTIP);
		configPanel.add(srcLabel, c.nextRow());
		configPanel.add(this.srcName, c.nextCol());
		
		configPanel.add(this.includeNamespaces, c.nextRow().nextCol());
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(this.cancelButton);
		buttonPanel.add(this.createButton);
		
		mainPanel.add(configPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		this.setContentPane(mainPanel);
		this.pack();
		this.setLocationRelativeTo(this.ovManager.getService(CySwingApplication.class).getJFrame());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.selectNetwork) {
			CyNetwork selectedNetwork = (CyNetwork) this.selectNetwork.getSelectedItem();
			Collection<CyColumn> nodeColumns = selectedNetwork.getDefaultNodeTable().getColumns();
			
			this.selectKeyCol.removeAllItems();
			CyColumn colSUID = null;
			for(CyColumn col : nodeColumns) {
				// We don't want to use SUID
				if(!col.getName().equals(CyNetwork.SUID)) {
					this.selectKeyCol.addItem(col.getName());
				} else {
					colSUID = col;
				}
			}
			
			// We get rid of the SUID column
			if(colSUID != null) {
				nodeColumns.remove(colSUID);
			}
			
			this.columnsSelector.update(nodeColumns, null);
			
			this.tableName.setText(selectedNetwork.toString()+" node table");
		} else if(e.getSource() == this.createButton) {
			CyNetwork selectedNetwork = (CyNetwork) this.selectNetwork.getSelectedItem();
			CyTable nodeTable = selectedNetwork.getDefaultNodeTable();
			
			// We check that the columns have the same type
			List<String> selectedColumns = new ArrayList<>();
			Class<?> colsType = null;
			boolean sameType = true;
			for(String colName : this.columnsSelector.getSelectedColumnNames()) {
				CyColumn col = nodeTable.getColumn(colName);
				selectedColumns.add(colName);
				
				if(colsType == null) {
					colsType = col.getType();
				} else if(!colsType.equals(col.getType())) {
					sameType = false;
				}
			}
			
			if(!sameType) {
				JOptionPane.showMessageDialog(this,
						"The selected columns must have the same type.",
						"Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			// Selected columns must not be empty
			if(selectedColumns.isEmpty()) {
				JOptionPane.showMessageDialog(this,
						"You must select at least one column to import.",
						"Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			// We check the table name
			final CyTableManager tableMgr = this.ovManager.getService(CyTableManager.class);

			for (CyTable table : tableMgr.getGlobalTables()) {
					if (table.getTitle().equals(tableName.getText())) {
						JOptionPane.showMessageDialog(this,
								"The table name already exists. Please select another name.",
								"Error",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
			}
			
			CreateOVTableFromNetworkTaskFactory factory = new CreateOVTableFromNetworkTaskFactory(ovManager,
					selectedNetwork,
					(String) selectKeyCol.getSelectedItem(),
					selectedColumns,
					tableName.getText(),
					valuesName.getText(),
					srcName.getText(),
					this.includeNamespaces.isSelected());
			this.ovManager.executeTask(factory.createTaskIterator());
			this.setVisible(false);
		} else if(e.getSource() == this.cancelButton) {
			this.setVisible(false);
		}
	}

}
