package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVConnection;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;
import dk.ku.cpr.OmicsVisualizer.internal.utils.ViewUtil;

public class OVStyleWindow extends JFrame implements ActionListener {
	private static final long serialVersionUID = 6606921043986517714L;
	
	private static final String NO_LABEL = "--- NONE ---";
	
	private OVCytoPanel cytoPanel;
	private OVManager ovManager;
	
	private OVTable table;
	
	private JComboBox<String> selectNetwork;
	private JComboBox<ChartType> selectChartType;
	private List<JComboBox<String>> selectChartValues;
	private JButton addValues;
	private JComboBox<String> selectChartLabels;
	
	private JButton closeButton;
	
	public OVStyleWindow(OVCytoPanel cytoPanel, OVManager ovManager) {
		super();
		
		this.cytoPanel=cytoPanel;
		this.ovManager=ovManager;
		
		this.table=null;
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(5, 2));
		
		this.selectNetwork = new JComboBox<>();
		this.selectNetwork.addActionListener(this);
		mainPanel.add(new JLabel("Select Network:"));
		mainPanel.add(this.selectNetwork);

		this.selectChartType = new JComboBox<>();
		for(ChartType ct : ChartType.values()) {
			this.selectChartType.addItem(ct);
		}
		this.selectChartType.addActionListener(this);
		mainPanel.add(new JLabel("Select Chart Type:"));
		mainPanel.add(this.selectChartType);

		this.selectChartValues = new ArrayList<>();
		JComboBox<String> firstSelect = new JComboBox<String>();
		firstSelect.addActionListener(this);
		this.selectChartValues.add(firstSelect);
		mainPanel.add(new JLabel("Select Values:"));
		mainPanel.add(firstSelect);
		
		mainPanel.add(new JPanel()); // We add a new empty JPanel so that the addValues JButton is under the JComboBoxes
		this.addValues = new JButton("Add Values");
		this.addValues.addActionListener(this);
		mainPanel.add(this.addValues);

		this.selectChartLabels = new JComboBox<String>();
		this.selectChartLabels.addActionListener(this);
		mainPanel.add(new JLabel("Select Labels:"));
		mainPanel.add(this.selectChartLabels);
		
		JPanel buttonPanel = new JPanel();
		closeButton = new JButton("Draw");
		closeButton.addActionListener(this);
		buttonPanel.add(closeButton);
		
		this.setLayout(new BorderLayout());
		
		mainPanel.setBackground(this.getBackground());
		buttonPanel.setBackground(this.getBackground());
		
		this.add(mainPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);
	}
	
	public void setTable(OVTable table) {
		if(!table.equals(this.table)) {
			this.table=table;
			
			this.selectNetwork.removeActionListener(this);
			for(OVConnection ovCon : this.ovManager.getConnections(this.table)) {
				this.selectNetwork.addItem(ovCon.getNetwork().toString());
			}
			this.selectNetwork.addActionListener(this);
			
			this.setTitle("Style of " + table.getTitle());
	
			for(JComboBox<String> select : this.selectChartValues) {
				select.removeAllItems();
			}
			this.selectChartLabels.removeAllItems();
	
			this.selectChartLabels.addItem(OVStyleWindow.NO_LABEL);
			for(String colName : this.table.getColNames()) {
				if(!OVShared.isOVCol(colName)) {
					for(JComboBox<String> select : this.selectChartValues) {
						select.addItem(colName);
					}
					this.selectChartLabels.addItem(colName);
				}
			}
		}
		
		this.pack();
		this.setLocationRelativeTo(this.cytoPanel.getTopLevelAncestor());
	}
	
	private void createOVListColumn(CyTable cyTable, String colName, Class<?> valueType) {
		if(cyTable.getColumn(colName) == null) {
			cyTable.createListColumn(colName, valueType, false);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == closeButton) {
			/*
			ChartType chartType = (ChartType) selectChartType.getSelectedItem();
			List<ChartValues> chartValues = new ArrayList<>();
			int colCount=0;
			List<String> attributeList = new ArrayList<>();
			for(JComboBox<String> select : selectChartValues) {
				String ovColName = (String) select.getSelectedItem();
				String ntColName = OVShared.CYNODETABLE_STYLECOL_VALUES+(++colCount);
				Class<?> type = this.table.getColType(ovColName);
				chartValues.add(new ChartValues(ovColName, ntColName, type));
				attributeList.add(ntColName);
			}
			
			String chartLabels = (String) selectChartLabels.getSelectedItem();
			Class<?> chartLabelsType = this.table.getColType(chartLabels);
			
			// First we erase all previous charts
			CyTable nodeTable = this.table.getLinkedNetwork().getDefaultNodeTable();
			OVShared.deleteOVColumns(nodeTable);
			nodeTable.createColumn(OVShared.CYNODETABLE_STYLECOL, String.class, false);
			
			// Then we fill the columns
			for(CyNode node : this.table.getLinkedNetwork().getNodeList()) {
				String nodeStyle = chartType.getStyle();
				
				nodeStyle += " colorlist=\"rainbow\"";
				
				ArrayList<Object> nodeValues = new ArrayList<>();
				String nodeLabels = "";
				for(CyRow tableRow : this.table.getLinkedRows(nodeTable.getRow(node.getSUID()))) {
					for(ChartValues cv : chartValues) {
						// We create the column if this one does not exist yet
						createOVListColumn(nodeTable, cv.getNodeTableColName(), cv.getType());
						
						Object val = tableRow.get(cv.getOVColName(), cv.getType());
						if(val != null) {
							nodeValues.add(tableRow.get(cv.getOVColName(), cv.getType()));
						} else {
							if(cv.getType() == Integer.class) {
								nodeValues.add(new Integer(0));
							} else if(cv.getType() == Long.class) {
								nodeValues.add(new Long(0));
							} else if(cv.getType() == Double.class) {
								nodeValues.add(new Double(0.0));
							} else {
								nodeValues.add("");
							}
						}
						
						nodeTable.getRow(node.getSUID()).set(cv.getNodeTableColName(), nodeValues);
					}
					if(!chartLabels.equals(OVStyleWindow.NO_LABEL)) {
						nodeLabels += tableRow.get(chartLabels, chartLabelsType);
						nodeLabels += ",";
					}
				}

				
				nodeStyle += " attributelist=\"" + String.join(",", attributeList) + "\"";
				nodeStyle += " valuelist=\"" + String.join(",", Collections.nCopies(nodeValues.size(), "1")) + "\"";
				
				
				if(nodeLabels.length()>0) {
					nodeLabels = "labellist=\"" + nodeLabels.substring(0, nodeLabels.length()-1) + "\" showlabels=\"true\"";
				} else {
					nodeLabels = "showlabels=\"false\"";
				}
				
				nodeStyle += " " + nodeLabels;

				nodeTable.getRow(node.getSUID()).set(OVShared.CYNODETABLE_STYLECOL, nodeStyle);
			}
			
			
			// Finally we draw
			VisualMappingManager vmm = this.ovManager.getService(VisualMappingManager.class);
			// The connected network has been automatically selected when the window was opened
			CyNetworkView netView = this.ovManager.getService(CyApplicationManager.class).getCurrentNetworkView();
			if(netView != null) {
				VisualMappingFunctionFactory passthroughFactory = this.ovManager.getService(VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
				VisualLexicon lex = this.ovManager.getService(RenderingEngineManager.class).getDefaultVisualLexicon();
			// Set up the passthrough mapping for the label
//				if (show) {
				VisualProperty<?> customGraphics = lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_1");
				PassthroughMapping<?,?> pMapping = (PassthroughMapping<?,?>) passthroughFactory.createVisualMappingFunction(OVShared.CYNODETABLE_STYLECOL, String.class, customGraphics);
				vmm.getVisualStyle(netView).addVisualMappingFunction(pMapping);
//				} else {
//					stringStyle
//							.removeVisualMappingFunction(lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_4"));
//				}
				netView.updateView();
			}
			*/
			
			this.setVisible(false);
		}
	}
	
	private class ChartValues {
		private String ovColName;
		private String nodeTableColName;
		private Class<?> type;
		
		public ChartValues(String ovColName, String nodeTableColName, Class<?> type) {
			super();
			this.ovColName = ovColName;
			this.nodeTableColName = nodeTableColName;
			this.type = type;
		}

		public String getOVColName() {
			return ovColName;
		}

		public String getNodeTableColName() {
			return nodeTableColName;
		}

		public Class<?> getType() {
			return type;
		}
	}
}
