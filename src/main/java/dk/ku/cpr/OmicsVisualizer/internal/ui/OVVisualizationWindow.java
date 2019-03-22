package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVColor;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVColorContinuous;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVColorDiscrete;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVConnection;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVVisualization;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVVisualization.ChartType;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;
import dk.ku.cpr.OmicsVisualizer.internal.task.ApplyVisualizationTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.task.RemoveViualizationTaskFactory;

public class OVVisualizationWindow extends OVWindow implements ActionListener {
	private static final long serialVersionUID = 6606921043986517714L;

	private static final String NO_LABEL = "--- NONE ---";
	private static final String CONTINUOUS = "Continuous";
	private static final String DISCRETE = "Discrete";

	/** Used when values are positive and negative */
	private static final Color DEFAULT_MIN_COLOR = new Color(33,102,172);
	/** Used when values are positive and negative */
	private static final Color DEFAULT_ZERO_COLOR = Color.WHITE;
	/** Used when values are positive and negative */
	private static final Color DEFAULT_MAX_COLOR = new Color(178,24,43);
	/** Used when values are only positive */
	private static final Color DEFAULT_MIN_COLOR_POS = new Color(253,231,37);
	/** Used when values are only positive */
	private static final Color DEFAULT_ZERO_COLOR_POS = new Color(33,145,140);
	/** Used when values are only positive */
	private static final Color DEFAULT_MAX_COLOR_POS = new Color(68,1,84);
	// We switch colors between POS and NEG values (so that the "low" color is always near 0)
	/** Used when values are only negative */
	private static final Color DEFAULT_MIN_COLOR_NEG = DEFAULT_MAX_COLOR_POS;
	/** Used when values are only negative */
	private static final Color DEFAULT_ZERO_COLOR_NEG = DEFAULT_ZERO_COLOR_POS;
	/** Used when values are only negative */
	private static final Color DEFAULT_MAX_COLOR_NEG = DEFAULT_MIN_COLOR_POS;

	private static int MAXIMUM_COLOR_NUMBERS = 50;

	private OVCytoPanel cytoPanel;

	private OVTable ovTable;
	private OVConnection ovCon;

	private JButton cancelButton;

	private JComboBox<String> selectNetwork;
//	private JComboBox<String> selectCopyNetwork;
//	private JButton copyButton;
	private JButton deleteButton;
	private SelectValuesPanel selectValues;
	private JCheckBox filteredCheck;
	private JComboBox<ChartType> selectChartType;
	private ChartType oldType;
	private JComboBox<String> selectChartLabels;
	private JComboBox<String> selectDiscreteContinuous;
	private String oldDC;
	private JButton nextButton;

	private ColorChooser colorChooser;
	private JTextField rangeMin;
	private JTextField rangeZero;
	private JTextField rangeMax;
	private ColorPanel[] colorPanels;
	private Object[] discreteValues;
	private JCheckBox transposeCheck;
	private JButton backButton;
	private JButton resetButton;
	private JButton drawButton;

	public OVVisualizationWindow(OVManager ovManager) {
		super(ovManager);

		this.cytoPanel=ovManager.getOVCytoPanel();

		this.ovTable=null;

		// Both panels
		this.cancelButton = new JButton("Cancel");
		this.cancelButton.addActionListener(this);

		// Panel 1
		this.selectNetwork = new JComboBox<>();
		this.selectNetwork.setToolTipText("Select the Network Collection for which you want to apply the visualization.");
		this.selectNetwork.addActionListener(this);

		this.deleteButton = new JButton("Delete Visualization");
		this.deleteButton.addActionListener(this);
		this.deleteButton.setForeground(Color.RED);

		this.selectValues = new SelectValuesPanel(this);

		this.filteredCheck = new JCheckBox("Apply visualization only to filtered rows", true);

		this.selectChartType = new JComboBox<>();
		this.selectChartType.addActionListener(this);
		for(ChartType ct : ChartType.values()) {
			this.selectChartType.addItem(ct);
		}
		this.selectChartType.setToolTipText("<html>"
				+ "Select the type of Chart you want to draw.<br>"
				+ "<b>Donut Charts</b> can have several rings, thus you can select several values columns.<br>"
				+ "<b>Pie Charts</b> can have only one values column."
				+ "</html>");

		this.selectChartLabels = new JComboBox<>();

		this.selectDiscreteContinuous = new JComboBox<>();
		this.selectDiscreteContinuous.addItem(OVVisualizationWindow.CONTINUOUS);
		this.selectDiscreteContinuous.addItem(OVVisualizationWindow.DISCRETE);
		this.selectDiscreteContinuous.addActionListener(this);

		this.nextButton = new JButton("Next >");
		this.nextButton.addActionListener(this);

		// Panel 2
		this.backButton = new JButton("< Back");
		this.backButton.addActionListener(this);

		this.resetButton = new JButton("Reset values");
		this.resetButton.addActionListener(this);

		this.drawButton = new JButton("Draw");
		this.drawButton.addActionListener(this);

		this.colorChooser = new ColorChooser();

		this.transposeCheck = new JCheckBox("Transpose data");
		
		LookAndFeelUtil.equalizeSize(this.cancelButton, this.nextButton, this.backButton, this.drawButton);

		// We show Panel1 first, obviously
		this.displayPanel1();
	}

	private void displayPanel1() {
		this.setPreferredSize(null); // We want to recompute the size each time


		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());

		MyGridBagConstraints c = new MyGridBagConstraints();
		c.expandHorizontal().setAnchor("C");

		JPanel definePanel = new JPanel();
		definePanel.setBorder(LookAndFeelUtil.createTitledBorder("Define Visualization for"));
		definePanel.setLayout(new GridBagLayout());
		MyGridBagConstraints c2 = new MyGridBagConstraints();
		definePanel.add(this.selectNetwork, c2.expandHorizontal());

		mainPanel.add(definePanel, c.expandBoth());
		mainPanel.add(this.deleteButton, c.nextCol().noExpand().setAnchor("C"));
		c.expandHorizontal();

		JPanel chartPanel = new JPanel();
		chartPanel.setBorder(LookAndFeelUtil.createTitledBorder("Chart properties"));
		chartPanel.setLayout(new GridBagLayout());
		c2 = new MyGridBagConstraints();
		c2.expandHorizontal().setAnchor("C");

		chartPanel.add(new JLabel("Select Chart Type:"), c2);
		chartPanel.add(this.selectChartType, c2.nextCol());

		// This JLabel must be displayed in the top-left corner
		// We add the default margin on top and left because otherwise the JLabel is not aligned with the center of the first JComboBox
		chartPanel.add(new JLabel("Select Values:"), c2.nextRow().setAnchor("NW").setInsets(MyGridBagConstraints.DEFAULT_INSET, MyGridBagConstraints.DEFAULT_INSET, 0, 0));
		// reset constraint
		c2.setAnchor("C").setInsets(MyGridBagConstraints.DEFAULT_INSET, MyGridBagConstraints.DEFAULT_INSET, MyGridBagConstraints.DEFAULT_INSET, MyGridBagConstraints.DEFAULT_INSET);
		chartPanel.add(this.selectValues, c2.nextCol());

		// TODO Version 1.0: Without filters
		//		stylePanel.add(this.filteredCheck, c2.nextRow().useNCols(2));
		//		c2.useNCols(1);

		chartPanel.add(new JLabel("Select Labels:"), c2.nextRow());
		chartPanel.add(this.selectChartLabels, c2.nextCol());

		// TODO Version 1.0: Without Discrete Mapping
		//		stylePanel.add(new JLabel("Mapping:"), c2.nextRow());
		//		stylePanel.add(this.selectDiscreteContinuous, c2.nextCol());

		mainPanel.add(chartPanel, c.nextRow().useNCols(2));

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(this.cancelButton);
		buttonPanel.add(this.nextButton);

		this.setContentPane(new JPanel());
		this.setLayout(new BorderLayout());

		this.add(mainPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);

		this.pack();
		this.setLocationRelativeTo(this.cytoPanel.getTopLevelAncestor());
	}

	private void displayPanel2(boolean reset) {
		this.setPreferredSize(null); // We want to recompute the size each time

		OVVisualization ovViz = null;
		if(this.ovCon != null) {
			ovViz = this.ovCon.getVisualization();
		}

		JPanel mainPanel = new JPanel();
		//		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		mainPanel.setBorder(LookAndFeelUtil.createTitledBorder("Color properties"));

		List<String> colNames = this.selectValues.getValues();
		Class<?> valueType = this.selectValues.getValueType();

		// We only look at values of rows connected to the network
		List<CyRow> valueRows = new ArrayList<>();
		for(CyRow netRow : this.ovCon.getBaseNetwork().getDefaultNodeTable().getAllRows()) {
			valueRows.addAll(this.ovCon.getLinkedRows(netRow));
		}

		this.transposeCheck.setSelected(false);

		if(this.selectDiscreteContinuous.getSelectedItem() == OVVisualizationWindow.CONTINUOUS) {
			double rangeMin=0.0, rangeZero=0.0, rangeMax=0.0; // enhancedGraphics uses double for ranges
			Color colorMin = DEFAULT_MIN_COLOR;
			Color colorZero = DEFAULT_ZERO_COLOR;
			Color colorMax = DEFAULT_MAX_COLOR;

			boolean vizLoaded=false;
			if(ovViz != null && ovViz.getColors() instanceof OVColorContinuous) {
				// There is already a visualization applied for this, we simply load the information from it
				OVColorContinuous colorViz = (OVColorContinuous) ovViz.getColors();

				rangeMax = colorViz.getRangeMax();
				rangeZero = colorViz.getRangeZero();
				rangeMin = colorViz.getRangeMin();

				colorMin = colorViz.getDown();
				colorZero = colorViz.getZero();
				colorMax = colorViz.getUp();

				this.transposeCheck.setSelected(ovViz.isTranspose());

				vizLoaded=true;
			}

			if(reset || !vizLoaded) {
				// We don't have any information, we infer them from the data
				// We identify max and min value
				// We can't compare two Number, so we have to do the same for the 3 types
				if(valueType == Integer.class) {
					int min = Integer.MAX_VALUE;
					int max = Integer.MIN_VALUE;

					for(CyRow row : valueRows) {
						if(this.filteredCheck.isSelected() && !this.ovTable.isFiltered(row)) {
							continue;
						}

						for(String colName : colNames) {
							Integer Val = row.get(colName, Integer.class);
							int val=0;
							if(Val != null) {
								val = Val.intValue();
							} else {
								continue; // We do not take missing values into account
							}

							if(val < min) {
								min=val;
							}
							if(val > max) {
								max=val;
							}
						}
					}

					if((max <= 0) || (min >= 0)) {
						// The values have the same sign
						rangeMin = min;
						rangeMax = max;
						rangeZero = (max+min)/2;

						if(max <= 0) { // Values all negatives
							colorMin = DEFAULT_MIN_COLOR_NEG;
							colorZero = DEFAULT_ZERO_COLOR_NEG;
							colorMax = DEFAULT_MAX_COLOR_NEG;
						} else { // values all positives
							colorMin = DEFAULT_MIN_COLOR_POS;
							colorZero = DEFAULT_ZERO_COLOR_POS;
							colorMax = DEFAULT_MAX_COLOR_POS;
						}
					} else {
						// We detect the highest absolute value for the range
						max = (max >= -min ? max : -min);
						rangeMin = max * -1.0;
						rangeMax = max * 1.0;
						rangeZero = 0.0;

						colorMin = DEFAULT_MIN_COLOR;
						colorZero = DEFAULT_ZERO_COLOR;
						colorMax = DEFAULT_MAX_COLOR;
					}
				} else if(valueType == Long.class) {
					long min = Long.MAX_VALUE;
					long max = Long.MIN_VALUE;

					for(CyRow row : valueRows) {
						if(this.filteredCheck.isSelected() && !this.ovTable.isFiltered(row)) {
							continue;
						}

						for(String colName : colNames) {
							Long Val = row.get(colName, Long.class);
							long val=0;
							if(Val != null) {
								val = Val.longValue();
							} else {
								continue; // We do not take missing values into account
							}

							if(val < min) {
								min=val;
							}
							if(val > max) {
								max=val;
							}
						}
					}

					if((max <= 0) || (min >= 0)) {
						// The values have the same sign
						rangeMin = min;
						rangeMax = max;
						rangeZero = (max+min)/2;

						if(max <= 0) { // Values all negatives
							colorMin = DEFAULT_MIN_COLOR_NEG;
							colorZero = DEFAULT_ZERO_COLOR_NEG;
							colorMax = DEFAULT_MAX_COLOR_NEG;
						} else { // values all positives
							colorMin = DEFAULT_MIN_COLOR_POS;
							colorZero = DEFAULT_ZERO_COLOR_POS;
							colorMax = DEFAULT_MAX_COLOR_POS;
						}
					} else {
						// We detect the highest absolute value for the range
						max = (max >= -min ? max : -min);
						rangeMin = max * -1.0;
						rangeMax = max * 1.0;
						rangeZero = 0.0;

						colorMin = DEFAULT_MIN_COLOR;
						colorZero = DEFAULT_ZERO_COLOR;
						colorMax = DEFAULT_MAX_COLOR;
					}
				} else { // Double
					double min = Double.POSITIVE_INFINITY;
					double max = Double.NEGATIVE_INFINITY;

					for(CyRow row : valueRows) {
						if(this.filteredCheck.isSelected() && !this.ovTable.isFiltered(row)) {
							continue;
						}

						for(String colName : colNames) {
							Double Val = row.get(colName, Double.class);
							double val=0.0;
							if(Val != null) {
								val = Val.doubleValue();
							} else {
								continue; // We do not take missing values into account
							}

							if(val < min) {
								min=val;
							}
							if(val > max) {
								max=val;
							}
						}
					}

					if((max <= 0) || (min >= 0)) {
						// The values have the same sign
						rangeMin = min;
						rangeMax = max;
						rangeZero = (max+min)/2;

						if(max <= 0) { // Values all negatives
							colorMin = DEFAULT_MIN_COLOR_NEG;
							colorZero = DEFAULT_ZERO_COLOR_NEG;
							colorMax = DEFAULT_MAX_COLOR_NEG;
						} else { // values all positives
							colorMin = DEFAULT_MIN_COLOR_POS;
							colorZero = DEFAULT_ZERO_COLOR_POS;
							colorMax = DEFAULT_MAX_COLOR_POS;
						}
					} else {
						// We detect the highest absolute value for the range
						max = (max >= -min ? max : -min);
						rangeMin = max * -1.0;
						rangeMax = max * 1.0;
						rangeZero = 0.0;

						colorMin = DEFAULT_MIN_COLOR;
						colorZero = DEFAULT_ZERO_COLOR;
						colorMax = DEFAULT_MAX_COLOR;
					}
				}
			}

			mainPanel.setLayout(new GridBagLayout());
			MyGridBagConstraints c = new MyGridBagConstraints();
			c.expandHorizontal();

			this.colorPanels = new ColorPanel[3];

			this.rangeMax = new JTextField(String.valueOf(rangeMax));
			this.colorPanels[0] = new ColorPanel(colorMax, this, this.colorChooser);
			mainPanel.add(new JLabel("Max:"), c.nextRow());
			mainPanel.add(this.rangeMax, c.nextCol());
			mainPanel.add(this.colorPanels[0], c.nextCol().expandBoth());
			c.expandHorizontal();

			this.rangeZero = new JTextField(String.valueOf(rangeZero));
			this.colorPanels[1] = new ColorPanel(colorZero, this, this.colorChooser);
			mainPanel.add(new JLabel("Middle:"), c.nextRow());
			mainPanel.add(this.rangeZero, c.nextCol());
			mainPanel.add(this.colorPanels[1], c.nextCol().expandBoth());
			c.expandHorizontal();

			this.rangeMin = new JTextField(String.valueOf(rangeMin));
			this.colorPanels[2] = new ColorPanel(colorMin, this, this.colorChooser);
			mainPanel.add(new JLabel("Min:"), c.nextRow());
			mainPanel.add(this.rangeMin, c.nextCol());
			mainPanel.add(this.colorPanels[2], c.nextCol().expandBoth());
			c.expandHorizontal();

			if(this.selectChartType.getSelectedItem().equals(ChartType.CIRCOS)) {
				// Only CIRCOS can have several layouts
				mainPanel.add(transposeCheck, c.nextRow().useNCols(3));
			}
			mainPanel.add(resetButton, c.nextRow().useNCols(3).noExpand().setAnchor("E"));

		} else { // Discrete mapping
			Set<Object> values = new HashSet<>();

			if(!reset && ovViz != null && ovViz.getColors() instanceof OVColorDiscrete) {
				// There is already a style applied for this, we simply load the information from it
				OVColorDiscrete colorViz = (OVColorDiscrete) ovViz.getColors();

				values = colorViz.getValues();

				this.transposeCheck.setSelected(ovViz.isTranspose());
			} else {
				// We look for the values in the data
				for(CyRow row : valueRows) {
					if(this.filteredCheck.isSelected() && !this.ovTable.isFiltered(row)) {
						continue;
					}

					for(String colName : colNames) {
						Object val = row.get(colName, valueType);
						if(val != null ) {
							values.add(val);
						} else {
							if(valueType == Integer.class) {
								values.add(new Integer(0));
							} else if(valueType == Long.class) {
								values.add(new Long(0));
							} else if(valueType == Double.class) {
								values.add(new Double(0.0));
							} else {
								values.add("");
							}
						}
					}
				}
			}

			mainPanel.setLayout(new GridBagLayout());
			MyGridBagConstraints c = new MyGridBagConstraints();
			c.expandHorizontal();

			this.colorPanels = new ColorPanel[values.size()];
			// To be sure that values and colors are well associated, we do not use toArray() but we copy each value
			this.discreteValues = new Object[values.size()];

			JPanel valuesList = new JPanel();
			valuesList.setLayout(new GridBagLayout());
			MyGridBagConstraints clist = new MyGridBagConstraints();
			clist.expandHorizontal();
			int i=0;
			int nb_values = values.size();
			for(Object val : values) {
				Color color = generateRandomColor(i, nb_values);

				if(ovViz != null && ovViz.getColors() instanceof OVColorDiscrete) {
					OVColorDiscrete colorViz = (OVColorDiscrete) ovViz.getColors();
					color = colorViz.getColor(val);

					if(color == null) {
						color = generateRandomColor(i, nb_values);
					}
				}

				this.discreteValues[i] = val;

				this.colorPanels[i] = new ColorPanel(color, this, this.colorChooser);

				valuesList.add(new JLabel(val.toString()), clist.nextRow());
				valuesList.add(this.colorPanels[i], clist.nextCol().expandBoth());

				++i;
			}
			JScrollPane valuesScroll = new JScrollPane(valuesList);
			valuesScroll.setBorder(null);

			mainPanel.add(valuesScroll, c);
			if(this.selectChartType.getSelectedItem().equals(ChartType.CIRCOS)) {
				// Only CIRCOS can have several layers
				mainPanel.add(transposeCheck, c.nextRow());
			}
			mainPanel.add(resetButton, c.nextRow().noExpand().setAnchor("E"));
		}

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		buttonPanel.add(this.backButton);
		buttonPanel.add(this.cancelButton);
		buttonPanel.add(this.drawButton);

		this.setContentPane(new JPanel());
		this.setLayout(new BorderLayout());

		this.add(mainPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);

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

	private void setTitle(OVTable ovTable, String netName) {
		if(ovTable == null) {
			this.setTitle("Visualization");
		} else {
			String title = "Visualization of " + ovTable.getTitle();

			if(netName != null && !netName.isEmpty()) {
				title += " for " + netName;
			}

			this.setTitle(title);
		}
	}

	public void setTable(OVTable ovTable) {
		if(ovTable == null || !ovTable.isConnected()) {
			this.setVisible(false);
			return;
		}

		this.ovTable=ovTable;

		this.setTitle(ovTable, null);

		this.selectNetwork.removeActionListener(this);
		this.selectNetwork.removeAllItems();
		for(OVConnection ovCon : this.ovManager.getConnections(this.ovTable)) {
			this.selectNetwork.addItem(ovCon.getCollectionNetworkName());
		}
		this.selectNetwork.addActionListener(this);

		this.selectValues.setTable(ovTable);

		this.selectChartLabels.removeAllItems();

		this.selectChartLabels.addItem(OVVisualizationWindow.NO_LABEL);
		for(String colName : this.ovTable.getColNames()) {
			if(!OVShared.isOVCol(colName)) {
				this.selectChartLabels.addItem(colName);
			}
		}

		// We look for the current displayed network
		CyApplicationManager appManager = this.ovManager.getService(CyApplicationManager.class);
		CyRootNetworkManager rootNetManager = this.ovManager.getService(CyRootNetworkManager.class);
		CyNetwork currentNetwork=appManager.getCurrentNetwork();

		if(currentNetwork != null) {
			CyRootNetwork currentRoot = rootNetManager.getRootNetwork(currentNetwork);
			OVConnection currentRootConnection = this.ovManager.getConnection(currentRoot);
			if(currentRootConnection != null && currentRootConnection.getOVTable().equals(ovTable)) {
				this.selectNetwork.setSelectedItem(currentRoot.toString());
			}
		}
		if(this.selectNetwork.getSelectedIndex()==0) { // Here it means that the ActionListener was not triggered
			// So we triger it
			changedNetwork();
		}

		this.displayPanel1();

		this.pack();
		this.setLocationRelativeTo(this.cytoPanel.getTopLevelAncestor());
	}

	private void changedNetwork() {
		for(OVConnection ovCon : this.ovManager.getConnections(this.ovTable)) {
			if(ovCon.getCollectionNetworkName().equals(this.selectNetwork.getSelectedItem())) {
				this.ovCon = ovCon;

				this.setTitle(this.ovTable, this.ovCon.getCollectionNetworkName());

				this.updateVisualization(this.ovCon.getVisualization());

				this.selectValues.setTable(this.ovCon.getOVTable());
				this.selectValues.setVisualization(this.ovCon.getVisualization());

				// We change the network to the one selected
				CyApplicationManager appManager = this.ovManager.getService(CyApplicationManager.class);
				CyRootNetworkManager netRootManager = this.ovManager.getService(CyRootNetworkManager.class);
				CyNetwork currentNetwork = appManager.getCurrentNetwork();
				if(currentNetwork != null) {
					CyRootNetwork currentRoot = netRootManager.getRootNetwork(currentNetwork);
					if(!this.ovCon.getRootNetwork().equals(currentRoot)) {
						appManager.setCurrentNetwork(this.ovCon.getBaseNetwork());
					}
				}

				break;
			}
		}
	}

	private void checkValueTypes() {
		if(!this.selectValues.allSameType()) {
			JOptionPane.showMessageDialog(null,
					"All the values should have the same type.",
					"Error: Bad value types",
					JOptionPane.ERROR_MESSAGE);
			return;
		}


		// TODO Version 1.0: Without Discrete Mapping
		this.selectDiscreteContinuous.setSelectedItem(OVVisualizationWindow.CONTINUOUS);
		//		Class<?> valueType = this.selectValues.getValueType();
		//		if(valueType == String.class || valueType == Boolean.class) {
		//			// No choice but discrete mapping
		//			this.oldDC = (String) this.selectDiscreteContinuous.getSelectedItem();
		//			this.selectDiscreteContinuous.setSelectedItem(OVStyleWindow.DISCRETE);
		//			this.selectDiscreteContinuous.setEnabled(false);
		//		} else if(!this.selectDiscreteContinuous.isEnabled()) {
		//			this.selectDiscreteContinuous.setEnabled(true);
		//			this.selectDiscreteContinuous.setSelectedItem(this.oldDC);
		//		}
	}

	private void updateVisualization(OVVisualization ovViz) {
		this.ovCon.setVisualization(ovViz);

		// We only update the Panel1 information, the information of Panel2 always check for ovViz
		this.selectValues.setVisualization(ovViz);
		if(ovViz != null) {
			this.filteredCheck.setSelected(ovViz.isOnlyFiltered());
			this.selectChartType.setSelectedItem(ovViz.getType());
			if(ovViz.getLabel() == null) {
				this.selectChartLabels.setSelectedItem(OVVisualizationWindow.NO_LABEL);
			} else {
				this.selectChartLabels.setSelectedItem(ovViz.getLabel());
			}
			if(ovViz.getColors() instanceof OVColorDiscrete) {
				this.selectDiscreteContinuous.setSelectedItem(OVVisualizationWindow.DISCRETE);
			} else {
				this.selectDiscreteContinuous.setSelectedItem(OVVisualizationWindow.CONTINUOUS);
			}
		} else {
			this.selectChartType.setSelectedIndex(0);
			this.selectChartLabels.setSelectedIndex(0);
			this.selectDiscreteContinuous.setSelectedIndex(0);
		}

		this.visualizationUpdated();
	}

	private void visualizationUpdated() {
		if(this.selectValues.getValues().size() > 1) {
			// If we have more than one value, we authorize only one type of chart
			this.oldType = (ChartType) this.selectChartType.getSelectedItem();
			this.selectChartType.setSelectedItem(ChartType.CIRCOS);
			this.selectChartType.setEnabled(false);
		} else if(!this.selectChartType.isEnabled()) {
			this.selectChartType.setEnabled(true);
			this.selectChartType.setSelectedItem(this.oldType);
		}

		this.pack();
		this.setLocationRelativeTo(this.cytoPanel.getTopLevelAncestor());
	}

	private Color generateRandomColor(int i, int nb_colors) {
		if(nb_colors > MAXIMUM_COLOR_NUMBERS) {
			nb_colors = MAXIMUM_COLOR_NUMBERS;
		}

		Random random = new Random();
		float h = (float)(i % nb_colors)/(float)nb_colors;
		float s, b;

		switch(i/nb_colors) {
		case 0:
			s = 1.0f;
			b = 1.0f;
			break;
		case 1:
			s = 0.5f;
			b = 1.0f;
			break;
		case 2:
			s = 0.5f;
			b = 0.5f;
			break;
		case 3:
			s = 1.0f;
			b = 0.5f;
			break;
		default: // after more than 4 "turns around the HSB circle", we just pick random values between 0.5 and 1
			s = random.nextFloat()/2 + 0.5f;
			b = random.nextFloat()/2 + 0.5f;
		}

		return Color.getHSBColor(h, s, b);
	}

	@Override
	public void setVisible(boolean b) {
		// We disable the fact that the window can be visible if the table is not connected
		if(b && (this.ovTable == null || !this.ovTable.isConnected())) {
			b=false;
		}

		// If we hide this window, the ColorChooser should be hidden also
		if(!b) {
			this.colorChooser.setVisible(false);
		}

		super.setVisible(b);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.cancelButton) {
			this.setVisible(false);
		} else if(e.getSource() == this.deleteButton) {
			if(JOptionPane.showConfirmDialog(this, "You are about to delete this visualization applied to the network \"" + this.ovCon.getCollectionNetworkName() + "\".", "Visualization deletion confirmation", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
				RemoveViualizationTaskFactory factory = new RemoveViualizationTaskFactory(this.ovManager, this.ovCon);
				this.ovManager.executeSynchronousTask(factory.createTaskIterator());
				
				this.updateVisualization(this.ovCon.getVisualization());
			}
		} else if(e.getSource() == this.selectNetwork) {
			this.changedNetwork();
		} else if(e.getSource() == this.selectChartType) {
			this.selectValues.addButton.setEnabled(((ChartType)this.selectChartType.getSelectedItem()) == ChartType.CIRCOS);
		} else if(e.getSource() == this.nextButton) {
			if(!this.selectValues.allSameType()) {
				JOptionPane.showMessageDialog(null,
						"All the values should have the same type.",
						"Error: Bad value types",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			// We look if something has changed from the loaded visualization
			// To know if we have to reset panel2 or not
			boolean reset = false;
			if(this.ovCon.getVisualization() != null) {
				reset = !ovCon.getVisualization().getValues().equals(this.selectValues.getValues());
				reset |= ovCon.getVisualization().isOnlyFiltered() != this.filteredCheck.isSelected();
			}

			this.displayPanel2(reset);
		} else if(e.getSource() == this.backButton) {
			this.displayPanel1();
		} else if(e.getSource() == this.resetButton) {
			this.displayPanel2(true);
		} else if(e.getSource() == this.drawButton) {
			OVColor colors = null;

			if(this.selectDiscreteContinuous.getSelectedItem().equals(OVVisualizationWindow.CONTINUOUS)) {
				Double rangeMin, rangeZero, rangeMax;
				try {
					rangeMin = Double.valueOf(this.rangeMin.getText());
					rangeZero = Double.valueOf(this.rangeZero.getText());
					rangeMax = Double.valueOf(this.rangeMax.getText());
				} catch(NumberFormatException nfe) {
					JOptionPane.showMessageDialog(this, "Limits and middle values should be floating-point values.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				if((rangeMin > rangeZero) || (rangeZero > rangeMax)) {
					JOptionPane.showMessageDialog(this, "The minimum limit should be lower than the middle value, that should be lower than the maximum limit.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				colors = new OVColorContinuous(this.colorPanels[2].getColor(), // min
						this.colorPanels[0].getColor(), // max
						this.colorPanels[1].getColor(), // zero
						rangeMin.doubleValue(),
						rangeZero.doubleValue(),
						rangeMax.doubleValue());
			} else { // Discrete
				Map<Object, Color> mapping = new HashMap<>();

				for(int i=0; i<this.colorPanels.length; ++i) {
					mapping.put(this.discreteValues[i], this.colorPanels[i].getColor());
				}

				colors = new OVColorDiscrete(mapping);
			}

			String label = null;
			if(!this.selectChartLabels.getSelectedItem().equals(OVVisualizationWindow.NO_LABEL)) {
				label = (String) this.selectChartLabels.getSelectedItem();
			}

			this.selectValues.getValues();

			OVVisualization ovViz = new OVVisualization((ChartType) this.selectChartType.getSelectedItem(),
					this.selectValues.getValues(),
					this.selectValues.getValueType(),
					this.filteredCheck.isSelected(),
					colors,
					label,
					this.transposeCheck.isSelected());

			this.ovCon.setVisualization(ovViz);

			ApplyVisualizationTaskFactory factory = new ApplyVisualizationTaskFactory(this.ovManager, this.ovCon, this.filteredCheck.isSelected());
			this.ovManager.executeTask(factory.createTaskIterator());

			this.setVisible(false);
		}
	}

	private class ChartValues {
		private String colName;
		private Class<?> colType;

		public ChartValues(String colName, Class<?> colType) {
			super();
			this.colName = colName;
			this.colType = colType;
		}

		public String getColName() {
			return colName;
		}

		public Class<?> getColType() {
			return colType;
		}

		public String toString() {
			return this.getColName() + " [" + this.colType.getSimpleName() + "]";
		}
	}

	private class SelectValuesPanel extends JPanel implements ActionListener {
		private static final long serialVersionUID = 1330452410238222949L;

		private OVVisualizationWindow ovVisualizationWindow;

		private List<String> selectItemStringValues;
		private ChartValues[] selectItemChartValues;

		private List<JComboBox<ChartValues>> selects;
		private List<JButton> buttons;
		private JButton addButton;

		private ComboBoxActionListener comboBoxActionListener;

		public SelectValuesPanel(OVVisualizationWindow ovVisualizationWindow) {
			this.ovVisualizationWindow = ovVisualizationWindow;

			this.setOpaque(!LookAndFeelUtil.isAquaLAF());

			this.selects = new ArrayList<>();
			this.buttons = new ArrayList<>();

			this.addButton = new JButton("add");
			this.addButton.addActionListener(this);

			this.comboBoxActionListener = new ComboBoxActionListener(this.ovVisualizationWindow);
		}

		public void setTable(OVTable ovTable) {
			if(ovTable != null) {
				this.selectItemStringValues = new ArrayList<>();
				List<ChartValues> selectItems = new ArrayList<>();
				for(String colName : ovTable.getColNames()) {
					Class<?> colType = ovTable.getColType(colName);
					// We don't want OVCol, neither do we want List columns
					// TODO Version 1.0: Without Discrete Mapping (so without String nor Boolean columns)
					if(!OVShared.isOVCol(colName) && colType != List.class
							&& colType != String.class
							&& colType != Boolean.class) {
						selectItems.add(new ChartValues(colName, colType));
						this.selectItemStringValues.add(colName);
					}
				}
				this.selectItemChartValues = new ChartValues[selectItems.size()];
				this.selectItemChartValues = selectItems.toArray(this.selectItemChartValues);
			}
		}

		public void setVisualization(OVVisualization ovViz) {
			if(this.selectItemChartValues == null) {
				return;
			}

			this.selects = new ArrayList<>();
			this.buttons = new ArrayList<>();

			if(ovViz == null) {
				this.createSelect();

				JButton del = new JButton("del");
				del.addActionListener(this);
				this.buttons.add(del);
			} else {
				for(String val : ovViz.getValues()) {
					// We create the JComboBox and select the visualization value
					this.createSelect().setSelectedIndex(this.selectItemStringValues.indexOf(val));

					JButton del = new JButton("del");
					del.addActionListener(this);
					this.buttons.add(del);
				}
			}

			// We can not delete a value if there is only one
			if(this.buttons.size() == 1) {
				this.buttons.get(0).setEnabled(false);
			}

			this.update();
		}

		public void update() {
			this.removeAll();

			this.setLayout(new GridBagLayout());
			MyGridBagConstraints c = new MyGridBagConstraints();
			c.expandHorizontal().setInsets(0, 0, 0, 0);

			this.add(this.selects.get(0), c);
			this.add(this.buttons.get(0), c.nextCol());
			for(int i=1; i<this.selects.size(); ++i) {
				this.add(this.selects.get(i), c.nextRow());
				this.add(this.buttons.get(i), c.nextCol());
			}

			this.add(this.addButton, c.nextRow().nextCol());

			this.ovVisualizationWindow.visualizationUpdated();
		}

		private JComboBox<ChartValues> createSelect() {
			JComboBox<ChartValues> select = new JComboBox<>(this.selectItemChartValues);
			select.setRenderer(new ComboBoxRenderer());
			select.addActionListener(this.comboBoxActionListener);

			this.selects.add(select);

			return select;
		}

		private void addSelect() {
			JComboBox<ChartValues> select = this.createSelect();
			// The select has the first item selected
			// By default we will select the last ChartValues
			select.removeActionListener(this.comboBoxActionListener);
			// this select was added, so the "last" ChartValues is at size()-2
			select.setSelectedItem(this.selects.get(this.selects.size()-2).getSelectedItem());
			select.addActionListener(this.comboBoxActionListener);

			JButton del = new JButton("del");
			del.addActionListener(this);
			this.buttons.add(del);

			// If we add a button, we can delete all the values
			this.buttons.get(0).setEnabled(true);

			this.update();
		}

		private void delSelect(int index) {
			if(index >= 0 && index < this.selects.size()) {
				this.selects.remove(index);
				this.buttons.remove(index);

				if(this.buttons.size()==1) {
					// We can not delete the last value
					this.buttons.get(0).setEnabled(false);
				}

				this.update();
			}
		}

		public boolean allSameType() {
			Class<?> type = ((ChartValues)this.selects.get(0).getSelectedItem()).getColType();

			for(int i=1; i<this.selects.size(); ++i) {
				if(type != ((ChartValues)this.selects.get(i).getSelectedItem()).getColType()) {
					return false;
				}
			}

			return true;
		}

		public List<String> getValues() {
			List<String> values = new ArrayList<>();

			for(JComboBox<ChartValues> combo : this.selects) {
				values.add(((ChartValues)combo.getSelectedItem()).getColName());
			}

			return values;
		}

		/**
		 * Returns the type of the values.<br>
		 * <b>Important:</b> The function only look at the first value, you have to make sure that all the data has the same type.
		 * @return the type of the values
		 */
		public Class<?> getValueType() {
			return ((ChartValues)this.selects.get(0).getSelectedItem()).getColType();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == this.addButton) {
				this.addSelect();
			} else {
				// We look for the "del" button that was clicked
				int i=0;
				while(i<this.buttons.size() && e.getSource()!=this.buttons.get(i)) {
					++i;
				}
				if(i<this.buttons.size()) {
					this.delSelect(i);
				}
			}
		}

		private class ComboBoxRenderer extends DefaultListCellRenderer {
			private static final long serialVersionUID = -1311177239035163106L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				if(!(value instanceof ChartValues)) {
					return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				}
				ChartValues cValue = (ChartValues)value;

				// We need to get some style from the super method
				Component old = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

				JPanel mainPanel = new JPanel();
				mainPanel.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
				mainPanel.setBackground(old.getBackground());
				mainPanel.setForeground(old.getForeground());
				mainPanel.setFont(old.getFont());

				mainPanel.setLayout(new GridBagLayout());
				MyGridBagConstraints c = new MyGridBagConstraints();
				c.setInsets(0, 0, 0, 0);

				mainPanel.add(new JLabel(cValue.getColName()), c.expandHorizontal());

				// We try do display the same as Cytoscape display types
				String type = cValue.getColType().getSimpleName(); // If there is a new type at least we display the class name
				if(cValue.getColType() == Integer.class) {
					type = "1";
				} else if(cValue.getColType() == Long.class) {
					type = "123";
				} else if(cValue.getColType() == Double.class) {
					type = "1.0";
				} else if(cValue.getColType() == String.class) {
					type = "ab";
				} else if(cValue.getColType() == Boolean.class) {
					type = "y/n";
				}
				JLabel labelType = new JLabel(type);
				labelType.setFont(new Font("Serif", Font.BOLD, 11)); // See dk.ku.cpr.OmicsVisualizer.internal.tableimport.ui.AttributeEditor :: createDataTypeButton(AttributeDataType)


				mainPanel.add(labelType, c.nextCol().noExpand());

				return mainPanel;
			}
		}

		private class ComboBoxActionListener implements ActionListener {
			private OVVisualizationWindow ovVisualizationWindow;

			public ComboBoxActionListener(OVVisualizationWindow ovVisualizationWindow) {
				super();
				this.ovVisualizationWindow = ovVisualizationWindow;
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				this.ovVisualizationWindow.checkValueTypes();
			}

		}
	}
}
