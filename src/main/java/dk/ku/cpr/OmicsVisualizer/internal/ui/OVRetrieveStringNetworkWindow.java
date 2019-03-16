package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;
import dk.ku.cpr.OmicsVisualizer.internal.task.StringCommandTaskFactory;

public class OVRetrieveStringNetworkWindow extends JFrame implements TaskObserver, ActionListener, CaretListener {
	private static final long serialVersionUID = 8015437684470645491L;

	private OVManager ovManager;
	private OVTable ovTable;

	private List<OVSpecies> speciesList;

	private JTextField querySpecies;
	private JComboBox<OVSpecies> selectSpecies;
	private JComboBox<String> selectQuery;
	private JCheckBox filteredOnly;

	private JButton retrieveButton;

	private CyNetwork retrievedNetwork;

	public OVRetrieveStringNetworkWindow(OVManager ovManager, OVTable ovTable) {
		super("Retrieve a STRING Network");
		this.ovManager=ovManager;
		this.ovTable=ovTable;

		this.querySpecies = new JTextField();
		this.querySpecies.setToolTipText("You can type here the name of a species to search it quickly in the dropdown list below.");
		this.querySpecies.addCaretListener(this);

		this.speciesList = new ArrayList<>();
		this.selectSpecies = new JComboBox<>();

		this.selectQuery = new JComboBox<>();
		for(String colName : this.ovTable.getColNames()) {
			if(!OVShared.isOVCol(colName)) {
				this.selectQuery.addItem(colName);
			}
		}

		this.filteredOnly = new JCheckBox("Only filtered rows", true);

		this.retrieveButton = new JButton("Retrieve network");
		this.retrieveButton.addActionListener(this);

		StringCommandTaskFactory factory = new StringCommandTaskFactory(this.ovManager, OVShared.STRING_CMD_LIST_SPECIES, null, this);
		TaskIterator ti = factory.createTaskIterator();
		this.ovManager.executeSynchronousTask(ti);


		// We make sure that the JFrame is always on top, only when Cytoscape is on top
		JFrame me = this;
		if(this.ovManager.getOVCytoPanel().getTopLevelAncestor() instanceof JFrame) {
			JFrame ancestor = (JFrame)this.ovManager.getOVCytoPanel().getTopLevelAncestor();

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

		this.init();
	}

	public void init() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		//		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		//		mainPanel.setBorder(LookAndFeelUtil.createPanelBorder());

		JPanel selectPanel = new JPanel();
		selectPanel.setLayout(new GridBagLayout());
		selectPanel.setBorder(LookAndFeelUtil.createPanelBorder());
		MyGridBagConstraints c = new MyGridBagConstraints();
		c.expandHorizontal();

		selectPanel.add(new JLabel("Species:"), c);
		//		selectPanel.add(this.querySpecies, c.nextCol());
		//
		//		selectPanel.add(this.selectSpecies, c.nextRow().useNCols(2));
		//		c.useNCols(1);
		selectPanel.add(this.selectSpecies, c.nextCol());

		selectPanel.add(new JLabel("Protein identifier column:"), c.nextRow());
		selectPanel.add(this.selectQuery, c.nextCol());

		// TODO Version 1.0: Without filters
		//		selectPanel.add(this.filteredOnly, c.nextRow().useNCols(2));
		//		c.useNCols(1);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(this.retrieveButton);

		mainPanel.add(selectPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		this.setContentPane(mainPanel);

		this.pack();
		this.setLocationRelativeTo(this.ovManager.getOVCytoPanel().getTopLevelAncestor());
		this.setResizable(false);
		
		JComboBoxDecorator.originalDimension = this.selectSpecies.getPreferredSize();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void taskFinished(ObservableTask task) {
		if(task.getClass().getSimpleName().equals("GetSpeciesTask")) {
			List<Map<String,String>> res = task.getResults(List.class);

			for(Map<String,String> r : res) {
				OVSpecies species = new OVSpecies(r);
				this.selectSpecies.addItem(species);
				this.speciesList.add(species);

				//We select Human as default
				if(species.abbreviatedName.equals("Homo sapiens")) {
					this.selectSpecies.setSelectedItem(species);
				}
			}

			JComboBoxDecorator.decorate(this.selectSpecies);
		} else if(task.getClass().getSimpleName().equals("ProteinQueryTask")) {
			this.retrievedNetwork = task.getResults(CyNetwork.class);
		}
	}

	@Override
	public void allFinished(FinishStatus finishStatus) {
		// We connect the table we the imported String Network with the "query term" column
		// This column is a special column that contains the matching query term
		this.ovTable.connect(this.retrievedNetwork.toString(), "query term", (String)this.selectQuery.getSelectedItem());
		this.ovManager.getOVCytoPanel().update();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.retrieveButton) {
			// We check if the species is OK
			Object selectedSpecies = this.selectSpecies.getSelectedItem();
			if(!(selectedSpecies instanceof OVSpecies)) {
				JOptionPane.showMessageDialog(this, "Error: Unknown species \""+selectedSpecies+"\".", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			// We identify the query column
			String queryCol = (String) this.selectQuery.getSelectedItem();
			Class<?> colType = this.ovTable.getColType(queryCol);

			// We retrieve the list for the query
			Set<String> queryTerms = new HashSet<>();
			for(CyRow row : this.ovTable.getCyTable().getAllRows()) {
				if(!this.filteredOnly.isSelected() || this.ovTable.isFiltered(row)) {
					queryTerms.add(row.get(queryCol, colType).toString());
				}
			}

			// We set the arguments for the STRING command
			String query = String.join(",", queryTerms);
			Integer taxonID = ((OVSpecies) this.selectSpecies.getSelectedItem()).getTaxonID();
			Map<String, Object> args = new HashMap<>();
			args.put("query", query);
			args.put("taxonID", taxonID);
			args.put("limit", "0");

			// We call the STRING command
			StringCommandTaskFactory factory = new StringCommandTaskFactory(this.ovManager, OVShared.STRING_CMD_PROTEIN_QUERY, args, this);
			TaskIterator ti = factory.createTaskIterator();
			this.ovManager.executeTask(ti, this);

			// The task is executed in background, we don't want the window to be displayed
			this.setVisible(false);
		}
	}

	@Override
	public void caretUpdate(CaretEvent e) {
		if(e.getSource() == this.querySpecies) {
			OVSpecies selectedSpecies = (OVSpecies) this.selectSpecies.getSelectedItem();
			this.selectSpecies.removeAllItems();
			for(OVSpecies spe : this.speciesList) {
				if(spe.getQueryString().contains(this.querySpecies.getText().toLowerCase())) {
					this.selectSpecies.addItem(spe);
				}
			}
			// We always display at leat one species
			if(this.selectSpecies.getItemCount() == 0) {
				this.selectSpecies.addItem(selectedSpecies);
			}
			this.selectSpecies.setSelectedItem(selectedSpecies);
		}
	}

	private class OVSpecies {
		private Integer taxonID;
		private String abbreviatedName;
		private String scientificName;

		public OVSpecies(Map<String,String> data) {
			super();
			this.taxonID = Integer.valueOf(data.get("taxonomyId"));
			this.abbreviatedName = data.get("scientificName"); // TODO: Bug ? The scientificName is the shortest one
			this.scientificName =  data.get("abbreviatedName"); // TODO: Bug ? The abbreviatedName looks like a scientific Name
		}

		public Integer getTaxonID() {
			return this.taxonID;
		}

		public String getQueryString() {
			return (this.abbreviatedName + " " + this.scientificName).toLowerCase();
		}

		public String toString() {
			return this.scientificName;
		}
	}

	/**
	 * Makes the species combo box searchable.
	 */
	private static class JComboBoxDecorator {

		public static List<OVSpecies> previousEntries = new ArrayList<>();
		public static String previousText = "";
		public static Dimension originalDimension;

		public static void decorate(final JComboBox<OVSpecies> jcb) {
			List<OVSpecies> entries = new ArrayList<>();
			for (int i = 0; i < jcb.getItemCount(); i++) {
				entries.add(jcb.getItemAt(i));
			}
			decorate(jcb, true, entries);
			previousText = jcb.getSelectedItem().toString();
		}

		public static void decorate(final JComboBox<OVSpecies> jcb, boolean editable,
				final List<OVSpecies> entries) {
			
			OVSpecies selectedSpecies = (OVSpecies)jcb.getSelectedItem();
			// System.out.println("JComboBoxDecorator: selectedItem = "+selectedSpecies);
			jcb.setEditable(editable);

			final JTextComponent textComponent = (JTextComponent)jcb.getEditor().getEditorComponent();
			// textField.setText(selectedSpecies.getName());
			jcb.setSelectedItem(selectedSpecies);
			
			originalDimension = jcb.getPreferredSize();

			textComponent.addCaretListener(new CaretListener() {
				@Override
				public void caretUpdate(CaretEvent e) {
					if(e.getMark() == e.getDot()) { // It means that it is not a selection
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								int currentCaretPosition=textComponent.getCaretPosition();
								comboFilter(textComponent.getText(), jcb, entries);
								textComponent.setCaretPosition(currentCaretPosition);
								jcb.setPreferredSize(originalDimension);
							}
						});
					}
				}
			});
			//			textField.addKeyListener(new KeyAdapter() {
			//				public void keyReleased(KeyEvent e) {
			//					SwingUtilities.invokeLater(new Runnable() {
			//					 	public void run() {
			//							int currentCaretPosition=textField.getCaretPosition();
			//							comboFilter(textField.getText(), jcb, entries);
			//							textField.setCaretPosition(currentCaretPosition);
			//					 	}
			//					});
			//				}
			//			});

			textComponent.addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent e) {
					if(!previousEntries.isEmpty()) {
						jcb.setSelectedItem(previousEntries.get(0));
					}
				}
				@Override
				public void focusGained(FocusEvent e) {
					// Do nothing
				}
			});
		}

		/**
		 * Create a list of entries that match the user's entered text.
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private static void comboFilter(String enteredText, JComboBox<OVSpecies> jcb, List<OVSpecies> entries) {
			List<OVSpecies> entriesFiltered = new ArrayList<>();
			boolean changed = true;

			if (enteredText == null) {
				return;
			}

			if(previousText.equals(enteredText)) {
				return;
			}
			previousText = enteredText;

			for (OVSpecies entry : entries) {
				if (entry.getQueryString().toLowerCase().contains(enteredText.toLowerCase())) {
					entriesFiltered.add(entry);
					// System.out.println(jcbModel.getIndexOf(entry));
				}
			}

			if (previousEntries.size() == entriesFiltered.size()
					&& previousEntries.containsAll(entriesFiltered)) {
				changed = false;
			}

			if (changed && entriesFiltered.size() > 1) {
				previousEntries = entriesFiltered;
				jcb.setModel(new DefaultComboBoxModel(entriesFiltered.toArray()));
				jcb.setSelectedItem(enteredText);
				jcb.showPopup();
			} else if (entriesFiltered.size() == 1) {
				if (entriesFiltered.get(0).toString().equalsIgnoreCase(enteredText)) {
					previousEntries = new ArrayList<>();
					jcb.setSelectedItem(entriesFiltered.get(0));
					jcb.hidePopup();
				} else {
					previousEntries = entriesFiltered;
					jcb.setModel(new DefaultComboBoxModel(entriesFiltered.toArray()));
					jcb.setSelectedItem(enteredText);
					jcb.showPopup();
				}
			} else if (entriesFiltered.size() == 0) {
				previousEntries = new ArrayList<>();
				jcb.hidePopup();
			}
		}

	}
}
