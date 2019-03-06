package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.cytoscape.command.AvailableCommands;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVConnection;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;

public class OVConnectWindow extends JFrame implements ActionListener {
	private static final long serialVersionUID = -5328093228061621675L;

	private static final String CHOOSE ="--- Choose ---";
	private static final String STRING_NETWORK = "--- Get a STRING Network ---";

	private OVCytoPanel cytoPanel;
	private OVManager ovManager;
	private OVTable ovTable;

	private CyNetworkManager netManager;

	private JComboBox<String> selectNetwork;
	private JComboBox<String> selectColNetwork;
	private JComboBox<String> selectColTable;
	private JButton connectButton;

	private JButton closeButton;

	public OVConnectWindow(OVCytoPanel cytoPanel, OVManager ovManager) {
		super();

		this.cytoPanel=cytoPanel;
		this.ovManager=ovManager;

		this.netManager = this.ovManager.getNetworkManager();

		this.selectNetwork = new JComboBox<>();
		this.selectNetwork.addActionListener(this);

		this.selectColNetwork = new JComboBox<>();
		this.selectColNetwork.addActionListener(this);
		this.selectColNetwork.setEnabled(false);

		this.selectColTable = new JComboBox<>();
		this.selectColTable.addActionListener(this);
		this.selectColTable.setEnabled(false);

		this.connectButton = new JButton("Connect");
		this.connectButton.addActionListener(this);
		this.connectButton.setEnabled(false);

		this.closeButton = new JButton("Close");
		this.closeButton.addActionListener(this);

		this.setResizable(false);


		// We make sure that the JFrame is always on top, only when Cytoscape is on top
		JFrame me = this;
		if(this.cytoPanel.getTopLevelAncestor() instanceof JFrame) {
			JFrame ancestor = (JFrame)this.cytoPanel.getTopLevelAncestor();

			ancestor.addWindowListener(new WindowAdapter() {
				@Override
				public void windowDeactivated(WindowEvent e) {
					super.windowDeactivated(e);

					me.setAlwaysOnTop(false);
				}

				@Override
				public void windowActivated(WindowEvent e) {
					super.windowActivated(e);

					me.setAlwaysOnTop(true);
				}
				
				@Override
				public void windowGainedFocus(WindowEvent e) {
					super.windowGainedFocus(e);
					
					me.toFront();
				}
			});
		}
	}

	public void update(OVTable ovTable) {
		this.ovTable=ovTable;
		this.setTitle("Connect " + ovTable.getTitle());

		this.setPreferredSize(null); // We want to recompute the size each time

		// We don't want to trigger event when we make the list of networks
		this.selectNetwork.removeActionListener(this);
		this.selectNetwork.removeAllItems();
		this.selectNetwork.addItem(CHOOSE);
		this.selectNetwork.addItem(STRING_NETWORK);
		for(CyNetwork net : this.netManager.getNetworkSet()) {
			this.selectNetwork.addItem(net.toString());
		}
		// Now that the list is complete, we can re-add the event listener
		this.selectNetwork.addActionListener(this);

		this.selectColNetwork.removeAllItems();

		this.selectColTable.removeAllItems();
		for(String colName : this.ovTable.getColNames()) {
			if(!OVShared.isOVCol(colName)) {
				this.selectColTable.addItem(colName);
			}
		}

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel addNetworkPanel = new JPanel();
		addNetworkPanel.setLayout(new BorderLayout());

		JPanel addPanel = new JPanel();
		addPanel.setLayout(new GridBagLayout());

		MyGridBagConstraints c = new MyGridBagConstraints();
		c.expandHorizontal();

		addPanel.add(new JLabel("Select Network:"), c);
		addPanel.add(this.selectNetwork, c.nextCol());
		
		if(this.selectNetwork.getSelectedItem().equals(OVConnectWindow.CHOOSE)) {
			this.selectColNetwork.setEnabled(false);
			this.selectColTable.setEnabled(false);
			this.connectButton.setEnabled(false);
		}

		addPanel.add(new JLabel("Select key column from Network:"), c.nextRow());
		addPanel.add(this.selectColNetwork, c.nextCol());

		addPanel.add(new JLabel("Select key column from Table:"), c.nextRow());
		addPanel.add(this.selectColTable, c.nextCol());

		JPanel addButtonPanel = new JPanel();
		addButtonPanel.setLayout(new FlowLayout());
		addButtonPanel.add(this.connectButton);

		JLabel addLabel = new JLabel("Add a new connection");
		Font fontLabel = new Font("Title Font", addLabel.getFont().getStyle(), (int) (addLabel.getFont().getSize()*1.3));
		addLabel.setFont(fontLabel);

		addNetworkPanel.add(addLabel, BorderLayout.NORTH);
		addNetworkPanel.add(addPanel, BorderLayout.CENTER);
		addNetworkPanel.add(addButtonPanel, BorderLayout.SOUTH);

		JPanel listNetworkPanel = new JPanel();
		listNetworkPanel.setLayout(new BorderLayout());
		JLabel linkLabel = new JLabel("Connected Networks:");
		linkLabel.setFont(fontLabel);
		listNetworkPanel.add(linkLabel, BorderLayout.NORTH);

		int nbCons = this.ovTable.getConnections().size();
		if(nbCons == 0) {
			listNetworkPanel.add(new JLabel("None"), BorderLayout.CENTER);
		} else {
			JPanel listPanel = new JPanel();
			listPanel.setLayout(new GridLayout(nbCons, 1));
			for(OVConnection con : this.ovTable.getConnections()) {
				OVConnectPanel conPanel = new OVConnectPanel(this, con);

				listPanel.add(conPanel);
			}
			JScrollPane scrollList = new JScrollPane(listPanel);
			scrollList.setBorder(null);

			listNetworkPanel.add(scrollList, BorderLayout.CENTER);
		}

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(this.closeButton);

		mainPanel.add(addNetworkPanel, BorderLayout.NORTH);
		mainPanel.add(listNetworkPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		this.setContentPane(mainPanel);

		this.pack(); // We pack so that getWidth and getHeight are computed
		// Then we set the size limits ...
		int prefWidth = this.getWidth()+30; // +30 so that the vertical slide can fit
		int prefHeight = (int) (this.cytoPanel.getTopLevelAncestor().getHeight() * 0.8); // at most 80% of the Cytoscape window
		int curHeight = this.getHeight();
		prefHeight = (prefHeight < curHeight ? prefHeight : curHeight);
		this.setPreferredSize(new Dimension(prefWidth, prefHeight));

		this.pack(); // We recompute the size with the new preferences
		this.setLocationRelativeTo(this.cytoPanel.getTopLevelAncestor()); // We center the Frame according to the Cytoscape window
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.selectNetwork) {
			String netName = (String) this.selectNetwork.getSelectedItem();

			if(netName.equals(STRING_NETWORK)) {
				
				AvailableCommands availableCommands = (AvailableCommands) this.ovManager.getService(AvailableCommands.class);
				if (!availableCommands.getNamespaces().contains("string")) {
					JOptionPane.showMessageDialog(null,
							"You need to install stringApp from the App Manager or Cytoscape App Store.",
							"Dependency error", JOptionPane.ERROR_MESSAGE);
					
					this.selectNetwork.setSelectedItem(OVConnectWindow.CHOOSE);
					return;
				}
				
				OVRetrieveStringNetworkWindow retrieveString = new OVRetrieveStringNetworkWindow(this.ovManager, this, this.ovTable);
				retrieveString.setVisible(true);
				return;
			}

			CyNetwork net=null;
			for(CyNetwork cyNet : this.netManager.getNetworkSet()) {
				if(cyNet.toString().equals(netName)) {
					net=cyNet;
				}
			}

			OVConnection ovCon = this.ovManager.getConnection(net);
			if(ovCon != null) {
				JOptionPane.showMessageDialog(null,
						"This network is already connected to \""+ovCon.getOVTable().getTitle()+"\".",
						"Warning",
						JOptionPane.WARNING_MESSAGE);
			}

			if(net == null) {
				this.selectColNetwork.setEnabled(false);
				this.selectColTable.setEnabled(false);
				this.connectButton.setEnabled(false);
			} else {
				this.selectColNetwork.removeAllItems();
				for(CyColumn col : net.getDefaultNodeTable().getColumns()) {
					this.selectColNetwork.addItem(col.getName());
				}

				this.selectColNetwork.setEnabled(true);
				this.selectColTable.setEnabled(true);
				this.connectButton.setEnabled(true);
			}
		} else if (e.getSource() == this.connectButton && this.connectButton.isEnabled()) {
			String netName = (String) this.selectNetwork.getSelectedItem();

			int response = JOptionPane.OK_OPTION;
			OVConnection ovCon = this.ovManager.getConnection(netName);
			if(ovCon != null) {
				response = JOptionPane.showConfirmDialog(null,
						"This network is already connected to \""+ovCon.getOVTable().getTitle()+"\". You will disconnect this table if you continue.",
						"Disconnection warning",
						JOptionPane.OK_CANCEL_OPTION);

				if(response == JOptionPane.OK_OPTION) {
					ovCon.disconnect();
				}
			}

			if(response != JOptionPane.OK_OPTION) {
				return;
			}

			if(netName != null) {
				this.ovTable.connect(
						(String) this.selectNetwork.getSelectedItem(),
						(String) this.selectColNetwork.getSelectedItem(),
						(String) this.selectColTable.getSelectedItem()
						);
			}
			
			this.update(this.ovTable);
			this.cytoPanel.update();
		} else if(e.getSource() == this.closeButton) {
			this.setVisible(false);
		}
	}
}
