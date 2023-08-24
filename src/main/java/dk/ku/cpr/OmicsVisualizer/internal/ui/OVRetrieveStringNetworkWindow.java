package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;

import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVSpecies;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;
import dk.ku.cpr.OmicsVisualizer.internal.task.RetrieveStringNetworkTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.task.StringCommandTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.model.NetworkType;

public class OVRetrieveStringNetworkWindow extends OVWindow implements TaskObserver, ActionListener {
	private static final long serialVersionUID = 8015437684470645491L;

	private static final int defaultConfidence = 40;
	
	private OVTable ovTable;

	private List<OVSpecies> speciesList;

	private JComboBox<OVSpecies> selectSpecies;
	private JComboBox<String> selectQuery;

	//private JComboBox<String> selectNetType;
	private JRadioButton physicalNetwork;
	private JRadioButton functionalNetwork;
	private NetworkType networkType = null;
	
	private JSlider confidenceSlider;
	private JTextField confidenceValue;
	private boolean ignore = false;
	NumberFormat formatter;
	
	private JCheckBox filteredOnly;
	private JCheckBox selectedOnly;

	private JButton closeButton;
	private JButton retrieveButton;

	public OVRetrieveStringNetworkWindow(OVManager ovManager) {
		super(ovManager, "Retrieve a STRING Network");
		this.ovTable=null;

		this.speciesList = new ArrayList<OVSpecies>();
		this.selectSpecies = new JComboBox<OVSpecies>(speciesList.toArray(new OVSpecies[1]));

		this.selectQuery = new JComboBox<>();
		
		// this.selectNetType = new JComboBox<>();
		this.functionalNetwork = new JRadioButton(NetworkType.FUNCTIONAL.toString(), true);
		this.physicalNetwork = new JRadioButton(NetworkType.PHYSICAL.toString(), false);
		this.networkType = NetworkType.FUNCTIONAL;
		
		this.confidenceSlider = new JSlider();
		this.confidenceValue = new JTextField();

		this.selectedOnly = new JCheckBox("Only selected rows", false);
		this.selectedOnly.addActionListener(this);
		this.filteredOnly = new JCheckBox("Only filtered rows", true);

		this.closeButton = new JButton("Close");
		this.closeButton.addActionListener(this);

		this.retrieveButton = new JButton("Retrieve network");
		this.retrieveButton.addActionListener(this);

		// After fixing stringApp bug, this is not needed anymore
		//Map<String, Object> args = new HashMap<>();
		// args.put("category", "core");
		StringCommandTaskFactory factory = new StringCommandTaskFactory(this.ovManager, OVShared.STRING_CMD_LIST_SPECIES, null, this);
		TaskIterator ti = factory.createTaskIterator();
		this.ovManager.executeSynchronousTask(ti);
		
		// Init the NumberFormat to be 0.00 (with a dot)
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		this.formatter = new DecimalFormat("#0.00", dfs);

		LookAndFeelUtil.equalizeSize(this.closeButton, this.retrieveButton);

//		this.init();
	}
	
	private JPanel createNetTypeRadioButtons() {
		JPanel netTypePanel = new JPanel(new GridBagLayout());
		netTypePanel.setOpaque(!LookAndFeelUtil.isAquaLAF());
		MyGridBagConstraints c = new MyGridBagConstraints();
		netTypePanel.add(functionalNetwork, c);
		netTypePanel.add(physicalNetwork, c.nextCol());
				
		ButtonGroup group = new ButtonGroup();
		group.add(physicalNetwork);
		group.add(functionalNetwork);
				
		if (networkType.equals(NetworkType.PHYSICAL)) 
			physicalNetwork.setSelected(true);

		return netTypePanel;
	}
	
	public NetworkType getNetworkType() {
		if (physicalNetwork.isSelected())
			return NetworkType.PHYSICAL;
		else 
			return NetworkType.FUNCTIONAL;
	}

	private JPanel createConfidenceSlider() {
		JPanel confidencePanel = new JPanel(new GridBagLayout());
		confidencePanel.setOpaque(!LookAndFeelUtil.isAquaLAF());
		MyGridBagConstraints c = new MyGridBagConstraints();

		Font labelFont;
		{
			c.setAnchor("W").expandVertical();//.setInsets(0,5,0,5);
			JLabel confidenceLabel = new JLabel("Confidence (score) cutoff:");
			labelFont = confidenceLabel.getFont();
//			confidenceLabel.setFont(new Font(labelFont.getFontName(), Font.BOLD, labelFont.getSize()));
			confidencePanel.add(confidenceLabel, c);
		}

		{
			this.confidenceSlider = new JSlider();
			Dictionary<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
			Font valueFont = new Font(labelFont.getFontName(), Font.BOLD, labelFont.getSize()-4);
			for (int value = 0; value <= 100; value += 10) {
				double labelValue = (double)value/100.0;
				JLabel label = new JLabel(formatter.format(labelValue));
				label.setFont(valueFont);
				labels.put(value, label);
			}
			confidenceSlider.setLabelTable(labels);
			confidenceSlider.setPaintLabels(true);
			confidenceSlider.setValue(OVRetrieveStringNetworkWindow.defaultConfidence);

			confidenceSlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					if (ignore) return;
					ignore = true;
					int value = confidenceSlider.getValue();
					confidenceValue.setText(formatter.format(((double)value)/100.0));
					ignore = false;
				}
			});
			// c.anchor("southwest").expandHoriz().insets(0,5,0,5);
			c.nextCol().expandHorizontal();//.setInsets(0,5,0,5);
			confidencePanel.add(confidenceSlider, c);
		}

		{
			confidenceValue = new JTextField(4);
			confidenceValue.setHorizontalAlignment(JTextField.RIGHT);
			confidenceValue.setText(this.formatter.format(((double)OVRetrieveStringNetworkWindow.defaultConfidence)/100.0));
			c.nextCol().noExpand().setAnchor("C");//.setInsets(0,5,0,5);
			confidencePanel.add(confidenceValue, c);

			confidenceValue.addActionListener(new AbstractAction() {
				private static final long serialVersionUID = 6284825408676836773L;

				@Override
				public void actionPerformed(ActionEvent e) {
					textFieldValueChanged();
				}
			});

			confidenceValue.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					textFieldValueChanged();
				}
			});

		}
		return confidencePanel;
	}
	
	private void textFieldValueChanged() {
		if (ignore) return;
		ignore = true;
		String text = confidenceValue.getText();
		Number n = formatter.parse(text, new ParsePosition(0));
		double val = 0.0;
		if (n == null) {
			try {
				val = Double.valueOf(confidenceValue.getText());
			} catch (NumberFormatException nfe) {
				val = inputError();
			}
		} else if (n.doubleValue() > 1.0 || n.doubleValue() < 0.0) {
			val = inputError();
		} else {
			val = n.doubleValue();
		}

		val = val*100.0;
		confidenceSlider.setValue((int)val);
		ignore = false;
	}

	private double inputError() {
		confidenceValue.setBackground(Color.RED);
		JOptionPane.showMessageDialog(this, 
				                          "Please enter a confidence cutoff between 0.0 and 1.0", 
											            "Alert", JOptionPane.ERROR_MESSAGE);
		confidenceValue.setBackground(UIManager.getColor("TextField.background"));

		// Reset the value to correspond to the current slider setting
		double val = ((double)confidenceSlider.getValue())/100.0;
		confidenceValue.setText(formatter.format(val));
		return val;
	}
	
	@Override
	public void setVisible(boolean b) {
		if(b) {
			this.ovTable = this.ovManager.getActiveOVTable();
			
			this.selectQuery.removeAllItems();
			String selectedColumn="";
			for(String colName : this.ovTable.getColNames()) {
				if(!OVShared.isOVCol(colName)) {
					this.selectQuery.addItem(colName);
					
					if(colName.toLowerCase().equals("uniprot")) {
						// If a column name is Uniprot, then we should select it by default
						selectedColumn = colName;
					} else if(!selectedColumn.toLowerCase().equals("uniprot") && 
							colName.toLowerCase().contains("uniprot")) {
						// else, if we do not already have a column named Uniprot
						// if it contains Uniprot, we select it
						selectedColumn = colName;
					}
				}
			}
			this.selectQuery.setSelectedItem(selectedColumn);
			
			this.confidenceSlider.setValue(defaultConfidence);
			
			init();
		}
		
		super.setVisible(b);
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
		
		//selectPanel.add(new JLabel("Network type:"), c.nextRow());		
		//selectPanel.add(this.selectNetType, c.nextCol());
		
		selectPanel.add(new JLabel("Network type:"), c.nextRow());
		selectPanel.add(this.createNetTypeRadioButtons(), c.nextCol());

		selectPanel.add(this.createConfidenceSlider(), c.nextRow().useNCols(2).setInsets(MyGridBagConstraints.DEFAULT_INSET, 0, MyGridBagConstraints.DEFAULT_INSET, 0));
		c.useNCols(1).setInsets(MyGridBagConstraints.DEFAULT_INSET, MyGridBagConstraints.DEFAULT_INSET, MyGridBagConstraints.DEFAULT_INSET, MyGridBagConstraints.DEFAULT_INSET);

		this.selectedOnly.setSelected(false);
		this.filteredOnly.setSelected(true);
		if(!this.ovTable.getSelectedRows().isEmpty()) {
			this.selectedOnly.setSelected(true);
			this.filteredOnly.setSelected(false);
			this.filteredOnly.setEnabled(false);
			selectPanel.add(this.selectedOnly, c.nextRow().useNCols(2));
			c.useNCols(1);
		}

		if(this.ovTable.getFilter() != null) {
			selectPanel.add(this.filteredOnly, c.nextRow().useNCols(2));
			c.useNCols(1);
		}
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(this.closeButton);
		buttonPanel.add(this.retrieveButton);

		mainPanel.add(selectPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		this.setContentPane(mainPanel);

		this.pack();
		this.setLocationRelativeTo(this.ovManager.getOVCytoPanel().getTopLevelAncestor());
		
		// JComboBoxDecorator.originalDimension = this.selectSpecies.getPreferredSize();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void taskFinished(ObservableTask task) {
		if(task.getClass().getSimpleName().equals("GetSpeciesTask")) {
			List<Map<String,String>> res = task.getResults(List.class);

			try {
				OVSpecies.readSpecies(res);
				this.speciesList = OVSpecies.getModelSpecies();
			} catch (Exception e) {
				throw new RuntimeException("Can't read species information");
			}

			this.selectSpecies = new JComboBox<OVSpecies>(speciesList.toArray(new OVSpecies[1]));
			//We select Human as default
			this.selectSpecies.setSelectedItem(OVSpecies.getHumanSpecies());
			
			// JComboBoxDecorator.decorate(this.selectSpecies);
			JComboBoxDecorator decorator = new JComboBoxDecorator(this.selectSpecies, true, true, speciesList);
			decorator.decorate(speciesList);
		}
	}

	@Override
	public void allFinished(FinishStatus finishStatus) {
		// Do nothing
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.selectedOnly) {
			if(this.selectedOnly.isSelected()) {
				this.filteredOnly.setEnabled(false);
				this.filteredOnly.setSelected(false);
			} else {
				this.filteredOnly.setEnabled(true);
				this.filteredOnly.setSelected(true);
			}
		} else if(e.getSource() == this.retrieveButton) {
			// We check if the species is OK
			Object selectedSpecies = this.selectSpecies.getSelectedItem();
			OVSpecies species = null;
			if (selectedSpecies instanceof OVSpecies) {
				species = (OVSpecies) selectedSpecies;
			} else if (selectedSpecies instanceof String && selectedSpecies.equals("Homo sapiens")) {
				species = OVSpecies.getHumanSpecies();
			} else {
				JOptionPane.showMessageDialog(this, "Error: Unknown species \"" + selectedSpecies + "\".", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}			
			
			RetrieveStringNetworkTaskFactory factory = new RetrieveStringNetworkTaskFactory(this.ovManager);
			try {
				this.ovManager.executeTask(factory.createTaskIterator((String)this.selectQuery.getSelectedItem(),
						this.selectedOnly.isSelected(),
						this.filteredOnly.isSelected(),
						species.getTaxonID(),
						species.getName(),
						formatter.parse(this.confidenceValue.getText()).doubleValue(),
						this.getNetworkType().toString(),
						true));
			} catch (ParseException e1) {
				inputError();
				return;
			}

			// The task is executed in background, we don't want the window to be displayed
			this.setVisible(false);
		} else if(e.getSource() == this.closeButton) {
			this.setVisible(false);
		}
	}

}
