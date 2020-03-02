package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.application.swing.CyColumnSelector;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.events.SelectedNodesAndEdgesEvent;
import org.cytoscape.model.events.SelectedNodesAndEdgesListener;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.task.destroy.DeleteTableTaskFactory;
import org.cytoscape.util.color.Palette;
import org.cytoscape.util.color.PaletteProvider;
import org.cytoscape.util.color.PaletteProviderManager;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVColorContinuous;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVColorDiscrete;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVConnection;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVVisualization;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVVisualization.ChartType;
import dk.ku.cpr.OmicsVisualizer.internal.task.FilterTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.ui.table.OVTableModel;
import dk.ku.cpr.OmicsVisualizer.internal.utils.TextIcon;
import dk.ku.cpr.OmicsVisualizer.internal.utils.ViewUtil;

public class OVCytoPanel extends JPanel
implements CytoPanelComponent2,
ActionListener,
PopupMenuListener,
SetCurrentNetworkListener,
SelectedNodesAndEdgesListener {

	private static final long serialVersionUID = 1L;

	private OVManager ovManager;

	private JTable mainTable=null;
	private JScrollPane scrollPane=null;
	private OVTableModel mainTableModel=null;
	private final Font iconFont;
	private Font iconStringFont=null;
	private Font iconChartsFont=null;
	private final Color iconActive;
	private final Color iconInactive;

	private IconManager iconManager=null;

	private GlobalTableChooser tableChooser=null;

	private JButton selectButton=null;
	private JButton filterButton=null;
	private JButton deleteTableButton=null;
	private JButton retrieveNetworkButton=null;
	private JButton connectButton=null;
	private JButton vizInnerButton=null;
	private JButton vizOuterButton=null;
	private JButton legendButton=null;
	
	// According to Charts Font
	private static final char INNER_CHART_LETTER = 'e';
	private static final char OUTER_CHART_LETTER = 'a';

	private JPopupMenu columnSelectorPopupMenu=null;
	private CyColumnSelector columnSelector=null;

	private OVFilterWindow filterWindow=null;
	private OVConnectWindow connectWindow=null;
	private OVVisualizationWindow vizInnerWindow=null;
	private OVVisualizationWindow vizOuterWindow=null;
	private OVLegendWindow legendWindow=null;
	private OVRetrieveStringNetworkWindow retrieveWindow=null;

	private JPanel toolBarPanel=null;
	private SequentialGroup hToolBarGroup=null;
	private ParallelGroup vToolBarGroup=null;
	private JScrollPane toolBarPane;

	private OVTable displayedTable=null;

	private final  float ICON_FONT_SIZE = 22.0f;

	public OVCytoPanel(OVManager ovManager) {
		this.setLayout(new BorderLayout());
		this.setOpaque(!LookAndFeelUtil.isAquaLAF());
		this.ovManager=ovManager;

		iconManager = this.ovManager.getServiceRegistrar().getService(IconManager.class);
		iconFont = iconManager.getIconFont(ICON_FONT_SIZE);
		
		try {
			this.iconStringFont = Font.createFont(Font.TRUETYPE_FONT, OVCytoPanel.class.getResourceAsStream("/fonts/string.ttf"));
			this.iconStringFont = this.iconStringFont.deriveFont(ICON_FONT_SIZE);
		} catch (FontFormatException e) {
			this.iconStringFont=null;
		} catch (IOException e) {
			this.iconStringFont=null;
		}
		
		try {
			this.iconChartsFont = Font.createFont(Font.TRUETYPE_FONT, OVCytoPanel.class.getResourceAsStream("/fonts/charts.ttf"));
			this.iconChartsFont = this.iconChartsFont.deriveFont(ICON_FONT_SIZE);
		} catch (FontFormatException e) {
			this.iconChartsFont=null;
		} catch (IOException e) {
			this.iconChartsFont=null;
		}

		iconActive = new Color(0,153,0); // Green
		iconInactive = Color.BLACK;

		this.reload();
	}

	public void reload() {
		ViewUtil.invokeOnEDT(() -> {
			tableChooser = new GlobalTableChooser();
			tableChooser.addActionListener(this);

			GlobalTableComboBoxModel tcModel = (GlobalTableComboBoxModel)tableChooser.getModel();
			for(OVTable table : ovManager.getOVTables()) {
				tcModel.addAndSetSelectedItem(table);

				// We look for a potential filter previously applied to the table
				if(table.getFilter() != null) {
					FilterTaskFactory factory = new FilterTaskFactory(this.ovManager, this);
					TaskIterator ti = factory.createTaskIterator(table);

					this.ovManager.executeTask(ti);
				}
			}

			initPanel(null);
		});
	}

	private OVTable getLastAddedTable() {
		List<OVTable> ovTables = this.ovManager.getOVTables();

		if(ovTables.size() == 0)
			return null;

		return ovTables.get(ovTables.size()-1);
	}

	public String getIdentifier() {
		return OVShared.CYTOPANEL_NAME;
	}

	public Component getComponent() {
		return this;
	}

	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.SOUTH;
	}

	public String getTitle() {
		return "Omics Visualizer Tables";
	}

	public Icon getIcon() {
		return null;
	}

	public OVTable getDisplayedTable() {
		return this.displayedTable;
	}

	public void addToolBarComponent(final JComponent component, final ComponentPlacement placement) {
		if (placement != null)
			hToolBarGroup.addPreferredGap(placement);

		hToolBarGroup.addComponent(component);
		vToolBarGroup.addComponent(component, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
	}

	protected void styleButton(final AbstractButton btn, final Font font) {
		this.styleButton(btn, font, null);
	}

	protected void styleButton(final AbstractButton btn, final Font font, final Color color) {
		btn.setFont(font);
		btn.setBorder(null);
		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);

		if(color != null) {
			btn.setForeground(color);
		}

		int w = 32, h = 32;

		if (tableChooser != null)
			h = Math.max(h, tableChooser.getPreferredSize().height);

		btn.setMinimumSize(new Dimension(w, h));
		btn.setPreferredSize(new Dimension(w, h));
	}
	
	private Palette getPalette(String paletteName) {
		Palette p=null;
		
		for(PaletteProvider paletteProvider : this.ovManager.getService(PaletteProviderManager.class).getPaletteProviders()) {
			p=paletteProvider.getPalette(paletteName);
			if(p != null) {
				return p;
			}
		}
		
		return p;
	}
	
	/**
	 * Creates an Icon for a given Visualization.
	 * The Charts Icon is made so that a chart character is followed by the 3 parts of the charts separately.
	 * @param ovViz The Visualization to create the Icon
	 * @param chart The letter used in the Charts Font to describe the visualization
	 * @return The corresponding TextIcon
	 */
	protected TextIcon getChartIcon(OVVisualization ovViz, char chart) {
		int w=(int)ICON_FONT_SIZE;
		int h=(int)ICON_FONT_SIZE;
		
		if(ovViz == null) {
			return new TextIcon(Character.toString(chart), iconChartsFont, Color.BLACK, w, h);
		} else {
			Color textColors[] = {iconActive, iconActive, iconActive};
			if(ovViz.getColors() instanceof OVColorContinuous) {
				OVColorContinuous ovColors = (OVColorContinuous) ovViz.getColors();
				textColors[0] = ovColors.getDown();
				textColors[1] = ovColors.getZero();
				textColors[2] = ovColors.getUp();
			} else {
				OVColorDiscrete ovColors = (OVColorDiscrete) ovViz.getColors();
				Object values[] = (new TreeSet<Object>(ovColors.getValues())).toArray();
				Map<Object,Color> colorMapping = ovColors.getMapping();
				int nbValues = values.length;
				
				if(nbValues >= 3) {
					textColors[0] = colorMapping.get(values[0]);
					textColors[1] = colorMapping.get(values[nbValues/2]);
					textColors[2] = colorMapping.get(values[nbValues-1]);
				} else {
					Palette p = this.getPalette(ovViz.getPaletteName());
					if(p == null) {
						return new TextIcon(Character.toString(chart), iconChartsFont, Color.BLACK, w, h);
					}
					Color paletteColors[] = p.getColors(9);
					
					if(nbValues == 2) {
						textColors[0] = colorMapping.get(values[0]);
						textColors[1] = new Color(190, 190, 190); // Missing values color
						textColors[2] = colorMapping.get(values[1]);
					} else if(nbValues == 1) {
						textColors[0] = colorMapping.get(paletteColors[0]);
						textColors[1] = colorMapping.get(values[0]);
						textColors[2] = colorMapping.get(paletteColors[8]);
					}
				}
			}
			
			String texts[] = {Character.toString((char) (chart+1)), Character.toString((char) (chart+2)), Character.toString((char) (chart+3))};
			
			return new TextIcon(texts, iconChartsFont, textColors, w, h);
		}
	}

	private JPopupMenu getColumnSelectorPopupMenu() {
		if (columnSelectorPopupMenu == null) {
			columnSelectorPopupMenu = new JPopupMenu();
			columnSelectorPopupMenu.add(getColumnSelector());
			columnSelectorPopupMenu.addPopupMenuListener(this);
			columnSelectorPopupMenu.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (SwingUtilities.isRightMouseButton(e)) {
						columnSelectorPopupMenu.setVisible(false);
					}
				}
			});
		}

		return columnSelectorPopupMenu;
	}

	private CyColumnSelector getColumnSelector() {
		if (columnSelector == null) {
			IconManager iconManager = ovManager.getService(IconManager.class);
			CyColumnPresentationManager presentationManager = ovManager.getService(CyColumnPresentationManager.class);
			columnSelector = new CyColumnSelector(iconManager, presentationManager);
		}

		return columnSelector;
	}
	
	public OVFilterWindow getFilterWindow() {
		if(this.filterWindow == null) {
			this.filterWindow = new OVFilterWindow(this.ovManager);
		}
		
		return this.filterWindow;
	}

	public OVConnectWindow getConnectWindow() {
		if(this.connectWindow == null) {
			this.connectWindow = new OVConnectWindow(this.ovManager);
		}

		return this.connectWindow;
	}

	public OVVisualizationWindow getVizInnerWindow() {
		if(this.vizInnerWindow == null) {
			this.vizInnerWindow = new OVVisualizationWindow(this.ovManager, ChartType.PIE);
		}

		return this.vizInnerWindow;
	}
	public OVVisualizationWindow getVizOuterWindow() {
		if(this.vizOuterWindow == null) {
			this.vizOuterWindow = new OVVisualizationWindow(this.ovManager, ChartType.CIRCOS);
		}

		return this.vizOuterWindow;
	}

	public OVLegendWindow getLegendWindow() {
		if(this.legendWindow == null) {
			this.legendWindow = new OVLegendWindow(this.ovManager);
		}

		return this.legendWindow;
	}

	public OVRetrieveStringNetworkWindow getRetrieveWindow() {
		if(this.retrieveWindow == null) {
			this.retrieveWindow = new OVRetrieveStringNetworkWindow(this.ovManager);
		}

		return this.retrieveWindow;
	}
	
	private void resizeToolBar() {
		int viewWidth = toolBarPanel.getWidth();
		int scrollWidth = toolBarPane.getWidth();
		
		if(viewWidth > scrollWidth) {
			int viewHeight = toolBarPanel.getPreferredSize().height;
			int scrollBarHeight = toolBarPane.getHorizontalScrollBar().getPreferredSize().height;
			
			toolBarPane.setPreferredSize(new Dimension(scrollWidth, viewHeight+scrollBarHeight));
		} else {
			int viewHeight = toolBarPanel.getPreferredSize().height;
			
			toolBarPane.setPreferredSize(new Dimension(scrollWidth, viewHeight));
		}
		this.repaint();
	}
	
	public void initPanel(OVTable ovTable) {
		this.initPanel(ovTable, this.ovManager.getService(CyApplicationManager.class).getCurrentNetwork());
	}

	public void initPanel(OVTable ovTable, CyNetwork currentNetwork) {
		if(ovTable==null) {
			ovTable = this.getLastAddedTable();
		}
		
		if(ovTable == null) {
			// There was a problem in the import table, we do nothing
			return;
		}
		
		if(!this.ovManager.getOVTables().contains(ovTable)) {
			// Bug? When we load a session from a current session where OVTable are present
			// Sometimes the old OVTable remains
			return;
		}
		
		// We register the panel
		if(this.ovManager.getOVCytoPanel() == null) {
			CytoPanel cytoPanel = this.ovManager.getService(CySwingApplication.class).getCytoPanel(CytoPanelName.SOUTH);
			if (cytoPanel.indexOfComponent(OVShared.CYTOPANEL_NAME) < 0) {
				this.ovManager.registerOVCytoPanel(this);
				
				if (cytoPanel.getState() == CytoPanelState.HIDE)
					cytoPanel.setState(CytoPanelState.DOCK);

				cytoPanel.setSelectedIndex(cytoPanel.indexOfComponent(OVShared.CYTOPANEL_NAME));
			}
		}

		this.removeAll();

		if(!ovTable.equals(this.displayedTable)) {
			if(this.connectWindow != null) {
				this.connectWindow.setVisible(false);
			}
			if(this.vizInnerWindow != null) {
				this.vizInnerWindow.setVisible(false);
			}
		}

		this.displayedTable = ovTable;

		// We check for selected rows
		CyApplicationManager applicationManager = this.ovManager.getService(CyApplicationManager.class);
		this.displayedTable.displaySelectedRows(applicationManager.getCurrentNetwork());

		JTable currentTable=ovTable.getJTable();

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setOpaque(!LookAndFeelUtil.isAquaLAF());
		toolBar.setOrientation(JToolBar.HORIZONTAL);

		final GroupLayout layout = new GroupLayout(toolBar);
		toolBar.setLayout(layout);
		hToolBarGroup = layout.createSequentialGroup();
		vToolBarGroup = layout.createParallelGroup(Alignment.CENTER, false);

		// Layout information.
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(hToolBarGroup));
		layout.setVerticalGroup(vToolBarGroup);

		if (selectButton == null) {
			selectButton = new JButton(IconManager.ICON_COLUMNS);
			selectButton.setToolTipText("Show Columns");
			styleButton(selectButton, iconFont);

			selectButton.addActionListener(e -> {
				if (this.mainTableModel != null) {
					getColumnSelector().update(this.displayedTable.getColumnsInOrder(),
							this.displayedTable.getVisibleColumns());
					getColumnSelectorPopupMenu().pack();
					getColumnSelectorPopupMenu().show(selectButton, 0, selectButton.getHeight());
				}
			});
		}
		if (filterButton == null) {
			filterButton = new JButton(IconManager.ICON_FILTER);
			filterButton.setToolTipText("Filter rows");

			filterButton.addActionListener(e -> {
//				FilterTaskFactory factory = new FilterTaskFactory(this.ovManager, this);
//				this.ovManager.executeTask(factory.createTaskIterator());
				this.getFilterWindow().setVisible(true);
			});
		}
		if(this.displayedTable.getFilter() == null) {
			styleButton(filterButton, iconFont, iconInactive);
		} else {
			styleButton(filterButton, iconFont, iconActive);
		}

		if (deleteTableButton == null) {
			deleteTableButton = new JButton(IconManager.ICON_TABLE + "" + IconManager.ICON_TIMES_CIRCLE);
			deleteTableButton.setToolTipText("Delete Table...");
			styleButton(deleteTableButton, iconManager.getIconFont(ICON_FONT_SIZE / 2.0f));

			// Create pop-up window for deletion
			deleteTableButton.addActionListener(e -> removeTable());
		}
		if(retrieveNetworkButton == null) {
//			retrieveNetworkButton = new JButton(IconManager.ICON_NAVICON);
			if(this.iconStringFont == null) { // We use the image instead of the font
				retrieveNetworkButton = new JButton(new ImageIcon(OVCytoPanel.class.getResource("/images/string_logo_22.png")));
				styleButton(retrieveNetworkButton, iconFont);
			} else {
				// In the "String Font", the character "a" is the String logo
				retrieveNetworkButton = new JButton("a");
				styleButton(retrieveNetworkButton, iconStringFont);
			}
			retrieveNetworkButton.setToolTipText("Retrieve and connect the table with a String Network...");
			
			retrieveNetworkButton.addActionListener(e -> {
				AvailableCommands availableCommands = (AvailableCommands) this.ovManager.getService(AvailableCommands.class);
				if (!availableCommands.getNamespaces().contains("string")) {
					JOptionPane.showMessageDialog(this.ovManager.getService(CySwingApplication.class).getJFrame(),
							"You need to install stringApp from the App Manager or Cytoscape App Store.",
							"Dependency error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				this.getRetrieveWindow().setVisible(true);
			});
		}
		if (connectButton == null ) {
			connectButton = new JButton(IconManager.ICON_LINK);
			connectButton.setToolTipText("Manage table connections...");
			styleButton(connectButton, iconFont);

			connectButton.addActionListener(e -> {
				if(this.displayedTable != null) {
					this.getConnectWindow().setVisible(true);
				}
			});
		}
		connectButton.setEnabled(this.displayedTable != null && this.ovManager.getNetworkManager().getNetworkSet().size() != 0);
		if (vizInnerButton == null ) {
			if(this.iconChartsFont == null) {
				vizInnerButton = new JButton(IconManager.ICON_PIE_CHART);
				styleButton(vizInnerButton, iconFont);
			} else {
				// In the "Charts Font", the character "E" is the pie
				vizInnerButton = new JButton(this.getChartIcon(null, INNER_CHART_LETTER));
				styleButton(vizInnerButton, iconChartsFont);
			}
			vizInnerButton.setToolTipText("Apply a Pie Chart visualization...");

			vizInnerButton.addActionListener(e -> {
				if(this.displayedTable != null && this.displayedTable.isConnected()) {
					//					resetCharts();

					AvailableCommands availableCommands = (AvailableCommands) this.ovManager.getService(AvailableCommands.class);
					if (!availableCommands.getNamespaces().contains("enhancedGraphics")) {
						JOptionPane.showMessageDialog(this.ovManager.getService(CySwingApplication.class).getJFrame(),
								"You need to install enhancedGraphics from the App Manager or Cytoscape App Store.",
								"Dependency error", JOptionPane.ERROR_MESSAGE);
						return;
					}

					this.getVizInnerWindow().setTable(this.displayedTable);
					this.getVizInnerWindow().setVisible(true);
				}
			});
		}
		vizInnerButton.setEnabled(this.displayedTable != null && this.displayedTable.isConnectedTo(currentNetwork));
		if(iconChartsFont != null) {
			OVVisualization ovViz = null;
			
			// If the button is enabled, it means that the table is connected to the current network
			if(vizInnerButton.isEnabled()) {
				ovViz = this.displayedTable.getConnection(currentNetwork).getInnerVisualization();
			}
			
			vizInnerButton.setIcon(this.getChartIcon(ovViz, INNER_CHART_LETTER));
		}
		if (vizOuterButton == null ) {
			if(this.iconChartsFont == null) { // We use the image instead of the font
				URL imageURL = OVCytoPanel.class.getResource("/images/donut-chart.png");
				if(imageURL != null) {
					vizOuterButton = new JButton(new ImageIcon(imageURL));
				} else {
					vizOuterButton = new JButton(IconManager.ICON_CIRCLE_O); // If the image is not present, at least we display a circle
				}
				styleButton(vizOuterButton, iconFont);
			} else {
				// In the "Charts Font", the character "A" is the donut
				vizOuterButton = new JButton(this.getChartIcon(null, OUTER_CHART_LETTER));
				styleButton(vizOuterButton, iconChartsFont);
			}
			vizOuterButton.setToolTipText("Apply a Donut Chart visualization...");

			vizOuterButton.addActionListener(e -> {
				if(this.displayedTable != null && this.displayedTable.isConnected()) {
					//					resetCharts();

					AvailableCommands availableCommands = (AvailableCommands) this.ovManager.getService(AvailableCommands.class);
					if (!availableCommands.getNamespaces().contains("enhancedGraphics")) {
						JOptionPane.showMessageDialog(this.ovManager.getService(CySwingApplication.class).getJFrame(),
								"You need to install enhancedGraphics from the App Manager or Cytoscape App Store.",
								"Dependency error", JOptionPane.ERROR_MESSAGE);
						return;
					}

					this.getVizOuterWindow().setTable(this.displayedTable);
					this.getVizOuterWindow().setVisible(true);
				}
			});
		}
		vizOuterButton.setEnabled(this.displayedTable != null && this.displayedTable.isConnectedTo(currentNetwork));
		if(iconChartsFont != null) {
			OVVisualization ovViz = null;
			
			// If the button is enabled, it means that the table is connected to the current network
			if(vizOuterButton.isEnabled()) {
				ovViz = this.displayedTable.getConnection(currentNetwork).getOuterVisualization();
			}
			
			vizOuterButton.setIcon(this.getChartIcon(ovViz, OUTER_CHART_LETTER));
		}
		if (legendButton == null ) {
			legendButton = new JButton(IconManager.ICON_MAP_O);
			legendButton.setToolTipText("Generate legend...");
			styleButton(legendButton, iconFont);

			legendButton.addActionListener(e -> {
				this.getLegendWindow().init(this.displayedTable.getConnection(this.ovManager.getService(CyApplicationManager.class).getCurrentNetwork()));
				this.getLegendWindow().setVisible(true);
			});
		}
		legendButton.setEnabled(this.displayedTable != null && this.displayedTable.getConnection(currentNetwork) != null);
		
		int totalRows = this.displayedTable.getAllRows(false).size();
		String labelTxt = totalRows + " rows";
		if(this.displayedTable.getFilter() != null) {
			labelTxt = this.displayedTable.getAllRows(true).size()+" rows ("+totalRows+" before filtering)";
		}
		
		JLabel label = new JLabel(labelTxt);
		label.setHorizontalAlignment(JLabel.RIGHT);
		Font labelFont = label.getFont();
		label.setFont(labelFont.deriveFont((float)(labelFont.getSize() * 0.8)));
		
		addToolBarComponent(selectButton, ComponentPlacement.RELATED);
		addToolBarComponent(filterButton, ComponentPlacement.RELATED);
		addToolBarComponent(retrieveNetworkButton, ComponentPlacement.RELATED);
		addToolBarComponent(connectButton, ComponentPlacement.RELATED);
		addToolBarComponent(vizInnerButton, ComponentPlacement.RELATED);
		addToolBarComponent(vizOuterButton, ComponentPlacement.RELATED);
		addToolBarComponent(legendButton, ComponentPlacement.RELATED);

		if (tableChooser != null) {
			hToolBarGroup.addGap(0, 20, Short.MAX_VALUE);
			addToolBarComponent(label, ComponentPlacement.RELATED);
			addToolBarComponent(tableChooser, ComponentPlacement.RELATED);
		}
		addToolBarComponent(deleteTableButton, ComponentPlacement.RELATED);

		toolBarPanel = new JPanel();
		toolBarPanel.setLayout(new BorderLayout());
		toolBarPanel.setOpaque(!LookAndFeelUtil.isAquaLAF());
		toolBarPanel.add(toolBar, BorderLayout.CENTER);
		
		toolBarPane = new JScrollPane(toolBarPanel);
		toolBarPane.setBorder(null);
		toolBarPane.setOpaque(!LookAndFeelUtil.isAquaLAF());
		toolBarPane.getViewport().setOpaque(!LookAndFeelUtil.isAquaLAF());
		
		this.addComponentListener(new ComponentListener() {
			@Override
			public void componentShown(ComponentEvent e) {
				// Do nothing
			}
			
			@Override
			public void componentResized(ComponentEvent e) {
				resizeToolBar();
			}
			
			@Override
			public void componentMoved(ComponentEvent e) {
				// Do nothing
			}
			
			@Override
			public void componentHidden(ComponentEvent e) {
				// Do nothing
			}
		});

		scrollPane = new JScrollPane(currentTable);

		this.setLayout(new BorderLayout());
		this.add(scrollPane, BorderLayout.CENTER);
//		this.add(toolBarPanel, BorderLayout.NORTH);
		this.add(toolBarPane, BorderLayout.NORTH);

		final GlobalTableComboBoxModel comboBoxModel = (GlobalTableComboBoxModel) tableChooser.getModel();
		comboBoxModel.addAndSetSelectedItem(ovTable);

		this.mainTable = currentTable;
		this.mainTableModel = (OVTableModel)this.mainTable.getModel();

		this.revalidate();
		this.repaint();
		
		// We compute the size of toolBar
		SwingUtilities.invokeLater(() -> {
			this.updateUI();
			this.resizeToolBar();
		});
	}

	public void update() {
		this.initPanel(this.displayedTable);
	}

	private void removeTable() {
		final OVTable table = this.displayedTable;

		String title = "Please confirm this action";
		String msg = "Delete table \""+table.getTitle()+"\"?";
		int confirmValue = JOptionPane.showConfirmDialog(this, msg, title, JOptionPane.YES_NO_OPTION);

		// if user selects yes delete the table
		if (confirmValue == JOptionPane.YES_OPTION) {
			table.disconnectAll();

			final DialogTaskManager taskMgr = ovManager.getService(DialogTaskManager.class);
			final DeleteTableTaskFactory deleteTableTaskFactory = ovManager.getService(DeleteTableTaskFactory.class);

			taskMgr.execute(deleteTableTaskFactory.createTaskIterator(table.getCyTable()));
			removeTable(table);
		}
	}

	public void removeTable(OVTable ovTable) {
		this.ovManager.removeOVTable(ovTable);

		final GlobalTableComboBoxModel comboBoxModel = (GlobalTableComboBoxModel) tableChooser.getModel();
		comboBoxModel.removeItem(ovTable);

		if(this.ovManager.getOVTables().size() == 0) {
			// No more Omics Visualizer tables, we unregister the panel
			this.ovManager.unregisterOVCytoPanel();
		} else {
			initPanel(null);
		}
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final OVTable table = (OVTable) tableChooser.getSelectedItem();

		if (table == displayedTable || table == null)
			return;

		initPanel(table);
	}

	// Code from cytoscape/table-browser-impl view/GlobalTableBrowser.java
	@SuppressWarnings("serial")
	private class GlobalTableChooser extends JComboBox<OVTable> {

		private final Map<OVTable, String> tableToStringMap;

		GlobalTableChooser() {
			tableToStringMap = new HashMap<>();
			setModel(new GlobalTableComboBoxModel(tableToStringMap));
			setRenderer(new TableChooserCellRenderer(tableToStringMap));
		}
	}

	// Code from cytoscape/table-browser-impl view/GlobalTableBrowser.java
	@SuppressWarnings("serial")
	private class GlobalTableComboBoxModel extends DefaultComboBoxModel<OVTable> {

		private final Comparator<OVTable> tableComparator;
		private final Map<OVTable, String> tableToStringMap;
		private final List<OVTable> tables;

		GlobalTableComboBoxModel(final Map<OVTable, String> tableToStringMap) {
			this.tableToStringMap = tableToStringMap;
			tables = new ArrayList<>();
			tableComparator = new Comparator<OVTable>() {
				@Override
				public int compare(final OVTable table1, final OVTable table2) {
					return table1.getTitle().compareTo(table2.getTitle());
				}
			};
		}

		private void updateTableToStringMap() {
			tableToStringMap.clear();

			for (final OVTable table : tables)
				tableToStringMap.put(table, table.getTitle());
		}

		@Override
		public int getSize() {
			return tables.size();
		}

		@Override
		public OVTable getElementAt(int index) {
			return tables.get(index);
		}

		void addAndSetSelectedItem(final OVTable newTable) {
			if (!tables.contains(newTable)) {
				tables.add(newTable);
				Collections.sort(tables, tableComparator);
				updateTableToStringMap();
				fireContentsChanged(this, 0, tables.size() - 1);
			}

			// This is necessary to avoid deadlock!
			ViewUtil.invokeOnEDT(() -> {
				setSelectedItem(newTable);
			});
		}

		void removeItem(final OVTable deletedTable) {
			if (tables.contains(deletedTable)) {
				tables.remove(deletedTable);

				if (tables.size() > 0) {
					Collections.sort(tables, tableComparator);
					setSelectedItem(tables.get(0));
				} else {
					setSelectedItem(null);
				}
			}
		}
	}

	@SuppressWarnings("serial")
	private class TableChooserCellRenderer extends DefaultListCellRenderer {

		private final Map<OVTable, String> tableToStringMap;

		TableChooserCellRenderer(final Map<OVTable, String> tableToStringMap) {
			this.tableToStringMap = tableToStringMap;
		}

		@Override
		public Component getListCellRendererComponent(final JList<?> list, final Object value,
				final int index, final boolean isSelected, final boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			if (value instanceof OVTable == false) {
				setText("-- No Table --");
				return this;
			}

			final OVTable table = (OVTable) value;
			String label = tableToStringMap.get(table);

			if (label == null)
				label = table == null ? "-- No Table --" : table.getTitle();

			setText(label);

			return this;
		}
	}

	@Override
	public void popupMenuCanceled(PopupMenuEvent e) {
		// Do nothing
	}

	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		// Update actual table
		try {
			if(e.getSource()==this.columnSelectorPopupMenu) {
				final Set<String> visibleAttributes = getColumnSelector().getSelectedColumnNames();
				displayedTable.setVisibleColumns(visibleAttributes);
				//			updateEnableState();
			}
		} catch (Exception ex) {
		}
	}

	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		// Do nothing
	}

	@Override
	public void handleEvent(SetCurrentNetworkEvent e) {
		CyNetwork newCurrentNetwork = e.getNetwork();
		if(newCurrentNetwork != null) {
			CyRootNetworkManager rootNetManager = this.ovManager.getService(CyRootNetworkManager.class);
			CyRootNetwork newCurrentRootNetwork = rootNetManager.getRootNetwork(newCurrentNetwork);
			
			OVConnection ovCon = this.ovManager.getConnection(newCurrentRootNetwork);
			if(ovCon != null) {
				this.initPanel(ovCon.getOVTable(), newCurrentNetwork);
				ovCon.getOVTable().displaySelectedRows(newCurrentNetwork);
			} else {
				// We display the same table but we update the panel to disable contextual actions
				this.initPanel(this.displayedTable, newCurrentNetwork);
			}
		} else {
			// We display the same table but we update the panel to disable contextual actions
			this.initPanel(this.displayedTable, newCurrentNetwork);
		}
	}

	@Override
	public void handleEvent(SelectedNodesAndEdgesEvent event) {
		if(event.nodesChanged()) {
			CyNetwork cyNetwork = event.getNetwork();
			if(cyNetwork == null) {
				return;
			}
			
			CyRootNetworkManager rootNetManager = this.ovManager.getService(CyRootNetworkManager.class);
			OVConnection ovCon = this.ovManager.getConnection(rootNetManager.getRootNetwork(cyNetwork));

			if(ovCon != null) {
				ovCon.getOVTable().displaySelectedRows(cyNetwork);
				this.update();
			}
		}
	}
}
