package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;
import dk.ku.cpr.OmicsVisualizer.internal.task.StringCommandTaskFactory;

public class OVRetrieveStringNetworkWindow extends JFrame implements TaskObserver, ActionListener {
	private static final long serialVersionUID = 8015437684470645491L;

	private OVManager ovManager;
	private OVConnectWindow ovConnectWindow;
	private OVTable ovTable;

	private JComboBox<OVSpecies> selectSpecies;
	private JComboBox<String> selectQuery;

	private JButton retrieveButton;
	
	private CyNetwork retrievedNetwork;

	public OVRetrieveStringNetworkWindow(OVManager ovManager, OVConnectWindow ovConnectWindow, OVTable ovTable) {
		super("Retrieve a STRING Network");
		this.ovManager=ovManager;
		this.ovConnectWindow=ovConnectWindow;
		this.ovTable=ovTable;

		this.selectSpecies = new JComboBox<>();

		this.selectQuery = new JComboBox<>();
		for(String colName : this.ovTable.getColNames()) {
			if(!OVShared.isOVCol(colName)) {
				this.selectQuery.addItem(colName);
			}
		}

		this.retrieveButton = new JButton("Retrieve the network");
		this.retrieveButton.addActionListener(this);

		StringCommandTaskFactory factory = new StringCommandTaskFactory(this.ovManager, OVShared.STRING_CMD_LIST_SPECIES, null, this);
		TaskIterator ti = factory.createTaskIterator();
		this.ovManager.executeSynchronousTask(ti);

		this.init();
	}

	public void init() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel selectPanel = new JPanel();
		selectPanel.setLayout(new GridBagLayout());
		MyGridBagConstraints c = new MyGridBagConstraints();
		c.expandHorizontal();

		selectPanel.add(new JLabel("Species:"), c);
		selectPanel.add(this.selectSpecies, c.nextCol());

		selectPanel.add(new JLabel("Protein names column:"), c.nextRow());
		selectPanel.add(this.selectQuery, c.nextCol());

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(this.retrieveButton);

		mainPanel.add(selectPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		this.setContentPane(mainPanel);

		this.pack();
		this.setLocationRelativeTo(ovConnectWindow);
		this.setResizable(false);
		this.setAlwaysOnTop(true);
	}
	
	@Override
	public void setVisible(boolean b) {
		this.ovConnectWindow.setEnabled(!b);
		super.setVisible(b);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void taskFinished(ObservableTask task) {
		if(task.getClass().getSimpleName().equals("GetSpeciesTask")) {
			List<Map<String,String>> res = task.getResults(List.class);

			for(Map<String,String> r : res) {
				OVSpecies species = new OVSpecies(r);
				this.selectSpecies.addItem(species);
				
				//We select Human as default
				if(species.abbreviatedName.equals("Homo sapiens")) {
					this.selectSpecies.setSelectedItem(species);
				}
			}
		} else if(task.getClass().getSimpleName().equals("ProteinQueryTask")) {
			this.retrievedNetwork = task.getResults(CyNetwork.class);
		}
	}

	@Override
	public void allFinished(FinishStatus finishStatus) {
		try { // We try to wait that the TaskMonitor window closes
			Thread.sleep(1500);
		} catch(Exception e) {
			// Do nothing
		}
		this.ovConnectWindow.update(this.ovTable);
		this.ovConnectWindow.setStringNetwork(this.retrievedNetwork.toString(), (String)this.selectQuery.getSelectedItem());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.retrieveButton) {
			// We identify the query column
			String queryCol = (String) this.selectQuery.getSelectedItem();
			Class<?> colType = this.ovTable.getColType(queryCol);
			
			// We retrieve the list for the query
			Set<String> queryTerms = new HashSet<>();
			for(CyRow row : this.ovTable.getCyTable().getAllRows()) {
				queryTerms.add(row.get(queryCol, colType).toString());
			}
			
			// We set the arguments for the STRING command
			String query = String.join(",", queryTerms);
			Integer taxonID = ((OVSpecies) this.selectSpecies.getSelectedItem()).getTaxonID();
			Map<String, Object> args = new HashMap<>();
			args.put("query", query);
			args.put("taxonID", taxonID);
			
			// We call the STRING command
			StringCommandTaskFactory factory = new StringCommandTaskFactory(this.ovManager, OVShared.STRING_CMD_PROTEIN_QUERY, args, this);
			TaskIterator ti = factory.createTaskIterator();
			this.ovManager.executeTask(ti, this);
			
			// The task is executed in background, we don't want the window to be displayed
			this.setVisible(false);
			// We also hide the OVConnectWindow while the process is running
			this.ovConnectWindow.setVisible(false);
		}
	}

	private class OVSpecies {
		private Integer taxonID;
		private String abbreviatedName;
		private String scientificName;

		public OVSpecies(Map<String,String> data) {
			super();
			this.taxonID = Integer.valueOf(data.get("taxonomyId"));
			this.abbreviatedName = data.get("abbreviatedName");
			this.scientificName =  data.get("scientificName");
		}

		public Integer getTaxonID() {
			return this.taxonID;
		}

		public String toString() {
			return this.abbreviatedName + " [" + this.scientificName + "]";
		}
	}
}
