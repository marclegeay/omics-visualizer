package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskObserver;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVConnection;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;
import dk.ku.cpr.OmicsVisualizer.internal.task.ConnectTaskFactory;

public class OVConnectWindow extends OVWindow implements ActionListener, TaskObserver {
	private static final long serialVersionUID = -5328093228061621675L;

	private static CyNetwork CHOOSE = null;

	private OVCytoPanel cytoPanel;
	private OVTable ovTable;

	private CyNetworkManager netManager;
	private CyRootNetworkManager rootNetManager;

	private JComboBox<CyNetwork> selectNetwork;
	private JComboBox<String> selectColNetwork;
	private JComboBox<String> selectColTable;
	private JButton connectButton;

	private JButton closeButton;

	public OVConnectWindow(OVManager ovManager) {
		super(ovManager);
		
		// We init the CHOOSE Network
		CHOOSE = this.ovManager.getService(CyNetworkFactory.class).createNetworkWithPrivateTables(SavePolicy.DO_NOT_SAVE);
		CHOOSE.getDefaultNetworkTable().getRow(CHOOSE.getSUID()).set(CyNetwork.NAME, "--- Choose ---");

		this.cytoPanel=ovManager.getOVCytoPanel();

		this.netManager = this.ovManager.getNetworkManager();
		this.rootNetManager = this.ovManager.getService(CyRootNetworkManager.class);

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
		
		LookAndFeelUtil.equalizeSize(this.connectButton, this.closeButton);
	}

	public void update(OVTable ovTable) {
		this.ovTable=ovTable;
		this.setTitle("Connect " + ovTable.getTitle());

		this.setPreferredSize(null); // We want to recompute the size each time

		// We don't want to trigger event when we make the list of networks
		this.selectNetwork.removeActionListener(this);
		this.selectNetwork.removeAllItems();
		this.selectNetwork.addItem(CHOOSE);
		
		Set<CyNetwork> rootNets = new HashSet<>();
		for(CyNetwork net : this.netManager.getNetworkSet()) {
			if(rootNets.add(this.rootNetManager.getRootNetwork(net))) {
				this.selectNetwork.addItem(this.rootNetManager.getRootNetwork(net));
			}
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
//		mainPanel.setBorder(LookAndFeelUtil.createPanelBorder());

		JPanel addNetworkPanel = new JPanel();
		addNetworkPanel.setLayout(new BorderLayout());

		JPanel addPanel = new JPanel();
		addPanel.setOpaque(!LookAndFeelUtil.isAquaLAF());
		addPanel.setLayout(new GridBagLayout());

		MyGridBagConstraints c = new MyGridBagConstraints();
		c.expandHorizontal();

		addPanel.add(new JLabel("Select Network Collection:"), c);
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
		addButtonPanel.setOpaque(!LookAndFeelUtil.isAquaLAF());
		addButtonPanel.setLayout(new FlowLayout());
		addButtonPanel.add(this.connectButton);

//		JLabel addLabel = new JLabel("Add a new connection");
//		Font fontLabel = new Font("Title Font", addLabel.getFont().getStyle(), (int) (addLabel.getFont().getSize()*1.3));
//		addLabel.setFont(fontLabel);

//		addNetworkPanel.add(addLabel, BorderLayout.NORTH);
		addNetworkPanel.add(addPanel, BorderLayout.CENTER);
		addNetworkPanel.add(addButtonPanel, BorderLayout.SOUTH);
		
		addNetworkPanel.setBorder(LookAndFeelUtil.createTitledBorder("Add a new connection"));

		JPanel listNetworkPanel = new JPanel();
		listNetworkPanel.setLayout(new BorderLayout());
//		JLabel linkLabel = new JLabel("Connected Network Collections:");
//		linkLabel.setFont(fontLabel);
//		listNetworkPanel.add(linkLabel, BorderLayout.NORTH);
		listNetworkPanel.setBorder(LookAndFeelUtil.createTitledBorder("Connected Network Collections"));

		int nbCons = this.ovTable.getConnections().size();
		if(nbCons > 0) {
			JPanel listPanel = new JPanel();
			listPanel.setOpaque(!LookAndFeelUtil.isAquaLAF());
			listPanel.setLayout(new GridLayout(nbCons, 1));
			for(OVConnection con : this.ovTable.getConnections()) {
				OVConnectPanel conPanel = new OVConnectPanel(this, con);

				listPanel.add(conPanel);
			}
			JScrollPane scrollList = new JScrollPane(listPanel);
			scrollList.setBorder(null);
			scrollList.setOpaque(!LookAndFeelUtil.isAquaLAF());
			scrollList.getViewport().setOpaque(!LookAndFeelUtil.isAquaLAF());

			listNetworkPanel.add(scrollList, BorderLayout.CENTER);
		}

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(this.closeButton);

		mainPanel.add(addNetworkPanel, BorderLayout.NORTH);
		if(nbCons > 0) {
			mainPanel.add(listNetworkPanel, BorderLayout.CENTER);
		}
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
	public void setVisible(boolean b) {
		if(b) {
			this.update(this.cytoPanel.getDisplayedTable());
		}
		
		super.setVisible(b);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.selectNetwork) {
			CyNetwork selectedNetwork = (CyNetwork) this.selectNetwork.getSelectedItem();
			if(selectedNetwork != null && !selectedNetwork.equals(CHOOSE)) {
				CyRootNetworkManager rootNetManager = this.ovManager.getService(CyRootNetworkManager.class);
				CyRootNetwork rootNet=rootNetManager.getRootNetwork(selectedNetwork);
	
				OVConnection ovCon = this.ovManager.getConnection(rootNet);
				if(ovCon != null) {
					JOptionPane.showMessageDialog(this,
							"This network collection is already connected to \""+ovCon.getOVTable().getTitle()+"\".",
							"Warning",
							JOptionPane.WARNING_MESSAGE);
				}
	
				if(rootNet == null) {
					this.selectColNetwork.setEnabled(false);
					this.selectColTable.setEnabled(false);
					this.connectButton.setEnabled(false);
				} else {
					this.selectColNetwork.removeAllItems();
					for(CyColumn col : rootNet.getSharedNodeTable().getColumns()) {
						this.selectColNetwork.addItem(col.getName());
					}
	
					this.selectColNetwork.setEnabled(true);
					this.selectColTable.setEnabled(true);
					this.connectButton.setEnabled(true);
					
					if(rootNet.getSharedNodeTable().getColumn("query term") != null) {
						this.selectColNetwork.setSelectedItem("query term");
					}
				}
			} else {
				this.selectColNetwork.setEnabled(false);
				this.selectColTable.setEnabled(false);
				this.connectButton.setEnabled(false);
			}
		} else if (e.getSource() == this.connectButton && this.connectButton.isEnabled()) {
			CyNetwork selectedNetwork = (CyNetwork) this.selectNetwork.getSelectedItem();
			if(selectedNetwork == null) {
				return;
			}
			CyRootNetworkManager rootNetManager = this.ovManager.getService(CyRootNetworkManager.class);
			CyRootNetwork rootNet=rootNetManager.getRootNetwork(selectedNetwork);

			int response = JOptionPane.OK_OPTION;
			OVConnection ovCon = this.ovManager.getConnection(rootNet);
			if(ovCon != null) {
				response = JOptionPane.showConfirmDialog(this,
						"This network collection is already connected to \""+ovCon.getOVTable().getTitle()+"\". You will disconnect \""+ovCon.getOVTable().getTitle()+"\" if you continue.",
						"Disconnection warning",
						JOptionPane.OK_CANCEL_OPTION);

				if(response == JOptionPane.OK_OPTION) {
					ovCon.disconnect();
				}
			}

			if(response != JOptionPane.OK_OPTION) {
				return;
			}
			
			ConnectTaskFactory factory = new ConnectTaskFactory(this.ovManager,
					this.ovTable,
					selectedNetwork,
					(String) this.selectColNetwork.getSelectedItem(),
					(String) this.selectColTable.getSelectedItem()
					);
			this.ovManager.executeSynchronousTask(factory.createTaskIterator(), this);
		} else if(e.getSource() == this.closeButton) {
			this.setVisible(false);
		}
	}

	@Override
	public void taskFinished(ObservableTask task) {
		// end of ConnectTask
		
		OVConnection ovCon = task.getResults(OVConnection.class);

		if(ovCon.getNbConnectedTableRows() == 0) {
			JOptionPane.showMessageDialog(this, "Error: No table row is connected to the network.", "Error", JOptionPane.ERROR_MESSAGE);
			ovCon.disconnect();
			return;
		}
		//				else {
		//					int totalNbRows = this.ovTable.getAllRows(true).size();
		//					
		//					if((((double) ovCon.getNbConnectedTableRows())/totalNbRows) < OVConnection.MINIMUM_CONNECTED_ROWS) {
		//						JOptionPane.showMessageDialog(null, "Warning: Less than " + (int)(OVConnection.MINIMUM_CONNECTED_ROWS*100) + "% of the table rows are connected to the network.", "Warning", JOptionPane.WARNING_MESSAGE);
		//					}
		//				}
		
		this.update(this.ovTable);
		this.cytoPanel.update();
	}

	@Override
	public void allFinished(FinishStatus finishStatus) {
		// TODO Auto-generated method stub
		
	}
}
