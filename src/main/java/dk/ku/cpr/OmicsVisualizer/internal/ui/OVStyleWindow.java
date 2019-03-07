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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVColor;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVColorContinuous;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVColorDiscrete;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVConnection;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVStyle;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVStyle.ChartType;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;
import dk.ku.cpr.OmicsVisualizer.internal.task.ApplyStyleTaskFactory;

public class OVStyleWindow extends JFrame implements ActionListener {
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
	/** Used when values are only positive or only negative */
	private static final Color DEFAULT_MIN_COLOR_2 = new Color(253,231,37);
	/** Used when values are only positive or only negative */
	private static final Color DEFAULT_ZERO_COLOR_2 = new Color(33,145,140);
	/** Used when values are only positive or only negative */
	private static final Color DEFAULT_MAX_COLOR_2 = new Color(68,1,84);

	private OVCytoPanel cytoPanel;
	private OVManager ovManager;

	private OVTable ovTable;

	private OVConnection ovCon;

	private JComboBox<String> selectNetwork;
	private JComboBox<String> selectCopyNetwork;
	private JButton copyButton;
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
	private JButton[] colorButtons;
	private Object[] discreteValues;
	private JCheckBox transposeCheck;
	private JButton backButton;
	private JButton resetButton;
	private JButton drawButton;

	public OVStyleWindow(OVCytoPanel cytoPanel, OVManager ovManager) {
		super();

		this.cytoPanel=cytoPanel;
		this.ovManager=ovManager;

		this.ovTable=null;

		// Panel 1
		this.selectNetwork = new JComboBox<>();
		this.selectNetwork.addActionListener(this);

		this.selectCopyNetwork = new JComboBox<>();

		this.copyButton = new JButton("Copy Style");
		this.copyButton.addActionListener(this);

		this.selectValues = new SelectValuesPanel(this);

		this.filteredCheck = new JCheckBox("Apply style only to filtered rows", true);

		this.selectChartType = new JComboBox<>();
		for(ChartType ct : ChartType.values()) {
			this.selectChartType.addItem(ct);
		}

		this.selectChartLabels = new JComboBox<>();

		this.selectDiscreteContinuous = new JComboBox<>();
		this.selectDiscreteContinuous.addItem(OVStyleWindow.CONTINUOUS);
		this.selectDiscreteContinuous.addItem(OVStyleWindow.DISCRETE);
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

		// We show Panel1 first, obviously
		this.displayPanel1();

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

	private void displayPanel1() {
		this.setPreferredSize(null); // We want to recompute the size each time


		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		MyGridBagConstraints c = new MyGridBagConstraints();
		c.expandHorizontal().setAnchor("C");

		mainPanel.add(new JLabel("Define Style for:"), c);
		mainPanel.add(this.selectNetwork, c.nextCol());

		JPanel copyButtonPanel = new JPanel();
		copyButtonPanel.setLayout(new FlowLayout());
		copyButtonPanel.add(this.copyButton);

		mainPanel.add(new JLabel("Copy Style from:"), c.nextRow());
		mainPanel.add(this.selectCopyNetwork, c.nextCol());
		mainPanel.add(copyButtonPanel, c.nextRow().useNCols(2));
		mainPanel.add(new JSeparator(), c.nextRow());
		c.useNCols(1); // reset the default value

		// This JLabel must be displayed in the top-left corner
		// We add 5px on top because otherwise the JLabel is not aligned with the center of the first JComboBox
		mainPanel.add(new JLabel("Select Values:"), c.nextRow().setAnchor("NW").setInsets(5, 0, 0, 0));
		//reset constraint
		c.setAnchor("C").setInsets(0, 0, 0, 0);
		mainPanel.add(this.selectValues, c.nextCol());

		mainPanel.add(this.filteredCheck, c.nextRow().useNCols(2));
		c.useNCols(1);

		mainPanel.add(new JLabel("Select Chart Type:"), c.nextRow());
		mainPanel.add(this.selectChartType, c.nextCol());

		mainPanel.add(new JLabel("Select Labels:"), c.nextRow());
		mainPanel.add(this.selectChartLabels, c.nextCol());

		mainPanel.add(new JLabel("Mapping:"), c.nextRow());
		mainPanel.add(this.selectDiscreteContinuous, c.nextCol());

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
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

		OVStyle ovStyle = null;
		if(this.ovCon != null) {
			ovStyle = this.ovCon.getStyle();
		}

		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		List<String> colNames = this.selectValues.getValues();
		Class<?> valueType = this.selectValues.getValueType();

		// We only look at values of rows connected to the network
		List<CyRow> valueRows = new ArrayList<>();
		for(CyRow netRow : this.ovCon.getNetwork().getDefaultNodeTable().getAllRows()) {
			valueRows.addAll(this.ovCon.getLinkedRows(netRow));
		}

		this.transposeCheck.setSelected(false);

		if(this.selectDiscreteContinuous.getSelectedItem() == OVStyleWindow.CONTINUOUS) {
			double rangeMin=0.0, rangeZero=0.0, rangeMax=0.0; // enhancedGraphics uses double for ranges
			Color colorMin = DEFAULT_MIN_COLOR;
			Color colorZero = DEFAULT_ZERO_COLOR;
			Color colorMax = DEFAULT_MAX_COLOR;

			boolean styleLoaded=false;
			if(ovStyle != null && ovStyle.getColors() instanceof OVColorContinuous) {
				// There is already a style applied for this, we simply load the information from it
				OVColorContinuous colorStyle = (OVColorContinuous) ovStyle.getColors();

				rangeMax = colorStyle.getRangeMax();
				rangeZero = colorStyle.getRangeZero();
				rangeMin = colorStyle.getRangeMin();

				colorMin = colorStyle.getDown();
				colorZero = colorStyle.getZero();
				colorMax = colorStyle.getUp();

				this.transposeCheck.setSelected(ovStyle.isTranspose());

				styleLoaded=true;
			}

			if(reset || !styleLoaded) {
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
						
						colorMin = DEFAULT_MIN_COLOR_2;
						colorZero = DEFAULT_ZERO_COLOR_2;
						colorMax = DEFAULT_MAX_COLOR_2;
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
						
						colorMin = DEFAULT_MIN_COLOR_2;
						colorZero = DEFAULT_ZERO_COLOR_2;
						colorMax = DEFAULT_MAX_COLOR_2;
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
						
						colorMin = DEFAULT_MIN_COLOR_2;
						colorZero = DEFAULT_ZERO_COLOR_2;
						colorMax = DEFAULT_MAX_COLOR_2;
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
			this.colorButtons = new JButton[3];

			this.rangeMax = new JTextField(String.valueOf(rangeMax));
			this.colorPanels[0] = new ColorPanel(colorMax, this);
			this.colorButtons[0] = new JButton("Pick color");
			this.colorButtons[0].addActionListener(this);
			mainPanel.add(this.rangeMax, c.nextRow());
			mainPanel.add(this.colorPanels[0], c.nextCol().expandBoth());
			mainPanel.add(this.colorButtons[0], c.nextCol().expandHorizontal());

			this.rangeZero = new JTextField(String.valueOf(rangeZero));
			this.colorPanels[1] = new ColorPanel(colorZero, this);
			this.colorButtons[1] = new JButton("Pick color");
			this.colorButtons[1].addActionListener(this);
			mainPanel.add(this.rangeZero, c.nextRow());
			mainPanel.add(this.colorPanels[1], c.nextCol().expandBoth());
			mainPanel.add(this.colorButtons[1], c.nextCol().expandHorizontal());

			this.rangeMin = new JTextField(String.valueOf(rangeMin));
			this.colorPanels[2] = new ColorPanel(colorMin, this);
			this.colorButtons[2] = new JButton("Pick color");
			this.colorButtons[2].addActionListener(this);
			mainPanel.add(this.rangeMin, c.nextRow());
			mainPanel.add(this.colorPanels[2], c.nextCol().expandBoth());
			mainPanel.add(this.colorButtons[2], c.nextCol().expandHorizontal());

			if(this.selectChartType.getSelectedItem().equals(ChartType.CIRCOS)) {
				// Only CIRCOS can have several layouts
				mainPanel.add(transposeCheck, c.nextRow().useNCols(3));
			}

		} else { // Discrete mapping
			Set<Object> values = new HashSet<>();

			if(!reset && ovStyle != null && ovStyle.getColors() instanceof OVColorDiscrete) {
				// There is already a style applied for this, we simply load the information from it
				OVColorDiscrete colorStyle = (OVColorDiscrete) ovStyle.getColors();

				values = colorStyle.getValues();

				this.transposeCheck.setSelected(ovStyle.isTranspose());
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

			mainPanel.setLayout(new BorderLayout());

			this.colorPanels = new ColorPanel[values.size()];
			this.colorButtons = new JButton[values.size()];
			// To be sure that values and colors are well associated, we do not use toArray() but we copy each value
			this.discreteValues = new Object[values.size()];

			JPanel valuesList = new JPanel();
			valuesList.setLayout(new GridBagLayout());
			MyGridBagConstraints clist = new MyGridBagConstraints();
			clist.expandHorizontal();
			int i=0;
			for(Object val : values) {
				Color color = generateRandomColor();

				if(ovStyle != null && ovStyle.getColors() instanceof OVColorDiscrete) {
					OVColorDiscrete colorStyle = (OVColorDiscrete) ovStyle.getColors();
					color = colorStyle.getColor(val);

					if(color == null) {
						color = generateRandomColor();
					}
				}

				this.discreteValues[i] = val;

				this.colorPanels[i] = new ColorPanel(color, this);
				this.colorButtons[i] = new JButton("Pick color");
				this.colorButtons[i].addActionListener(this);

				valuesList.add(new JLabel(val.toString()), clist.nextRow());
				valuesList.add(this.colorPanels[i], clist.nextCol().expandBoth());
				valuesList.add(this.colorButtons[i], clist.nextCol().expandHorizontal());

				++i;
			}
			JScrollPane valuesScroll = new JScrollPane(valuesList);
			valuesScroll.setBorder(null);

			mainPanel.add(valuesScroll, BorderLayout.CENTER);
			if(this.selectChartType.getSelectedItem().equals(ChartType.CIRCOS)) {
				// Only CIRCOS can have several layers
				mainPanel.add(transposeCheck, BorderLayout.SOUTH);
			}
		}

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		buttonPanel.add(this.backButton);
		buttonPanel.add(this.resetButton);
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
			this.setTitle("Style");
		} else {
			String title = "Style of " + ovTable.getTitle();

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
		this.selectCopyNetwork.removeAllItems();
		for(OVConnection ovCon : this.ovManager.getConnections(this.ovTable)) {
			this.selectNetwork.addItem(ovCon.getNetwork().toString());
			this.selectCopyNetwork.addItem(ovCon.getNetwork().toString());
		}
		this.selectNetwork.addActionListener(this);

		this.selectValues.setTable(ovTable);

		this.selectChartLabels.removeAllItems();

		this.selectChartLabels.addItem(OVStyleWindow.NO_LABEL);
		for(String colName : this.ovTable.getColNames()) {
			if(!OVShared.isOVCol(colName)) {
				this.selectChartLabels.addItem(colName);
			}
		}

		// We look for the current displayed network
		CyNetwork currentNetwork=null;

		CyApplicationManager appManager = this.ovManager.getService(CyApplicationManager.class);
		currentNetwork=appManager.getCurrentNetwork();

		if(currentNetwork != null
				&& this.ovManager.getConnection(currentNetwork) != null
				&& this.ovManager.getConnection(currentNetwork).getOVTable().equals(ovTable)) {
			this.selectNetwork.setSelectedItem(currentNetwork.toString());
			this.selectCopyNetwork.setSelectedItem(currentNetwork.toString());
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
			if(ovCon.getNetwork().toString().equals(this.selectNetwork.getSelectedItem())) {
				this.ovCon = ovCon;

				this.setTitle(this.ovTable, this.ovCon.getNetwork().toString());

				this.selectCopyNetwork.setSelectedItem(this.ovCon.getNetwork().toString());

				this.updateStyle(this.ovCon.getStyle());

				this.selectValues.setTable(this.ovCon.getOVTable());
				this.selectValues.setStyle(this.ovCon.getStyle());

				// We change the network to the one selected
				CyApplicationManager appManager = this.ovManager.getService(CyApplicationManager.class);
				if(!this.ovCon.getNetwork().equals(appManager.getCurrentNetwork())) {
					appManager.setCurrentNetwork(this.ovCon.getNetwork());
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


		Class<?> valueType = this.selectValues.getValueType();
		if(valueType == String.class || valueType == Boolean.class) {
			// No choice but discrete mapping
			this.oldDC = (String) this.selectDiscreteContinuous.getSelectedItem();
			this.selectDiscreteContinuous.setSelectedItem(OVStyleWindow.DISCRETE);
			this.selectDiscreteContinuous.setEnabled(false);
		} else if(!this.selectDiscreteContinuous.isEnabled()) {
			this.selectDiscreteContinuous.setEnabled(true);
			this.selectDiscreteContinuous.setSelectedItem(this.oldDC);
		}
	}

	private void updateStyle(OVStyle ovStyle) {
		this.ovCon.setStyle(ovStyle);

		// We only update the Panel1 information, the information of Panel2 always check for ovStyle
		this.selectValues.setStyle(ovStyle);
		if(ovStyle != null) {
			this.filteredCheck.setSelected(ovStyle.isOnlyFiltered());
			this.selectChartType.setSelectedItem(ovStyle.getType());
			if(ovStyle.getLabel() == null) {
				this.selectChartLabels.setSelectedItem(OVStyleWindow.NO_LABEL);
			} else {
				this.selectChartLabels.setSelectedItem(ovStyle.getLabel());
			}
			if(ovStyle.getColors() instanceof OVColorDiscrete) {
				this.selectDiscreteContinuous.setSelectedItem(OVStyleWindow.DISCRETE);
			} else {
				this.selectDiscreteContinuous.setSelectedItem(OVStyleWindow.CONTINUOUS);
			}
		} else {
			this.selectChartType.setSelectedIndex(0);
			this.selectChartLabels.setSelectedIndex(0);
			this.selectDiscreteContinuous.setSelectedIndex(0);
		}

		this.styleUpdated();
	}

	private void styleUpdated() {
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

	private Color generateRandomColor() {
		Random rand = new Random();

		int r = rand.nextInt(255);
		int g = rand.nextInt(255);
		int b = rand.nextInt(255);

		return new Color(r,g,b);
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
		if(e.getSource() == this.selectNetwork) {
			this.changedNetwork();
		} else if(e.getSource() == this.copyButton) {
			for(OVConnection ovCon : this.ovManager.getConnections(this.ovTable)) {
				if(ovCon.getNetwork().toString().equals(this.selectCopyNetwork.getSelectedItem())) {
					this.updateStyle(ovCon.getStyle());
					break;
				}
			}
		} else if(e.getSource() == this.nextButton) {
			if(!this.selectValues.allSameType()) {
				JOptionPane.showMessageDialog(null,
						"All the values should have the same type.",
						"Error: Bad value types",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			// We look if something has changed from the loaded style
			// To know if we have to reset panel2 or not
			boolean reset = false;
			if(this.ovCon.getStyle() != null) {
				reset = !ovCon.getStyle().getValues().equals(this.selectValues.getValues());
				reset |= ovCon.getStyle().isOnlyFiltered() != this.filteredCheck.isSelected();
			}

			this.displayPanel2(reset);
		} else if(e.getSource() == this.backButton) {
			this.displayPanel1();
		} else if(e.getSource() == this.resetButton) {
			this.displayPanel2(true);
		} else if(e.getSource() == this.drawButton) {
			OVColor colors = null;

			if(this.selectDiscreteContinuous.getSelectedItem().equals(OVStyleWindow.CONTINUOUS)) {
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
			if(!this.selectChartLabels.getSelectedItem().equals(OVStyleWindow.NO_LABEL)) {
				label = (String) this.selectChartLabels.getSelectedItem();
			}

			this.selectValues.getValues();

			OVStyle ovStyle = new OVStyle((ChartType) this.selectChartType.getSelectedItem(),
					this.selectValues.getValues(),
					this.selectValues.getValueType(),
					this.filteredCheck.isSelected(),
					colors,
					label,
					this.transposeCheck.isSelected());

			this.ovCon.setStyle(ovStyle);

			ApplyStyleTaskFactory factory = new ApplyStyleTaskFactory(this.ovManager, this.ovCon, this.filteredCheck.isSelected());
			this.ovManager.executeTask(factory.createTaskIterator());

			this.setVisible(false);
		} else if(this.colorButtons != null) {
			// We have to identify which colorButton was clicked
			for(int i=0; i<this.colorButtons.length; ++i) {
				if(e.getSource() == this.colorButtons[i]) {
					this.colorChooser.show(this.colorPanels[i]);

					break;
				}
			}
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

		private OVStyleWindow ovStyleWindow;

		private List<String> selectItemStringValues;
		private ChartValues[] selectItemChartValues;

		private List<JComboBox<ChartValues>> selects;
		private List<JButton> buttons;
		private JButton addButton;

		private ComboBoxActionListener comboBoxActionListener;

		public SelectValuesPanel(OVStyleWindow ovStyleWindow) {
			this.ovStyleWindow = ovStyleWindow;

			this.selects = new ArrayList<>();
			this.buttons = new ArrayList<>();

			this.addButton = new JButton("add");
			this.addButton.addActionListener(this);

			this.comboBoxActionListener = new ComboBoxActionListener(this.ovStyleWindow);
		}

		public void setTable(OVTable ovTable) {
			if(ovTable != null) {
				this.selectItemStringValues = new ArrayList<>();
				List<ChartValues> selectItems = new ArrayList<>();
				for(String colName : ovTable.getColNames()) {
					Class<?> colType = ovTable.getColType(colName);
					// We don't want OVCol, neither do we want List columns
					if(!OVShared.isOVCol(colName) && colType != List.class) {
						selectItems.add(new ChartValues(colName, colType));
						this.selectItemStringValues.add(colName);
					}
				}
				this.selectItemChartValues = new ChartValues[selectItems.size()];
				this.selectItemChartValues = selectItems.toArray(this.selectItemChartValues);
			}
		}

		public void setStyle(OVStyle ovStyle) {
			if(this.selectItemChartValues == null) {
				return;
			}

			this.selects = new ArrayList<>();
			this.buttons = new ArrayList<>();

			if(ovStyle == null) {
				this.createSelect();

				JButton del = new JButton("del");
				del.addActionListener(this);
				this.buttons.add(del);
			} else {
				for(String val : ovStyle.getValues()) {
					// We create the JComboBox and select the style value
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
			c.expandHorizontal();

			this.add(this.selects.get(0), c);
			this.add(this.buttons.get(0), c.nextCol());
			for(int i=1; i<this.selects.size(); ++i) {
				this.add(this.selects.get(i), c.nextRow());
				this.add(this.buttons.get(i), c.nextCol());
			}

			this.add(this.addButton, c.nextRow().nextCol());

			this.ovStyleWindow.styleUpdated();
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
			// But we will look for the first ChartValues that has the same type as the other selected items
			// We take the first select as a reference
			select.removeActionListener(this.comboBoxActionListener);
			while(((ChartValues)select.getSelectedItem()).getColType() != ((ChartValues)this.selects.get(0).getSelectedItem()).getColType()) {
				select.setSelectedIndex(select.getSelectedIndex()+1);
			}
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
			private OVStyleWindow ovStyleWindow;

			public ComboBoxActionListener(OVStyleWindow ovStyleWindow) {
				super();
				this.ovStyleWindow = ovStyleWindow;
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				this.ovStyleWindow.checkValueTypes();
			}

		}
	}
}
