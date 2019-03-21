package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.cytoscape.model.CyColumn;
import org.cytoscape.util.swing.LookAndFeelUtil;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVConnection;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;

public class OVConnectPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1195112078817426334L;

	private OVConnectWindow connectWindow;
	
	private OVConnection ovCon;
	
//	private JPanel mainPanel;
	
	private JComboBox<String> selectColNetwork;
	private JComboBox<String> selectColTable;
	
	private JButton updateButton;
	private JButton disconnectButton;

	public OVConnectPanel(OVConnectWindow connectWindow, OVConnection con) {
		super();
		this.connectWindow = connectWindow;
		this.ovCon=con;
		
		// GUI initialization
		this.initBorder();
		
		this.setOpaque(!LookAndFeelUtil.isAquaLAF());
		this.setLayout(new BorderLayout());
		
		JPanel selectPanel = new JPanel();
		selectPanel.setOpaque(!LookAndFeelUtil.isAquaLAF());
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
		buttonPanel.setOpaque(!LookAndFeelUtil.isAquaLAF());
		buttonPanel.setLayout(new FlowLayout());
		
		this.updateButton = new JButton("Update");
		this.updateButton.addActionListener(this);
		this.disconnectButton = new JButton("Disconnect");
		this.disconnectButton.setForeground(Color.RED);
		this.disconnectButton.addActionListener(this);
		
		buttonPanel.add(this.updateButton);
		buttonPanel.add(this.disconnectButton);
		
//		this.mainPanel.add(selectPanel, BorderLayout.CENTER);
//		this.mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		this.add(selectPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);
		
//		this.setLayout(new BorderLayout());
//		this.add(this.mainPanel, BorderLayout.CENTER);
		
		update();
	}
	
	public void update() {
		if(this.ovCon.getRootNetwork() == null) {
			this.setVisible(false);
			return;
		}

		this.initBorder();
		
		this.selectColNetwork.removeAllItems();
		for(CyColumn col : this.ovCon.getBaseNetwork().getDefaultNodeTable().getColumns()) {
			this.selectColNetwork.addItem(col.getName());
		}
		this.selectColNetwork.setSelectedItem(this.ovCon.getMappingColCyto());
		
		this.selectColTable.removeAllItems();
		for(String col : this.ovCon.getOVTable().getColNames()) {
			if(!OVShared.isOVCol(col)) {
				this.selectColTable.addItem(col);
			}
		}
		this.selectColTable.setSelectedItem(this.ovCon.getMappingColOVTable());
		
		this.setVisible(true);
	}
	
	private void initBorder() {
		this.setBorder(LookAndFeelUtil.createTitledBorder("<html><b>"+this.ovCon.getCollectionNetworkName()+"</b></html>"));
		((TitledBorder)this.getBorder()).setTitleJustification(TitledBorder.CENTER);
		((TitledBorder)this.getBorder()).setTitleFont(getFont());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.updateButton) {
			String oldColNetwork = this.ovCon.getMappingColCyto();
			String oldColTable = this.ovCon.getMappingColOVTable();
			
			int nbLinks = this.ovCon.update(
					(String) this.selectColNetwork.getSelectedItem(),
					(String) this.selectColTable.getSelectedItem()
			);
			
			if(nbLinks == 0) {
				JOptionPane.showMessageDialog(null, "No row from the table is connected to the network.\nThe previous connection will be restored.",  "Error", JOptionPane.ERROR_MESSAGE);
				this.ovCon.update(oldColNetwork, oldColTable);
				this.selectColNetwork.setSelectedItem(oldColNetwork);
				this.selectColTable.setSelectedItem(oldColTable);
			} else {
				int totalNbRows = this.ovCon.getOVTable().getAllRows(true).size();
				
				if((((double) nbLinks)/totalNbRows) < OVConnection.MINIMUM_CONNECTED_ROWS) {
					JOptionPane.showMessageDialog(null, "Warning: Less than " + (int)(OVConnection.MINIMUM_CONNECTED_ROWS*100) + "% of the table rows are connected to the network.", "Warning", JOptionPane.WARNING_MESSAGE);
				}
			}
		} else if(e.getSource() == this.disconnectButton) {
			int response = JOptionPane.showConfirmDialog(null,
					"You are disconnecting \""+this.ovCon.getOVTable().getTitle()+"\" and \""+this.ovCon.getCollectionNetworkName()+"\".",
					"Confirmation",
					JOptionPane.OK_CANCEL_OPTION);
			
			if(response == JOptionPane.OK_OPTION) {
				this.ovCon.disconnect();
				this.connectWindow.update(this.ovCon.getOVTable());
			}
		}
	}
}
