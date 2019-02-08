package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.application.CyApplicationManager;
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

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;

public class OVStyleWindow extends JFrame implements ActionListener {
	private static final long serialVersionUID = 6606921043986517714L;
	
	private static final String NO_LABEL = "--- NONE ---";
	
	private OVCytoPanel cytoPanel;
	private OVManager ovManager;
	
	private OVTable table;
	
	private JButton closeButton;
	
	private JComboBox<ChartType> selectChartType;
	private JComboBox<String> selectChartValues;
	private JComboBox<String> selectChartLabels;
	
	public OVStyleWindow(OVCytoPanel cytoPanel, OVManager ovManager) {
		super();
		
		this.cytoPanel=cytoPanel;
		this.ovManager=ovManager;
		
		this.table=null;
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(3, 2));

		this.selectChartType = new JComboBox<>();
		for(ChartType ct : ChartType.values()) {
			this.selectChartType.addItem(ct);
		}
		this.selectChartType.addActionListener(this);
		mainPanel.add(new JLabel("Select Chart Type:"));
		mainPanel.add(this.selectChartType);

		this.selectChartValues = new JComboBox<String>();
		this.selectChartValues.addActionListener(this);
		mainPanel.add(new JLabel("Select Values:"));
		mainPanel.add(this.selectChartValues);

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
	
			this.selectChartValues.removeAllItems();
			this.selectChartLabels.removeAllItems();
	
			this.selectChartLabels.addItem(OVStyleWindow.NO_LABEL);
			for(String colName : this.table.getColNames()) {
				if(!OVShared.isOVCol(colName)) {
					Class<?> colType = table.getColType(colName);
					// Only Numeric column can be used as values
					if(colType == Integer.class || colType == Long.class || colType == Double.class) {
						this.selectChartValues.addItem(colName);
					}
					this.selectChartLabels.addItem(colName);
				}
			}
		}
		
		this.pack();
		this.setLocationRelativeTo(this.cytoPanel.getTopLevelAncestor());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == closeButton) {
			ChartType chartType = (ChartType) selectChartType.getSelectedItem();
			String chartValues = (String) selectChartValues.getSelectedItem();
			String chartLabels = (String) selectChartLabels.getSelectedItem();

			Class<?> chartValuesType = this.table.getColType(chartValues);
			Class<?> chartLabelsType = this.table.getColType(chartLabels);
			
			// First we erase all previous charts
			CyTable nodeTable = this.table.getLinkedNetwork().getDefaultNodeTable();
			if(nodeTable.getColumn(OVShared.CYNODETABLE_STYLECOL) != null) {
				nodeTable.deleteColumn(OVShared.CYNODETABLE_STYLECOL);
			}
			if(nodeTable.getColumn(OVShared.CYNODETABLE_STYLECOL_VALUES) != null) {
				nodeTable.deleteColumn(OVShared.CYNODETABLE_STYLECOL_VALUES);
			}
			nodeTable.createColumn(OVShared.CYNODETABLE_STYLECOL, String.class, false);
			nodeTable.createListColumn(OVShared.CYNODETABLE_STYLECOL_VALUES, chartValuesType, false);
			
			// Then we fill the columns
			for(CyNode node : this.table.getLinkedNetwork().getNodeList()) {
				String nodeStyle = chartType.getStyle();
				nodeStyle += " attributelist=\""+OVShared.CYNODETABLE_STYLECOL_VALUES+"\" colorlist=\"rainbow\"";
				ArrayList<Object> nodeValues = new ArrayList<>();
				String nodeLabels = "";
				for(CyRow tableRow : this.table.getLinkedRows(nodeTable.getRow(node.getSUID()))) {
					Object val = tableRow.get(chartValues, chartValuesType);
					if(val != null) {
						nodeValues.add(tableRow.get(chartValues, chartValuesType));
					} else {
						if(chartValuesType == Integer.class) {
							nodeValues.add(new Integer(0));
						} else if(chartValuesType == Long.class) {
							nodeValues.add(new Long(0));
						} else if(chartValuesType == Double.class) {
							nodeValues.add(new Double(0.0));
						} else {
							nodeValues.add("");
						}
					}
					if(!chartLabels.equals(OVStyleWindow.NO_LABEL)) {
						nodeLabels += tableRow.get(chartLabels, chartLabelsType);
						nodeLabels += ",";
					}
				}
				if(nodeLabels.length()>0) {
					nodeLabels = "labellist=\"" + nodeLabels.substring(0, nodeLabels.length()-1) + "\" showlabels=\"true\"";
				} else {
					nodeLabels = "showlabels=\"false\"";
				}
				
				nodeStyle += " " + nodeLabels;

				nodeTable.getRow(node.getSUID()).set(OVShared.CYNODETABLE_STYLECOL, nodeStyle);
				nodeTable.getRow(node.getSUID()).set(OVShared.CYNODETABLE_STYLECOL_VALUES, nodeValues);
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
			
			this.setVisible(false);
		}
	}
}
