package dk.ku.cpr.OmicsVisualizer.internal.ui;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.IconManager.ICON_COLUMNS;
import static org.cytoscape.util.swing.IconManager.ICON_PLUS;
import static org.cytoscape.util.swing.IconManager.ICON_TABLE;
import static org.cytoscape.util.swing.IconManager.ICON_TIMES_CIRCLE;
import static org.cytoscape.util.swing.IconManager.ICON_TRASH_O;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.application.swing.CyColumnSelector;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.RowsDeletedEvent;
import org.cytoscape.model.events.RowsDeletedListener;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.model.events.TableAboutToBeDeletedEvent;
import org.cytoscape.model.events.TableAboutToBeDeletedListener;
import org.cytoscape.model.events.TableAddedEvent;
import org.cytoscape.model.events.TableAddedListener;
import org.cytoscape.model.events.TablePrivacyChangedEvent;
import org.cytoscape.model.events.TablePrivacyChangedListener;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.destroy.DeleteTableTaskFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.work.swing.DialogTaskManager;

import dk.ku.cpr.OmicsVisualizer.internal.model.OmicsVisualizerShared;

public class OmicsVisualizerCytoPanel extends JPanel
implements CytoPanelComponent2, ListSelectionListener, ActionListener, RowsSetListener, TableModelListener,
TableAboutToBeDeletedListener, RowsDeletedListener, TableAddedListener, TablePrivacyChangedListener,
PopupMenuListener {

	private static final long serialVersionUID = 1L;

	CyServiceRegistrar serviceRegistrar;

	JTable mainTable;
	JPanel topPanel;
	JPanel mainPanel;
	JScrollPane scrollPane;
	boolean clearSelection = false;
	// JComboBox<String> boxTables;
	List<String> availableTables;
	// boolean createBoxTables = true;
	JButton butSettings; 
	JButton butDrawCharts; 
	JButton butResetCharts;
	JButton butAnalyzedNodes;
	JButton butExportTable;
	JButton butFilter;
	JLabel labelPPIEnrichment;
	JMenuItem menuItemReset; 
	JPopupMenu popupMenu;
	OmicsVisualizerTableModel mainTableModel;
	final Font iconFont;

	final String colEnrichmentTerms = "enrichmentTerms";
	final String colEnrichmentTermsPieChart = "enrichmentTermsPieChart";
	final String colEnrichmentPieChart = "enrichmentPieChart";

	final String butSettingsName = "Network-specific enrichment panel settings";
	final String butFilterName = "Filter enrichment table";
	final String butDrawChartsName = "Draw charts using default color palette";
	final String butResetChartsName = "Reset charts";
	final String butAnalyzedNodesName = "Select all analyzed nodes";
	final String butExportTableDescr = "Export enrichment table";
	
	private IconManager iconManager;
	
	private Map<CyTable, JTable> jTables;

	private final GlobalTableChooser tableChooser;
	
	private JButton selectButton;
	private JButton createNewAttributeButton;
	private JButton deleteAttributeButton;
	private JButton deleteTableButton;
	
	private JPopupMenu columnSelectorPopupMenu;
	private CyColumnSelector columnSelector;
	private JPopupMenu createColumnMenu;
	
	private JPanel toolBarPanel;
	private SequentialGroup hToolBarGroup;
	private ParallelGroup vToolBarGroup;
	
	private CyTable mainCyTable;
	
	private final  float ICON_FONT_SIZE = 22.0f;

	public OmicsVisualizerCytoPanel(CyServiceRegistrar serviceRegistrar) {
		this.setLayout(new BorderLayout());
		this.serviceRegistrar=serviceRegistrar;
		iconManager = this.serviceRegistrar.getService(IconManager.class);
		iconFont = iconManager.getIconFont(ICON_FONT_SIZE);
		
		tableChooser = new GlobalTableChooser();
		tableChooser.addActionListener(this);
		final Dimension d = new Dimension(400, tableChooser.getPreferredSize().height);
		tableChooser.setMaximumSize(d);
		tableChooser.setMinimumSize(d);
		tableChooser.setPreferredSize(d);
		tableChooser.setSize(d);
		
		this.jTables = new HashMap<CyTable, JTable>();
		
		initPanel(null);
	}

	@SuppressWarnings("unchecked")
	private CyTable getLastAddedTable() {
		CyTableManager tblMgr = this.serviceRegistrar.getService(CyTableManager.class);
		
		CyProperty<Properties> props = this.serviceRegistrar.getService(CyProperty.class, "(cyPropertyName="+OmicsVisualizerShared.CYPROPERTY_NAME+")");
		String tableIDs = props.getProperties().getProperty(OmicsVisualizerShared.PROPERTIES_TABLE_SUID);
		
		String tblIDs[] = tableIDs.split(";");
		Long tableSUID = Long.valueOf(tblIDs[tblIDs.length-1]);

		return tblMgr.getTable(tableSUID);
	}

	public String getIdentifier() {
		return OmicsVisualizerShared.CYTOPANEL_NAME;
	}

	public Component getComponent() {
		return this;
	}

	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.SOUTH;
	}

	public String getTitle() {
		return "Omics Visualizer Table";
	}
	
	public Icon getIcon() {
		return null;
	}

	public OmicsVisualizerTableModel getTableModel() { return mainTableModel; }

	// table selection handler
	public void valueChanged(ListSelectionEvent e) {
		/*
		if (e.getValueIsAdjusting())
			return;

		CyNetwork network = this.getCurrentNetwork();
		if (network == null)
			return;
		clearNetworkSelection(network);
		// TODO: clear table selection when switching
		if (this.mainTable.getSelectedColumnCount() == 1 && this.mainTable.getSelectedRow() > -1) {
			Object cellContent = this.mainTable.getModel().getValueAt(
					this.mainTable.convertRowIndexToModel(this.mainTable.getSelectedRow()),
					this.getMappingKeyIndex());
			if (cellContent instanceof List) {
				List<Long> nodeIDs = (List<Long>) cellContent;
				for (Long nodeID : nodeIDs) {
					network.getDefaultNodeTable().getRow(nodeID).set(CyNetwork.SELECTED, true);
				}
			}
		}
		 */
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		/*
		int column = e.getColumn();
		if (column != EnrichmentTerm.chartColumnCol)
			return;
		// int row = e.getFirstRow();
		// TableModel model = (TableModel)e.getSource();
		// String columnName = model.getColumnName(column);
		// Object data = model.getValueAt(row, column);
		Map<EnrichmentTerm, String> preselectedTerms = getUserSelectedTerms();
		if (preselectedTerms.size() > 0) {
			CyNetwork network = manager.getCurrentNetwork();
			ViewUtils.drawCharts(manager, preselectedTerms, manager.getChartType(network));
		}
		 */
	}

	// TODO: make this network-specific
	/*
	public void actionPerformed(ActionEvent e) {
		// if (e.getSource().equals(boxTables)) {
		// if (boxTables.getSelectedItem() == null) {
		// return;
		// }
		// // System.out.println("change selected table");
		// showTable = (String) boxTables.getSelectedItem();
		// // TODO: do some cleanup for old table?
		// createBoxTables = false;
		// initPanel();
		// createBoxTables = true;
		// } else
		TaskManager<?, ?> tm = manager.getService(TaskManager.class);
		CyNetwork network = manager.getCurrentNetwork();
		if (e.getSource().equals(butDrawCharts)) {
			resetCharts();
			// do something fancy here...
			// piechart: attributelist="test3" colorlist="modulated" showlabels="false"
			Map<EnrichmentTerm, String> preselectedTerms = getUserSelectedTerms();
			if (preselectedTerms.size() == 0) {
				preselectedTerms = getAutoSelectedTopTerms(manager.getTopTerms(network));
			}
			AvailableCommands availableCommands = (AvailableCommands) manager
					.getService(AvailableCommands.class);
			if (!availableCommands.getNamespaces().contains("enhancedGraphics")) {
				JOptionPane.showMessageDialog(null,
						"Charts will not be displayed. You need to install enhancedGraphics from the App Manager or Cytoscape App Store.",
						"No results", JOptionPane.WARNING_MESSAGE);
				return;
			}
			ViewUtils.drawCharts(manager, preselectedTerms, manager.getChartType(network));
		} else if (e.getSource().equals(butResetCharts)) {
			// reset colors and selection
			resetCharts();
		} else if (e.getSource().equals(butAnalyzedNodes)) {
			List<CyNode> analyzedNodes = ModelUtils.getEnrichmentNodes(network);  
			if (network == null || analyzedNodes == null)
				return;
			for (CyNode node : analyzedNodes) {
				network.getDefaultNodeTable().getRow(node.getSUID()).set(CyNetwork.SELECTED, true);
				// System.out.println("select node: " + nodeID);
			}
		} else if (e.getSource().equals(butFilter)) {
			// ...
			tm.execute(new TaskIterator(new FilterEnrichmentTableTask(manager, this)));
		} else if (e.getSource().equals(butSettings)) {
			tm.execute(new TaskIterator(new EnrichmentSettingsTask(manager)));
		} else if (e.getSource().equals(butExportTable)) {
			if (network != null)
				tm.execute(new TaskIterator(new ExportEnrichmentTableTask(manager, network)));
		} else if (e.getSource().equals(menuItemReset)) {
			// System.out.println("reset color now");
			Component c = (Component)e.getSource();
			JPopupMenu popup = (JPopupMenu)c.getParent();
			JTable table = (JTable)popup.getInvoker();
			// System.out.println("action listener: " + table.getSelectedRow() + " : " + table.getSelectedColumn());
			if (table.getSelectedRow() > -1) {
				resetColor(table.getSelectedRow());
			}
		}
	}
	// */
	
	public void addToolBarComponent(final JComponent component, final ComponentPlacement placement) {
		if (placement != null)
			hToolBarGroup.addPreferredGap(placement);
		
		hToolBarGroup.addComponent(component);
		vToolBarGroup.addComponent(component, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
	}

	protected void styleButton(final AbstractButton btn, final Font font) {
		btn.setFont(font);
		btn.setBorder(null);
		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);
		
		int w = 32, h = 32;
		
		if (tableChooser != null)
			h = Math.max(h, tableChooser.getPreferredSize().height);
		
		btn.setMinimumSize(new Dimension(w, h));
		btn.setPreferredSize(new Dimension(w, h));
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
			IconManager iconManager = serviceRegistrar.getService(IconManager.class);
			CyColumnPresentationManager presetationManager = serviceRegistrar.getService(CyColumnPresentationManager.class);
			columnSelector = new CyColumnSelector(iconManager, presetationManager);
		}
		
		return columnSelector;
	}
	


	private JMenuItem getJMenuItemStringAttribute() {
		final JMenuItem mi = new JMenuItem();
		mi.setText("String");
		mi.addActionListener(e -> createNewAttribute("String"));

		return mi;
	}

	private JMenuItem getJMenuItemIntegerAttribute() {
		final JMenuItem mi = new JMenuItem();
		mi.setText("Integer");
		mi.addActionListener(e -> createNewAttribute("Integer"));

		return mi;
	}

	private JMenuItem getJMenuItemLongIntegerAttribute() {
		final JMenuItem mi = new JMenuItem();
		mi.setText("Long Integer");
		mi.addActionListener(e -> createNewAttribute("Long Integer"));
		
		return mi;
	}

	private JMenuItem getJMenuItemFloatingPointAttribute() {
		final JMenuItem mi = new JMenuItem();
		mi.setText("Floating Point");
		mi.addActionListener(e -> createNewAttribute("Floating Point"));

		return mi;
	}

	private JMenuItem getJMenuItemBooleanAttribute() {
		final JMenuItem mi = new JMenuItem();
		mi.setText("Boolean");
		mi.addActionListener(e -> createNewAttribute("Boolean"));

		return mi;
	}

	private JMenuItem getJMenuItemStringListAttribute() {
		final JMenuItem mi = new JMenuItem();
		mi.setText("String");
		mi.addActionListener(e -> createNewAttribute("String List"));

		return mi;
	}

	private JMenuItem getJMenuItemIntegerListAttribute() {
		final JMenuItem mi = new JMenuItem();
		mi.setText("Integer");
		mi.addActionListener(e -> createNewAttribute("Integer List"));

		return mi;
	}

	private JMenuItem getJMenuItemLongIntegerListAttribute() {
		final JMenuItem mi = new JMenuItem();
		mi.setText("Long Integer");
		mi.addActionListener(e -> createNewAttribute("Long Integer List"));

		return mi;
	}

	private JMenuItem getJMenuItemFloatingPointListAttribute() {
		final JMenuItem mi = new JMenuItem();
		mi.setText("Floating Point");
		mi.addActionListener(e -> createNewAttribute("Floating Point List"));

		return mi;
	}

	private JMenuItem getJMenuItemBooleanListAttribute() {
		final JMenuItem mi = new JMenuItem();
		mi.setText("Boolean");
		mi.addActionListener(e -> createNewAttribute("Boolean List"));

		return mi;
	}

	private JPopupMenu getCreateColumnMenu() {
		if (createColumnMenu == null) {
			createColumnMenu = new JPopupMenu();
			
			final JMenu columnRegular = new JMenu("New Single Column");
			final JMenu columnList = new JMenu("New List Column");

			columnRegular.add(getJMenuItemIntegerAttribute());
			columnRegular.add(getJMenuItemLongIntegerAttribute());
			columnRegular.add(getJMenuItemStringAttribute());
			columnRegular.add(getJMenuItemFloatingPointAttribute());
			columnRegular.add(getJMenuItemBooleanAttribute());
			columnList.add(getJMenuItemIntegerListAttribute());
			columnList.add(getJMenuItemLongIntegerListAttribute());
			columnList.add(getJMenuItemStringListAttribute());
			columnList.add(getJMenuItemFloatingPointListAttribute());
			columnList.add(getJMenuItemBooleanListAttribute());
			
			createColumnMenu.add(columnRegular);
			createColumnMenu.add(columnList);
		}

		return createColumnMenu;
	}
	
	private Collection<CyColumn> getColumns(CyTable table) {
		Collection<CyColumn> cols = table.getColumns();
		
		CyColumn keyCol = null;
		for(CyColumn col : cols) {
			if(col.isPrimaryKey()) {
				keyCol = col;
			}
		}
		
		cols.remove(keyCol);
		
		return cols;
	}

	private String[] getAttributeArray() {
		final CyTable attrs = this.mainCyTable;
		final Collection<CyColumn> columns = attrs.getColumns();
		final String[] attributeArray = new String[columns.size() - 1];
		int index = 0;
		for (final CyColumn column : columns) {
			if (!column.isPrimaryKey())
				attributeArray[index++] = column.getName();
		}
		Arrays.sort(attributeArray);

		return attributeArray;
	}
	
	private void createNewAttribute(final String type) {
		try {
			final String[] existingAttrs = getAttributeArray();
			String newAttribName = null;
			
			do {
				newAttribName = JOptionPane.showInputDialog(this, "Column Name: ",
									    "Create New " + type + " Column",
									    JOptionPane.QUESTION_MESSAGE);
				
				if (newAttribName == null)
					return;
				
				newAttribName = newAttribName.trim();
				
				if (newAttribName.isEmpty()) {
					JOptionPane.showMessageDialog(null, "Column name must not be blank.",
						      "Error", JOptionPane.ERROR_MESSAGE);
					newAttribName = null;
				} else if (Arrays.binarySearch(existingAttrs, newAttribName) >= 0) {
					JOptionPane.showMessageDialog(null,
								      "Column " + newAttribName + " already exists.",
								      "Error", JOptionPane.ERROR_MESSAGE);
					newAttribName = null;
				}
			} while (newAttribName == null);
	
			final CyTable attrs;
			
			
			attrs = this.mainCyTable;
		
			if (type.equals("String"))
				attrs.createColumn(newAttribName, String.class, false);
			else if (type.equals("Floating Point"))
				attrs.createColumn(newAttribName, Double.class, false);
			else if (type.equals("Integer"))
				attrs.createColumn(newAttribName, Integer.class, false);
			else if (type.equals("Long Integer"))
				attrs.createColumn(newAttribName, Long.class, false);
			else if (type.equals("Boolean"))
				attrs.createColumn(newAttribName, Boolean.class, false);
			else if (type.equals("String List"))
				attrs.createListColumn(newAttribName, String.class, false);
			else if (type.equals("Floating Point List"))
				attrs.createListColumn(newAttribName, Double.class, false);
			else if (type.equals("Integer List"))
				attrs.createListColumn(newAttribName, Integer.class, false);
			else if (type.equals("Long Integer List"))
				attrs.createListColumn(newAttribName, Long.class, false);
			else if (type.equals("Boolean List"))
				attrs.createListColumn(newAttribName, Boolean.class, false);
			else
				throw new IllegalArgumentException("unknown column type \"" + type + "\".");
		} catch (IllegalArgumentException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void initPanel(CyTable table) {
		this.removeAll();

		if(table==null) {
			table = this.getLastAddedTable();
		}
		this.mainCyTable = table;
		
		JTable currentTable=this.jTables.get(table);
		if(currentTable == null) {
			currentTable = createJTable(table);
		}

		//* TODO ML
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setOrientation(JToolBar.HORIZONTAL);
		toolBar.setOpaque(!isAquaLAF());

		final GroupLayout layout = new GroupLayout(toolBar);
		toolBar.setLayout(layout);
		hToolBarGroup = layout.createSequentialGroup();
		vToolBarGroup = layout.createParallelGroup(Alignment.CENTER, false);
		
		// Layout information.
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(hToolBarGroup));
		layout.setVerticalGroup(vToolBarGroup);
		
		if (selectButton == null) {
			selectButton = new JButton(ICON_COLUMNS);
			selectButton.setToolTipText("Show Columns");
			styleButton(selectButton, iconFont);

			selectButton.addActionListener(e -> {
				if (this.mainTableModel != null) {
					getColumnSelector().update(getColumns(this.mainCyTable),
							this.mainTableModel.getVisibleColumnNames());
					getColumnSelectorPopupMenu().pack();
					getColumnSelectorPopupMenu().show(selectButton, 0, selectButton.getHeight());
				}
			});
		}
		if (createNewAttributeButton == null) {
			createNewAttributeButton = new JButton(ICON_PLUS);
			createNewAttributeButton.setToolTipText("Create New Column");
			styleButton(createNewAttributeButton, iconFont);
			
			createNewAttributeButton.addActionListener(e -> {
				if (this.mainTableModel != null)
					getCreateColumnMenu().show(createNewAttributeButton, 0, createNewAttributeButton.getHeight());
			});
			
			createNewAttributeButton.setEnabled(false);
		}
		if (deleteAttributeButton == null) {
			deleteAttributeButton = new JButton(ICON_TRASH_O);
			deleteAttributeButton.setToolTipText("Delete Columns...");
			styleButton(deleteAttributeButton, iconFont);
			
			// Create pop-up window for deletion
			deleteAttributeButton.addActionListener(e -> {
//				removeAttribute();
//				updateEnableState();
			});
		}
		if (deleteTableButton == null) {
			deleteTableButton = new JButton(ICON_TABLE + "" + ICON_TIMES_CIRCLE);
			deleteTableButton.setToolTipText("Delete Table...");
			styleButton(deleteTableButton, iconManager.getIconFont(ICON_FONT_SIZE / 2.0f));
			
			// Create pop-up window for deletion
			deleteTableButton.addActionListener(e -> removeTable());
		}
		
		addToolBarComponent(selectButton, ComponentPlacement.RELATED);
		addToolBarComponent(createNewAttributeButton, ComponentPlacement.RELATED);
		addToolBarComponent(deleteAttributeButton, ComponentPlacement.RELATED);
		addToolBarComponent(deleteTableButton, ComponentPlacement.RELATED);
		
		toolBarPanel = new JPanel();
		toolBarPanel.setLayout(new BorderLayout());
		toolBarPanel.add(toolBar, BorderLayout.CENTER);
		
		if (tableChooser != null) {
			hToolBarGroup.addGap(0, 20, Short.MAX_VALUE);
			addToolBarComponent(tableChooser, ComponentPlacement.UNRELATED);
		}
		/*

		JPanel buttonsPanelLeft = new JPanel(new GridLayout(1, 3)); 
		butFilter = new JButton(IconManager.ICON_FILTER);
		butFilter.setFont(iconFont);
		butFilter.addActionListener(this);
		butFilter.setToolTipText(butFilterName);
		butFilter.setBorderPainted(false);
		butFilter.setContentAreaFilled(false);
		butFilter.setFocusPainted(false);
		butFilter.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));

//		butDrawCharts = new JButton(chartIcon);
//		butDrawCharts.addActionListener(this);
//		butDrawCharts.setToolTipText(butDrawChartsName);
//		butDrawCharts.setBorderPainted(false);
//		butDrawCharts.setContentAreaFilled(false);
//		butDrawCharts.setFocusPainted(false);
//		butDrawCharts.setBorder(BorderFactory.createEmptyBorder(2,0,2,2));


		butResetCharts = new JButton(IconManager.ICON_CIRCLE_O);
		butResetCharts.setFont(iconFont);
		butResetCharts.addActionListener(this);
		butResetCharts.setToolTipText(butResetChartsName);
		butResetCharts.setBorderPainted(false);
		butResetCharts.setContentAreaFilled(false);
		butResetCharts.setFocusPainted(false);
		butResetCharts.setBorder(BorderFactory.createEmptyBorder(2,2,2,20));

		buttonsPanelLeft.add(butFilter);
//		buttonsPanelLeft.add(butDrawCharts);
		buttonsPanelLeft.add(butResetCharts);

		JPanel buttonsPanelRight = new JPanel(new GridLayout(1, 3)); 
		butAnalyzedNodes = new JButton(IconManager.ICON_CHECK_SQUARE_O);			
		butAnalyzedNodes.addActionListener(this);
		butAnalyzedNodes.setFont(iconFont);
		butAnalyzedNodes.setToolTipText(butAnalyzedNodesName);
		butAnalyzedNodes.setBorderPainted(false);
		butAnalyzedNodes.setContentAreaFilled(false);
		butAnalyzedNodes.setFocusPainted(false);
		butAnalyzedNodes.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		butExportTable = new JButton(IconManager.ICON_SAVE);			
		butExportTable.addActionListener(this);
		butExportTable.setFont(iconFont);
		butExportTable.setToolTipText(butExportTableDescr);
		butExportTable.setBorderPainted(false);
		butExportTable.setContentAreaFilled(false);
		butExportTable.setFocusPainted(false);
		butExportTable.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		butSettings = new JButton(IconManager.ICON_COG);
		butSettings.setFont(iconFont);
		butSettings.addActionListener(this);
		butSettings.setToolTipText(butSettingsName);
		butSettings.setBorderPainted(false);
		butSettings.setContentAreaFilled(false);
		butSettings.setFocusPainted(false);
		butSettings.setBorder(BorderFactory.createEmptyBorder(2,2,2,20));

		buttonsPanelRight.add(butAnalyzedNodes);
		buttonsPanelRight.add(butExportTable);
		buttonsPanelRight.add(butSettings);

//		Double ppiEnrichment = ModelUtils.getPPIEnrichment(network);
//		labelPPIEnrichment = new JLabel();
//		if (ppiEnrichment != null) {				
//			labelPPIEnrichment = new JLabel("PPI Enrichment: " + ppiEnrichment.toString());
//			labelPPIEnrichment.setToolTipText(
//					"<html>If the PPI enrichment is less or equal 0.05, your proteins have more interactions among themselves <br />"
//							+ "than what would be expected for a random set of proteins of similar size, drawn from the genome. Such <br />"
//							+ "an enrichment indicates that the proteins are at least partially biologically connected, as a group.</html>");
//		}

		topPanel = new JPanel(new BorderLayout());
		topPanel.add(buttonsPanelLeft, BorderLayout.WEST);
//		topPanel.add(labelPPIEnrichment, BorderLayout.CENTER);
		topPanel.add(buttonsPanelRight, BorderLayout.EAST);
		// topPanel.add(boxTables, BorderLayout.EAST);
		this.add(topPanel, BorderLayout.NORTH);
		//*/

		// System.out.println("show table: " + showTable);
		scrollPane = new JScrollPane(currentTable);
		
		this.setLayout(new BorderLayout());
		this.add(scrollPane, BorderLayout.CENTER);
		this.add(toolBarPanel, BorderLayout.NORTH);
		
		final GlobalTableComboBoxModel comboBoxModel = (GlobalTableComboBoxModel) tableChooser.getModel();
		comboBoxModel.addAndSetSelectedItem(table);
		
		this.jTables.put(table, currentTable);
		this.mainTable = currentTable;
		this.mainTableModel = (OmicsVisualizerTableModel)this.mainTable.getModel();

		// JScrollPane scrollPane2 = new JScrollPane(currentTable);
		// scrollPane.setViewportView(jTable);
		// scrollPane2.add(currentTable);

		// subPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(),
		// showTable, TitledBorder.CENTER, TitledBorder.TOP));
		// subPanel.add(scrollPane2);
		// mainPanel.add(subPanel, BorderLayout.CENTER);

//		if (tableModel != null) 
//			tableModel.filter(manager.getCategoryFilter(network), manager.getRemoveOverlap(network), manager.getOverlapCutoff(network));
		this.revalidate();
		this.repaint();
		//*/
	}

	private JTable createJTable(CyTable cyTable) {
		//*
		Collection<CyColumn> cols = cyTable.getColumns();

		// We do not display the Custom column ID name
		String[] colNames = new String[cols.size()]; //-1];
		Iterator<CyColumn> it = cols.iterator();
		int i=0;
		int custom_col_id=0;
		while(it.hasNext()) {
			CyColumn col = it.next();
			colNames[i] = col.getName();
			// We do not want to display our custom_col_id
			if(col.getName().equals(OmicsVisualizerShared.MAPPING_CUSTOM_COLID_NAME)) {
				custom_col_id = i;
			}
			i++;
		}

		OmicsVisualizerTableModel tableModel = new OmicsVisualizerTableModel(cyTable, colNames);
		JTable jTable = new JTable(tableModel);
		OmicsVisualizerTableColumnModel tcm = new OmicsVisualizerTableColumnModel();
		
		for (int i1 = 0; i1 < tableModel.getColumnCount(); i1++) {
			TableColumn tableColumn = new TableColumn(i1);
			tableColumn.setHeaderValue(tableModel.getColumnName(i1));
			tcm.addColumn(tableColumn);
		}
		jTable.setColumnModel(tcm);

		// We remove the custom_col_id from the model because we do not want it to be displayed:
		tcm.removeColumn(tcm.getColumn(custom_col_id));
		//		tcm.getColumn(EnrichmentTerm.fdrColumn).setCellRenderer(new DecimalFormatRenderer());
		//jTable.setDefaultEditor(Object.class, null);
		//jTable.setPreferredScrollableViewportSize(jTable.getPreferredSize());
		//jTable.setFillsViewportHeight(true);
		jTable.setAutoCreateRowSorter(true);
		jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		jTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTable.getSelectionModel().addListSelectionListener(this);
		jTable.getModel().addTableModelListener(this);
		//		jTable.setDefaultRenderer(Color.class, new ColorRenderer(true));
		//		jTable.setDefaultEditor(Color.class, new ColorEditor());
		//		popupMenu = new JPopupMenu();
		//		menuItemReset = new JMenuItem("Remove color");
		//		menuItemReset.addActionListener(this);
		//		popupMenu.add(menuItemReset);
		//		jTable.setComponentPopupMenu(popupMenu);
		//		jTable.addMouseListener(new MouseAdapter() {
		//
		//			public void mousePressed(MouseEvent e) {
		//				if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
		//					JTable source = (JTable) e.getSource();
		//					int row = source.rowAtPoint(e.getPoint());
		//					int column = source.columnAtPoint(e.getPoint());
		//					if (!source.isRowSelected(row)) {
		//						source.changeSelection(row, column, false, false);
		//					}
		//				}
		//			}
		//
		//			public void mouseReleased(MouseEvent e) {
		//				if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
		//					JTable source = (JTable) e.getSource();
		//					int row = source.rowAtPoint(e.getPoint());
		//					int column = source.columnAtPoint(e.getPoint());
		//					if (!source.isRowSelected(row)) {
		//						source.changeSelection(row, column, false, false);
		//					}
		//				}
		//			}
		//		});
		//		// jTable.addMouseListener(new TableMouseListener(jTable));
		//
		//		enrichmentTables.put(cyTable.getTitle(), jTable);
		//this.mainTable = jTable;
		return jTable;
		//*/
	}

	private void clearNetworkSelection(CyNetwork network) {
		List<CyNode> nodes = network.getNodeList();
		clearSelection = true;
		for (CyNode node : nodes) {
			if (network.getRow(node).get(CyNetwork.SELECTED, Boolean.class)) {
				network.getRow(node).set(CyNetwork.SELECTED, false);
			}
		}
		clearSelection = false;
	}

	/*
	public void handleEvent(RowsSetEvent rse) {
		CyNetworkManager networkManager = manager.getService(CyNetworkManager.class);
		CyNetwork selectedNetwork = null;
		if (rse.containsColumn(CyNetwork.SELECTED)) {
			Collection<RowSetRecord> columnRecords = rse.getColumnRecords(CyNetwork.SELECTED);
			for (RowSetRecord rec : columnRecords) {
				CyRow row = rec.getRow();
				if (row.toString().indexOf("FACADE") >= 0)
					continue;
				Long networkID = row.get(CyNetwork.SUID, Long.class);
				Boolean selectedValue = (Boolean) rec.getValue();
				if (selectedValue && networkManager.networkExists(networkID)) {
					selectedNetwork = networkManager.getNetwork(networkID);
				}
			}
		}
		if (selectedNetwork != null) {
			initPanel(selectedNetwork, false);
			return;
		}
		// experimental: clear term selection when all network nodes are unselected
		CyNetwork network = manager.getCurrentNetwork();
		JTable currentTable = enrichmentTables.get(showTable);
		if (!clearSelection && network != null && currentTable != null) {
			List<CyNode> nodes = network.getNodeList();
			for (CyNode node : nodes) {
				if (network.getRow(node).get(CyNetwork.SELECTED, Boolean.class)) {
					return;
				}
			}
			currentTable.clearSelection();
		}
	}
	*/

	/*
	public void resetColor(int currentRow) {
		JTable currentTable = enrichmentTables.get(showTable);
		// currentRow = currentTable.getSelectedRow();
		CyNetwork network = manager.getCurrentNetwork();
		if (network == null)
			return;
		CyTable enrichmentTable = ModelUtils.getEnrichmentTable(manager, network,
				TermCategory.ALL.getTable());
		Color color = (Color)currentTable.getModel().getValueAt(
				currentTable.convertRowIndexToModel(currentRow),
				EnrichmentTerm.chartColumnCol);
		String termName = (String)currentTable.getModel().getValueAt(
				currentTable.convertRowIndexToModel(currentRow),
				EnrichmentTerm.nameColumn);
		if (color == null || termName == null) 
			return;

		//currentTable.getModel().setValueAt(Color.OPAQUE, currentTable.convertRowIndexToModel(currentRow),
		//		EnrichmentTerm.chartColumnCol);
		for (CyRow row : enrichmentTable.getAllRows()) {
			if (enrichmentTable.getColumn(EnrichmentTerm.colName) != null
					&& row.get(EnrichmentTerm.colName, String.class) != null
					&& row.get(EnrichmentTerm.colName, String.class).equals(termName)) {
				row.set(EnrichmentTerm.colChartColor, "");
			}
		}
		tableModel.fireTableDataChanged();

		// re-draw charts if the user changed the color
		Map<EnrichmentTerm, String> preselectedTerms = getUserSelectedTerms();
		if (preselectedTerms.size() > 0)
			ViewUtils.drawCharts(manager, preselectedTerms, manager.getChartType(network));
	}

	public void resetCharts() {
		CyNetwork network = manager.getCurrentNetwork();
		if (network == null)
			return;

		CyTable nodeTable = network.getDefaultNodeTable();
		// replace columns
		ModelUtils.replaceListColumnIfNeeded(nodeTable, String.class,
				EnrichmentTerm.colEnrichmentTermsNames);
		ModelUtils.replaceListColumnIfNeeded(nodeTable, Integer.class,
				EnrichmentTerm.colEnrichmentTermsIntegers);
		ModelUtils.replaceColumnIfNeeded(nodeTable, String.class,
				EnrichmentTerm.colEnrichmentPassthrough);

		// remove colors from table?
		CyTable currTable = ModelUtils.getEnrichmentTable(manager, network,
				TermCategory.ALL.getTable());
		if (currTable.getRowCount() == 0) {
			return;
		}
		for (CyRow row : currTable.getAllRows()) {
			if (currTable.getColumn(EnrichmentTerm.colChartColor) != null
					&& row.get(EnrichmentTerm.colChartColor, String.class) != null
					&& !row.get(EnrichmentTerm.colChartColor, String.class).equals("")) {
				row.set(EnrichmentTerm.colChartColor, "");
			}
		}
		// initPanel();
		tableModel.fireTableDataChanged();
	}

	public void drawCharts() {
		CyNetwork network = manager.getCurrentNetwork();
		if (network == null)
			return;

		resetCharts();
		Map<EnrichmentTerm, String> preselectedTerms = getUserSelectedTerms();
		if (preselectedTerms.size() == 0) {
			preselectedTerms = getAutoSelectedTopTerms(manager.getTopTerms(network));
		}
		ViewUtils.drawCharts(manager, preselectedTerms, manager.getChartType(network));
	}

	private Map<EnrichmentTerm, String> getUserSelectedTerms() {
		Map<EnrichmentTerm, String> selectedTerms = new LinkedHashMap<EnrichmentTerm, String>();
		CyNetwork network = manager.getCurrentNetwork();
		if (network == null)
			return selectedTerms;

		// Set<CyTable> currTables = ModelUtils.getEnrichmentTables(manager, network);
		// for (CyTable currTable : currTables) {
		CyTable currTable = ModelUtils.getEnrichmentTable(manager, network,
				TermCategory.ALL.getTable());
		// currTable.getColumn(EnrichmentTerm.colShowChart) == null || 
		if (currTable.getRowCount() == 0) {
			return selectedTerms;
		}
		for (CyRow row : currTable.getAllRows()) {
			if (currTable.getColumn(EnrichmentTerm.colChartColor) != null
					&& row.get(EnrichmentTerm.colChartColor, String.class) != null
					&& !row.get(EnrichmentTerm.colChartColor, String.class).equals("")
					&& !row.get(EnrichmentTerm.colChartColor, String.class).equals("#ffffff")) {
				String selTerm = row.get(EnrichmentTerm.colName, String.class);
				if (selTerm != null) {
					EnrichmentTerm enrTerm = new EnrichmentTerm(selTerm,
							row.get(EnrichmentTerm.colDescription, String.class),
							row.get(EnrichmentTerm.colCategory, String.class), -1.0, -1.0,
							row.get(EnrichmentTerm.colFDR, Double.class));
					enrTerm.setNodesSUID(row.getList(EnrichmentTerm.colGenesSUID, Long.class));
					selectedTerms.put(enrTerm, row.get(EnrichmentTerm.colChartColor, String.class));
				}				
			}
		}
		// System.out.println(selectedTerms);
		return selectedTerms;
	}


	private Map<EnrichmentTerm, String> getAutoSelectedTopTerms(int termNumber) {
		Map<EnrichmentTerm, String> selectedTerms = new LinkedHashMap<EnrichmentTerm, String>();
		CyNetwork network = manager.getCurrentNetwork();
		if (network == null)
			return selectedTerms;

		CyTable currTable = ModelUtils.getEnrichmentTable(manager, network,
				TermCategory.ALL.getTable());
		if (currTable.getRowCount() == 0) {
			return selectedTerms;
		}

		// List<CyRow> rows = currTable.getAllRows();
		Color[] colors = manager.getBrewerPalette(network).getColorPalette(manager.getTopTerms(network));
		Long[] rowNames = tableModel.getRowNames();
		for (int i = 0; i < manager.getTopTerms(network); i++) {
			if (i >= rowNames.length)
				continue;
			CyRow row = currTable.getRow(rowNames[i]);
			String selTerm = row.get(EnrichmentTerm.colName, String.class);
			if (selTerm != null) {
				EnrichmentTerm enrTerm = new EnrichmentTerm(selTerm,
						row.get(EnrichmentTerm.colDescription, String.class),
						row.get(EnrichmentTerm.colCategory, String.class), -1.0, -1.0,
						row.get(EnrichmentTerm.colFDR, Double.class));
				enrTerm.setNodesSUID(row.getList(EnrichmentTerm.colGenesSUID, Long.class));
				String color = String.format("#%02x%02x%02x", colors[i].getRed(), colors[i].getGreen(),
						colors[i].getBlue());
				row.set(EnrichmentTerm.colChartColor, color);
				selectedTerms.put(enrTerm, color);
			}
		}
		// initPanel();
		tableModel.fireTableDataChanged();
		return selectedTerms;
	}
	 */
	
	private void removeTable() {
			final CyTable table = this.mainCyTable;

			if (table.getMutability() == CyTable.Mutability.MUTABLE) {
				String title = "Please confirm this action";
				String msg = "Are you sure you want to delete this table?";
				int confirmValue = JOptionPane.showConfirmDialog(this, msg, title, JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);

				// if user selects yes delete the table
				if (confirmValue == JOptionPane.OK_OPTION) {
					final DialogTaskManager taskMgr = serviceRegistrar.getService(DialogTaskManager.class);
					final DeleteTableTaskFactory deleteTableTaskFactory =
							serviceRegistrar.getService(DeleteTableTaskFactory.class);
					
					taskMgr.execute(deleteTableTaskFactory.createTaskIterator(table));
					removeTable(table);
				}
			} else if (table.getMutability() == CyTable.Mutability.PERMANENTLY_IMMUTABLE) {
				String title = "Error";
				String msg = "Can not delete this table, it is PERMANENTLY_IMMUTABLE";
				JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
			} else if (table.getMutability() == CyTable.Mutability.IMMUTABLE_DUE_TO_VIRT_COLUMN_REFERENCES) {
				String title = "Error";
				String msg = "Can not delete this table, it is IMMUTABLE_DUE_TO_VIRT_COLUMN_REFERENCES";
				JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
			}
		}
	
	private void removeTable(CyTable cyTable) {
		@SuppressWarnings("unchecked")
		CyProperty<Properties> props = this.serviceRegistrar.getService(CyProperty.class, "(cyPropertyName="+OmicsVisualizerShared.CYPROPERTY_NAME+")");
		Properties OmicsVisualizerProps = props.getProperties();
		
		String tableIDs = OmicsVisualizerProps.getProperty(OmicsVisualizerShared.PROPERTIES_TABLE_SUID);
		
		String newTableIDs = "";
		
		String tblIDs[] = tableIDs.split(";");
		for(String id : tblIDs) {
			Long tableSUID = Long.valueOf(id);
			if(tableSUID != cyTable.getSUID()) {
				newTableIDs += id+";";
			}
		}
		OmicsVisualizerProps.setProperty(OmicsVisualizerShared.PROPERTIES_TABLE_SUID, newTableIDs);
		
		this.jTables.remove(cyTable);
		
		final GlobalTableComboBoxModel comboBoxModel = (GlobalTableComboBoxModel) tableChooser.getModel();
		comboBoxModel.removeItem(cyTable);
		
		if(newTableIDs == "") {
			// No more Omics Visualizer tables, we unregister the panel
			this.serviceRegistrar.unregisterService(this, CytoPanelComponent.class);
			this.serviceRegistrar.unregisterService(this, RowsSetListener.class);
		} else {
			initPanel(null);
		}
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final CyTable table = (CyTable) tableChooser.getSelectedItem();
		
		if (table == mainCyTable || table == null)
			return;

//		serviceRegistrar.getService(CyApplicationManager.class).setCurrentTable(table);
//		showSelectedTable();
		initPanel(table);
	}

	@Override
	public void handleEvent(final TableAboutToBeDeletedEvent e) {
		final CyTable cyTable = e.getTable();
	}

	/**
	 * Switch to new table when it is registered to the table manager.
	 * 
	 * Note: This combo box only displays Omics Visualizer Table.
	 */
	@Override
	public void handleEvent(TableAddedEvent e) {
//		System.out.println("Table added");
//		final CyTable newTable = e.getTable();
//		
//		@SuppressWarnings("unchecked")
//		CyProperty<Properties> props = this.serviceRegistrar.getService(CyProperty.class, "(cyPropertyName="+OmicsVisualizerShared.CYPROPERTY_NAME+")");
//		String tableIDs = props.getProperties().getProperty(OmicsVisualizerShared.PROPERTIES_TABLE_SUID);
//		
//		boolean tableIsOmicsVisualizer=false;
//		
//		String tblIDs[] = tableIDs.split("//d+");
//		for(String id : tblIDs) {
//			Long tableSUID = Long.valueOf(id);
//			if(tableSUID == newTable.getSUID()) {
//				tableIsOmicsVisualizer=true;
//			}
//		}
//		System.out.println("Table added : " + tableIsOmicsVisualizer);
//
//		if (tableIsOmicsVisualizer) {
//			final GlobalTableComboBoxModel comboBoxModel = (GlobalTableComboBoxModel) tableChooser.getModel();
//			comboBoxModel.addAndSetSelectedItem(newTable);
////			getToolBar().updateEnableState(tableChooser);
//		}
	}

	@Override
	public void handleEvent(TablePrivacyChangedEvent e) {
//		final CyTable table = e.getSource();
//		final GlobalTableComboBoxModel comboBoxModel = (GlobalTableComboBoxModel) tableChooser.getModel();
//		final boolean showPrivateTables = showPrivateTables();
//		
//		if (!table.isPublic() && !showPrivateTables){
//			comboBoxModel.removeItem(table);
//
//			if (comboBoxModel.getSize() == 0) {
//				invokeOnEDT(() -> {
//					tableChooser.setEnabled(false);
//					// The last table is deleted, refresh the browser table (this is a special case)
//					removeTable(table);
//					serviceRegistrar.unregisterService(GlobalTableBrowser.this, CytoPanelComponent.class);
//					showSelectedTable();
//				});
//			}
//		} else if (table.isPublic() || showPrivateTables) {
//			comboBoxModel.addAndSetSelectedItem(table);
//		}
//		
//		serviceRegistrar.getService(CyApplicationManager.class).setCurrentTable(currentTable);
	}
	
	@Override
	public void handleEvent(final RowsSetEvent e) {
		//TODO
//		BrowserTable table = getCurrentBrowserTable();
//		
//		if (table == null)
//			return;
//		
//		BrowserTableModel model = (BrowserTableModel) table.getModel();
//		CyTable dataTable = model.getDataTable();
//
//		if (e.getSource() != dataTable)
//			return;
//		
//		synchronized (this) {
//			model.fireTableDataChanged();
//		}
	}
	
	@Override
	public void handleEvent(final RowsDeletedEvent e) {
		//TODO
//		BrowserTable table = getCurrentBrowserTable();
//		if (table == null)
//			return;
//		BrowserTableModel model = (BrowserTableModel) table.getModel();
//		CyTable dataTable = model.getDataTable();
//
//		if (e.getSource() != dataTable)
//			return;		
//		synchronized (this) {
//				model.fireTableDataChanged();
//		}
	}

	// Code from cytoscape/table-browser-impl view/GlobalTableBrowser.java
	private class GlobalTableChooser extends JComboBox<CyTable> {

		private final Map<CyTable, String> tableToStringMap;
		
		GlobalTableChooser() {
			tableToStringMap = new HashMap<>();
			setModel(new GlobalTableComboBoxModel(tableToStringMap));
//			setRenderer(new TableChooserCellRenderer(tableToStringMap));
		}
	}

	// Code from cytoscape/table-browser-impl view/GlobalTableBrowser.java
	private class GlobalTableComboBoxModel extends DefaultComboBoxModel<CyTable> {

		private final Comparator<CyTable> tableComparator;
		private final Map<CyTable, String> tableToStringMap;
		private final List<CyTable> tables;

		GlobalTableComboBoxModel(final Map<CyTable, String> tableToStringMap) {
			this.tableToStringMap = tableToStringMap;
			tables = new ArrayList<>();
			tableComparator = new Comparator<CyTable>() {
				@Override
				public int compare(final CyTable table1, final CyTable table2) {
					return table1.getTitle().compareTo(table2.getTitle());
				}
			};
		}

		private void updateTableToStringMap() {
			tableToStringMap.clear();
			
			for (final CyTable table : tables)
				tableToStringMap.put(table, table.getTitle());
		}

		@Override
		public int getSize() {
			return tables.size();
		}

		@Override
		public CyTable getElementAt(int index) {
			return tables.get(index);
		}

		void addAndSetSelectedItem(final CyTable newTable) {
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

		void removeItem(final CyTable deletedTable) {
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
	
	public void setVisibleColumnNames(final Collection<String> visibleAttributes) {
		OmicsVisualizerTableModel model = (OmicsVisualizerTableModel) this.mainTable.getModel();
		OmicsVisualizerTableColumnModel columnModel = (OmicsVisualizerTableColumnModel) this.mainTable.getColumnModel();
		
		for (final String name : model.getAllColumnNames()) {
			int col = model.mapColumnNameToColumnIndex(name);
			TableColumn column = columnModel.getColumnByModelIndex(col);
			columnModel.setColumnVisible(column, visibleAttributes.contains(name));
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
			final Set<String> visibleAttributes = getColumnSelector().getSelectedColumnNames();
			this.setVisibleColumnNames(visibleAttributes);
			this.mainTableModel.setVisibleColumnNames(visibleAttributes);
//			updateEnableState();
		} catch (Exception ex) {
		}
	}

	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		// Do nothing
	}
}
