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

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

public class CopyNodeTableWindow extends OVWindow implements ActionListener {
	private static final long serialVersionUID = -8874802023098233849L;
	private static final String KEY_COL_TOOLTIP = "Column used from the node table to identify rows in the new table. This column will be used to link the new table with the network.";
	
	private boolean ok;
	
	private JComboBox<CyNetwork> selectNetwork;
	private JComboBox<CyColumn> selectKeyCol;
	private SelectAndOrderColumnPanel columnsSelector;
	
	private JButton nextButton;
	private JButton cancelButton;

	public CopyNodeTableWindow(OVManager ovManager) {
		super(ovManager, "Import from node table");
		this.setResizable(true);
		
		this.ok = false;
		
		init();
		draw();
	}
	
	private void init() {
		this.selectNetwork = new JComboBox<>();
		this.selectNetwork.addActionListener(this);
		
		this.selectKeyCol = new JComboBox<>();
		this.selectKeyCol.setToolTipText(KEY_COL_TOOLTIP);
		this.selectKeyCol.setRenderer(new ColumnCellRenderer(this.ovManager));
		
		this.columnsSelector = new SelectAndOrderColumnPanel(this);
		
		this.nextButton = new JButton("Next >");
		this.nextButton.addActionListener(this);
		
		this.cancelButton = new JButton("Close");
		this.cancelButton.addActionListener(this);
		
		LookAndFeelUtil.equalizeSize(this.nextButton, this.cancelButton);
		
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
		buttonPanel.add(this.nextButton);
		
		mainPanel.add(configPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		this.setContentPane(mainPanel);
		this.pack();
		this.setLocationRelativeTo(this.ovManager.getService(CySwingApplication.class).getJFrame());
	}
	
	public boolean isOK() {
		return this.ok;
	}
	
	public CyNetwork getNetwork() {
		return (CyNetwork) this.selectNetwork.getSelectedItem();
	}
	
	public CyColumn getKeyColumn() {
		return (CyColumn) this.selectKeyCol.getSelectedItem();
	}
	
	public List<CyColumn> getColumnList() {
		return this.columnsSelector.getSelectedColumns();
	}
	
	public void selectCurrentNetwork() {
		// We pre-select the active network
		CyNetwork currentNetwork = this.ovManager.getService(CyApplicationManager.class).getCurrentNetwork();
		if(currentNetwork != null) {
			this.selectNetwork.setSelectedItem(currentNetwork);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.selectNetwork) {
			CyNetwork selectedNetwork = (CyNetwork) this.selectNetwork.getSelectedItem();
			Collection<CyColumn> availableColumns = new ArrayList<>();
			
			this.selectKeyCol.removeAllItems();
			for(CyColumn col : selectedNetwork.getDefaultNodeTable().getColumns()) {
				// We don't want to use SUID nor List columns
				if(!col.getName().equals(CyNetwork.SUID) && col.getType() != List.class) {
					availableColumns.add(col);
					// We can only connect to a virtual column
					if(col.getVirtualColumnInfo().isVirtual()) {
						this.selectKeyCol.addItem(col);
					}
				}
			}
			
			this.columnsSelector.update(availableColumns);
			
			// After updating the columns selector, we pack and center the window again
			this.pack();
			this.setLocationRelativeTo(this.ovManager.getService(CySwingApplication.class).getJFrame());
		} else if(e.getSource() == this.nextButton) {
			// We check that the columns have the same type
			List<CyColumn> selectedColumns = new ArrayList<>();
			Class<?> colsType = null;
			boolean sameType = true;
			for(CyColumn col : this.columnsSelector.getSelectedColumns()) {
				selectedColumns.add(col);
				
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
			
			this.ok = true;
			this.setVisible(false);
		} else if(e.getSource() == this.cancelButton) {
			this.ok = false;
			this.setVisible(false);
		}
	}

}
