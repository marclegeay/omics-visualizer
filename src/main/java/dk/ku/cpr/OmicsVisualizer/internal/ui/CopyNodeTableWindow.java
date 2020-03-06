package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

public class CopyNodeTableWindow extends OVWindow implements ActionListener {
	private static final long serialVersionUID = -8874802023098233849L;
	private static final String KEY_COL_TOOLTIP = "Column used from the node table to identify rows in the new table. This column will be used to link the new table with the network.";
	
	private JComboBox<CyNetwork> selectNetwork;
	private JComboBox<String> selectKeyCol;
	private SelectAndOrderColumnPanel columnsSelector;
	
	private JButton createButton;
	private JButton cancelButton;

	public CopyNodeTableWindow(OVManager ovManager) {
		super(ovManager, "Import from node table");
		this.setResizable(true);
		
		init();
		draw();
	}
	
	private void init() {
		this.selectNetwork = new JComboBox<>();
		this.selectNetwork.addActionListener(this);
		
		this.selectKeyCol = new JComboBox<>();
		this.selectKeyCol.setToolTipText(KEY_COL_TOOLTIP);
		
		this.columnsSelector = new SelectAndOrderColumnPanel(this);
		
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
	
	public void draw() {
		// If we re-draw the window, it should not be smaller than before
		this.setMinimumSize(new Dimension(this.getWidth(), this.getHeight()));
		
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

//		configPanel.add(new JLabel("Columns to import:"), c.nextRow());
		configPanel.add(this.columnsSelector, c.nextRow().useNCols(2).expandBoth().setInsets(0, 0, 0, 0));
		c.useNCols(1).expandHorizontal().setInsets(MyGridBagConstraints.DEFAULT_INSET, MyGridBagConstraints.DEFAULT_INSET, MyGridBagConstraints.DEFAULT_INSET, MyGridBagConstraints.DEFAULT_INSET);
		
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
	
	private void guessTableName(String original) {
		String name = original;
		
		int n=1;
		boolean found=false;
		CyTableManager tableManager = this.ovManager.getTableManager();
		for(CyTable table : tableManager.getAllTables(true)) {
			if(table.getTitle().equals(name)) {
				found = true;
			}
		}
		while(found) {
			name = original + " (" + (n++) + ")";
			found = false;
			for(CyTable table : tableManager.getAllTables(true)) {
				if(table.getTitle().equals(name)) {
					found = true;
				}
			}
		}
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
			
			this.columnsSelector.update(nodeColumns);
			
			guessTableName(selectedNetwork.toString()+" node table");
			
			// After updating the columns selector, we pack and center the window again
			this.pack();
			this.setLocationRelativeTo(this.ovManager.getService(CySwingApplication.class).getJFrame());
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
			
//			CreateOVTableFromNetworkTaskFactory factory = new CreateOVTableFromNetworkTaskFactory(ovManager,
//					selectedNetwork,
//					(String) selectKeyCol.getSelectedItem(),
//					selectedColumns);
//			this.ovManager.executeTask(factory.createTaskIterator());
			this.setVisible(false);
		} else if(e.getSource() == this.cancelButton) {
			this.setVisible(false);
		}
	}

}
