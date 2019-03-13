package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.cytoscape.model.CyColumn;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVConnection;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;

public class OVConnectPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1195112078817426334L;

	private OVConnectWindow connectWindow;
	
	private OVConnection con;
	
	private JPanel mainPanel;
	
	private JComboBox<String> selectColNetwork;
	private JComboBox<String> selectColTable;
	
	private JButton updateButton;
	private JButton disconnectButton;

	public OVConnectPanel(OVConnectWindow connectWindow, OVConnection con) {
		super();
		this.connectWindow = connectWindow;
		this.con=con;
		
		// GUI initialization
		
		this.mainPanel = new JPanel();
		this.mainPanel.setBorder(BorderFactory.createTitledBorder(this.con.getCollectionNetworkName()));
		this.mainPanel.setLayout(new BorderLayout());
		
		JPanel selectPanel = new JPanel();
		selectPanel.setLayout(new GridBagLayout());
		
		MyGridBagConstraints c = new MyGridBagConstraints();
		c.expandHorizontal();
	
		this.selectColNetwork = new JComboBox<String>();
		this.selectColNetwork.addActionListener(this);
		selectPanel.add(new JLabel("Select key column from Network:"), c);
		selectPanel.add(this.selectColNetwork, c.nextCol());

		this.selectColTable = new JComboBox<String>();
		this.selectColTable.addActionListener(this);
		selectPanel.add(new JLabel("Select key column from Table:"), c.nextRow());
		selectPanel.add(this.selectColTable, c.nextCol());
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		
		this.updateButton = new JButton("Update");
		this.updateButton.addActionListener(this);
		this.disconnectButton = new JButton("Disconnect");
		this.disconnectButton.setForeground(Color.RED);
		this.disconnectButton.addActionListener(this);
		
		buttonPanel.add(this.updateButton);
		buttonPanel.add(this.disconnectButton);
		
		this.mainPanel.add(selectPanel, BorderLayout.CENTER);
		this.mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		this.setLayout(new BorderLayout());
		this.add(this.mainPanel, BorderLayout.CENTER);
		
		
		update();
	}
	
	public void update() {
		if(this.con.getRootNetwork() == null) {
			this.setVisible(false);
			return;
		}

		this.mainPanel.setBorder(BorderFactory.createTitledBorder(this.con.getCollectionNetworkName()));
		
		this.selectColNetwork.removeAllItems();
		for(CyColumn col : this.con.getBaseNetwork().getDefaultNodeTable().getColumns()) {
			this.selectColNetwork.addItem(col.getName());
		}
		this.selectColNetwork.setSelectedItem(this.con.getMappingColCyto());
		
		this.selectColTable.removeAllItems();
		for(String col : this.con.getOVTable().getColNames()) {
			if(!OVShared.isOVCol(col)) {
				this.selectColTable.addItem(col);
			}
		}
		this.selectColTable.setSelectedItem(this.con.getMappingColOVTable());
		
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.updateButton) {
			this.con.update(
					(String) this.selectColNetwork.getSelectedItem(),
					(String) this.selectColTable.getSelectedItem()
			);
		} else if(e.getSource() == this.disconnectButton) {
			int response = JOptionPane.showConfirmDialog(null,
					"You are disconnecting \""+this.con.getOVTable().getTitle()+"\" and \""+this.con.getCollectionNetworkName()+"\".",
					"Confirmation",
					JOptionPane.OK_CANCEL_OPTION);
			
			if(response == JOptionPane.OK_OPTION) {
				this.con.disconnect();
				this.connectWindow.update(this.con.getOVTable());
			}
		}
	}
}
