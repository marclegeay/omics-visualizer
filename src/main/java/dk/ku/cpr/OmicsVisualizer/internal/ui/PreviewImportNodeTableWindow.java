package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

import dk.ku.cpr.OmicsVisualizer.external.tableimport.ui.PreviewTablePanel;
import dk.ku.cpr.OmicsVisualizer.external.tableimport.util.ImportType;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.task.CreateOVTableFromNetworkTaskFactory;

public class PreviewImportNodeTableWindow extends OVWindow {

	private static final long serialVersionUID = 7411907398894639957L;
	
	private CopyNodeTableWindow firstWindow;
	private boolean goBack;
	
	private JTextField newTableName;
	private PreviewTablePanel previewPanel;
	
	private CyNetwork selectedNetwork;
	private CyColumn keyColumn;
	private List<CyColumn> selectedColumns;

	public PreviewImportNodeTableWindow(OVManager ovManager) {
		super(ovManager, "Import from node table");
		this.setResizable(true);
		
		this.goBack = false;
		
		this.newTableName = new JTextField();
		this.previewPanel = new PreviewTablePanel(ImportType.OV_IMPORT_NODE_TABLE, this.ovManager.getService(IconManager.class));
		
		draw();
	}
	
	public void init(CopyNodeTableWindow firstWindow) {
		this.firstWindow = firstWindow;
		
		if(!this.firstWindow.isOK()) {
			return;
		}
		
		this.selectedNetwork = this.firstWindow.getNetwork();
		this.keyColumn = this.firstWindow.getKeyColumn();
		this.selectedColumns = this.firstWindow.getColumnList();
		
		List<String> selectedColnames = new ArrayList<>();
		
		boolean sameNamespace = true;
		String previousNamespace = this.selectedColumns.get(0).getNamespace();
		for(CyColumn col : this.selectedColumns) {
			// Columns can have no namespace (= null)
			if(previousNamespace == null) {
				sameNamespace &= col.getNamespace() == null;
			} else {
				sameNamespace &= previousNamespace.equals(col.getNamespace());
			}
			
			previousNamespace = col.getNamespace();
			
			selectedColnames.add(col.getName());
		}
		
		String newName = null;
		if(sameNamespace && (previousNamespace != null)) {
			newName = guessTableName(this.selectedNetwork + " - " + previousNamespace);
		} else {
			newName = guessTableName(this.selectedNetwork + " - node table");
		}
		this.newTableName.setText(newName);
		
		this.previewPanel.updatePreviewTable(this.ovManager.createCyTableFromNetwork(
				this.selectedNetwork,
				this.keyColumn.getName(),
				selectedColnames,
				this.newTableName.getText(),
				null, // we want default values
				null, // we want default values
				100 // we just want a preview of 100 first node rows
				));
		
		this.pack();
		this.setLocationRelativeTo(this.ovManager.getService(CySwingApplication.class).getJFrame());
	}
	
	private void draw() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		JPanel tableNamePanel = new JPanel();
		tableNamePanel.setLayout(new GridBagLayout());
		MyGridBagConstraints c = new MyGridBagConstraints();
		c.expandHorizontal();
		
		tableNamePanel.add(new JLabel("New table name: "), c);
		tableNamePanel.add(this.newTableName, c.nextCol());
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> {
			this.goBack = false;
			this.setVisible(false);
		});
		JButton backButton = new JButton("Back");
		backButton.addActionListener(e -> {
			this.goBack = true;
			this.setVisible(false);
		});
		JButton importButton = new JButton("Import");
		importButton.addActionListener(e -> {
			List<String> selectedColnames = new ArrayList<>();
			for(CyColumn col : this.selectedColumns) {
				selectedColnames.add(col.getName());
			}
			
			// The preview table is a table with 3 columns: key, value, source
			JTable preview = previewPanel.getPreviewTable();
			String valuesColName = (String) preview.getTableHeader().getColumnModel().getColumn(1).getHeaderValue();
			String srcColName = (String) preview.getTableHeader().getColumnModel().getColumn(2).getHeaderValue();
			
			CreateOVTableFromNetworkTaskFactory factory = new CreateOVTableFromNetworkTaskFactory(ovManager,
					selectedNetwork,
					keyColumn.getName(),
					selectedColnames,
					newTableName.getText(),
					valuesColName,
					srcColName);
			
			this.ovManager.executeSynchronousTask(factory.createTaskIterator());
			
			this.goBack = false;
			this.setVisible(false);
		});
		
		LookAndFeelUtil.equalizeSize(cancelButton, backButton, importButton);

		JPanel buttonPanel = new JPanel();
		MyGridBagConstraints cButton = new MyGridBagConstraints();
		buttonPanel.add(cancelButton, cButton);
		buttonPanel.add(backButton, cButton.nextCol());
		buttonPanel.add(importButton, cButton.nextCol());
		
		mainPanel.add(tableNamePanel, BorderLayout.NORTH);
		mainPanel.add(this.previewPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		this.setContentPane(mainPanel);
	}
	
	private String guessTableName(String original) {
		String name = original;
		
		int n=1;
		boolean found=false;
		CyTableManager tableManager = this.ovManager.getTableManager();
		for(CyTable table : tableManager.getAllTables(true)) {
			if(table.getTitle().equals(name)) {
				found = true;
			}
		}
		while(found) {
			name = original + " (" + (n++) + ")";
			found = false;
			for(CyTable table : tableManager.getAllTables(true)) {
				if(table.getTitle().equals(name)) {
					found = true;
				}
			}
		}
		
		return name;
	}
	
	public boolean goBack() {
		return this.goBack;
	}

}
