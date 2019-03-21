package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.table.TableColumn;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;

import dk.ku.cpr.OmicsVisualizer.internal.properties.OVProperties;
import dk.ku.cpr.OmicsVisualizer.internal.ui.OVTableColumnModel;
import dk.ku.cpr.OmicsVisualizer.internal.ui.OVTableModel;
import dk.ku.cpr.OmicsVisualizer.internal.utils.DataUtils;

public class OVTable {
	private OVManager ovManager;
	
	private CyTable cyTable;
	private JTable jTable;
	private OVTableModel tableModel;
	private OVTableColumnModel tableColumnModel;
	
	private OVFilter filter;

	private OVProperties ovProps;
	
	public OVTable(OVManager ovManager, CyTable cyTable) {
		this.ovManager=ovManager;
		this.cyTable=cyTable;
		this.jTable=null;
		
		this.cyTable.setPublic(false);
		
		this.filter = null;
		
		this.ovProps = new OVProperties(this.ovManager, OVShared.OVPROPERTY_NAME+"-"+this.cyTable.getTitle());

		this.load();
		this.save();
	}
	
	public CyTable getCyTable() {
		return this.cyTable;
	}
	
	public JTable getJTable() {
		if(this.jTable == null) {
			this.createJTable();
		}
		
		return this.jTable;
	}
	
	public List<OVConnection> getConnections() {
		return this.ovManager.getConnections(this);
	}
	
	public OVConnection getConnection(CyRootNetwork rootNetwork) {
		if(rootNetwork == null) {
			return null;
		}
		
		for(OVConnection con : this.getConnections()) {
			if(con.getRootNetwork() == rootNetwork) {
				return con;
			}
		}
		return null;
	}
	
	public OVConnection getConnection(CyNetwork network) {
		if(network == null) {
			return null;
		}
		
		CyRootNetworkManager manager = this.ovManager.getService(CyRootNetworkManager.class);
		return this.getConnection(manager.getRootNetwork(network));
	}
	
	public boolean isConnected() {
		return this.getConnections().size()>0;
	}
	
	public boolean isConnectedTo(CyNetwork net) {
		return this.getConnection(net) != null;
	}
	
	public OVConnection connect(String rootNetName, String mappingColCyto, String mappingColOVTable) {
		CyRootNetwork linkedRootNetwork=null;
		CyRootNetworkManager manager = this.ovManager.getService(CyRootNetworkManager.class);
		
		for(CyNetwork net : this.ovManager.getNetworkManager().getNetworkSet()) {
			CyRootNetwork rootNet = manager.getRootNetwork(net);
			if(rootNet.toString().equals(rootNetName)) {
				linkedRootNetwork = rootNet;
			}
		}
		
		if(linkedRootNetwork == null) {
			return null;
		}
		
		OVConnection con = this.getConnection(linkedRootNetwork);
		
		if(con == null) { // new network collection
			con = new OVConnection(this.ovManager, this, linkedRootNetwork, mappingColCyto, mappingColOVTable);
		} else {
			con.update(mappingColCyto, mappingColOVTable);
		}
		
		return con;
	}
	
	public void disconnectAll() {
		for(OVConnection con : this.getConnections()) {
			con.disconnect();
		}
	}
	
	public void disconnect(CyNetwork network) {
		OVConnection con = this.getConnection(network);
		
		if(con != null) {
			con.disconnectNetwork(network);
		}
	}

	private void createJTable() {
		List<String> colNames = new ArrayList<String>();
		Collection<CyColumn> cols = this.cyTable.getColumns();
		for(@SuppressWarnings("unused") CyColumn c : cols) {
			colNames.add("");
		}
		
		Set<String> visibleCols = new HashSet<String>();
		
		if(this.getTableProperty(OVShared.OVTABLE_COLID_NAME, "") != "") {
			// We load the columns from the Properties
			// Cytoscape store CyTable with columns sorted alphabetically, so we stored the order of each columns in Properties
			Iterator<CyColumn> it = cols.iterator();
			String propValue;
			while(it.hasNext()) {
				CyColumn col = it.next();
				
				propValue = this.getTableProperty(col.getName());
				if(propValue != null) { // Some CyColumn are not stored in property (OVColumns)
					String propValues[] = propValue.split(",");
					
					int i = Integer.parseInt(propValues[0]);
					colNames.set(i, col.getName());
					
					boolean visible = Boolean.parseBoolean(propValues[1]);
					if(visible) {
						visibleCols.add(col.getName());
					}
				}
			}
		} else {
			// We do not display the Custom column ID name
			Iterator<CyColumn> it = cols.iterator();
			int i=0;
			while(it.hasNext()) {
				CyColumn col = it.next();
				colNames.set(i, col.getName());
				
				// We do not want to display our special OVTable columns
				if(!OVShared.isOVCol(col.getName())) {
					visibleCols.add(col.getName());
				}
				
				i++;
			}
		}

		tableModel = new OVTableModel(this, colNames);
		JTable jTable = new JTable(tableModel);
		tableColumnModel = new OVTableColumnModel(this);
		
		for (int i = 0; i < tableModel.getColumnCount(); i++) {
			TableColumn tableColumn = new TableColumn(i);
			tableColumn.setHeaderValue(tableModel.getColumnName(i));
			tableColumnModel.addColumn(tableColumn);
		}
		jTable.setColumnModel(tableColumnModel);
		
		this.setVisibleColumns(visibleCols);

		jTable.setAutoCreateRowSorter(true);
		jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//		jTable.getColumnModel().addColumnModelListener(this);
		
		this.jTable = jTable;
	}
	
	public List<String> getColNames() {
		return this.tableColumnModel.getColumnNames(false);
	}
	
	public Class<?> getColType(String colName) {
		CyColumn col = this.cyTable.getColumn(colName);
		
		if(col != null) {
			return col.getType();
		}
		
		return null;
	}
	
	public Class<?> getColListType(String colName) {
		CyColumn col = this.cyTable.getColumn(colName);
		
		if(col != null) {
			return col.getListElementType();
		}
		
		return null;
	}
	
	public Collection<CyColumn> getColumns() {
		Collection<CyColumn> cols = this.cyTable.getColumns();
		
		CyColumn keyCol = null;
		for(CyColumn col : cols) {
			if(col.isPrimaryKey()) {
				keyCol = col;
			}
		}
		
		cols.remove(keyCol);
		
		return cols;
	}
	
	public Collection<CyColumn> getColumnsInOrder() {
		Collection<CyColumn> cols = new ArrayList<CyColumn>();
		
		for(String colName : this.tableColumnModel.getColumnNames(false)) {
			if(!colName.equals(OVShared.OVTABLE_COLID_NAME)) {
				cols.add(this.cyTable.getColumn(colName));
			}
		}
		
		return cols;
	}

	public void setVisibleColumns(Set<String> visibleAttributes) {
		for (final String name : tableModel.getAllColumnNames()) {
			int col = tableModel.mapColumnNameToColumnIndex(name);
			TableColumn column = tableColumnModel.getColumnByModelIndex(col);
			tableColumnModel.setColumnVisible(column, visibleAttributes.contains(name));
		}
		
		this.save();
	}
	
	public Collection<String> getVisibleColumns() {
		return this.tableColumnModel.getColumnNames(true);
	}
	
	public void addColumn(String colName, Class<?> type) {
		this.cyTable.createColumn(colName, type, false);
		
		this.tableModel.addColumnName(colName);
		
		int modelIndex = this.tableColumnModel.getColumnCount();
		TableColumn tableColumn = new TableColumn(modelIndex);
		tableColumn.setHeaderValue(this.tableModel.getColumnName(modelIndex));
		tableColumnModel.addColumn(tableColumn);
	}
	
	public List<CyRow> getAllRows(boolean filtered) {
		if(!filtered) {
			return this.cyTable.getAllRows();
		} else {
			List<CyRow> filteredRows = new ArrayList<>();
			
			for(CyRow row : this.cyTable.getAllRows()) {
				if(this.isFiltered(row)) {
					filteredRows.add(row);
				}
			}
			
			return filteredRows;
		}
	}
	
	public List<CyRow> getSelectedRows() {
		List<CyRow> selectedRows = new ArrayList<>();
		
		List<Object> selectedKeys = ((OVTableModel)this.jTable.getModel()).getDisplayedRowKeys();
		for(int i : this.jTable.getSelectedRows()) {
			selectedRows.add(this.cyTable.getRow(selectedKeys.get(i)));
		}
		
		return selectedRows;
	}
	
	public void filter(List<Object> filteredRowKeys) {
		this.tableModel.filter(filteredRowKeys);
	}
	
	public void removeFilter() {
		this.tableModel.removeFilter();
		this.setFilter(null);
	}
	
	public boolean isFiltered(CyRow row) {
		return this.tableModel.isFiltered(row);
	}
	
	/**
	 * Get the applied filter.
	 * @return the OVFilter or <code>null</code> if no filter is applied
	 */
	public OVFilter getFilter() {
		return this.filter;
	}
	
	public void setFilter(OVFilter filter) {
		this.filter=filter;
		this.save();
	}
	
	public void selectAllRows() {
		this.tableModel.setSelectedRowKeys(new ArrayList<>());
	}
	
	public void displaySelectedRows(CyNetwork cyNetwork) {
		List<Object> selectedRowKeys = new ArrayList<>();
		OVConnection ovCon = this.getConnection(cyNetwork);
		
		if(ovCon == null) {
			this.selectAllRows();
			return;
		}
		
		for(CyRow nodeRow : cyNetwork.getDefaultNodeTable().getAllRows()) {
			if(nodeRow.get(CyNetwork.SELECTED, Boolean.class)) {
				List<CyRow> linkedRows = ovCon.getLinkedRows(nodeRow);
				for(CyRow tableRow : linkedRows) {
					selectedRowKeys.add(tableRow.getRaw(OVShared.OVTABLE_COLID_NAME));
				}
			}
		}
		
		this.tableModel.setSelectedRowKeys(selectedRowKeys);
	}
	
	public String getTitle() {
		return this.cyTable.getTitle();
	}
	
	public void save() {
		Collection<String> visibleCols = this.getVisibleColumns();
		Collection<String> cols = this.getColNames();
		Integer index = new Integer(0);
		
		for(String col : cols) {
			String savedValue = index.toString()+","+visibleCols.contains(col);
			this.setTableProperty(col, savedValue);
			index += 1;
		}

		if(this.filter != null) {
			this.setTableProperty(OVShared.PROPERTY_FILTER, this.filter.save());
		} else {
			this.setTableProperty(OVShared.PROPERTY_FILTER, "");
		}
	}
	
	public void load() {
		// We first load the filter
		String filterStr = this.getTableProperty(OVShared.PROPERTY_FILTER, "");
		if(!filterStr.isEmpty()) {
			this.filter = OVFilter.load(filterStr);
		}
		
		this.createJTable();
		
		// We look for connected networks
		for(CyNetwork net : this.ovManager.getNetworkManager().getNetworkSet()) {
			CyTable netTable = net.getDefaultNetworkTable();
			
			if(netTable.getColumn(OVShared.CYNETWORKTABLE_OVCOL) != null) {
				// We found a connected network
				String link = netTable.getRow(net.getSUID()).get(OVShared.CYNETWORKTABLE_OVCOL, String.class);
				if(link != null && !link.isEmpty()) {
					String splittedLink[] = DataUtils.getCSV(link);
					
					if(splittedLink.length == 3 && splittedLink[0].equals(this.getTitle())) {
						OVConnection ovCon = this.connect(net.toString(), splittedLink[1], splittedLink[2]);
						
						// We try to load the Style
						if(ovCon != null && netTable.getColumn(OVShared.CYNETWORKTABLE_STYLECOL) != null) {
							String style = netTable.getRow(net.getSUID()).get(OVShared.CYNETWORKTABLE_STYLECOL, String.class);
							if(style != null && !style.isEmpty()) {
								ovCon.setStyle(OVStyle.load(style));
							}
						}
					}
				}
			}
		}
	}
	
	public OVProperties getTableOVProperties() {
		return (this.ovProps == null ? new OVProperties(this.ovManager, OVShared.OVPROPERTY_NAME + this.cyTable.getTitle()) : this.ovProps);
	}
	
	public String getTableProperty(String propName) {
		return this.getTableOVProperties().getProperty(propName);
	}
	public String getTableProperty(String propName, String propDefaultValue) {
		return this.getTableOVProperties().getProperty(propName, propDefaultValue);
	}
	public void setTableProperty(String propName, String propValue) {
		this.getTableOVProperties().setProperty(propName, propValue);
	}
	
	public void deleteProperties() {
		if(this.ovProps != null) {
			this.ovProps.delete();
			this.ovProps=null;
		}
	}
}
