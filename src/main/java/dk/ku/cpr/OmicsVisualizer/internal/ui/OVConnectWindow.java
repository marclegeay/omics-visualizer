package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;

public class OVConnectWindow extends JFrame implements ActionListener {
	private static final long serialVersionUID = -5328093228061621675L;
	
	private static final String NO_NETWORK = "--- NONE ---";
	
	private OVCytoPanel cytoPanel;
	private OVManager ovManager;
	private OVTable ovTable;
	
	private CyNetworkManager netManager;
	
	private JButton closeButton;
	
	private JComboBox<String> selectNetwork;
	private JComboBox<String> selectColNetwork;
	private JComboBox<String> selectColTable;
	
	public OVConnectWindow(OVCytoPanel cytoPanel, OVManager ovManager) {
		super();
		
		this.cytoPanel=cytoPanel;
		this.ovManager=ovManager;
		
		this.netManager = this.ovManager.getNetworkManager();
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(3, 2));

		this.selectNetwork = new JComboBox<String>();
		this.selectNetwork.addActionListener(this);
		mainPanel.add(new JLabel("Select Network:"));
		mainPanel.add(this.selectNetwork);

		this.selectColNetwork = new JComboBox<String>();
		this.selectColNetwork.addActionListener(this);
		mainPanel.add(new JLabel("Select key column from Network:"));
		mainPanel.add(this.selectColNetwork);

		this.selectColTable = new JComboBox<String>();
		this.selectColTable.addActionListener(this);
		mainPanel.add(new JLabel("Select key column from Table:"));
		mainPanel.add(this.selectColTable);
		
		JPanel buttonPanel = new JPanel();
		closeButton = new JButton("Connect");
		closeButton.addActionListener(this);
		buttonPanel.add(closeButton);
		
		this.setLayout(new BorderLayout());
		
		mainPanel.setBackground(this.getBackground());
		buttonPanel.setBackground(this.getBackground());
		
		this.add(mainPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);
	}
	
	public void update(OVTable ovTable) {
		this.ovTable=ovTable;
		
		this.selectColTable.removeAllItems();
		for(String col : this.ovTable.getColNames()) {
			if(!OVShared.isOVCol(col)) {
				this.selectColTable.addItem(col);
			}
		}
		
		// JComboBox has only one action "changed" :
		// if the user change the values it triggers the action (OK)
		// if we add or remove an item, it triggers the action (not wanted)
		// So we remove the listener before modifying the JComboBox, and put it back after
		this.selectNetwork.removeActionListener(this);
		this.selectNetwork.removeAllItems();
		this.selectNetwork.addItem(OVConnectWindow.NO_NETWORK);
		for(CyNetwork cyNet : this.netManager.getNetworkSet()) {
			this.selectNetwork.addItem(cyNet.toString()); // toString displays the name of the CyNetwork
		}
		this.selectNetwork.addActionListener(this);
		
		this.selectNetwork.setSelectedItem(this.ovTable.getLinkedNetworkName());
		this.selectColNetwork.setSelectedItem(this.ovTable.getMappingColCyto());
		this.selectColTable.setSelectedItem(this.ovTable.getMappingColOVTable());
		
		this.pack();
		this.setLocationRelativeTo(this.cytoPanel.getTopLevelAncestor());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.selectNetwork) {
			String netName = (String) this.selectNetwork.getSelectedItem();

			if(netName == null || netName.equals(OVConnectWindow.NO_NETWORK)) {
				this.selectColNetwork.setEnabled(false);
				this.selectColTable.setEnabled(false);
				return;
			} else {
				this.selectColNetwork.setEnabled(true);
				this.selectColTable.setEnabled(true);
			}
			
			CyNetwork net=null;
			for(CyNetwork cyNet : this.netManager.getNetworkSet()) {
				if(cyNet.toString().equals(netName)) {
					net=cyNet;
				}
			}
			
			if(net!= null) {
				this.selectColNetwork.removeAllItems();
				for(CyColumn col : net.getDefaultNodeTable().getColumns()) {
					this.selectColNetwork.addItem(col.getName());
				}
			}
		} else if(e.getSource() == this.closeButton) {
			String netName = (String) this.selectNetwork.getSelectedItem();
			
			if(netName == null || netName.equals(OVConnectWindow.NO_NETWORK)) {
				this.ovTable.disconnect();
			} else {
				this.ovTable.connect(
							(String) this.selectNetwork.getSelectedItem(),
							(String) this.selectColNetwork.getSelectedItem(),
							(String) this.selectColTable.getSelectedItem()
						);
			}
			this.setVisible(false);
			this.cytoPanel.update();
		}
	}
}
